package io.micronaut.docs.config.builder;

import javax.annotation.concurrent.Immutable;

/**
 * @author Will Buck
 * @since 1.1
 */
// tag::class[]
@Immutable
public class EngineImpl implements Engine {
    private final int cylinders;
    private final String manufacturer;
    private final CrankShaft crankShaft;
    private final SparkPlug sparkPlug;

    EngineImpl(String manufacturer, int cylinders, CrankShaft crankShaft, SparkPlug sparkPlug) {
        this.crankShaft = crankShaft;
        this.cylinders = cylinders;
        this.manufacturer = manufacturer;
        this.sparkPlug = sparkPlug;
    }                                                                

    @Override
    public int getCylinders() {
        return cylinders;
    }

    public String start() {
        return new StringBuilder()
                .append(manufacturer)
                .append(' ')
                .append("Engine Starting V")
                .append(cylinders)
                .append(" [rodLength=")
                .append(crankShaft.rodLength.orElse(6.0d))
                .append(", sparkPlug=")
                .append(sparkPlug)
                .append(']').toString();
    }

    static Builder builder() {
        return new Builder();
    }

    static final class Builder {
        private String manufacturer = "Ford";
        private int cylinders;

        public Builder withManufacturer(String manufacturer) {
            this.manufacturer = manufacturer;
            return this;
        }

        public Builder withCylinders(int cylinders) {
            this.cylinders = cylinders;
            return this;
        }

        EngineImpl build(CrankShaft.Builder crankShaft, SparkPlug.Builder sparkPlug) {
            return new EngineImpl(manufacturer, cylinders, crankShaft.build(), sparkPlug.build());
        }
    }
}
// end::class[]
