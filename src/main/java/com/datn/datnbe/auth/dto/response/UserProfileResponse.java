package com.datn.datnbe.auth.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserProfileResponse {

    String id;

    String email;

    String firstName;

    String lastName;

    @JsonFormat(pattern = "yyyy-MM-dd")
    Date dateOfBirth;

    String phoneNumber;

    String avatarUrl;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    Date createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    Date updatedAt;

    String username;

    String role;
}
