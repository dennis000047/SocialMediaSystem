package com.esunbanktest.community_platform.repository;

import com.esunbanktest.community_platform.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Integer> {

    // 使用 Stored Procedure 根據 phone 查詢資料庫
    // @Procedure(procedureName = "sp_GetUserByPhone")
    // UserEntity findByPhone(@Param("phone") String phone);
}