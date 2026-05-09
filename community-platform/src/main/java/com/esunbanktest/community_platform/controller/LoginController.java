package com.esunbanktest.community_platform.controller;

import com.esunbanktest.community_platform.entity.UserEntity;
import com.esunbanktest.community_platform.service.JwtUtil;
import com.esunbanktest.community_platform.service.LoginResult;
import com.esunbanktest.community_platform.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:5173")
public class LoginController {

    //注入
    @Autowired
    private UserService userService; 

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> payload, HttpSession session) {
        String phone = payload.get("phone");
        String password = payload.get("password");

        LoginResult result = userService.login(phone, password);

        Map<String, Object> response = new HashMap<>();
        response.put("success", result.isSuccess());
        response.put("message", result.getMessage());

        if (result.isSuccess()) {
            UserEntity user = result.getUser();
            session.setAttribute("USER_ID", user.getUserId());
            session.setAttribute("USER_NAME", user.getUserName());

            // 生成JWT token
            String token = jwtUtil.generateToken(user.getUserId(), user.getUserName());

            response.put("token", token);
            response.put("userId", user.getUserId());
            response.put("userName", user.getUserName());
        }

        return response;
    }

    @PostMapping("/register")
    public Map<String, Object> register(@RequestBody UserEntity user) {
        // 這裡會呼叫 service 存入資料庫
        boolean success = userService.register(user);
        return Map.of("success", success, "message", success ? "註冊成功" : "此手機號碼已被註冊");
    }
}