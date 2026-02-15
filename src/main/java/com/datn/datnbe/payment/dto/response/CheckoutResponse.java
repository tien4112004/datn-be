package com.datn.datnbe.payment.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Response for Payment Gateway checkout form creation.
 * Contains the checkout URL and form fields for Sepay payment page.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CheckoutResponse {
    /**
     * Transaction ID for this payment (internal database ID).
     */
    private String transactionId;

    /**
     * Order invoice number (sent to Sepay).
     */
    private String orderInvoiceNumber;

    /**
     * Payment gateway used.
     */
    private String gate;

    /**
     * Checkout URL for POST form submission to Sepay.
     * Client should auto-submit a form to this URL.
     */
    private String checkoutUrl;

    /**
     * Form fields to submit to Sepay (includes signature).
     * These should be included as hidden inputs in the HTML form.
     */
    private Map<String, String> formFields;

    /**
     * Order reference code.
     */
    private String referenceCode;

    /**
     * Payment amount in VND.
     */
    private java.math.BigDecimal amount;

    /**
     * Transaction status.
     */
    private String status;
}
