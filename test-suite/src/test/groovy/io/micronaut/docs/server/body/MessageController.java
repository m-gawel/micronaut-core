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
package io.micronaut.docs.server.body;

// tag::imports[]
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.micronaut.http.*;
import io.micronaut.http.annotation.*;
import javax.inject.Singleton;
import javax.validation.constraints.Size;
// end::imports[]

/**
 * @author Graeme Rocher
 * @since 1.0
 */
// tag::class[]
@Controller("/receive")
public class MessageController {
// end::class[]

    // tag::echo[]
    @Post(consumes = MediaType.TEXT_PLAIN) // <1>
    String echo(@Size(max = 1024) @Body String text) { // <2>
        return text; // <3>
    }
    // end::echo[]

    // tag::echoReactive[]
    @Post(consumes = MediaType.TEXT_PLAIN) // <1>
    Single<MutableHttpResponse<String>> echoFlow(@Body Flowable<String> text) { //<2>
        return text.collect(StringBuffer::new, StringBuffer::append) // <3>
                   .map(buffer ->
                        HttpResponse.ok(buffer.toString())
                   );
    }
    // end::echoReactive[]
}

