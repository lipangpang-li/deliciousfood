package org.example.serviceImpl;

import jakarta.annotation.Resource;
import org.example.entity.Name;
import org.example.mapper.NameMapper;
import org.example.service.NameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NameServiceImpl implements NameService {

    @Resource
    NameMapper nameMapper;
    @Override
    public List<Name> getAll() {
        return nameMapper.getAll();
    }
}
