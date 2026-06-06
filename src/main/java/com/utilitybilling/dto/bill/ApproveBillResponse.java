package com.utilitybilling.dto.bill;

import com.utilitybilling.dto.email.EmailDeliveryResult;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "Bill approval result including email delivery to the customer")
public class ApproveBillResponse {
    private BillResponse bill;
    private EmailDeliveryResult emailDelivery;
}
