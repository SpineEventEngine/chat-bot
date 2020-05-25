package io.spine.chatbot;

import io.micronaut.http.annotation.*;

@Controller("/hello")
public class HelloController {

    @Get(uri="/", produces="text/plain")
    public String index() {
        return "Example Response";
    }
}