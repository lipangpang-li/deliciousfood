package org.example.service;

import org.example.entity.DeliciousFood;


import java.util.List;


public interface DeliciousFoodService {
    List<DeliciousFood> getBySomeThing(String something);

    int creatDeliciousFood(DeliciousFood deliciousFood);
}
