package com.nutriflow.exceptions;

import com.nutriflow.dto.response.ErrorResponse;
import lombok.extern.slf4j.Slf4j; // Loglama üçün əlavə edildi
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j // Loglama üçün əlavə edildi
public class GlobalExceptionHandler {

    // Reject səbəbi tapılmadıqda - 404 Not Found
    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(ResourceNotFoundException ex) {
        log.warn("Resurs tapılmadı: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    // Fayl saxlama xətası - 500 Internal Server Error
    @ExceptionHandler(FileStorageException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<ErrorResponse> handleFileStorageException(FileStorageException ex) {
        log.error("Fayl saxlama xətası: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
    }



    // Abunəlik tapılmadıqda - 404 Not Found
    @ExceptionHandler(SubscriptionNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<ErrorResponse> handleSubscriptionNotFoundException(SubscriptionNotFoundException ex) {
        log.warn("Abunəlik tapılmadı: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(org.springframework.security.core.AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(org.springframework.security.core.AuthenticationException ex) {
        return buildErrorResponse(HttpStatus.UNAUTHORIZED, "Giriş rədd edildi: " + ex.getMessage());
    }

    // Resurs (Dietoloq/Caterer) çatışmazlığı - 503 Service Unavailable
    // (Çünki bu daxili resurs problemidir)
    @ExceptionHandler(ResourceNotAvailableException.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public ResponseEntity<ErrorResponse> handleResourceNotAvailable(ResourceNotAvailableException ex) {
        log.error("Resurs çatışmazlığı xətası: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.SERVICE_UNAVAILABLE, ex.getMessage());
    }

    // Webhook xətaları - 400 Bad Request (Stripe-a yanlış müraciət olduğunu bildirir)
    @ExceptionHandler(WebhookProcessingException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorResponse> handleWebhookException(WebhookProcessingException ex) {
        log.error("Webhook emal xətası: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    // Menyu tapılmadıqda - 404 Not Found
    @ExceptionHandler(MenuNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<ErrorResponse> handleMenuNotFoundException(MenuNotFoundException ex) {
        log.warn("Menyu tapılmadı: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    // Sağlamlıq profili tapılmadıqda - 404 Not Found
    @ExceptionHandler(HealthProfileNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<ErrorResponse> handleHealthProfileNotFoundException(HealthProfileNotFoundException ex) {
        log.warn("Sağlamlıq profili tapılmadı: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    // Menyu statusu (məsələn APPROVED deyilse) uyğun olmadıqda - 400 Bad Request
    @ExceptionHandler(InvalidMenuStatusException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorResponse> handleInvalidMenuStatusException(InvalidMenuStatusException ex) {
        log.warn("Menyu statusu uyğun deyil: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    // Refresh Token və ya JWT xətaları - 401 Unauthorized
    @ExceptionHandler(InvalidTokenException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ResponseEntity<ErrorResponse> handleInvalidTokenException(InvalidTokenException ex) {
        log.warn("Təhlükəsizlik xətası (Unauthorized): {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    // Resurs artıq mövcud olduqda - 409 Conflict
    @ExceptionHandler(ResourceAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ResponseEntity<ErrorResponse> handleResourceAlreadyExists(ResourceAlreadyExistsException ex) {
        log.warn("Resurs artıq mövcuddur xətası: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.CONFLICT, ex.getMessage());
    }

    // Id xətası: Id tapılmadıqda
    @ExceptionHandler(IdNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<ErrorResponse> handleIdNotFound(IdNotFoundException ex) {
        log.warn("ID tapılmadı xətası: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    // Biznes Xətası: Email artıq mövcud olduqda (409 Conflict)
    @ExceptionHandler(EmailAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ResponseEntity<ErrorResponse> handleEmailAlreadyExists(EmailAlreadyExistsException ex) {
        log.warn("Email artıq mövcuddur: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.CONFLICT, ex.getMessage());
    }

    // Tapılmama Xətası: İstifadəçi tapılmadıqda (404 Not Found)
    @ExceptionHandler(UserNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<ErrorResponse> handleUserNotFound(UserNotFoundException ex) {
        log.warn("İstifadəçi tapılmadı: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    // OTP Xətası: Kod yanlış və ya vaxtı bitmiş olduqda (400 Bad Request)
    @ExceptionHandler(InvalidOtpException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorResponse> handleInvalidOtp(InvalidOtpException ex) {
        log.warn("Yanlış və ya müddəti bitmiş OTP cəhdi: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    // Ümumi Biznes Xətası
    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException ex) {
        log.warn("Biznes qaydası pozuntusu: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    // Fayl Yükləmə Xətası
    @ExceptionHandler(FileUploadException.class)
    @ResponseStatus(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
    public ResponseEntity<ErrorResponse> handleFileUploadException(FileUploadException ex) {
        log.error("Fayl yükləmə xətası: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.UNSUPPORTED_MEDIA_TYPE, ex.getMessage());
    }

    // Validation Xətaları: @Valid-dən keçməyən sahələr
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        log.warn("Validasiya xətası baş verdi. Sahə sayı: {}", errors.size());
        log.debug("Validasiya detalları: {}", errors);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .httpStatus(HttpStatus.BAD_REQUEST.value())
                .message("Validation failed")
                .errors(errors)
                .build();
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    // Yanlış JSON formatı və ya Enum dəyəri
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        log.warn("Oxunula bilməyən HTTP mesajı (JSON formatı səhv ola bilər): {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "Malformed JSON request or invalid field value");
    }

    // Tip uyğunsuzluğu
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        log.warn("Parametr tip uyğunsuzluğu: {} - Gələn dəyər: {}", ex.getName(), ex.getValue());
        String message = String.format("The parameter '%s' of value '%s' could not be converted to type '%s'",
                ex.getName(), ex.getValue(),
                ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown");
        return buildErrorResponse(HttpStatus.BAD_REQUEST, message);
    }

    // Global Xəta: Gözlənilməz digər bütün xətalar
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<ErrorResponse> handleGeneralException(Exception ex) {
        // Bu hissə çox önəmlidir: Bütün stack trace-i loglayırıq ki, xətanın kökünü tapa bilək
        log.error("GÖZLƏNİLMƏZ SİSTEM XƏTASI: ", ex);
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred: " + ex.getMessage());
    }

    // Yardımçı metod
    private ResponseEntity<ErrorResponse> buildErrorResponse(HttpStatus status, String message) {
        // Hər bir error response qurulanda bunu trace səviyyəsində loglayırıq
        log.trace("ErrorResponse yaradıldı: Status={}, Message={}", status, message);
        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .httpStatus(status.value())
                .message(message)
                .build();
        return new ResponseEntity<>(error, status);
    }
}