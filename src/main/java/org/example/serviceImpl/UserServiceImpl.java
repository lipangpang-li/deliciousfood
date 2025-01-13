package org.example.serviceImpl;

import jakarta.annotation.Resource;
import org.example.entity.User;
import org.example.mapper.UserMapper;
import org.example.service.UserService;
import org.example.utils.EncrytionUtil;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;


@Service
public class UserServiceImpl implements UserService {
    @Resource
    private UserMapper userMapper;

    @Override
    public User createUser(User user) {
        //验证手机号是否被注册
        if (userMapper.existsByPhoneNumber(user.getPhoneNumber())){

            //把userid变成注册失败的原因。

            user.setUsername("该手机已被注册");

            return user;
            //throw new RuntimeException("该手机已被注册");
        }

        //验证邮箱是否已被注册
        if (userMapper.existsByEmail(user.getEmail())){

            user.setUsername("该邮箱已被注册");

            return user;
            //throw new RuntimeException("该邮箱已被注册");
        }

        //添加状态
        user.setStatus(User.Status.ACTIVE);

        //生成用户id
        user.setId(UUID.randomUUID().toString());

        //赋予用户角色
        user.setRole(User.Role.USER);

        //生成私盐
        String salt = UUID.randomUUID().toString();

        //私盐加密码生成后台存储的密码
        user.setPassword(EncrytionUtil.md5Encrypt(user.getPassword() + salt));

        user.setSalt(salt);

        userMapper.save(user);

        return user;
    }

    @Override
    public User getUserById(String id) {
        return userMapper.findById(id);
    }

    @Override
    public User getUserByUsername(String username) {
        return userMapper.findByUsername(username);
    }

    @Override
    public User getUserByEmail(String email) {
        return userMapper.findByEmail(email);
    }

    @Override
    public User getUserByPhone(String phone) {
        User byPhone = userMapper.findByPhone(phone);
        return byPhone;
    }

    @Override
    public boolean existsByPhoneNumber(String email) {
        return userMapper.existsByPhoneNumber(email);
    }
    @Override
    public List<User> getAllUsers() {
        return userMapper.getAll();
    }

    @Override
    public void updateUser(User user) {
        userMapper.save(user);
    }

    @Override
    public void deleteUser(Integer id) {
        userMapper.deleteById(id);
    }
}
