package com.datn.datnbe.sharedkernel.notification.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UnreadCountDto {
    private long count;
}
