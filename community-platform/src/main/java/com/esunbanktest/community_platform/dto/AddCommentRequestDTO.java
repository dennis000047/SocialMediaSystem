package com.esunbanktest.community_platform.dto;

import lombok.Data;

@Data
public class AddCommentRequestDTO {
    private Integer postId;
    private Integer authorId;
    private String authorName;
    private String content;
}