package com.esunbanktest.community_platform.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CommentDTO {
    private Integer commentId;
    private String content;
    private LocalDateTime createdAt;
    private Integer userId;
    private String userName;
}