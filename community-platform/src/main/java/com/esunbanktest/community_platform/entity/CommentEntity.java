package com.esunbanktest.community_platform.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "Comments")
@Data
public class CommentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CommentID")
    private Integer commentId;

    @Column(name = "UserID", nullable = false)
    private Integer userId;

    @Column(name = "PostID", nullable = false)
    private Integer postId;

    @Column(name = "Content", nullable = false, columnDefinition = "NVARCHAR(1000)")
    private String content;

    @Column(name = "IsDeleted", insertable = false)
    private Boolean isDeleted;

    @Column(name = "CreatedAt", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    // 關聯到User
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "UserID", insertable = false, updatable = false)
    private UserEntity user;

    // 關聯到Post
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PostID", insertable = false, updatable = false)
    private PostEntity post;
}