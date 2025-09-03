package com.datn.datnbe.sharedkernel.idempotency.api;

import com.datn.datnbe.sharedkernel.idempotency.internal.DefaultIdempotencyService;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Idempotent {
    Class<? extends AbstractIdempotencyService> serviceType() default DefaultIdempotencyService.class;
}