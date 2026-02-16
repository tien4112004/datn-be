/**
 * Payment Adapter Package
 *
 * This package contains the adapter pattern implementation for payment
 * gateways.
 * Each payment gateway (SePay, PayOS, etc.) implements the
 * PaymentGatewayAdapter
 * interface to provide a consistent API for payment operations.
 *
 * The PaymentGatewayFactory manages adapter selection and provides the
 * appropriate
 * adapter based on the gateway name specified by the client.
 */
package com.datn.datnbe.payment.adapter;
