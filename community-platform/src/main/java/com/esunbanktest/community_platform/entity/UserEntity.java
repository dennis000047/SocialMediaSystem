package com.esunbanktest.community_platform.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "Users")
@Data
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "UserID") // 對應 SQL 的 [UserID]
    private Integer userId;

    @Column(name = "Phone", nullable = false, unique = true)
    private String phone;

    @Column(name = "UserName", nullable = false)
    private String userName;
    

    @Column(name = "Email")
    private String email;

    @Column(name = "Password", nullable = false)
    private String password;

    @Column(name = "CoverImage")
    private String coverImage;
    

    @Column(name = "Biography")
    private String biography;

    @Column(name = "CreatedAt", insertable = false, updatable = false)
    private LocalDateTime createdAt;
}