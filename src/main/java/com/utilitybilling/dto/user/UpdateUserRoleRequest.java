package com.utilitybilling.dto.user;

import com.utilitybilling.enums.RoleName;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateUserRoleRequest {
    @NotNull(message = "Role is required")
    private RoleName role;
}
