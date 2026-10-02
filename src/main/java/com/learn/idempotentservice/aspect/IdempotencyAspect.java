package com.learn.idempotentservice.aspect;

import com.learn.idempotentservice.annotations.Idempotent;
import com.learn.idempotentservice.dto.IdempotentResponse;
import com.learn.idempotentservice.service.IdempotencyStorageService;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import tools.jackson.databind.ObjectMapper;

import java.lang.reflect.Method;

@Aspect
@Component
public class IdempotencyAspect {

    private final IdempotencyStorageService storageService;
    private final ObjectMapper objectMapper;

    public IdempotencyAspect(IdempotencyStorageService storageService, ObjectMapper objectMapper) {
        this.storageService = storageService;
        this.objectMapper = objectMapper;
    }

    @Around("@annotation(com.learn.idempotentservice.annotations.Idempotent)")
    public Object handleIdempotency(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Method method = methodSignature.getMethod();
        Idempotent idempotent = method.getAnnotation(Idempotent.class);
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        if(requestAttributes == null) {
            return joinPoint.proceed();
        }

        HttpServletRequest request = requestAttributes.getRequest();
        String idempotencyKey = request.getHeader(idempotent.headerName());

        if (!StringUtils.hasText(idempotencyKey)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                 .body("Missing required header: " + idempotent.headerName());
        }

        boolean lockAcquired = storageService.claimLock(idempotencyKey, idempotent.ttl(), idempotent.timeUnit());

        if(!lockAcquired) {
            IdempotentResponse existingRecord = storageService.getRecord(idempotencyKey);

            if(existingRecord != null) {
                if("IN_PROGRESS".equals(existingRecord.getStatus())) {
                    return ResponseEntity.status(HttpStatus.CONFLICT)
                                         .body("A request with this key is currently being processed.");
                }
                Object bodyObject = existingRecord.getBody();
                return ResponseEntity.status(existingRecord.getStatusCode()).body(bodyObject);
            }

            return ResponseEntity.status(HttpStatus.CONFLICT).body("Duplicate execution attempt.");
        }

        Object result;
        try {
            // execute the method
            result = joinPoint.proceed();
        }
        catch (Exception ex) {
            // Revert state on unhandled exception to permit retry
            storageService.deleteKey(idempotencyKey);
            throw ex;
        }

        // Processing successful, persist complete execution state
        int statusCode = HttpStatus.OK.value();
        String serializedBody = "";

        if (result instanceof ResponseEntity<?> responseEntity) {
            statusCode = responseEntity.getStatusCode().value();
            serializedBody = objectMapper.writeValueAsString(responseEntity.getBody());
        } else {
            serializedBody = objectMapper.writeValueAsString(result);
        }

        storageService.saveResponse(idempotencyKey, statusCode, serializedBody, idempotent.ttl(), idempotent.timeUnit());

        return result;
    }

}
