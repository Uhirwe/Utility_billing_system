package com.utilitybilling.enums;

/**
 * POSTPAID: monthly bill generation (water + transitioning electricity).
 * PREPAID: legacy electricity model — no monthly bills generated.
 */
public enum BillingMode {
    POSTPAID,
    PREPAID
}
