package org.example.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.example.entity.User;

import java.util.List;

@Mapper
public interface UserMapper {
    User findByUsername(String username);
    User findByEmail(String email);

    void save(User user);
    User findById(String id);

    User findByPhone(String phone);

    int updateUser(User user);

    int deleteById(Integer id);

    int updateUserStatus(User user);

    int updateUserRole(User user);

    List<User> getAll();

    boolean existsByPhoneNumber(String phoneNumber);

    boolean existsByEmail(String email);

}
