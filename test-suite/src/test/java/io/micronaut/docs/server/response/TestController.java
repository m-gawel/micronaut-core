package io.micronaut.docs.server.response;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Produces;

//tag::clazz[]
@Controller("/test")
public class TestController {

    @Get
    public HttpResponse index() {
        return HttpResponse.ok().body("{\"msg\":\"This is JSON\"}");
    }

    @Produces(MediaType.TEXT_HTML) // <1>
    @Get("/html")
    public String html() {
        return "<html><title><h1>HTML</h1></title><body></body></html>";
    }
}
//end::clazz[]