package com.example.backend.dto;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PasskeyResponse {
    private Long id;
    private String deviceName;
    private LocalDateTime createdAt;
    private LocalDateTime lastUsed;
    private Boolean active;
    
}
