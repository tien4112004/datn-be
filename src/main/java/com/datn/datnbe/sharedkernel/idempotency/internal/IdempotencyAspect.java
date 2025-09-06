package com.datn.datnbe.sharedkernel.idempotency.internal;

import com.datn.datnbe.sharedkernel.exceptions.AppException;
import com.datn.datnbe.sharedkernel.exceptions.ErrorCode;
import com.datn.datnbe.sharedkernel.idempotency.api.IdempotencyKey;
import com.datn.datnbe.sharedkernel.idempotency.api.AbstractIdempotencyService;
import com.datn.datnbe.sharedkernel.idempotency.api.Idempotent;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.context.ApplicationContext;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class IdempotencyAspect {
    private final ApplicationContext applicationContext;
    private final IdempotencyRepository repository;
    private final ObjectMapper objectMapper;

    @Pointcut("@annotation(com.datn.datnbe.sharedkernel.idempotency.api.Idempotent) && @within(org.springframework.web.bind.annotation.RestController)")
    public void idempotentMethods() {
    }

    @Transactional
    @Around("idempotentMethods() && @annotation(idempotent)")
    public Object enforceIdempotency(ProceedingJoinPoint joinPoint, Idempotent idempotent) throws Throwable {
        AbstractIdempotencyService service = getIdempotencyService(idempotent);
        String key = getKeyFromRequest();

        if (key == null) {
            throw new AppException(ErrorCode.IDEMPOTENCY_KEY_MISSING,
                    "Idempotency key is required in the 'idempotency-key' header.");
        }

        if (!service.isValid(key)) {
            service.invalidate(key);
        }

        var existingEntity = repository.findById(key).orElse(null);

        log.info("Idempotency key: {}", key);
        try {
            if (existingEntity == null) {
                existingEntity = service.initialize(key);
                repository.save(existingEntity);
            } else if (service.isInProgress(existingEntity)) {
                service.processing(existingEntity);
            } else if (service.isFailed(existingEntity) && service.canRetry(existingEntity)) {
                service.retry(existingEntity);
                repository.save(existingEntity);
            } else if (service.isCompleted(existingEntity)) {
                return createCachedResponse(existingEntity, joinPoint);
            }

            var result = joinPoint.proceed();
            if (result instanceof ResponseEntity<?> responseEntity) {
                String resultJson = objectMapper.writeValueAsString(responseEntity.getBody());
                service.complete(existingEntity, resultJson, responseEntity.getStatusCode().value());
            }
            repository.save(existingEntity);
            return result;
        } catch (Exception e) {
            log.error("Error processing idempotent request with key {}: {}", key, e.getMessage());
            if (existingEntity != null) {
                service.fail(existingEntity);
                repository.save(existingEntity);
            }
            throw e;
        }
    }

    private Object createCachedResponse(IdempotencyKey entity, ProceedingJoinPoint joinPoint) throws Exception {
        log.info("Returning cached response for idempotency key: {}", entity.getKey());
        String responseData = entity.getResponseData();
        Integer statusCode = entity.getStatusCode();

        //  https://www.baeldung.com/java-deserialize-generic-type-with-jackson
        // Extract generic type from ResponseEntity<T>
        Type returnType = ((org.aspectj.lang.reflect.MethodSignature) joinPoint.getSignature()).getMethod()
                .getGenericReturnType();
        JavaType responseEntityType;
        if (returnType instanceof ParameterizedType parameterizedType) {
            responseEntityType = objectMapper.getTypeFactory()
                    .constructType(parameterizedType.getActualTypeArguments()[0]);
        } else
            responseEntityType = objectMapper.getTypeFactory().constructType(Object.class);

        Object body = objectMapper.readValue(responseData, responseEntityType);
        return ResponseEntity.status(statusCode != null ? statusCode : 200).body(body);
    }

    private String getKeyFromRequest() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs != null) {
            HttpServletRequest request = attrs.getRequest();
            return request.getHeader("idempotency-key");
        }
        return null;
    }

    private AbstractIdempotencyService getIdempotencyService(Idempotent idempotent) {
        return applicationContext.getBean(idempotent.serviceType());
    }
}
