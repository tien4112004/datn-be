package com.datn.datnbe.ai.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class UpdateModelStatusRequest {
    @JsonAlias({"enable", "isEnable", "isEnabled"})
    Boolean isEnabled;
    @JsonAlias({"default", "isDefault"})
    Boolean isDefault;
}
