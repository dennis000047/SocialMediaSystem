package com.esunbanktest.community_platform.controller;

import com.esunbanktest.community_platform.dto.AddCommentRequestDTO;
import com.esunbanktest.community_platform.dto.AddPostRequestDTO;
import com.esunbanktest.community_platform.dto.CommentDTO;
import com.esunbanktest.community_platform.dto.DeletePostRequestDTO;
import com.esunbanktest.community_platform.dto.EditPostRequestDTO;
import com.esunbanktest.community_platform.dto.PostResponseDTO;
import com.esunbanktest.community_platform.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:5173")
public class PostController {

    @Autowired
    private PostService postService;

    @GetMapping("/getpostinfo")
    public List<PostResponseDTO> getPostInfo() {
        return postService.getAllPostsWithDetails();
    }

    @PostMapping("/addPost")
    public ResponseEntity<Map<String, Object>> addPost(@RequestBody AddPostRequestDTO request) {
        try {
            PostResponseDTO createdPost = postService.createPost(request);
            Map<String, Object> body = new HashMap<>();
            body.put("success", true);
            body.put("message", "發文成功");
            body.put("data", createdPost);
            return ResponseEntity.ok(body);
        } catch (Exception ex) {
            Map<String, Object> errorBody = new HashMap<>();
            errorBody.put("success", false);
            errorBody.put("message", ex.getMessage() != null ? ex.getMessage() : "新增發文失敗");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorBody);
        }
    }

    @PostMapping("/editPost")
    public ResponseEntity<Map<String, Object>> editPost(@RequestBody EditPostRequestDTO request) {
        try {
            PostResponseDTO updatedPost = postService.updatePost(request);
            Map<String, Object> body = new HashMap<>();
            body.put("success", true);
            body.put("message", "編輯成功");
            body.put("data", updatedPost);
            return ResponseEntity.ok(body);
        } catch (IllegalArgumentException ex) {
            Map<String, Object> errorBody = new HashMap<>();
            errorBody.put("success", false);
            errorBody.put("message", ex.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorBody);
        } catch (Exception ex) {
            Map<String, Object> errorBody = new HashMap<>();
            errorBody.put("success", false);
            errorBody.put("message", ex.getMessage() != null ? ex.getMessage() : "編輯發文失敗");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorBody);
        }
    }

    @PostMapping("/deletemsg")
    public ResponseEntity<Map<String, Object>> deletePost(@RequestBody DeletePostRequestDTO request) {
        try {
            Integer deletedId = postService.deletePost(request);
            Map<String, Object> body = new HashMap<>();
            body.put("success", true);
            body.put("message", "刪除成功");
            body.put("data", Map.of("deletedPostId", deletedId));
            return ResponseEntity.ok(body);
        } catch (IllegalArgumentException ex) {
            Map<String, Object> errorBody = new HashMap<>();
            errorBody.put("success", false);
            errorBody.put("message", ex.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorBody);
        } catch (Exception ex) {
            Map<String, Object> errorBody = new HashMap<>();
            errorBody.put("success", false);
            errorBody.put("message", ex.getMessage() != null ? ex.getMessage() : "刪除發文失敗");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorBody);
        }
    }

    @PostMapping("/commentmsg")
    public ResponseEntity<Map<String, Object>> addComment(@RequestBody AddCommentRequestDTO request) {
        try {
            CommentDTO comment = postService.addComment(request);
            Map<String, Object> body = new HashMap<>();
            body.put("success", true);
            body.put("message", "留言成功");
            body.put("comment", Map.of(
                    "id", comment.getCommentId(),
                    "author", comment.getUserName(),
                    "content", comment.getContent(),
                    "timestamp", comment.getCreatedAt(),
                    "userId", comment.getUserId()
            ));
            return ResponseEntity.ok(body);
        } catch (IllegalArgumentException ex) {
            Map<String, Object> errorBody = new HashMap<>();
            errorBody.put("success", false);
            errorBody.put("message", ex.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorBody);
        } catch (Exception ex) {
            Map<String, Object> errorBody = new HashMap<>();
            errorBody.put("success", false);
            errorBody.put("message", ex.getMessage() != null ? ex.getMessage() : "留言失敗");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorBody);
        }
    }
    @PostMapping("/editComment")
    public ResponseEntity<Map<String, Object>> editComment(@RequestBody Map<String, Object> request) {
        System.out.println("editComment request: " + request);
        try {
            System.out.println("calling postService.updateComment...");
            postService.updateComment(request);
            System.out.println("postService.updateComment done"); 
            Map<String, Object> body = new HashMap<>();
            body.put("success", true);
            body.put("message", "留言編輯成功");
            return ResponseEntity.ok(body);
        } catch (IllegalArgumentException ex) {
            System.out.println("IllegalArgumentException: " + ex.getMessage());
            Map<String, Object> errorBody = new HashMap<>();
            errorBody.put("success", false);
            errorBody.put("message", ex.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorBody);
        } catch (Exception ex) {
            System.out.println("Exception: " + ex.getMessage());                // ← 加這行
            ex.printStackTrace();  
            Map<String, Object> errorBody = new HashMap<>();
            errorBody.put("success", false);
            errorBody.put("message", ex.getMessage() != null ? ex.getMessage() : "留言編輯失敗");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorBody);
        }
    }

    @PostMapping("/deleteComment")
    public ResponseEntity<Map<String, Object>> deleteComment(@RequestBody Map<String, Object> request) {
        System.out.println("deleteComment request: " + request);
        try {
            postService.deleteComment(request);
            Map<String, Object> body = new HashMap<>();
            body.put("success", true);
            body.put("message", "留言刪除成功");
            return ResponseEntity.ok(body);
        } catch (IllegalArgumentException ex) {
            Map<String, Object> errorBody = new HashMap<>();
            errorBody.put("success", false);
            errorBody.put("message", ex.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorBody);
        } catch (Exception ex) {
            Map<String, Object> errorBody = new HashMap<>();
            errorBody.put("success", false);
            errorBody.put("message", ex.getMessage() != null ? ex.getMessage() : "留言刪除失敗");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorBody);
        }
    }
}

