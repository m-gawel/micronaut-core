/*
 * Copyright 2017-2019 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.micronaut.configuration.cassandra;

import com.datastax.driver.core.Cluster;
import io.micronaut.context.annotation.ConfigurationBuilder;
import io.micronaut.context.annotation.EachProperty;

/**
 * Allows the configuration of a Cassandra Cluster connection using the datastax driver.
 *
 * The client is able to be configured to multiple clusters. If there are multiple configuration keys, default can be
 * used to denote the primary cluster bean.
 *
 * @author Nirav Assar
 * @since 1.0
 */
@EachProperty(value = "cassandra", primary = "default")
public class CassandraConfiguration {

    @SuppressWarnings("WeakerAccess")
    @ConfigurationBuilder(allowZeroArgs = true, prefixes = { "with", "add" })
    Cluster.Builder builder = Cluster.builder();

    /**
     * The configuration builder.
     *
     * @return The builder
     */
    public Cluster.Builder getBuilder() {
        return builder;
    }
}
