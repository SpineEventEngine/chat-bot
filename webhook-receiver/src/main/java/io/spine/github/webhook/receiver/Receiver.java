package io.spine.github.webhook.receiver;

import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;

@Controller("/web-hook")
public class Receiver {

    @Get(uri="/", produces="text/plain")
    public String process() {
        return "OK";
    }
}