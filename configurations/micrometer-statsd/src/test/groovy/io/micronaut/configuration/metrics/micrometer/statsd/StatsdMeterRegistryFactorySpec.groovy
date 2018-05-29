package io.micronaut.configuration.metrics.micrometer.statsd

import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.composite.CompositeMeterRegistry
import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import io.micrometer.statsd.StatsdFlavor
import io.micrometer.statsd.StatsdMeterRegistry
import io.micronaut.context.ApplicationContext
import spock.lang.Specification
import spock.lang.Unroll

import java.time.Duration

import static io.micronaut.configuration.metrics.micrometer.MeterRegistryFactory.MICRONAUT_METRICS_ENABLED
import static io.micronaut.configuration.metrics.micrometer.statsd.StatsdConfiguration.STATSD_ENABLED
import static io.micronaut.configuration.metrics.micrometer.statsd.StatsdConfiguration.STATSD_FLAVOR
import static io.micronaut.configuration.metrics.micrometer.statsd.StatsdConfiguration.STATSD_HOST
import static io.micronaut.configuration.metrics.micrometer.statsd.StatsdConfiguration.STATSD_PORT
import static io.micronaut.configuration.metrics.micrometer.statsd.StatsdConfiguration.STATSD_STEP

class StatsdMeterRegistryFactorySpec extends Specification {

    def "wireup the bean manually"() {
        when:
        StatsdMeterRegistryFactory factory = new StatsdMeterRegistryFactory(new StatsdConfigurationProperties())

        then:
        factory.statsdMeterRegistry()
    }

    void "verify StatsdMeterRegistry is created by default when this configuration used"() {
        when:
        ApplicationContext context = ApplicationContext.run()

        then:
        context.getBeansOfType(MeterRegistry).size() == 3
        context.getBeansOfType(MeterRegistry)*.class*.simpleName.containsAll(['CompositeMeterRegistry', 'SimpleMeterRegistry', 'StatsdMeterRegistry'])
    }

    void "verify CompositeMeterRegistry created by default"() {
        when:
        ApplicationContext context = ApplicationContext.run()
        CompositeMeterRegistry compositeRegistry = context.getBean(CompositeMeterRegistry)

        then:
        compositeRegistry
        compositeRegistry.registries.size() == 2
        compositeRegistry.registries*.class.containsAll([StatsdMeterRegistry, SimpleMeterRegistry])
    }

    @Unroll
    void "verify StatsdMeterRegistry bean exists = #result when config #cfg = #setting"() {
        when:
        ApplicationContext context = ApplicationContext.run([(cfg): setting])

        then:
        context.findBean(StatsdMeterRegistry).isPresent() == result

        where:
        cfg                       | setting | result
        MICRONAUT_METRICS_ENABLED | false   | false
        MICRONAUT_METRICS_ENABLED | true    | true
        STATSD_ENABLED            | true    | true
        STATSD_ENABLED            | false   | false
    }

    void "verify StatsdMeterRegistry bean exists datadog flavor"() {
        when:
        ApplicationContext context = ApplicationContext.run([(STATSD_ENABLED): true, (STATSD_FLAVOR): StatsdFlavor.DATADOG])
        Optional<StatsdMeterRegistry> statsdMeterRegistry = context.findBean(StatsdMeterRegistry)

        then:
        statsdMeterRegistry.isPresent()
        statsdMeterRegistry.get().statsdConfig.enabled()
        statsdMeterRegistry.get().statsdConfig.flavor() == StatsdFlavor.DATADOG
        statsdMeterRegistry.get().statsdConfig.port() == 8125
        statsdMeterRegistry.get().statsdConfig.host() == "localhost"
        statsdMeterRegistry.get().statsdConfig.step() == Duration.ofMinutes(1)
    }

    void "verify StatsdMeterRegistry bean exists etsy flavor"() {
        when:
        ApplicationContext context = ApplicationContext.run([(STATSD_ENABLED): true, (STATSD_FLAVOR): StatsdFlavor.ETSY])
        Optional<StatsdMeterRegistry> statsdMeterRegistry = context.findBean(StatsdMeterRegistry)

        then:
        statsdMeterRegistry.isPresent()
        statsdMeterRegistry.get().statsdConfig.enabled()
        statsdMeterRegistry.get().statsdConfig.flavor() == StatsdFlavor.ETSY
        statsdMeterRegistry.get().statsdConfig.port() == 8125
        statsdMeterRegistry.get().statsdConfig.host() == "localhost"
        statsdMeterRegistry.get().statsdConfig.step() == Duration.ofMinutes(1)
    }

    void "verify StatsdMeterRegistry bean exists telegraf flavor"() {
        when:
        ApplicationContext context = ApplicationContext.run([(STATSD_ENABLED): true, (STATSD_FLAVOR): StatsdFlavor.TELEGRAF])
        Optional<StatsdMeterRegistry> statsdMeterRegistry = context.findBean(StatsdMeterRegistry)

        then:
        statsdMeterRegistry.isPresent()
        statsdMeterRegistry.get().statsdConfig.enabled()
        statsdMeterRegistry.get().statsdConfig.flavor() == StatsdFlavor.TELEGRAF
        statsdMeterRegistry.get().statsdConfig.port() == 8125
        statsdMeterRegistry.get().statsdConfig.host() == "localhost"
        statsdMeterRegistry.get().statsdConfig.step() == Duration.ofMinutes(1)
    }

    void "verify StatsdMeterRegistry bean exists datadog flavor changed port, host and step"() {
        when:
        ApplicationContext context = ApplicationContext.run([
                (STATSD_ENABLED): true,
                (STATSD_FLAVOR) : StatsdFlavor.DATADOG,
                (STATSD_HOST)   : "zerocool",
                (STATSD_PORT)   : 8122,
                (STATSD_STEP)   : "PT2M",
        ])
        Optional<StatsdMeterRegistry> statsdMeterRegistry = context.findBean(StatsdMeterRegistry)

        then:
        statsdMeterRegistry.isPresent()
        statsdMeterRegistry.get().statsdConfig.enabled()
        statsdMeterRegistry.get().statsdConfig.flavor() == StatsdFlavor.DATADOG
        statsdMeterRegistry.get().statsdConfig.port() == 8122
        statsdMeterRegistry.get().statsdConfig.host() == "zerocool"
        statsdMeterRegistry.get().statsdConfig.step() == Duration.ofMinutes(2)
    }
}
