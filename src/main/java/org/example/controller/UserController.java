package org.example.controller;

import cn.dev33.satoken.stp.StpUtil;
import io.netty.util.internal.StringUtil;
import jakarta.annotation.Resource;
import org.example.entity.User;
import org.example.entity.basic.Data;
import org.example.service.UserService;
import org.example.utils.EncrytionUtil;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Scanner;

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

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("请输入要爬取的网址: ");
        String url = scanner.nextLine();
        scanner.close();

        try {
            // 获取网页文档
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/107.0.0.0 Safari/537.36")
                    .timeout(10000)
                    .get();

            // 提取页面所有文本
            String text = doc.text();

            // 输出结果
            System.out.println("\n网页文本内容:");
            System.out.println("==================================");
            System.out.println(text);
            System.out.println("==================================");
            System.out.println("爬取完成！共提取字符数: " + text.length());

        } catch (Exception e) {
            System.err.println("爬取过程中出错: " + e.getMessage());
        }
    }
}
