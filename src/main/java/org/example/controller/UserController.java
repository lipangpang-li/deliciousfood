package org.example.controller;

import cn.dev33.satoken.stp.StpUtil;
import io.netty.util.internal.StringUtil;
import jakarta.annotation.Resource;
import org.example.entity.User;
import org.example.entity.basic.Data;
import org.example.service.UserService;
import org.example.utils.EncrytionUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private UserService userService;

    @RequestMapping("/register")
    public Data<User> register(@RequestBody User user){
        User userReal = userService.createUser(user);

        Data<User> userData = new Data<>();

        if(StringUtil.isNullOrEmpty(userReal.getId())){

            //status变成999，意为注册重复
            userData.setStatus("999");
            userData.setMessage("手机号已被注册！");
            userData.setMessage(userReal.getUsername());

        }else {
            userData.setStatus("200");
            userData.setMessage("注册成功！");
            userData.setResult(userReal);

        }

        return userData;
    }

    @RequestMapping("/login")
    public Data<User> doLogin(@RequestBody User user ) {

        Data<User> userData = new Data<>();

        User userByUsername = userService.getUserByPhone(user.getPhoneNumber());



        if(userByUsername == null){

            userData.setStatus("999");
            userData.setMessage("用户不存在");
            userData.setResult(userByUsername);

            return userData;

        }

        //对用户输入的密码进行校验
        if(userByUsername.getPassword().equals(EncrytionUtil.md5Encrypt(user.getPassword() + userByUsername.getSalt()))){
            StpUtil.login(userByUsername.getId());

            userData.setStatus("200");
            userData.setMessage("登录成功");
            userData.setResult(userByUsername);


            return userData;
        }else {

            userData.setStatus("999");
            userData.setMessage("密码错误");

            return userData;
        }

    }

    // 查询登录状态，浏览器访问： http://localhost:8080/user/isLogin
    @RequestMapping("isLogin")
    public String isLogin() {
        return "当前会话是否登录：" + StpUtil.isLogin();
    }

}
