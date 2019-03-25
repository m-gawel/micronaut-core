package io.micronaut.validation.validator.constraints;

import io.micronaut.core.annotation.AnnotationValue;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Singleton;
import javax.validation.constraints.NotEmpty;

/**
 * {@link NotEmpty} validator for double arrays.
 *
 * @author graemerocher
 * @since 1.2
 */
@Singleton
public class NotEmptyDoubleArrayValidator implements ConstraintValidator<NotEmpty, double[]> {
    @Override
    public boolean isValid(@Nullable double[] value, @Nonnull AnnotationValue<NotEmpty> annotationMetadata, @Nonnull ConstraintValidatorContext context) {
        return value != null && value.length > 0;
    }
}
