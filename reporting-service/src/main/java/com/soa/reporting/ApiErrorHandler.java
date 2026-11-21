package com.soa.reporting;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
@RestControllerAdvice public class ApiErrorHandler {
 @ExceptionHandler(ApiException.class) public ResponseEntity<?> handle(ApiException e){return ResponseEntity.status(e.status).body(Map.of("error",e.getMessage()));}
}
