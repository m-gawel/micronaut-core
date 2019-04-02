package io.micronaut.validation.validator.constraints;

import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.util.CollectionUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Singleton;
import javax.validation.constraints.NotEmpty;
import java.util.Map;

/**
 * Validates that a map is not empty.
 *
 * @author graemerocher
 * @since 1.2
 */
@Singleton
public class NotEmptyValidator implements ConstraintValidator<NotEmpty, Map> {
    @Override
    public boolean isValid(@Nullable Map value, @Nonnull AnnotationMetadata annotationMetadata, @Nonnull ConstraintValidatorContext context) {
        return CollectionUtils.isNotEmpty(value);
    }
}
