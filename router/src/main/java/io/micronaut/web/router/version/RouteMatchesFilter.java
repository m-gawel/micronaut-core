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

package io.micronaut.web.router.version;

import io.micronaut.http.HttpRequest;
import io.micronaut.web.router.UriRouteMatch;

import java.util.List;

/**
 * A filter responsible for filtering route matches.
 *
 * @author Bogdan Oros
 * @since 1.1.0
 */
public interface RouteMatchesFilter {

    /**
     * A method responsible for filtering route matches based on request.
     *
     * @param matches The list of {@link UriRouteMatch}
     * @param request The HTTP request
     * @param <T>     The target type
     * @param <R>     The return type
     * @return A filtered list of route matches
     */
    <T, R> List<UriRouteMatch<T, R>> filter(List<UriRouteMatch<T, R>> matches, HttpRequest<?> request);

}
