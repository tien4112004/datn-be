package com.datn.datnbe.sharedkernel.idempotency.internal;

import com.datn.datnbe.sharedkernel.exceptions.AppException;
import com.datn.datnbe.sharedkernel.idempotency.api.AbstractIdempotencyService;
import com.datn.datnbe.sharedkernel.idempotency.api.IdempotencyKey;
import com.datn.datnbe.sharedkernel.idempotency.api.IdempotencyStatus;
import com.datn.datnbe.sharedkernel.idempotency.api.Idempotent;
import com.datn.datnbe.sharedkernel.idempotency.internal.DefaultIdempotencyService;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("IdempotencyAspect Tests")
class IdempotencyAspectTest {

    @Mock
    private ApplicationContext applicationContext;
    
    @Mock
    private IdempotencyRepository repository;
    
    @Mock
    private ObjectMapper objectMapper;
    
    @Mock
    private AbstractIdempotencyService idempotencyService;
    
    @Mock
    private ProceedingJoinPoint joinPoint;
    
    @Mock
    private Idempotent idempotent;
    
    @Mock
    private ServletRequestAttributes requestAttributes;
    
    @Mock
    private HttpServletRequest request;
    
    @Mock
    private MethodSignature methodSignature;
    
    @Mock
    private TypeFactory typeFactory;
    
    private IdempotencyAspect aspect;
    
    @BeforeEach
    void setUp() {
        aspect = new IdempotencyAspect(applicationContext, repository, objectMapper);
    }

    @Test
    @DisplayName("Should throw exception when idempotency key is missing")
    void enforceIdempotency_MissingIdempotencyKey_ThrowsException() throws Throwable {
        doReturn(DefaultIdempotencyService.class).when(idempotent).serviceType();
        doReturn(idempotencyService).when(applicationContext).getBean(any(Class.class));
        
        try (MockedStatic<RequestContextHolder> mockedStatic = mockStatic(RequestContextHolder.class)) {
            mockedStatic.when(RequestContextHolder::getRequestAttributes).thenReturn(requestAttributes);
            when(requestAttributes.getRequest()).thenReturn(request);
            when(request.getHeader("idempotency-key")).thenReturn(null);
            
            assertThatThrownBy(() -> aspect.enforceIdempotency(joinPoint, idempotent))
                    .isInstanceOf(AppException.class)
                    .hasMessage("Idempotency key is required in the 'idempotency-key' header.");
        }
    }

    @Test
    @DisplayName("Should throw exception when idempotency key is invalid")
    void enforceIdempotency_InvalidIdempotencyKey_ThrowsException() throws Throwable {
        String invalidKey = "";
        doReturn(DefaultIdempotencyService.class).when(idempotent).serviceType();
        doReturn(idempotencyService).when(applicationContext).getBean(any(Class.class));
        when(idempotencyService.isValid(invalidKey)).thenReturn(false);
        
        try (MockedStatic<RequestContextHolder> mockedStatic = mockStatic(RequestContextHolder.class)) {
            mockedStatic.when(RequestContextHolder::getRequestAttributes).thenReturn(requestAttributes);
            when(requestAttributes.getRequest()).thenReturn(request);
            when(request.getHeader("idempotency-key")).thenReturn(invalidKey);
            
            assertThatThrownBy(() -> aspect.enforceIdempotency(joinPoint, idempotent))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Idempotency key is invalid.");
        }
    }

    @Test
    @DisplayName("Should process new request and save result")
    void enforceIdempotency_NewRequest_ProcessesAndSaves() throws Throwable {
        String key = "test-key";
        String responseBody = "{\"data\":\"test\"}";
        ResponseEntity<String> response = ResponseEntity.ok(responseBody);
        IdempotencyKey newEntity = IdempotencyKey.builder()
                .key(key)
                .status(IdempotencyStatus.IN_PROGRESS)
                .retryCount(0)
                .build();
        
        setupMocks(key);
        when(repository.findById(key)).thenReturn(Optional.empty());
        when(idempotencyService.initialize(key)).thenReturn(newEntity);
        when(joinPoint.proceed()).thenReturn(response);
        when(objectMapper.writeValueAsString(responseBody)).thenReturn(responseBody);
        
        try (MockedStatic<RequestContextHolder> mockedStatic = mockStatic(RequestContextHolder.class)) {
            setupRequestMocks(key, mockedStatic);
            
            Object result = aspect.enforceIdempotency(joinPoint, idempotent);
            
            assertThat(result).isEqualTo(response);
            verify(repository, times(2)).save(newEntity);
            verify(idempotencyService).complete(newEntity, responseBody, 200);
        }
    }

