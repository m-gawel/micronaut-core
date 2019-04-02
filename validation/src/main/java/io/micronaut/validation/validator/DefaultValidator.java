package io.micronaut.validation.validator;

import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.core.beans.BeanIntrospection;
import io.micronaut.core.beans.BeanIntrospector;
import io.micronaut.core.beans.BeanProperty;
import io.micronaut.core.util.ArgumentUtils;
import io.micronaut.core.util.CollectionUtils;
import io.micronaut.validation.validator.constraints.ConstraintValidator;
import io.micronaut.validation.validator.constraints.ConstraintValidatorContext;
import io.micronaut.validation.validator.constraints.ConstraintValidatorRegistry;
import io.micronaut.validation.validator.extractors.SimpleValueReceiver;
import io.micronaut.validation.validator.extractors.ValueExtractorRegistry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.*;
import javax.validation.metadata.BeanDescriptor;
import javax.validation.metadata.ConstraintDescriptor;
import javax.validation.valueextraction.ValueExtractor;
import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.util.*;

/**
 * Default implementation of the {@link Validator} interface.
 *
 * @author graemerocher
 * @since 1.2
 */
@Singleton
public class DefaultValidator implements Validator {

    private final ConstraintValidatorRegistry constraintValidatorRegistry;
    private final ClockProvider clockProvider;
    private final ValueExtractorRegistry valueExtractorRegistry;
    private final TraversableResolver traversableResolver;

    /**
     * Default constructor.
     *
     * @param constraintValidatorRegistry The validator registry.
     * @param valueExtractorRegistry      The value extractor registry.
     * @param clockProvider               The clock provider
     * @param traversableResolver         The traversable resolver to use
     */
    @Inject
    protected DefaultValidator(
            @Nonnull ConstraintValidatorRegistry constraintValidatorRegistry,
            @Nonnull ValueExtractorRegistry valueExtractorRegistry,
            @Nonnull ClockProvider clockProvider,
            @Nullable TraversableResolver traversableResolver) {
        ArgumentUtils.requireNonNull("constraintValidatorRegistry", constraintValidatorRegistry);
        ArgumentUtils.requireNonNull("valueExtractorRegistry", valueExtractorRegistry);
        ArgumentUtils.requireNonNull("clockProvider", clockProvider);
        this.constraintValidatorRegistry = constraintValidatorRegistry;
        this.clockProvider = clockProvider;
        this.valueExtractorRegistry = valueExtractorRegistry;
        this.traversableResolver = traversableResolver != null ? traversableResolver : new TraversableResolver() {
            @Override
            public boolean isReachable(Object object, Path.Node node, Class<?> rootType, Path path, ElementType elementType) {
                return true;
            }

            @Override
            public boolean isCascadable(Object object, Path.Node node, Class<?> rootType, Path path, ElementType elementType) {
                return true;
            }
        };
    }

    @Nonnull
    @Override
    public <T> Set<ConstraintViolation<T>> validate(@Nonnull T object, @Nullable Class<?>... groups) {
        ArgumentUtils.requireNonNull("object", object);
        final BeanIntrospection<Object> introspection = getBeanIntrospection(object);
        if (introspection == null) {
            throw new ValidationException("Passed object [" + object + "] cannot be introspected. Please annotation with @Introspected");
        }
        final Collection<? extends BeanProperty<Object, Object>> constrainedProperties = introspection.getIndexedProperties(Constraint.class);
        final Collection<BeanProperty<Object, Object>> cascadeProperties =
                introspection.getIndexedProperties(Valid.class);

        if (CollectionUtils.isNotEmpty(constrainedProperties) || CollectionUtils.isNotEmpty(cascadeProperties)) {
            DefaultConstraintValidatorContext context = new DefaultConstraintValidatorContext(object);
            Set<ConstraintViolation<T>> overallViolations = new HashSet<>(5);
            return doValidate(
                    object,
                    object,
                    constrainedProperties,
                    cascadeProperties,
                    context,
                    overallViolations
            );
        }
        return Collections.emptySet();
    }

