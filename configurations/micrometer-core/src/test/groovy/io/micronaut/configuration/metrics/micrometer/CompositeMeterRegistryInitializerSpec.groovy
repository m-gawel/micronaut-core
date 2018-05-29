package io.micronaut.configuration.metrics.micrometer

import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import io.micronaut.context.ApplicationContext
import spock.lang.Specification

import static io.micronaut.configuration.metrics.micrometer.MeterRegistryFactory.MICRONAUT_METRICS_ENABLED

class CompositeMeterRegistryInitializerSpec extends Specification {

    def "test getting the registry types"() {
        given:
        ApplicationContext context = ApplicationContext.run()
        CompositeMeterRegistryInitializer initializer = new CompositeMeterRegistryInitializer()

        when:
        def types = initializer.findMeterRegistryTypes(context)

        then:
        types.size() == 1
        types.contains(SimpleMeterRegistry)

        cleanup:
        context.close()
    }

    def "test getting the registry types when metrics are disabled"() {
        given:
        ApplicationContext context = ApplicationContext.run([(MICRONAUT_METRICS_ENABLED): false])
        CompositeMeterRegistryInitializer initializer = new CompositeMeterRegistryInitializer()

        when:
        def types = initializer.findMeterRegistryTypes(context)

        then:
        types.size() == 0

        cleanup:
        context.close()
    }
}
