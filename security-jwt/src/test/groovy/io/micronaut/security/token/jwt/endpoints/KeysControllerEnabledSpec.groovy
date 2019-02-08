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
package io.micronaut.security.token.jwt.endpoints

import io.micronaut.context.ApplicationContext
import io.micronaut.context.env.Environment
import io.micronaut.context.exceptions.NoSuchBeanException
import io.micronaut.runtime.server.EmbeddedServer
import spock.lang.AutoCleanup
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

class KeysControllerEnabledSpec extends Specification {

    @Unroll("if m.s.enabled=true and m.s.token.jwt.enabled=true and m.s.token.jwt.endpoints.oauth.enabled=false bean [#description] is not loaded")
    void "if micronaut.security.enabled=false security related beans are not loaded"(Class clazz, String description) {
        given:
        EmbeddedServer embeddedServer = ApplicationContext.run(EmbeddedServer, [
                'spec.name'                                          : KeysControllerEnabledSpec.simpleName,
                'micronaut.security.enabled'                         : true,
                'micronaut.security.token.jwt.enabled'               : true,
                'micronaut.security.token.jwt.endpoints.keys.enabled': false,

        ], Environment.TEST)

        when:
        embeddedServer.applicationContext.getBean(clazz)

        then:
        def e = thrown(NoSuchBeanException)
        e.message.contains('No bean of type [' + clazz.name + '] exists.')

        cleanup:
        embeddedServer.close()

        where:
        clazz << [
                KeysController,
                KeysControllerConfiguration,
                KeysControllerConfigurationProperties,
        ]

        description = clazz.name
    }

    @Unroll
    void "#description loads if micronaut.security.endpoints.keys.enabled=true"(Class clazz, String description) {
        given:
        EmbeddedServer embeddedServer = ApplicationContext.run(EmbeddedServer, [
                'spec.name'                 : KeysControllerEnabledSpec.simpleName,
                'micronaut.security.enabled': true,
                'micronaut.security.token.jwt.enabled': true,
                'micronaut.security.endpoints.keys.enabled': true,

        ], Environment.TEST)

        when:
        embeddedServer.applicationContext.getBean(clazz)

        then:
        noExceptionThrown()

        cleanup:
        embeddedServer.close()

        where:
        clazz << [KeysController, KeysControllerConfiguration, KeysControllerConfigurationProperties]
        description = clazz.name
    }
}