    @Nonnull
    @Override
    public <T> Set<ConstraintViolation<T>> validateProperty(
            @Nonnull T object,
            @Nonnull String propertyName,
            @Nullable Class<?>... groups) {
        ArgumentUtils.requireNonNull("object", object);
        ArgumentUtils.requireNonNull("propertyName", propertyName);
        final BeanIntrospection<Object> introspection = getBeanIntrospection(object);
        if (introspection == null) {
            throw new ValidationException("Passed object [" + object + "] cannot be introspected. Please annotation with @Introspected");
        }

        final Optional<BeanProperty<Object, Object>> property = introspection.getProperty(propertyName);

        if (property.isPresent()) {
            DefaultConstraintValidatorContext context = new DefaultConstraintValidatorContext(object);
            final BeanProperty<Object, Object> constrainedProperty = property.get();
            Set overallViolations = new HashSet<>(5);
            final Object propertyValue = constrainedProperty.get(object);

            validateConstrainedPropertyInternal(
                    object,
                    object,
                    constrainedProperty,
                    propertyValue,
                    context,
                    overallViolations
            );

            //noinspection unchecked
            return Collections.unmodifiableSet(overallViolations);
        }

        return Collections.emptySet();
    }

    @Nonnull
    @Override
    public <T> Set<ConstraintViolation<T>> validateValue(
            @Nonnull Class<T> beanType,
            @Nonnull String propertyName,
            @Nullable Object value,
            @Nullable Class<?>... groups) {
        ArgumentUtils.requireNonNull("beanType", beanType);
        ArgumentUtils.requireNonNull("propertyName", propertyName);

        final BeanIntrospection<Object> introspection = getBeanIntrospection(beanType);
        if (introspection == null) {
            throw new ValidationException("Passed bean type [" + beanType + "] cannot be introspected. Please annotation with @Introspected");
        }

        final BeanProperty<Object, Object> beanProperty = introspection.getProperty(propertyName)
                .orElseThrow(() -> new ValidationException("No property [" + propertyName + "] found on type: " + beanType));


        final HashSet overallViolations = new HashSet<>(5);
        validatePropertyInternal(null, null, new DefaultConstraintValidatorContext(), overallViolations, beanProperty, value);
        return Collections.unmodifiableSet(overallViolations);
    }

    @Override
    public BeanDescriptor getConstraintsForClass(Class<?> clazz) {
        throw new UnsupportedOperationException("BeanDescriptor metadata not supported by this implementation");
    }

    @Override
    public <T> T unwrap(Class<T> type) {
        throw new UnsupportedOperationException("Validator unwrapping not supported by this implementation");
    }

    @Override
    public ExecutableMethodValidator forExecutables() {
        return null; // TODO
    }


    /**
     * looks up a bean introspection for the given object.
     * @param object The object, never null
     * @return The introspection or null
     */
    protected @Nullable BeanIntrospection<Object> getBeanIntrospection(@Nonnull Object object) {
        if (object instanceof Class) {
            return BeanIntrospector.SHARED.findIntrospection((Class<Object>) object).orElse(null);
        }
        return BeanIntrospector.SHARED.findIntrospection((Class<Object>) object.getClass()).orElse(null);
    }

    private <T> Set<ConstraintViolation<T>> doValidate(
            @Nonnull T rootBean,
            @Nonnull Object object,
            Collection<? extends BeanProperty<Object, Object>> constrainedProperties,
            Collection<BeanProperty<Object, Object>> cascadeProperties,
            DefaultConstraintValidatorContext context,
            Set overallViolations) {
        for (BeanProperty<Object, Object> constrainedProperty : constrainedProperties) {
            final Object propertyValue = constrainedProperty.get(object);
            validateConstrainedPropertyInternal(
                    rootBean,
                    object,
                    constrainedProperty,
                    propertyValue,
                    context,
                    overallViolations
            );
        }

        // now handle cascading validation
        for (BeanProperty<Object, Object> cascadeProperty : cascadeProperties) {
            final Object propertyValue = cascadeProperty.get(object);
            if (propertyValue != null) {
                final Optional<? extends ValueExtractor<Object>> opt = valueExtractorRegistry
                        .findValueExtractor((Class<Object>) propertyValue.getClass());
                opt.ifPresent(valueExtractor -> valueExtractor.extractValues(propertyValue, new ValueExtractor.ValueReceiver() {
                    @Override
                    public void value(String nodeName, Object object1) {

                    }

                    @Override
                    public void iterableValue(String nodeName, Object iterableValue) {
                        if (iterableValue != null && context.validatedObjects.contains(iterableValue)) {
                            return;
                        }
                        cascadeToIterableValue(
                                context,
                                rootBean,
                                object,
                                cascadeProperty,
                                iterableValue,
                                overallViolations,
                                null,
                                null,
                                true);
                    }

                    @Override
                    public void indexedValue(String nodeName, int i, Object iterableValue) {
                        if (iterableValue != null && context.validatedObjects.contains(iterableValue)) {
                            return;
                        }
                        cascadeToIterableValue(
                                context,
                                rootBean,
                                object,
                                cascadeProperty,
                                iterableValue,
                                overallViolations,
                                i,
                                null,
                                true);
                    }

                    @Override
                    public void keyedValue(String nodeName, Object key, Object keyedValue) {
                        if (keyedValue != null && context.validatedObjects.contains(keyedValue)) {
                            return;
                        }
                        cascadeToIterableValue(
                                context,
                                rootBean,
                                object,
                                cascadeProperty,
                                keyedValue,
                                overallViolations,
                                null,
                                key,
                                false
                        );
                    }
                }));

                if (!opt.isPresent() && !context.validatedObjects.contains(propertyValue)) {
                    // maybe a bean
                    final Path.Node node = context.addPropertyNode(cascadeProperty.getName());

                    try {
                        final boolean canCascade = canCascade(rootBean, context, propertyValue, node);
                        if (canCascade) {
                            cascadeToOne(
                                    rootBean,
                                    object,
                                    context,
                                    overallViolations,
                                    cascadeProperty,
                                    propertyValue
                            );
                        }
                    } finally {
                        context.removeLast();
                    }
                }
            }
        }
        //noinspection unchecked
        return Collections.unmodifiableSet(overallViolations);
    }

