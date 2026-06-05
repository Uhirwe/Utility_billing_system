package com.utilitybilling.dto.email;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "Result of an email send attempt")
public class EmailDeliveryResult {
    @Schema(description = "True only if Gmail SMTP accepted the message", example = "true")
    private boolean sent;
    @Schema(description = "Delivery channel", example = "SMTP", allowableValues = {"SMTP", "FAILED", "CONSOLE", "CONSOLE_FALLBACK"})
    private String channel;
    @Schema(description = "Recipient inbox", example = "estherhope980@gmail.com")
    private String recipient;
    @Schema(description = "Human-readable delivery note", example = "Email delivered via Gmail SMTP.")
    private String detail;
}
