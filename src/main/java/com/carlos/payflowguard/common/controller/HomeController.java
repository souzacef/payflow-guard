package com.carlos.payflowguard.common.controller;

import com.carlos.payflowguard.common.response.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    @GetMapping("/")
    public ApiResponse home() {
        return new ApiResponse("Welcome to PayFlow Guard", "success");
    }
}