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

package io.micronaut.http.server;

import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.core.convert.format.ReadableBytes;
import io.micronaut.core.util.Toggleable;
import io.micronaut.http.server.cors.CorsOriginConfiguration;
import io.micronaut.runtime.ApplicationConfiguration;

import javax.inject.Inject;
import java.io.File;
import java.nio.charset.Charset;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * <p>A base {@link ConfigurationProperties} for servers.</p>
 *
 * @author Graeme Rocher
 * @since 1.0
 */
@ConfigurationProperties(value = HttpServerConfiguration.PREFIX, cliPrefix = "")
public class HttpServerConfiguration {

    /**
     * The prefix used for configuration.
     */

    public static final String PREFIX = "micronaut.server";

    private int port = -1; // default to random port
    private String host;
    private Integer readTimeout;
    private long maxRequestSize = 1024 * 1024 * 10; // 10MB
    private Duration readIdleTime = Duration.of(60, ChronoUnit.SECONDS);
    private Duration writeIdleTime = Duration.of(60, ChronoUnit.SECONDS);
    private Duration idleTime = Duration.of(60, ChronoUnit.SECONDS);
    private MultipartConfiguration multipart = new MultipartConfiguration();
    private CorsConfiguration cors = new CorsConfiguration();
    private String serverHeader;
    private boolean dateHeader = true;

    private final ApplicationConfiguration applicationConfiguration;
    private Charset defaultCharset;

    /**
     * Default constructor.
     */
    public HttpServerConfiguration() {
        this.applicationConfiguration = new ApplicationConfiguration();
    }

    /**
     * @param applicationConfiguration The application configuration
     */
    @Inject
    public HttpServerConfiguration(ApplicationConfiguration applicationConfiguration) {
        if (applicationConfiguration != null) {
            this.defaultCharset = applicationConfiguration.getDefaultCharset();
        }

        this.applicationConfiguration = applicationConfiguration;
    }

    /**
     * @return The application configuration instance
     */
    public ApplicationConfiguration getApplicationConfiguration() {
        return applicationConfiguration;
    }

    /**
     * @return The default charset to use
     */
    public Charset getDefaultCharset() {
        return defaultCharset;
    }

    /**
     * @param defaultCharset The default charset to use
     */
    public void setDefaultCharset(Charset defaultCharset) {
        this.defaultCharset = defaultCharset;
    }

    /**
     * @return The default server port
     */
    public int getPort() {
        return port;
    }

    /**
     * @return The default host
     */
    public Optional<String> getHost() {
        return Optional.ofNullable(host);
    }

    /**
     * @return The read timeout setting for the server
     */
    public Optional<Integer> getReadTimeout() {
        return Optional.ofNullable(readTimeout);
    }

    /**
     * @return Configuration for multipart / file uploads
     */
    public MultipartConfiguration getMultipart() {
        return multipart;
    }

    /**
     * @return Configuration for CORS
     */
    public CorsConfiguration getCors() {
        return cors;
    }

    /**
     * @return The maximum request body size
     */
    public long getMaxRequestSize() {
        return maxRequestSize;
    }

    /**
     * @return The default amount of time to allow read operation connections  to remain idle
     */
    public Duration getReadIdleTime() {
        return readIdleTime;
    }

    /**
     * @return The default amount of time to allow write operation connections to remain idle
     */
    public Duration getWriteIdleTime() {
        return writeIdleTime;
    }

    /**
     * @return The time to allow an idle connection for
     */
    public Duration getIdleTime() {
        return idleTime;
    }

    /**
     * @return The optional server header value
     */
    public Optional<String> getServerHeader() {
        return Optional.ofNullable(serverHeader);
    }

    /**
     * @return True if the date header should be set
     */
    public boolean isDateHeader() {
        return dateHeader;
    }

    /**
     * Sets the port to bind to.
     * @param port The port
     */
    public void setPort(int port) {
        this.port = port;
    }

    /**
     * Sets the host to bind to
     * @param host The host
     */
    public void setHost(String host) {
        this.host = host;
    }

    /**
     * Sets the default read timeout.
     * @param readTimeout The read timeout
     */
    public void setReadTimeout(Integer readTimeout) {
        this.readTimeout = readTimeout;
    }

