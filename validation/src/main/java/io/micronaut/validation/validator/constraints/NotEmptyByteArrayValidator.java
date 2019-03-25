package io.micronaut.validation.validator.constraints;

import io.micronaut.core.annotation.AnnotationMetadata;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Singleton;
import javax.validation.constraints.NotEmpty;

/**
 * Validates a byte[] is not empty.
 *
 * @author graemerocher
 * @since 1.2
 */
@Singleton
public class NotEmptyByteArrayValidator implements ConstraintValidator<NotEmpty, byte[]> {
    @Override
    public boolean isValid(@Nullable byte[] value, @Nonnull AnnotationMetadata annotationMetadata, @Nonnull ConstraintValidatorContext context) {
        return value != null && value.length > 0;
    }
}

