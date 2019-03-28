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

package io.micronaut.context;

import io.micronaut.context.exceptions.NoSuchMessageException;
import io.micronaut.core.annotation.Indexed;
import io.micronaut.core.util.ArgumentUtils;

import javax.annotation.Nonnull;
import javax.inject.Singleton;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * Interface for resolving messages from some source.
 *
 * @author graemerocher
 * @since 1.2
 */
@Singleton
@Indexed(MessageSource.class)
public interface MessageSource {

    /**
     * An empty message source.
     */
    MessageSource EMPTY = new MessageSource() {
        @Nonnull
        @Override
        public Optional<String> getMessage(@Nonnull String code, @Nonnull MessageContext context) {
            return Optional.empty();
        }

        @Nonnull
        @Override
        public String interpolate(@Nonnull String template, @Nonnull MessageContext context) {
            return template;
        }
    };

    /**
     * Resolve a message for the given code and context.
     * @param code The code
     * @param context The context
     * @return A message if present
     */
    @Nonnull Optional<String> getMessage(@Nonnull String code, @Nonnull MessageContext context);

    /**
     * Resolve a message for the given code and context.
     * @param code The code
     * @param context The context
     * @param defaultMessage The default message to use if no other message is found
     * @return A message if present
     */
    default @Nonnull String getMessage(@Nonnull String code, @Nonnull MessageContext context, @Nonnull String defaultMessage) {
        ArgumentUtils.requireNonNull("defaultMessage", defaultMessage);
        return getMessage(code, context).orElse(defaultMessage);
    }

    /**
     * Interpolate the given message template.
     * @param template The template
     * @param context The context to use.
     * @return The interpolated message.
     * @throws IllegalArgumentException If any argument specified is null
     */
    @Nonnull String interpolate(@Nonnull String template, @Nonnull MessageContext context);

    /**
     * Resolve a message for the given code and context or throw an exception.
     *
     * @param code The code
     * @param context The context
     * @return The message
     * @throws NoSuchMessageException if the message is not found
     */
    default @Nonnull String getRequiredMessage(@Nonnull String code, @Nonnull MessageContext context) {
        return getMessage(code, context).orElseThrow(() ->
            new NoSuchMessageException(code)
        );
    }

    /**
     * The context to use.
     */
    interface MessageContext {
        /**
         * The locale to use to resolve messages.
         * @return The locale
         */
        @Nonnull default Locale getLocale() {
            return Locale.getDefault();
        }

        /**
         * @return The variables to use resolve message place holders
         */
        @Nonnull default Map<String, Object> getVariables() {
            return Collections.emptyMap();
        }
    }

}