    private <T> boolean canCascade(@Nonnull T rootBean, DefaultConstraintValidatorContext context, Object propertyValue, Path.Node node) {
        final boolean isCascadable = traversableResolver.isCascadable(propertyValue, node, rootBean.getClass(), context.currentPath, ElementType.FIELD);
        final boolean isReachable = traversableResolver.isReachable(propertyValue, node, rootBean.getClass(), context.currentPath, ElementType.FIELD);
        return isCascadable && isReachable;
    }

    private <T> void cascadeToIterableValue(
            DefaultConstraintValidatorContext context,
            @Nonnull T rootBean,
            Object object,
            BeanProperty<Object, Object> cascadeProperty,
            Object iterableValue,
            Set overallViolations,
            Integer index,
            Object key,
            boolean isIterable) {
        final Path.Node node = context.addPropertyNode(cascadeProperty.getName());
        try {
            if (canCascade(rootBean, context, iterableValue, node)) {
                context.currentContainerNode = new DefaultPropertyNode(
                        cascadeProperty.getName(),
                        cascadeProperty.getClass(),
                        index,
                        key,
                        ElementKind.CONTAINER_ELEMENT,
                        isIterable
                );

                cascadeToOne(
                        rootBean,
                        object,
                        context,
                        overallViolations,
                        cascadeProperty, iterableValue
                );
            }
        } finally {
            context.currentContainerNode = null;
            context.removeLast();
        }
    }

    private <T> void cascadeToOne(
            T rootBean,
            Object object,
            DefaultConstraintValidatorContext context,
            Set overallViolations,
            BeanProperty<Object, Object> cascadeProperty,
            Object propertyValue) {

        final BeanIntrospection<Object> beanIntrospection = getBeanIntrospection(propertyValue);

        if (beanIntrospection != null) {
            context.validatedObjects.add(propertyValue);
            final Collection<BeanProperty<Object, Object>> cascadeConstraints =
                    beanIntrospection.getIndexedProperties(Constraint.class);
            final Collection<BeanProperty<Object, Object>> cascadeNestedProperties =
                    beanIntrospection.getIndexedProperties(Valid.class);

            if (CollectionUtils.isNotEmpty(cascadeConstraints) || CollectionUtils.isNotEmpty(cascadeNestedProperties)) {
                doValidate(
                        rootBean,
                        propertyValue,
                        cascadeConstraints,
                        cascadeNestedProperties,
                        context,
                        overallViolations
                );
            }

        } else {
            // try apply cascade rules to actual property
            validateConstrainedPropertyInternal(
                    rootBean,
                    object,
                    cascadeProperty,
                    propertyValue,
                    context,
                    overallViolations
            );
        }
    }

    private <T> void validateConstrainedPropertyInternal(
            @Nullable T rootBean,
            @Nonnull Object object,
            BeanProperty<Object, Object> constrainedProperty,
            @Nullable Object propertyValue,
            DefaultConstraintValidatorContext context,
            Set<ConstraintViolation<Object>> overallViolations) {
        context.addPropertyNode(
                constrainedProperty.getName()
        );

        validatePropertyInternal(
                rootBean,
                object,
                context,
                overallViolations,
                constrainedProperty,
                propertyValue
        );
        context.removeLast();
    }

