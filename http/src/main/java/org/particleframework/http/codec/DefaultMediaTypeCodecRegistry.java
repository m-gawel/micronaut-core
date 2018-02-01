/*
 * Copyright 2017 original authors
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
package org.particleframework.http.codec;

import org.particleframework.http.MediaType;

import java.util.*;

/**
 * Registry of {@link MediaTypeCodec} instances
 *
 * @author Graeme Rocher
 * @since 1.0
 */
public class DefaultMediaTypeCodecRegistry implements MediaTypeCodecRegistry {

    private final Collection<MediaTypeCodec> codecs;
    Map<String, MediaTypeCodec> decodersByExtension = new LinkedHashMap<>(3);
    Map<MediaType, MediaTypeCodec> decodersByType = new LinkedHashMap<>(3);

    DefaultMediaTypeCodecRegistry(MediaTypeCodec...codecs) {
        this(Arrays.asList(codecs));
    }

    DefaultMediaTypeCodecRegistry(Collection<MediaTypeCodec> codecs) {
        if(codecs != null) {
            this.codecs = Collections.unmodifiableCollection(codecs);
            for (MediaTypeCodec decoder : codecs) {
                MediaType mediaType = decoder.getMediaType();
                if(mediaType != null) {
                    decodersByExtension.put(mediaType.getExtension(), decoder);
                    decodersByType.put(mediaType, decoder);
                }
            }
        }
        else {
            this.codecs = Collections.emptyList();
        }
    }
    @Override
    public Optional<MediaTypeCodec> findCodec(MediaType mediaType) {
        if(mediaType == null) {
            return Optional.empty();
        }
        MediaTypeCodec decoder = decodersByType.get(mediaType);
        if(decoder == null) {
            decoder = decodersByExtension.get(mediaType.getExtension());
        }
        return Optional.ofNullable(decoder);
    }

    @Override
    public Optional<MediaTypeCodec> findCodec(MediaType mediaType, Class<?> type) {
        Optional<MediaTypeCodec> codec = findCodec(mediaType);
        if(codec.isPresent()) {
            MediaTypeCodec mediaTypeCodec = codec.get();
            if(mediaTypeCodec.supportsType(type)) {
                return codec;
            }
            else {
                return Optional.empty();
            }
        }
        return codec;
    }

    @Override
    public Collection<MediaTypeCodec> getCodecs() {
        return codecs;
    }
}
