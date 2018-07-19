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

package io.micronaut.runtime.http.converters;

import io.micronaut.context.annotation.Requires;
import io.micronaut.core.convert.ConversionContext;
import io.micronaut.core.convert.TypeConverter;
import io.micronaut.http.HttpStatus;

import javax.inject.Singleton;
import java.util.Optional;

/**
 * Converts numbers to {@link HttpStatus} objects.
 *
 * @author graemerocher
 * @since 1.0
 */
@Requires(classes = HttpStatus.class)
@Singleton
public class HttpStatusConverter implements TypeConverter<Number, HttpStatus> {
    @Override
    public Optional<HttpStatus> convert(Number object, Class<HttpStatus> targetType, ConversionContext context) {
        try {
            HttpStatus status = HttpStatus.valueOf(object.shortValue());
            return Optional.of(status);
        } catch (IllegalArgumentException e) {
            context.reject(object, e);
            return Optional.empty();
        }
    }
}
