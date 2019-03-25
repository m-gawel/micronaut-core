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
package io.micronaut.validation.validator;

import io.micronaut.inject.ExecutableMethod;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.validation.ConstraintViolation;
import javax.validation.executable.ExecutableValidator;
import java.util.Set;

/**
 * Extended version of {@link ExecutableValidator} that operates on {@link io.micronaut.inject.ExecutableMethod} instances.
 *
 * @author graemerocher
 * @since 1.2
 */
public interface ExecutableMethodValidator extends ExecutableValidator  {

    /**
     * Validate the parameter values of the given {@link ExecutableMethod}.
     * @param object The object
     * @param method The method
     * @param parameterValues The values
     * @param groups The groups
     * @param <T> The object type
     * @return The constraint violations.
     */
    @Nonnull <T> Set<ConstraintViolation<T>> validateParameters(
            @Nonnull T object,
            @Nonnull ExecutableMethod method,
            @Nullable Object[] parameterValues, @Nullable Class<?>... groups);

}
