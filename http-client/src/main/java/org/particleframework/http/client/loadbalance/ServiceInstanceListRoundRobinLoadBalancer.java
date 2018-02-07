/*
 * Copyright 2018 original authors
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
package org.particleframework.http.client.loadbalance;

import org.particleframework.core.async.publisher.Publishers;
import org.particleframework.discovery.ServiceInstance;
import org.particleframework.discovery.ServiceInstanceList;
import org.reactivestreams.Publisher;

import javax.annotation.Nullable;

/**
 * @author Graeme Rocher
 * @since 1.0
 */
public class ServiceInstanceListRoundRobinLoadBalancer extends AbstractRoundRobinLoadBalancer {
    private final ServiceInstanceList serviceInstanceList;

    public ServiceInstanceListRoundRobinLoadBalancer(ServiceInstanceList serviceInstanceList) {
        this.serviceInstanceList = serviceInstanceList;
    }

    @Override
    public Publisher<ServiceInstance> select(@Nullable Object discriminator) {
        return Publishers.just(getNextAvailable(serviceInstanceList.getInstances()));
    }

    @Override
    public String getServiceID() {
        return serviceInstanceList.getID();
    }
}