    /**
     * Sets the name of the server header.
     * @param serverHeader The server header
     */
    public void setServerHeader(String serverHeader) {
        this.serverHeader = serverHeader;
    }

    /**
     * Sets the maximum request size.
     * @param maxRequestSize The max request size
     */
    public void setMaxRequestSize(@ReadableBytes long maxRequestSize) {
        this.maxRequestSize = maxRequestSize;
    }

    /**
     * Sets the amount of time a connection can remain idle without any reads occurring.
     * @param readIdleTime The read idle time
     */
    public void setReadIdleTime(Duration readIdleTime) {
        this.readIdleTime = readIdleTime;
    }

    /**
     * Sets the amount of time a connection can remain idle without any writes occurring.
     * @param writeIdleTime The write idle time
     */
    public void setWriteIdleTime(Duration writeIdleTime) {
        this.writeIdleTime = writeIdleTime;
    }

    /**
     * Sets the idle time of connections for the server.
     * @param idleTime The idle time
     */
    public void setIdleTime(Duration idleTime) {
        this.idleTime = idleTime;
    }

    /**
     * Sets the multipart configuration.
     * @param multipart The multipart configuration
     */
    public void setMultipart(MultipartConfiguration multipart) {
        this.multipart = multipart;
    }

    /**
     * Sets the cors configuration.
     * @param cors The cors configuration
     */
    public void setCors(CorsConfiguration cors) {
        this.cors = cors;
    }

    /**
     * Sets whether a date header should be sent back.
     * @param dateHeader True if a date header should be sent.
     */
    public void setDateHeader(boolean dateHeader) {
        this.dateHeader = dateHeader;
    }

    /**
     * Configuration for multipart handling.
     */
    @ConfigurationProperties("multipart")
    public static class MultipartConfiguration implements Toggleable {
        private File location;
        private long maxFileSize = 1024 * 1024; // 1MB
        private boolean enabled = true;
        private boolean disk = false;

        /**
         * @return The location to store temporary files
         */
        public Optional<File> getLocation() {
            return Optional.ofNullable(location);
        }

        /**
         * @return The max file size. Defaults to 1MB
         */
        public long getMaxFileSize() {
            return maxFileSize;
        }

        /**
         * @return Whether file uploads are enabled. Defaults to true.
         */
        @Override
        public boolean isEnabled() {
            return enabled;
        }

        /**
         * @return Whether to use disk. Defaults to false.
         */
        public boolean isDisk() {
            return disk;
        }

        /**
         * Sets the location to store files.
         * @param location The location
         */
        public void setLocation(File location) {
            this.location = location;
        }

        /**
         * Sets the max file size.
         * @param maxFileSize The max file size
         */
        public void setMaxFileSize(@ReadableBytes long maxFileSize) {
            this.maxFileSize = maxFileSize;
        }

        /**
         * Sets whether multipart processing is enabled.
         * @param enabled True if it is enabled
         */
        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        /**
         * Sets whether to buffer data to disk or not.
         * @param disk True if data should be written to disk
         */
        public void setDisk(boolean disk) {
            this.disk = disk;
        }
    }

    /**
     * Configuration for CORS.
     */
    @ConfigurationProperties("cors")
    public static class CorsConfiguration implements Toggleable {

        private boolean enabled = false;

        private Map<String, CorsOriginConfiguration> configurations = Collections.emptyMap();

        private Map<String, CorsOriginConfiguration> defaultConfiguration = new LinkedHashMap<>(1);

        /**
         * @return Whether cors is enabled. Defaults to false.
         */
        @Override
        public boolean isEnabled() {
            return enabled;
        }

        /**
         * @return The cors configurations
         */
        public Map<String, CorsOriginConfiguration> getConfigurations() {
            if (enabled && configurations.isEmpty()) {
                if (defaultConfiguration.isEmpty()) {
                    defaultConfiguration.put("default", new CorsOriginConfiguration());
                }
                return defaultConfiguration;
            }
            return configurations;
        }

        /**
         * Sets whether CORS is enabled.
         * @param enabled True if CORS is enabled
         */
        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        /**
         * Sets the CORS configurations.
         * @param configurations The CORS configurations
         */
        public void setConfigurations(Map<String, CorsOriginConfiguration> configurations) {
            this.configurations = configurations;
        }
    }
}
