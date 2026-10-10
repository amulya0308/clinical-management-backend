package com.clinic.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SpaForwardingController {

    @GetMapping(value = {
            "/",
            "/{path:[^\\.]*}"
    })
    public String forwardSpa() {
        return "forward:/index.html";
    }
}
