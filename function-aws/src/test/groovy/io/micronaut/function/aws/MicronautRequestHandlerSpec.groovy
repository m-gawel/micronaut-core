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
package io.micronaut.function.aws

import com.amazonaws.services.lambda.runtime.Context
import io.micronaut.context.env.Environment
import spock.lang.Specification

import javax.inject.Inject
import javax.inject.Singleton

/**
 * @author Graeme Rocher
 * @since 1.0
 */
class MicronautRequestHandlerSpec extends Specification {

    void "test micronaut request handler"() {
        given:
        System.setProperty(Environment.ENVIRONMENTS_PROPERTY, "foo")
        expect:
        new RoundHandler().handleRequest(1.6f, Mock(Context)) == 2
        cleanup:
        System.setProperty(Environment.ENVIRONMENTS_PROPERTY, "")
    }

    @Singleton
    static class MathService {
        Integer round(Float input) {
            return Math.round(input)
        }
    }

    static class RoundHandler extends MicronautRequestHandler<Float, Integer> {

        @Inject MathService mathService
        @Inject Environment env

        @Override
        Integer execute(Float input) {
            assert env.activeNames.contains(Environment.FUNCTION)
            assert env.activeNames.contains("foo")
            return mathService.round(input)
        }
    }
}
