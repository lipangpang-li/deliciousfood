package org.example.controller;

import jakarta.annotation.Resource;
import org.example.entity.DeliciousFood;
import org.example.entity.basic.Data;
import org.example.service.DeliciousFoodService;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/deliciousFood")
public class DeliciousFoodController {

    @Resource
    DeliciousFoodService deliciousFoodService;

    @RequestMapping("/getBySomeThing")
    public Data<DeliciousFood> getBySomeThing(@RequestParam("something") String something){
        Data<DeliciousFood> deliciousFoodData = new Data<>();

        deliciousFoodData.setResults(deliciousFoodService.getBySomeThing(something));

        return deliciousFoodData;

    }

    @RequestMapping("/creatDeliciousFood")
    public int creatDeliciousFood(@RequestBody DeliciousFood deliciousFood){

        int total = deliciousFoodService.creatDeliciousFood(deliciousFood);

        return total;
    }

}
