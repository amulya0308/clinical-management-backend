package com.clinic.dto.response;

import com.clinic.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSummaryResponse {
    private Long id;
    private String email;
    private Role role;
    private boolean enabled;
    private String fullName;
    private Long profileId;
    private LocalDateTime createdAt;
}
