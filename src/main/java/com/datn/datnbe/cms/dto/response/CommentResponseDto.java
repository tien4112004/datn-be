package com.datn.datnbe.cms.dto.response;

import com.datn.datnbe.auth.dto.response.UserMinimalInfoDto;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CommentResponseDto {
    private String id;
    private String postId;
    private String userId;
    private UserMinimalInfoDto user;
    private String content;
    private Date createdAt;
    private Date updatedAt;
}
