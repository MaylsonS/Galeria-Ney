package com.pessoal.galeria_ney.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/hellow")
public class HellowWorldController {

    @GetMapping
    public String HellowWorldController() {
        return "HellowWorldController aaaa";
    }
}
