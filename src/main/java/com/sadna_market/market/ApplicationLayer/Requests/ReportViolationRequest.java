package com.sadna_market.market.ApplicationLayer.Requests;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class ReportViolationRequest {
    private UUID messageId;
    private String reporterUsername;
    private String reason;
}
