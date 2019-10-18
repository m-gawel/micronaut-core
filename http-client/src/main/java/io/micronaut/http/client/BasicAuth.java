/*
 * Copyright 2017-2019 original authors
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
package io.micronaut.http.client;

import io.micronaut.context.annotation.Prototype;

/**
 * A class representing basic auth authorization header.
 * To be used in {@link io.micronaut.http.client.annotation.Client}
 *
 * @author Ashwini Mutalik Desai
 */
@Prototype
public final class BasicAuth {

    private final String username;
    private final String password;

    /**
     *
     * @param username The username
     * @param password The password
     */
    public BasicAuth(String username, String password) {
        this.username = username;
        this.password = password;
    }

    /**
     *
     * @return username
     */
    public String getUsername() {
        return username;
    }

    /**
     *
     * @return password
     */
    public String getPassword() {
        return password;
    }
}
