package com.esunbanktest.community_platform.dto;

import lombok.Data;

@Data
public class AddPostRequestDTO {
    private Integer authorId;
    private String content;
    private String image;
}