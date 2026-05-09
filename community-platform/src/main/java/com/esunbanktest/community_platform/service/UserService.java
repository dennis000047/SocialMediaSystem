package com.esunbanktest.community_platform.service;

import com.esunbanktest.community_platform.entity.UserEntity;
import com.esunbanktest.community_platform.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.ParameterMode;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.StoredProcedureQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @PersistenceContext
    private EntityManager entityManager; // 用來執行手動 SP 查詢

    /**
     * 封裝 SP 查詢邏輯：解決找不到人會噴 NoResultException 的問題
     */
    private UserEntity findUserByPhoneViaSP(String phone) {
        try {
            StoredProcedureQuery query = entityManager.createStoredProcedureQuery("sp_GetUserByPhone", UserEntity.class);
            query.registerStoredProcedureParameter("phone", String.class, ParameterMode.IN);
            query.setParameter("phone", phone);
            return (UserEntity) query.getSingleResult();
        } catch (NoResultException e) {
            return null; // 找不到回傳 null
        }
    }

    @Transactional
    public boolean register(UserEntity user) {
        // 使用封裝好的 SP 查詢
        if (findUserByPhoneViaSP(user.getPhone()) != null) {
            return false; 
        }
        // 1. 加密密碼
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        // 2. 存入資料庫 (JPA自動Insert)
        userRepository.save(user);
        return true;
    }

    @Transactional(readOnly = true)
    public LoginResult login(String phone, String rawPassword) {
        // 1. 使用封裝好的 SP 查詢
        UserEntity user = findUserByPhoneViaSP(phone);

        // 2. 查無資料
        if (user == null) {
            return new LoginResult(false, "此手機號碼尚未註冊", null);
        }

        // 3. 密碼驗證
        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            return new LoginResult(false, "密碼輸入錯誤", null);
        }

        return new LoginResult(true, "登入成功", user);
    }
}