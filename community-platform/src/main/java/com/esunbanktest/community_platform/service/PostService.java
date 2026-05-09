package com.esunbanktest.community_platform.service;

import com.esunbanktest.community_platform.dto.AddPostRequestDTO;
import com.esunbanktest.community_platform.dto.AddCommentRequestDTO;
import com.esunbanktest.community_platform.dto.CommentDTO;
import com.esunbanktest.community_platform.dto.EditPostRequestDTO;
import com.esunbanktest.community_platform.dto.PostResponseDTO;
import com.esunbanktest.community_platform.entity.CommentEntity;
import com.esunbanktest.community_platform.entity.PostEntity;
import com.esunbanktest.community_platform.repository.CommentRepository;
import com.esunbanktest.community_platform.repository.PostRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.StoredProcedureQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.esunbanktest.community_platform.dto.DeletePostRequestDTO;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PostService {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private CommentRepository commentRepository;

    @PersistenceContext
    private EntityManager entityManager;

    public List<PostResponseDTO> getAllPostsWithDetails() {
        StoredProcedureQuery query = entityManager.createStoredProcedureQuery("sp_GetAllPostsWithDetails");
        query.execute();

        @SuppressWarnings("unchecked")
        List<Object[]> rows = query.getResultList();

        Map<Integer, PostResponseDTO> postMap = new java.util.LinkedHashMap<>();

        for (Object[] row : rows) {
            Integer postId = ((Number) row[0]).intValue();
            Integer userId = row[1] != null ? ((Number) row[1]).intValue() : null;
            String content = (String) row[2];
            String image = (String) row[3];
            
            // ↓ 直接 cast 成 LocalDateTime，不要轉 Timestamp
            java.time.LocalDateTime createdAt = row[4] != null ? (java.time.LocalDateTime) row[4] : null;
            
            // ↓ IsDeleted 是 bit，用 Number 轉，不要直接 cast Boolean
            Boolean isDeleted = row[5] != null ? (Boolean) row[5] : false;
            
            String authorName = (String) row[6];
            Integer commentId = row[7] != null ? ((Number) row[7]).intValue() : null;
            String commentContent = (String) row[8];
            
            // ↓ 同上，直接 cast LocalDateTime
            java.time.LocalDateTime commentCreatedAt = row[9] != null ? (java.time.LocalDateTime) row[9] : null;
            
            Integer commentUserId = row[10] != null ? ((Number) row[10]).intValue() : null;
            String commentUserName = (String) row[11];

            if (Boolean.TRUE.equals(isDeleted)) {
                continue;
            }

            PostResponseDTO postDto = postMap.computeIfAbsent(postId, id -> {
                PostResponseDTO dto = new PostResponseDTO();
                dto.setPostId(id);
                dto.setContent(content);
                dto.setImage(image);
                dto.setCreatedAt(createdAt);
                dto.setAuthorName(authorName);
                dto.setAuthorId(userId);
                dto.setComments(new java.util.ArrayList<>());
                return dto;
            });

            if (commentId != null) {
                CommentDTO commentDto = new CommentDTO();
                commentDto.setCommentId(commentId);
                commentDto.setContent(commentContent);
                commentDto.setCreatedAt(commentCreatedAt);
                commentDto.setUserId(commentUserId);
                commentDto.setUserName(commentUserName);
                postDto.getComments().add(commentDto);
            }
        }

        return new java.util.ArrayList<>(postMap.values());
    }

    @Transactional
    public PostResponseDTO createPost(AddPostRequestDTO request) {
        StoredProcedureQuery query = entityManager.createStoredProcedureQuery("sp_CreatePost");
        
        // 註冊三個輸入參數（不要有輸出參數）
        query.registerStoredProcedureParameter("UserID", Integer.class, jakarta.persistence.ParameterMode.IN);
        query.registerStoredProcedureParameter("Content", String.class, jakarta.persistence.ParameterMode.IN);
        query.registerStoredProcedureParameter("Image", String.class, jakarta.persistence.ParameterMode.IN);

        // 設定參數值
        query.setParameter("UserID", request.getAuthorId());
        query.setParameter("Content", request.getContent());
        query.setParameter("Image", request.getImage());

        // 執行 SP 並取得回傳的 PostID
        Object result = query.getSingleResult();
        Integer newPostId = ((Number) result).intValue();

        // 從資料庫取得完整的 PostEntity（包含 user 和 comments）
        PostEntity post = postRepository.findById(newPostId)
                .orElseThrow(() -> new IllegalStateException("新增發文失敗，找不到已建立的文章"));

        return convertToDTO(post);
    }

    @Transactional
    public PostResponseDTO updatePost(EditPostRequestDTO request) {
        if (request.getId() == null || request.getUserId() == null) {
            throw new IllegalArgumentException("PostID 和 userId 都必須提供");
        }

        StoredProcedureQuery query = entityManager.createStoredProcedureQuery("sp_UpdatePost");
        query.registerStoredProcedureParameter("PostID", Integer.class, jakarta.persistence.ParameterMode.IN);
        query.registerStoredProcedureParameter("UserID", Integer.class, jakarta.persistence.ParameterMode.IN);
        query.registerStoredProcedureParameter("Content", String.class, jakarta.persistence.ParameterMode.IN);
        query.registerStoredProcedureParameter("Image", String.class, jakarta.persistence.ParameterMode.IN);

        query.setParameter("PostID", request.getId());
        query.setParameter("UserID", request.getUserId());
        query.setParameter("Content", request.getContent());
        query.setParameter("Image", request.getImage());

        Object result = query.getSingleResult();
        int affectedRows = ((Number) result).intValue();

        if (affectedRows == 0) {
            throw new IllegalArgumentException("更新失敗，可能不是發文者或文章不存在");
        }

        PostEntity post = postRepository.findById(request.getId())
                .orElseThrow(() -> new IllegalStateException("更新後找不到文章"));

        return convertToDTO(post);
    }

    @Transactional
    public CommentDTO addComment(AddCommentRequestDTO request) {
        if (request.getPostId() == null || request.getAuthorId() == null || request.getContent() == null || request.getContent().trim().isEmpty()) {
            throw new IllegalArgumentException("postId、authorId 和 content 都必須提供");
        }

        StoredProcedureQuery query = entityManager.createStoredProcedureQuery("sp_CreateComment");
        query.registerStoredProcedureParameter("PostID", Integer.class, jakarta.persistence.ParameterMode.IN);
        query.registerStoredProcedureParameter("UserID", Integer.class, jakarta.persistence.ParameterMode.IN);
        query.registerStoredProcedureParameter("Content", String.class, jakarta.persistence.ParameterMode.IN);

        query.setParameter("PostID", request.getPostId());
        query.setParameter("UserID", request.getAuthorId());
        query.setParameter("Content", request.getContent());

        Object result = query.getSingleResult();
        Integer newCommentId = ((Number) result).intValue();

        CommentEntity comment = commentRepository.findByIdWithUser(newCommentId)
                .orElseThrow(() -> new IllegalStateException("新增留言後找不到留言"));

        return convertCommentToDTO(comment);
    }

    @Transactional
    public Integer deletePost(DeletePostRequestDTO request) {
        if (request.getId() == null || request.getUserId() == null) {
            throw new IllegalArgumentException("PostID 和 userId 都必須提供");
        }

        StoredProcedureQuery query = entityManager.createStoredProcedureQuery("sp_DeletePost");
        query.registerStoredProcedureParameter("PostID", Integer.class, jakarta.persistence.ParameterMode.IN);
        query.registerStoredProcedureParameter("UserID", Integer.class, jakarta.persistence.ParameterMode.IN);

        query.setParameter("PostID", request.getId());
        query.setParameter("UserID", request.getUserId());

        Object result = query.getSingleResult();
        int affectedRows = ((Number) result).intValue();

        if (affectedRows == 0) {
            throw new IllegalArgumentException("刪除失敗，可能不是發文者或文章不存在");
        }

        return request.getId();
    }
   @Transactional
    public void updateComment(Map<String, Object> request) {
        Object commentIdObj = request.get("commentId");
        Object userIdObj = request.get("userId");
        String content = (String) request.get("content");

        // ↓ 改成這樣，字串和數字都能處理
        Integer commentId = commentIdObj != null ? Integer.parseInt(commentIdObj.toString()) : null;
        Integer userId = userIdObj != null ? Integer.parseInt(userIdObj.toString()) : null;

        if (commentId == null || userId == null) {
            throw new IllegalArgumentException("commentId 和 userId 都必須提供");
        }
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("留言內容不能為空");
        }

        StoredProcedureQuery query = entityManager.createStoredProcedureQuery("sp_UpdateComment");
        query.registerStoredProcedureParameter("CommentID", Integer.class, jakarta.persistence.ParameterMode.IN);
        query.registerStoredProcedureParameter("UserID", Integer.class, jakarta.persistence.ParameterMode.IN);
        query.registerStoredProcedureParameter("Content", String.class, jakarta.persistence.ParameterMode.IN);

        query.setParameter("CommentID", commentId);
        query.setParameter("UserID", userId);
        query.setParameter("Content", content);

        Object result = query.getSingleResult();
        int affectedRows = ((Number) result).intValue();

        if (affectedRows == 0) {
            throw new IllegalArgumentException("編輯失敗，可能不是留言者或留言不存在");
        }
    }

    @Transactional
    public void deleteComment(Map<String, Object> request) {
        Object commentIdObj = request.get("commentId");
        Object userIdObj = request.get("userId");

        // ↓ 同上
        Integer commentId = commentIdObj != null ? Integer.parseInt(commentIdObj.toString()) : null;
        Integer userId = userIdObj != null ? Integer.parseInt(userIdObj.toString()) : null;

        if (commentId == null || userId == null) {
            throw new IllegalArgumentException("commentId 和 userId 都必須提供");
        }

        StoredProcedureQuery query = entityManager.createStoredProcedureQuery("sp_DeleteComment");
        query.registerStoredProcedureParameter("CommentID", Integer.class, jakarta.persistence.ParameterMode.IN);
        query.registerStoredProcedureParameter("UserID", Integer.class, jakarta.persistence.ParameterMode.IN);

        query.setParameter("CommentID", commentId);
        query.setParameter("UserID", userId);

        Object result = query.getSingleResult();
        int affectedRows = ((Number) result).intValue();

        if (affectedRows == 0) {
            throw new IllegalArgumentException("刪除失敗，可能不是留言者或留言不存在");
        }
    }

    private PostResponseDTO convertToDTO(PostEntity post) {
        PostResponseDTO dto = new PostResponseDTO();
        dto.setPostId(post.getPostId());
        dto.setContent(post.getContent());
        dto.setImage(post.getImage());
        dto.setCreatedAt(post.getCreatedAt());
        dto.setAuthorName(post.getUser().getUserName());
        dto.setAuthorId(post.getUserId());

        List<CommentDTO> commentDTOs = post.getComments().stream()
                .map(this::convertCommentToDTO)
                .collect(Collectors.toList());
        dto.setComments(commentDTOs);

        return dto;
    }

    private CommentDTO convertCommentToDTO(CommentEntity comment) {
        CommentDTO dto = new CommentDTO();
        dto.setCommentId(comment.getCommentId());
        dto.setContent(comment.getContent());
        dto.setCreatedAt(comment.getCreatedAt());
        dto.setUserId(comment.getUserId());
        dto.setUserName(comment.getUser().getUserName());
        return dto;
    }
}