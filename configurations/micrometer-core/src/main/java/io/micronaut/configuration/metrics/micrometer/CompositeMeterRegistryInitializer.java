package io.micronaut.configuration.metrics.micrometer;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.composite.CompositeMeterRegistry;
import io.micronaut.context.BeanContext;
import io.micronaut.context.event.BeanCreatedEvent;
import io.micronaut.context.event.BeanCreatedEventListener;
import io.micronaut.inject.BeanDefinition;

import javax.inject.Singleton;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Initializes a CompositeMeterRegistry.
 */
@Singleton
public class CompositeMeterRegistryInitializer implements BeanCreatedEventListener<CompositeMeterRegistry> {

    @Override
    public CompositeMeterRegistry onCreated(BeanCreatedEvent<CompositeMeterRegistry> event) {
        System.out.println("-- CompositeMeterRegistryInitializer.onCreated");

        CompositeMeterRegistry compositeMeterRegistry = event.getBean();

        BeanContext ctx = event.getSource();
        Collection<Class> meterRegistryTypes = findMeterRegistryTypes(ctx);

        meterRegistryTypes.forEach(it -> compositeMeterRegistry.add((MeterRegistry) ctx.findBean(it).get()));

        return compositeMeterRegistry;
    }

    /**
     * Find all meterRegistry types, except the CompositeMeterRegistry.
     *
     * @param ctx The Bean Context
     * @return Collection of MeterRegistry classes
     */
    protected Collection<Class> findMeterRegistryTypes(BeanContext ctx) {
        return ctx.getAllBeanDefinitions().stream()
            .map(BeanDefinition::getBeanType)
            .filter(it -> MeterRegistry.class.isAssignableFrom(it) && it != CompositeMeterRegistry.class)
            .collect(Collectors.toList());
    }
}
