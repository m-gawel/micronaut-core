package io.micronaut.validation.validator.constraints;

import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.convert.ConversionService;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.validation.ValidationException;
import javax.validation.constraints.DecimalMin;
import java.math.BigDecimal;

/**
 * Abstract implementation of a validator for {@link DecimalMin}.
 *
 * @param <T> The target type.
 * @since 1.2
 * @author graemerocher
 */
public abstract class AbstractDecimalMinValidator<T> implements ConstraintValidator<DecimalMin, T> {

    @Override
    public final boolean isValid(@Nullable T value, @Nonnull AnnotationMetadata annotationMetadata, @Nonnull ConstraintValidatorContext context) {
        if (value == null) {
            // null considered valid according to spec
            return true;
        }

        final BigDecimal bigDecimal = annotationMetadata.getValue(DecimalMin.class, String.class)
                .map(s ->
                        ConversionService.SHARED.convert(s, BigDecimal.class)
                                .orElseThrow(() -> new ValidationException(s + " does not represent a valid BigDecimal format.")))
                .orElseThrow(() -> new ValidationException("null does not represent a valid BigDecimal format."));

        final boolean inclusive = annotationMetadata.getValue(DecimalMin.class, "inclusive", boolean.class).orElse(true);


        int result;
        try {
            result = doComparison(value, bigDecimal);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return inclusive ? result >= 0 : result > 0;
    }

    /**
     * Perform the comparison for the given value.
     * @param value The value
     * @param bigDecimal The big decimal
     * @return The result
     */
    protected abstract int doComparison(@Nonnull T value, @Nonnull BigDecimal bigDecimal);
}
