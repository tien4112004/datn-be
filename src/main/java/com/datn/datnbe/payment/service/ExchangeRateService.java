package com.datn.datnbe.payment.service;

import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.datn.datnbe.payment.dto.response.ExchangeRateApiResponse;
import com.datn.datnbe.payment.entity.ExchangeRate;
import com.datn.datnbe.payment.repository.ExchangeRateRepository;

import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class ExchangeRateService {

    static final String API_URL_TEMPLATE = "https://v6.exchangerate-api.com/v6/bd0c249777d45b67ea84fe24/history/USD/%d/%d/%d";

    ExchangeRateRepository exchangeRateRepository;
    RestTemplate restTemplate;

    private void fetchAndSave(LocalDate date) {
        if (exchangeRateRepository.findByDate(date) != null)
            return;

        String url = String.format(API_URL_TEMPLATE, date.getYear(), date.getMonthValue(), date.getDayOfMonth());
        try {
            ExchangeRateApiResponse response = restTemplate.getForObject(url, ExchangeRateApiResponse.class);
            if (response == null || !"success".equals(response.getResult())) {
                log.warn("Exchange rate API unsuccessful for {}: {}", date, response);
                return;
            }
            Double vndRate = response.getConversionAmounts() != null
                    ? response.getConversionAmounts().get("VND")
                    : null;
            if (vndRate == null) {
                log.warn("VND rate not found for {}", date);
                return;
            }

            exchangeRateRepository.save(
                    ExchangeRate.builder().fromCurrency("USD").toCurrency("VND").rate(vndRate).date(date).build());
            log.info("Saved exchange rate for {}: 1 USD = {} VND", date, vndRate);
        } catch (Exception e) {
            log.error("Failed to fetch exchange rate for {}: {}", date, e.getMessage());
        }
    }

    /**
     * Runs at midnight on the last day of every month.
     * Fetches the USD → VND historical exchange rate for that day and persists it.
     */
    @Scheduled(cron = "0 0 0 L * *")
    public void saveMonthEndExchangeRate() {
        LocalDate today = LocalDate.now();
        LocalDate lastDayOfMonth = today.with(TemporalAdjusters.lastDayOfMonth());

        if (!today.equals(lastDayOfMonth)) {
            log.debug("Not last day of month, skipping exchange rate fetch");
            return;
        }

        log.info("Last day of month ({}), fetching USD/VND exchange rate...", today);
        fetchAndSave(today);
    }
}
