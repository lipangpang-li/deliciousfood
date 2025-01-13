package org.example.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.example.entity.Name;

import java.util.List;

@Mapper
public interface NameMapper {
    public List<Name> getAll();

    public Name getone(String name);

}
