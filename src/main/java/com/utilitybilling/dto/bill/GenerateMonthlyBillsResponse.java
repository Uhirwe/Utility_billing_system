package com.utilitybilling.dto.bill;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "Batch monthly bill generation summary")
public class GenerateMonthlyBillsResponse {
    private int billsGenerated;
    private int emailsSent;
}
