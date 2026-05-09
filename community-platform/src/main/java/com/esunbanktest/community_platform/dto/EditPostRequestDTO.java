package com.esunbanktest.community_platform.dto;

import lombok.Data;

@Data
public class EditPostRequestDTO {
    private Integer id;
    private String content;
    private Integer userId;
    private String image;
}