    private <T> void validatePropertyInternal(
            @Nullable T rootBean,
            @Nullable Object object,
            DefaultConstraintValidatorContext context,
            Set<ConstraintViolation<Object>> overallViolations,
            BeanProperty<Object, Object> constrainedProperty,
            @Nullable Object propertyValue) {
        final List<Class<? extends Annotation>> constraintTypes = constrainedProperty.getAnnotationTypesByStereotype(Constraint.class);
        for (Class<? extends Annotation> constraintType : constraintTypes) {

            ValueExtractor<Object> valueExtractor = null;
            if (propertyValue != null && !constrainedProperty.hasAnnotation(Valid.class)) {
                valueExtractor = valueExtractorRegistry.findUnwrapValueExtractor((Class<Object>) propertyValue.getClass())
                        .orElse(null);
            }

            if (valueExtractor != null) {
                valueExtractor.extractValues(propertyValue, (SimpleValueReceiver) (nodeName, extractedValue) -> valueConstraintOnProperty(
                        rootBean,
                        object,
                        context,
                        overallViolations,
                        constrainedProperty,
                        extractedValue,
                        constraintType
                ));
            } else {
                valueConstraintOnProperty(
                        rootBean,
                        object,
                        context,
                        overallViolations,
                        constrainedProperty,
                        propertyValue,
                        constraintType
                );
            }
        }
    }

    private <T> void valueConstraintOnProperty(
            @Nonnull T rootBean,
            @Nonnull Object object,
            DefaultConstraintValidatorContext context,
            Set<ConstraintViolation<Object>> overallViolations,
            BeanProperty<Object, Object> constrainedProperty,
            @Nullable Object propertyValue,
            Class<? extends Annotation> constraintType) {
        final List<? extends AnnotationValue<? extends Annotation>> annotationValues = constrainedProperty.getAnnotationValuesByType(constraintType);

        @SuppressWarnings("unchecked")
        final Class<Object> targetType = propertyValue != null ? (Class<Object>) propertyValue.getClass() : constrainedProperty.getType();
        final ConstraintValidator<? extends Annotation, Object> validator = constraintValidatorRegistry
                .findConstraintValidator(constraintType, targetType).orElse(null);
        if (validator != null) {
            for (AnnotationValue annotationValue : annotationValues) {
                //noinspection unchecked
                if (!validator.isValid(propertyValue, annotationValue, context)) {

                    @SuppressWarnings("unchecked") final String messageTemplate = (String) annotationValue.get("message", String.class)
                            .orElse("{" + annotationValue.getAnnotationName() + ".message}");
                    //noinspection unchecked
                    overallViolations.add(
                            new BeanConstraintViolation(
                                    rootBean,
                                    rootBean.getClass(),
                                    object,
                                    propertyValue,
                                    messageTemplate, // TODO: message interpolation
                                    messageTemplate,
                                    new PathImpl(context.currentPath)
                            )
                    );
                }
            }
        }
    }

    /**
     * The context object.
     */
    private final class DefaultConstraintValidatorContext implements ConstraintValidatorContext {
        Set<Object> validatedObjects = new HashSet<>(20);
        PathImpl currentPath = new PathImpl();
        DefaultPropertyNode currentContainerNode;

        private <T> DefaultConstraintValidatorContext(T object) {
            validatedObjects.add(object);
        }

        private DefaultConstraintValidatorContext() {
        }

        @Nonnull
        @Override
        public ClockProvider getClockProvider() {
            return clockProvider;
        }

        Path.Node addPropertyNode(String name) {
            final DefaultPropertyNode node;
            if (currentContainerNode != null) {
                node = new DefaultPropertyNode(
                        name, currentContainerNode
                );
            } else {
                node = new DefaultPropertyNode(name, null, null, null, ElementKind.PROPERTY, false);
            }
            currentPath.nodes.add(node);
            return node;
        }

        Path.Node removeLast() {
            return currentPath.nodes.removeLast();
        }
    }

    /**
     * Path implementation.
     */
    private final class PathImpl implements Path {

        final Deque<Node> nodes;

        /**
         * Copy constructor.
         *
         * @param nodes The nodes
         */
        private PathImpl(PathImpl nodes) {
            this.nodes = new LinkedList<>(nodes.nodes);
        }

        private PathImpl() {
            this.nodes = new LinkedList<>();
        }

