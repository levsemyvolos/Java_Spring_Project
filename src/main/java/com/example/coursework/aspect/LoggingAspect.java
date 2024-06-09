package com.example.coursework.aspect;

import com.example.coursework.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.*;

@Aspect
@Component
public class LoggingAspect {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final UserService userService;

    @Autowired
    public LoggingAspect(UserService userService) {
        this.userService = userService;
    }

    @Around("@within(com.example.coursework.annotations.Loggable) || @annotation(com.example.coursework.annotations.Loggable)")
    public Object log(ProceedingJoinPoint joinPoint) throws Throwable {
        String layer = getLayer(joinPoint);
        String methodName = joinPoint.getSignature().toShortString();

        // Отримуємо параметри HTTP-запиту
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        Map<String, String> requestParams = getRequestParams(request);

        // Логуємо інформацію перед викликом методу
//        logger.info("{} - Before invocation: {}.{} with parameters: {}, request params: {}, userId: {}, userRole: {}",
//                layer, joinPoint.getSignature().getDeclaringTypeName(), methodName, Arrays.toString(joinPoint.getArgs()),
//                requestParams, userService.getCurrentUser().getId(), userService.getCurrentUser().getRoles());

        // Якщо є JSON-тіло, логуємо його
        if (request.getContentType() != null && request.getContentType().contains("application/json")) {
            try {
                String jsonBody = objectMapper.writeValueAsString(joinPoint.getArgs()[0]);
                logger.debug("{} - JSON body: {}", layer, jsonBody);
            } catch (Exception e) {
                logger.warn("{} - Failed to log JSON body: {}", layer, e.getMessage());
            }
        }

        Object result;
        try {
            result = joinPoint.proceed(); // Викликаємо цільовий метод
            logger.info("{} - After invocation: {}.{} with result: {}", layer, joinPoint.getSignature().getDeclaringTypeName(), methodName, result);
        } catch (Throwable throwable) {
            logger.error("{} - Exception in: {}.{} with message: {}", layer, joinPoint.getSignature().getDeclaringTypeName(), methodName, throwable.getMessage());
            throw throwable;
        }
        return result;
    }

    private String getLayer(JoinPoint joinPoint) {
        if (joinPoint.getSignature().getDeclaringTypeName().contains("rest")) {
            return "Controller";
        } else if (joinPoint.getSignature().getDeclaringTypeName().contains("service")) {
            return "Service";
        } else if (joinPoint.getSignature().getDeclaringTypeName().contains("repository")) {
            return "Repository";
        } else {
            return "Unknown";
        }
    }

    // Допоміжний метод для отримання параметрів HTTP-запиту
    private Map<String, String> getRequestParams(HttpServletRequest request) {
        Map<String, String> params = new HashMap<>();
        Enumeration<String> parameterNames = request.getParameterNames();
        while (parameterNames.hasMoreElements()) {
            String paramName = parameterNames.nextElement();
            params.put(paramName, request.getParameter(paramName));
        }
        return params;
    }

}