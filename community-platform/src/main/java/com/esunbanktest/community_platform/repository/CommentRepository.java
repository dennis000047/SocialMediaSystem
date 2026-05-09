package com.esunbanktest.community_platform.repository;

import com.esunbanktest.community_platform.entity.CommentEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CommentRepository extends JpaRepository<CommentEntity, Integer> {

    @EntityGraph(attributePaths = {"user"})
    @Query("SELECT c FROM CommentEntity c WHERE c.commentId = :id")
    Optional<CommentEntity> findByIdWithUser(Integer id);
}