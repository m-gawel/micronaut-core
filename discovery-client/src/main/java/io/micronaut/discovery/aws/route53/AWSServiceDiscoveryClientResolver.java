/*
 * Copyright 2017-2018 original authors
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

package io.micronaut.discovery.aws.route53;

import com.amazonaws.services.servicediscovery.AWSServiceDiscoveryAsync;
import com.amazonaws.services.servicediscovery.AWSServiceDiscoveryAsyncClientBuilder;
import io.micronaut.configurations.aws.AWSClientConfiguration;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.env.Environment;
import javax.inject.Singleton;

/**
 * This gets a real amazon service discovery client. It is abstracted to allow for it to be replaced easier during testing.
 */
@Requires(env = Environment.AMAZON_EC2)
@Singleton
public class AWSServiceDiscoveryClientResolver implements AWSServiceDiscoveryResolver {
    AWSServiceDiscoveryAsync awsServiceDiscoveryAsync;

    /**
     * Constructor builds standard client with given Micronaut configuration.
     * @param clientConfiguration aws client configuration to use
     */
    public AWSServiceDiscoveryClientResolver(AWSClientConfiguration clientConfiguration) {
        awsServiceDiscoveryAsync = AWSServiceDiscoveryAsyncClientBuilder.standard().withClientConfiguration(clientConfiguration.getClientConfiguration()).build();
    }

    /**
     * resolve the AWS Service Discovery client when making calls to AWS.
     * @param environment
     * @return AWSServiceDiscoveryAsync interface which works with blocking calls as well so no need for both
     */
    @Override
    public AWSServiceDiscoveryAsync resolve(Environment environment) {
          return awsServiceDiscoveryAsync;
    }
}
