package com.carlos.payflowguard.test.controller;

import com.carlos.payflowguard.common.response.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/test")
public class TestController {

    @GetMapping
    public ApiResponse test() {
        return new ApiResponse("PayFlow Guard API is working", "success");
    }
}