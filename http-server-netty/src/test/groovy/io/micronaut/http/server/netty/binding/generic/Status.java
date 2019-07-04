package io.micronaut.http.server.netty.binding.generic;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

@JsonDeserialize(builder = Status.StatusBuilder.class)
public class Status {

    private String name;

    private Status(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    @JsonPOJOBuilder
    public static class StatusBuilder {

        private String name;

        public StatusBuilder withName(String name) {
            this.name = name;
            return this;
        }

        public Status build() {
            return new Status(name);
        }
    }
}
