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
package io.micronaut.context.exceptions;

/**
 * An exception that occurs if a bean context is attempted to be used
 * before it has been started or after it has been shut down.
 *
 * @author Ryan Vanderwerf
 * @since 1.1.0
 */
public class BeanContextNotAvailableException extends BeanContextException {

    /**
     * Thrown if service is in the processing of shutting down or not running.
     *
     * @param cause   The throwable
     */
    public BeanContextNotAvailableException(Throwable cause) {
        super("Bean context is not running or has been shut down.", cause);
    }

    /**
     * Thrown if service is in the processing of shutting down or not running.
     */
    public BeanContextNotAvailableException() {
        super("Bean context is not running or has been shut down.");
    }
}
