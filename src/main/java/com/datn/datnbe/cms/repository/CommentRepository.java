package com.datn.datnbe.cms.repository;

import com.datn.datnbe.cms.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, String> {

    @Query("SELECT c FROM Comment c WHERE (:postId IS NULL OR c.postId = :postId)")
    List<Comment> findByPostId(String postId);
}