        @Override
        public Iterator<Node> iterator() {
            return nodes.iterator();
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            final Iterator<Node> i = nodes.iterator();
            boolean first = true;
            while (i.hasNext()) {
                final Node node = i.next();
                if (first) {
                    first = false;
                } else {
                    if (node.getKind() == ElementKind.CONTAINER_ELEMENT) {
                        final Integer index = node.getIndex();
                        if (index != null) {
                            builder.append('[').append(index).append(']');
                        } else {
                            final Object key = node.getKey();
                            if (key != null) {
                                builder.append('[').append(key).append(']');
                            } else {
                                builder.append("[]");
                            }
                        }

                    }
                    builder.append('.');
                }
                builder.append(node.getName());
            }
            return builder.toString();
        }
    }

    /**
     * Default property node impl.
     */
    private class DefaultPropertyNode implements Path.PropertyNode {
        private final Class<?> containerClass;
        private final String name;
        private final Integer index;
        private final Object key;
        private final ElementKind kind;
        private final boolean isIterable;

        DefaultPropertyNode(
                @Nonnull String name,
                @Nullable Class<?> containerClass,
                @Nullable Integer index,
                @Nullable Object key,
                @Nonnull ElementKind kind,
                boolean isIterable) {
            this.containerClass = containerClass;
            this.name = name;
            this.index = index;
            this.key = key;
            this.kind = kind;
            this.isIterable = isIterable || index != null;
        }

        DefaultPropertyNode(
                @Nonnull String name,
                @Nonnull DefaultPropertyNode parent
        ) {
            this(name, parent.containerClass, parent.index, parent.key, ElementKind.CONTAINER_ELEMENT, parent.isIterable);
        }

        DefaultPropertyNode(
                @Nonnull String name,
                @Nullable Class<?> containerClass,
                @Nullable Integer index,
                @Nullable Object key,
                @Nonnull ElementKind kind) {
            this(name, containerClass, index, key, kind, index != null);
        }

        DefaultPropertyNode(
                @Nonnull String name,
                @Nullable Class<?> containerClass,
                @Nullable Integer index,
                @Nullable Object key) {
            this(name, containerClass, index, key, ElementKind.PROPERTY);
        }

        @Override
        public Class<?> getContainerClass() {
            return containerClass;
        }

        @Override
        public Integer getTypeArgumentIndex() {
            return null;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public boolean isInIterable() {
            return isIterable;
        }

        @Override
        public Integer getIndex() {
            return index;
        }

        @Override
        public Object getKey() {
            return key;
        }

        @Override
        public ElementKind getKind() {
            return kind;
        }

        @Override
        public <T extends Path.Node> T as(Class<T> nodeType) {
            throw new UnsupportedOperationException("Unwrapping is unsupported by this implementation");
        }
    }

    /**
     * Default implementation of {@link ConstraintViolation}.
     *
     * @param <T> The bean type.
     */
    private final class BeanConstraintViolation<T> implements ConstraintViolation<T> {

        private final T rootBean;
        private final Object invalidValue;
        private final String message;
        private final String messageTemplate;
        private final Path path;
        private final Class<T> rootBeanClass;
        private final Object leafBean;

        private BeanConstraintViolation(
                @Nullable T rootBean,
                @Nullable Class<T> rootBeanClass,
                Object leafBean,
                Object invalidValue,
                String message,
                String messageTemplate,
                Path path) {
            this.rootBean = rootBean;
            this.rootBeanClass = rootBeanClass;
            this.invalidValue = invalidValue;
            this.message = message;
            this.messageTemplate = messageTemplate;
            this.path = path;
            this.leafBean = leafBean;
        }

        @Override
        public String getMessage() {
            return message;
        }

        @Override
        public String getMessageTemplate() {
            return messageTemplate;
        }

        @Override
        public T getRootBean() {
            return rootBean;
        }

        @SuppressWarnings("unchecked")
        @Override
        public Class<T> getRootBeanClass() {
            return rootBeanClass;
        }

        @Override
        public Object getLeafBean() {
            return leafBean;
        }

        @Override
        public Object[] getExecutableParameters() {
            return null;
        }

        @Override
        public Object getExecutableReturnValue() {
            return null;
        }

        @Override
        public Path getPropertyPath() {
            return path;
        }

        @Override
        public Object getInvalidValue() {
            return invalidValue;
        }

        @Override
        public ConstraintDescriptor<?> getConstraintDescriptor() {
            return null;
        }

        @Override
        public <U> U unwrap(Class<U> type) {
            throw new UnsupportedOperationException("Unwrapping is unsupported by this implementation");
        }
    }
}
