package com.esunbanktest.community_platform.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class PostResponseDTO {
    private Integer postId;
    private String content;
    private String image;
    private LocalDateTime createdAt;
    private String authorName;
    private Integer authorId;
    private List<CommentDTO> comments;
}