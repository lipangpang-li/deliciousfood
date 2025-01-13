package org.example.serviceImpl;

import jakarta.annotation.Resource;
import org.example.entity.DeliciousFood;
import org.example.mapper.DeliciousFoodMapper;
import org.example.service.DeliciousFoodService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DeliciousFoodServiceImpl implements DeliciousFoodService {

    @Resource
    DeliciousFoodMapper deliciousFoodMapper;

    @Override
    public List<DeliciousFood> getBySomeThing(String something) {
        return deliciousFoodMapper.getBySomeThing(something);
    }

    @Override
    @Transactional
    public int creatDeliciousFood(DeliciousFood deliciousFood) {
        try {

            deliciousFoodMapper.creatDeliciousFood(deliciousFood);
            return 1;

        }catch (Exception e){

            return 0;

        }
    }
}
