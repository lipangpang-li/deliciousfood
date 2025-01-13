package org.example.controller;

import jakarta.annotation.Resource;
import org.example.service.NameService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class IndexController {

    @Resource
    NameService nameService;

    @RequestMapping("/index")
    @ResponseBody
    public String index() {

        return "hello pangpang";

    }
}
