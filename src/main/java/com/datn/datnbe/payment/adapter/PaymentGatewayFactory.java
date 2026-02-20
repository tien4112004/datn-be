package com.datn.datnbe.payment.adapter;

import java.util.List;

import org.springframework.stereotype.Component;

import com.datn.datnbe.sharedkernel.exceptions.AppException;
import com.datn.datnbe.sharedkernel.exceptions.ErrorCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentGatewayFactory {

    private final List<PaymentGatewayAdapter> adapters;

    public PaymentGatewayAdapter getAdapter(String gatewayName) {
        if (gatewayName == null || gatewayName.isBlank()) {
            log.warn("Gateway name is null or blank, defaulting to SEPAY");
            gatewayName = "SEPAY";
        }

        final String finalGatewayName = gatewayName;

        return adapters.stream().filter(adapter -> adapter.supports(finalGatewayName)).findFirst().orElseThrow(() -> {
            log.error("Unsupported payment gateway: {}", finalGatewayName);
            return new AppException(ErrorCode.VALIDATION_ERROR, "Unsupported payment gateway: " + finalGatewayName);
        });
    }

    public List<String> getAvailableGateways() {
        return adapters.stream().map(PaymentGatewayAdapter::getGatewayName).toList();
    }
}
