package org.example.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.example.entity.DeliciousFood;

import java.util.List;

@Mapper
public interface DeliciousFoodMapper {


    List<DeliciousFood> getBySomeThing(String something);

    void creatDeliciousFood(DeliciousFood deliciousFood);
}
