package com.datn.datnbe.ai.dto.request;

import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class UpdateModelStatusRequest {
    Boolean isEnable;
    Boolean isDefault;
}
