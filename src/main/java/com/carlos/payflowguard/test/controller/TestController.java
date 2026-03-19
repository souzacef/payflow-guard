package com.carlos.payflowguard.test.controller;

import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/test")
public class TestController {

    @GetMapping
    public Map<String, String> test() {
        return Map.of(
                "message", "PayFlow Guard API is working",
                "version", "v1"
        );
    }
}