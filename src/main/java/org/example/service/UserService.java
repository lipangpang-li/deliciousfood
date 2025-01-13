package org.example.service;

import org.example.entity.User;

import java.util.List;

public interface UserService {
    User createUser(User user);
    User getUserById(String id);
    User getUserByUsername(String username);
    User getUserByEmail(String email);

    User getUserByPhone(String phone);
    List<User> getAllUsers();
    void updateUser(User user);
    void deleteUser(Integer id);
    boolean existsByPhoneNumber(String email);
}