    @Test
    @DisplayName("Should return cached response for completed request")
    void enforceIdempotency_CompletedRequest_ReturnsCachedResponse() throws Throwable {
        String key = "test-key";
        String cachedResponse = "{\"data\":\"cached\"}";
        IdempotencyKey completedEntity = IdempotencyKey.builder()
                .key(key)
                .status(IdempotencyStatus.COMPLETED)
                .responseData(cachedResponse)
                .statusCode(201)
                .build();
        
        setupMocks(key);
        when(repository.findById(key)).thenReturn(Optional.of(completedEntity));
        when(idempotencyService.isCompleted(completedEntity)).thenReturn(true);
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        
        Method mockMethod = TestController.class.getMethod("testMethod");
        when(methodSignature.getMethod()).thenReturn(mockMethod);
        when(objectMapper.getTypeFactory()).thenReturn(TypeFactory.defaultInstance());
        when(objectMapper.readValue(eq(cachedResponse), any(JavaType.class))).thenReturn("cached");

        try (MockedStatic<RequestContextHolder> mockedStatic = mockStatic(RequestContextHolder.class)) {
            setupRequestMocks(key, mockedStatic);
            
            Object result = aspect.enforceIdempotency(joinPoint, idempotent);
            
            assertThat(result).isInstanceOf(ResponseEntity.class);
            ResponseEntity<?> responseEntity = (ResponseEntity<?>) result;
            assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            verify(joinPoint, never()).proceed();
        }
    }

    @Test
    @DisplayName("Should retry failed request when retries are allowed")
    void enforceIdempotency_FailedRequestCanRetry_RetriesAndProcesses() throws Throwable {
        String key = "test-key";
        String responseBody = "{\"data\":\"retry\"}";
        ResponseEntity<String> response = ResponseEntity.ok(responseBody);
        IdempotencyKey failedEntity = IdempotencyKey.builder()
                .key(key)
                .status(IdempotencyStatus.FAILED)
                .retryCount(2)
                .build();
        
        setupMocks(key);
        when(repository.findById(key)).thenReturn(Optional.of(failedEntity));
        when(idempotencyService.isFailed(failedEntity)).thenReturn(true);
        when(idempotencyService.canRetry(failedEntity)).thenReturn(true);
        when(joinPoint.proceed()).thenReturn(response);
        when(objectMapper.writeValueAsString(responseBody)).thenReturn(responseBody);
        
        try (MockedStatic<RequestContextHolder> mockedStatic = mockStatic(RequestContextHolder.class)) {
            setupRequestMocks(key, mockedStatic);
            
            Object result = aspect.enforceIdempotency(joinPoint, idempotent);
            
            assertThat(result).isEqualTo(response);
            verify(idempotencyService).retry(failedEntity);
            verify(idempotencyService).complete(failedEntity, responseBody, 200);
            verify(repository, times(2)).save(failedEntity);
        }
    }

    @Test
    @DisplayName("Should handle in-progress request")
    void enforceIdempotency_InProgressRequest_CallsProcessing() throws Throwable {
        String key = "test-key";
        String responseBody = "{\"data\":\"processing\"}";
        ResponseEntity<String> response = ResponseEntity.ok(responseBody);
        IdempotencyKey inProgressEntity = IdempotencyKey.builder()
                .key(key)
                .status(IdempotencyStatus.IN_PROGRESS)
                .retryCount(0)
                .build();
        
        setupMocks(key);
        when(repository.findById(key)).thenReturn(Optional.of(inProgressEntity));
        when(idempotencyService.isInProgress(inProgressEntity)).thenReturn(true);
        when(joinPoint.proceed()).thenReturn(response);
        when(objectMapper.writeValueAsString(responseBody)).thenReturn(responseBody);
        
        try (MockedStatic<RequestContextHolder> mockedStatic = mockStatic(RequestContextHolder.class)) {
            setupRequestMocks(key, mockedStatic);
            
            Object result = aspect.enforceIdempotency(joinPoint, idempotent);
            
            assertThat(result).isEqualTo(response);
            verify(idempotencyService).processing(inProgressEntity);
            verify(idempotencyService).complete(inProgressEntity, responseBody, 200);
            verify(repository).save(inProgressEntity);
        }
    }

    @Test
    @DisplayName("Should mark as failed when exception occurs")
    void enforceIdempotency_ExceptionDuringProcessing_MarksAsFailed() throws Throwable {
        String key = "test-key";
        RuntimeException exception = new RuntimeException("Processing failed");
        IdempotencyKey entity = IdempotencyKey.builder()
                .key(key)
                .status(IdempotencyStatus.IN_PROGRESS)
                .retryCount(0)
                .build();
        
        setupMocks(key);
        when(repository.findById(key)).thenReturn(Optional.of(entity));
        when(idempotencyService.isInProgress(entity)).thenReturn(true);
        when(joinPoint.proceed()).thenThrow(exception);
        
        try (MockedStatic<RequestContextHolder> mockedStatic = mockStatic(RequestContextHolder.class)) {
            setupRequestMocks(key, mockedStatic);
            
            assertThatThrownBy(() -> aspect.enforceIdempotency(joinPoint, idempotent))
                    .isEqualTo(exception);
            
            verify(idempotencyService).fail(entity);
            verify(repository, atLeast(1)).save(entity);
        }
    }

    private void setupMocks(String key) {
        doReturn(DefaultIdempotencyService.class).when(idempotent).serviceType();
        doReturn(idempotencyService).when(applicationContext).getBean(any(Class.class));
        when(idempotencyService.isValid(key)).thenReturn(true);
    }

    private void setupRequestMocks(String key, MockedStatic<RequestContextHolder> mockedStatic) {
        mockedStatic.when(RequestContextHolder::getRequestAttributes).thenReturn(requestAttributes);
        when(requestAttributes.getRequest()).thenReturn(request);
        when(request.getHeader("idempotency-key")).thenReturn(key);
    }

    public static class TestController {
        public ResponseEntity<String> testMethod() {
            return ResponseEntity.ok("test");
        }
    }
}