/*
 * Copyright 2017-2018 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.micronaut.http.server.netty.types.files;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.netty.NettyHttpResponse;
import io.micronaut.http.server.netty.NettyHttpServer;
import io.micronaut.http.server.netty.SmartHttpContentCompressor;
import io.micronaut.http.server.netty.async.DefaultCloseHandler;
import io.micronaut.http.server.netty.types.NettyFileCustomizableResponseType;
import io.micronaut.http.server.types.CustomizableResponseTypeException;
import io.micronaut.http.server.types.files.SystemFileCustomizableResponseType;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.DefaultFileRegion;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpChunkedInput;
import io.netty.handler.codec.http.HttpContentCompressor;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.stream.ChunkedFile;
import io.netty.handler.stream.ChunkedWriteHandler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Optional;

/**
 * Writes a {@link File} to the Netty context.
 *
 * @author James Kleeh
 * @author Graeme Rocher
 * @since 1.0
 */
public class NettySystemFileCustomizableResponseType extends SystemFileCustomizableResponseType implements NettyFileCustomizableResponseType {

    private static final int LENGTH_8K = 8192;

    protected final RandomAccessFile raf;
    protected final long rafLength;
    protected Optional<SystemFileCustomizableResponseType> delegate = Optional.empty();

    /**
     * @param file The file
     */
    public NettySystemFileCustomizableResponseType(File file) {
        super(file);
        try {
            this.raf = new RandomAccessFile(file, "r");
        } catch (FileNotFoundException e) {
            throw new CustomizableResponseTypeException("Could not find file", e);
        }
        try {
            this.rafLength = raf.length();
        } catch (IOException e) {
            throw new CustomizableResponseTypeException("Could not determine file length", e);
        }
    }

    /**
     * @param delegate The system file customizable response type
     */
    public NettySystemFileCustomizableResponseType(SystemFileCustomizableResponseType delegate) {
        this(delegate.getFile());
        this.delegate = Optional.of(delegate);
    }

    @Override
    public long getLength() {
        return rafLength;
    }

    @Override
    public long getLastModified() {
        return delegate.map(SystemFileCustomizableResponseType::getLastModified).orElse(super.getLastModified());
    }

    @Override
    public String getName() {
        return delegate.map(SystemFileCustomizableResponseType::getName).orElse(super.getName());
    }

    /**
     * @param response The response to modify
     */
    public void process(MutableHttpResponse response) {
        response.header(io.micronaut.http.HttpHeaders.CONTENT_LENGTH, String.valueOf(getLength()));
        delegate.ifPresent((type) -> type.process(response));
    }

    @Override
    public void write(HttpRequest<?> request, MutableHttpResponse<?> response, ChannelHandlerContext context) {

        if (response instanceof NettyHttpResponse) {

            FullHttpResponse nettyResponse = ((NettyHttpResponse) response).getNativeResponse();

            //The streams codec prevents non full responses from being written
            Optional
                .ofNullable(context.pipeline().get(NettyHttpServer.HTTP_STREAMS_CODEC))
                .ifPresent(handler -> context.pipeline().replace(handler, "chunked-handler", new ChunkedWriteHandler()));

            // Write the request data
            HttpHeaders headers = nettyResponse.headers();
            context.write(new DefaultHttpResponse(nettyResponse.protocolVersion(), nettyResponse.status(), headers), context.voidPromise());

            // Write the content.
            ChannelFuture flushFuture;
            if (context.pipeline().get(SslHandler.class) == null && SmartHttpContentCompressor.shouldSkip(headers)) {
                // SSL not enabled - can use zero-copy file transfer.
                // Remove the content compressor to prevent incorrect behavior with zero-copy
                HttpContentCompressor compressor = context.pipeline().get(HttpContentCompressor.class);
                if (compressor != null) {
                    context.pipeline().remove(HttpContentCompressor.class);
                }

                context.write(new DefaultFileRegion(raf.getChannel(), 0, getLength()), context.newProgressivePromise());
                flushFuture = context.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
            } else {
                // SSL enabled - cannot use zero-copy file transfer.
                try {
                    // HttpChunkedInput will write the end marker (LastHttpContent) for us.
                    flushFuture = context.writeAndFlush(new HttpChunkedInput(new ChunkedFile(raf, 0, getLength(), LENGTH_8K)),
                        context.newProgressivePromise());
                } catch (IOException e) {
                    throw new CustomizableResponseTypeException("Could not read file", e);
                }
            }

            flushFuture.addListener(new DefaultCloseHandler(context, request, response.code()));
        } else {
            throw new IllegalArgumentException("Unsupported response type. Not a Netty response: " + response);
        }
    }
}
