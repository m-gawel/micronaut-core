package io.micronaut.http.server.netty.binding.generic;

import io.micronaut.http.annotation.Controller;

import java.util.UUID;

@Controller("/statuses/java")
public class StatusController extends GenericController<Status, UUID> {

}
