package com.esunbanktest.community_platform.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.config.Customizer;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // 1. 關閉 CSRF
            .csrf(csrf -> csrf.disable())
            // 2. 開啟 CORS (使用預設配置)
            .cors(Customizer.withDefaults())
            // 3. 添加JWT Filter
            .addFilterBefore(jwtAuthenticationFilter, org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class)
            // 4. 設定路徑權限
            .authorizeHttpRequests(auth -> auth
                // 允許登入和註冊
                .requestMatchers("/api/login", "/api/register").permitAll()
                // 其他API需要驗證
                .requestMatchers("/api/**").authenticated()
                .anyRequest().permitAll()
            )
            // 5. 開啟基礎表單登入 (選配，方便測試)
            .formLogin(Customizer.withDefaults())
            .httpBasic(Customizer.withDefaults());

        return http.build();
    }
}