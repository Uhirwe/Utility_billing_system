package com.utilitybilling.dto.user;

import com.utilitybilling.dto.email.EmailDeliveryResult;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "Staff user created by admin with email delivery status")
public class CreateUserResponse {
    private UserResponse user;
    private EmailDeliveryResult emailDelivery;
}
