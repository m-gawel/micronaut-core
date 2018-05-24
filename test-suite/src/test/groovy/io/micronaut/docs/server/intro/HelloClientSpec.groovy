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
package io.micronaut.docs.server.intro

import io.micronaut.context.ApplicationContext


// tag::imports[]
import io.micronaut.runtime.server.EmbeddedServer
import spock.lang.*
// end::imports[]

/**
 * @author graemerocher
 * @since 1.0
 */
// tag::class[]
class HelloClientSpec extends Specification {
    @Shared @AutoCleanup EmbeddedServer embeddedServer =
            ApplicationContext.run(EmbeddedServer) // <1>

    @Shared HelloClient client = embeddedServer
                                        .applicationContext
                                        .getBean(HelloClient) // <2>


    void "test hello world response"() {
        expect:
        client.hello().blockingGet() == "Hello World" // <3>
    }

}
// end::class[]