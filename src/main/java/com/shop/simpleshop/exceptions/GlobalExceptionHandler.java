package com.shop.simpleshop.exceptions;

import com.shop.simpleshop.dto.ErrorResponseDTO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

/**
 * Global exception handler for all REST API endpoints.
 * Provides centralized exception handling and standardized error responses.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles ProductNotFoundException (404 Not Found)
     */
    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleProductNotFound(
            ProductNotFoundException ex, HttpServletRequest request) {

        ErrorResponseDTO errorResponse = ErrorResponseDTO.builder()
                .errorCode("PRODUCT_NOT_FOUND")
                .message(ex.getMessage())
                .details("The requested product does not exist in the system")
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(errorResponse);
    }

    /**
     * Handles InsufficientStockException (400 Bad Request)
     */
    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<ErrorResponseDTO> handleInsufficientStock(
            InsufficientStockException ex, HttpServletRequest request) {

        ErrorResponseDTO errorResponse = ErrorResponseDTO.builder()
                .errorCode("INSUFFICIENT_STOCK")
                .message(ex.getMessage())
                .details("The requested quantity exceeds available stock")
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorResponse);
    }

    /**
     * Handles InvalidInventoryTransactionException (400 Bad Request)
     */
    @ExceptionHandler(InvalidInventoryTransactionException.class)
    public ResponseEntity<ErrorResponseDTO> handleInvalidInventoryTransaction(
            InvalidInventoryTransactionException ex, HttpServletRequest request) {

        ErrorResponseDTO errorResponse = ErrorResponseDTO.builder()
                .errorCode("INVALID_TRANSACTION")
                .message(ex.getMessage())
                .details("The inventory transaction parameters are invalid")
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorResponse);
    }

    /**
     * Handles DuplicateProductException (409 Conflict)
     */
    @ExceptionHandler(DuplicateProductException.class)
    public ResponseEntity<ErrorResponseDTO> handleDuplicateProduct(
            DuplicateProductException ex, HttpServletRequest request) {

        ErrorResponseDTO errorResponse = ErrorResponseDTO.builder()
                .errorCode("DUPLICATE_PRODUCT")
                .message(ex.getMessage())
                .details("A product with this SKU or name already exists")
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(errorResponse);
    }

    /**
     * Handles ProductDeleteConflictException (409 Conflict)
     */
    @ExceptionHandler(ProductDeleteConflictException.class)
    public ResponseEntity<ErrorResponseDTO> handleProductDeleteConflict(
            ProductDeleteConflictException ex, HttpServletRequest request) {

        ErrorResponseDTO errorResponse = ErrorResponseDTO.builder()
                .errorCode("PRODUCT_DELETE_CONFLICT")
                .message(ex.getMessage())
                .details("The product is referenced by sales or inventory transactions and cannot be deleted")
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(errorResponse);
    }

    /**
     * Handles InvalidInputException (400 Bad Request)
     */
    @ExceptionHandler(InvalidInputException.class)
    public ResponseEntity<ErrorResponseDTO> handleInvalidInput(
            InvalidInputException ex, HttpServletRequest request) {

        ErrorResponseDTO errorResponse = ErrorResponseDTO.builder()
                .errorCode("INVALID_INPUT")
                .message(ex.getMessage())
                .details("The provided input data is invalid or incomplete")
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorResponse);
    }

    /**
     * Handles ExpenseNotFoundException (404 Not Found)
     */
    @ExceptionHandler(ExpenseNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleExpenseNotFound(
            ExpenseNotFoundException ex, HttpServletRequest request) {

        ErrorResponseDTO errorResponse = ErrorResponseDTO.builder()
                .errorCode("EXPENSE_NOT_FOUND")
                .message(ex.getMessage())
                .details("The requested expense does not exist in the system")
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(errorResponse);
    }

    /**
     * Handles SaleNotFoundException (404 Not Found)
     */
    @ExceptionHandler(SaleNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleSaleNotFound(
            SaleNotFoundException ex, HttpServletRequest request) {

        ErrorResponseDTO errorResponse = ErrorResponseDTO.builder()
                .errorCode("SALE_NOT_FOUND")
                .message(ex.getMessage())
                .details("The requested sale does not exist in the system")
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(errorResponse);
    }

    /**
     * Handles SaleItemNotFoundException (404 Not Found)
     */
    @ExceptionHandler(SaleItemNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleSaleItemNotFound(
            SaleItemNotFoundException ex, HttpServletRequest request) {

        ErrorResponseDTO errorResponse = ErrorResponseDTO.builder()
                .errorCode("SALE_ITEM_NOT_FOUND")
                .message(ex.getMessage())
                .details("The requested sale item does not exist in the system")
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(errorResponse);
    }

    /**
     * Handles InvalidSaleItemException (400 Bad Request)
     */
    @ExceptionHandler(InvalidSaleItemException.class)
    public ResponseEntity<ErrorResponseDTO> handleInvalidSaleItem(
            InvalidSaleItemException ex, HttpServletRequest request) {

        ErrorResponseDTO errorResponse = ErrorResponseDTO.builder()
                .errorCode("INVALID_SALE_ITEM")
                .message(ex.getMessage())
                .details("The sale item contains invalid data")
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorResponse);
    }

    /**
     * Handles SaleAlreadyCompletedException (400 Bad Request)
     */
    @ExceptionHandler(SaleAlreadyCompletedException.class)
    public ResponseEntity<ErrorResponseDTO> handleSaleAlreadyCompleted(
            SaleAlreadyCompletedException ex, HttpServletRequest request) {

        ErrorResponseDTO errorResponse = ErrorResponseDTO.builder()
                .errorCode("SALE_ALREADY_COMPLETED")
                .message(ex.getMessage())
                .details("The sale is already completed and cannot be modified")
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorResponse);
    }

    /**
     * Handles SaleCancellationException (400 Bad Request)
     */
    @ExceptionHandler(SaleCancellationException.class)
    public ResponseEntity<ErrorResponseDTO> handleSaleCancellation(
            SaleCancellationException ex, HttpServletRequest request) {

        ErrorResponseDTO errorResponse = ErrorResponseDTO.builder()
                .errorCode("SALE_CANCELLATION_FAILED")
                .message(ex.getMessage())
                .details("The sale cannot be cancelled due to business rule violations")
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorResponse);
    }

    /**
     * Handles SaleRefundException (400 Bad Request)
     */
    @ExceptionHandler(SaleRefundException.class)
    public ResponseEntity<ErrorResponseDTO> handleSaleRefund(
            SaleRefundException ex, HttpServletRequest request) {

        ErrorResponseDTO errorResponse = ErrorResponseDTO.builder()
                .errorCode("SALE_REFUND_FAILED")
                .message(ex.getMessage())
                .details("The refund cannot be processed due to business rule violations")
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorResponse);
    }

    /**
     * Handles InvalidQueryParameterException (400 Bad Request)
     */
    @ExceptionHandler(InvalidQueryParameterException.class)
    public ResponseEntity<ErrorResponseDTO> handleInvalidQueryParameter(
            InvalidQueryParameterException ex, HttpServletRequest request) {

        ErrorResponseDTO errorResponse = ErrorResponseDTO.builder()
                .errorCode("INVALID_QUERY_PARAMETER")
                .message(ex.getMessage())
                .details("Expected: " + ex.getExpectedFormat() + ", Provided: '" + ex.getProvidedValue() + "'")
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorResponse);
    }

    /**
     * Handles validation errors from @Valid annotation (400 Bad Request)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDTO> handleValidationException(
            MethodArgumentNotValidException ex, HttpServletRequest request) {

        String details = ex.getBindingResult().getAllErrors().stream()
                .map(error -> {
                    if (error instanceof org.springframework.validation.FieldError fieldError) {
                        return fieldError.getField() + ": " + error.getDefaultMessage();
                    }
                    return error.getObjectName() + ": " + error.getDefaultMessage();
                })
                .collect(Collectors.joining(", "));

        ErrorResponseDTO errorResponse = ErrorResponseDTO.builder()
                .errorCode("VALIDATION_ERROR")
                .message("Input validation failed")
                .details(details)
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorResponse);
    }

    /**
     * Handles InvalidCredentialsException (401 Unauthorized)
     */
    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponseDTO> handleInvalidCredentials(
            InvalidCredentialsException ex, HttpServletRequest request) {

        ErrorResponseDTO errorResponse = ErrorResponseDTO.builder()
                .errorCode("INVALID_CREDENTIALS")
                .message(ex.getMessage())
                .details("The provided username/email or password is incorrect")
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(errorResponse);
    }

    /**
     * Handles DuplicateUserException (409 Conflict)
     */
    @ExceptionHandler(DuplicateUserException.class)
    public ResponseEntity<ErrorResponseDTO> handleDuplicateUser(
            DuplicateUserException ex, HttpServletRequest request) {

        ErrorResponseDTO errorResponse = ErrorResponseDTO.builder()
                .errorCode("DUPLICATE_USER")
                .message(ex.getMessage())
                .details("A user with this username or email already exists")
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(errorResponse);
    }

    /**
     * Handles AccessDeniedException (403 Forbidden), raised by method-security
     * when {@code @PreAuthorize} rejects a request. Also catches
     * {@code AuthorizationDeniedException} (Spring Security 7) which extends it.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponseDTO> handleAccessDenied(
            AccessDeniedException ex, HttpServletRequest request) {

        ErrorResponseDTO errorResponse = ErrorResponseDTO.builder()
                .errorCode("ACCESS_DENIED")
                .message("You do not have permission to perform this action")
                .details(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(errorResponse);
    }

    /**
     * Handles InsufficientRoleException (403 Forbidden).
     */
    @ExceptionHandler(InsufficientRoleException.class)
    public ResponseEntity<ErrorResponseDTO> handleInsufficientRole(
            InsufficientRoleException ex, HttpServletRequest request) {

        ErrorResponseDTO errorResponse = ErrorResponseDTO.builder()
                .errorCode("INSUFFICIENT_ROLE")
                .message(ex.getMessage())
                .details("This action requires a manager or admin role")
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(errorResponse);
    }

    /**
     * Handles ForbiddenActionException (403 Forbidden).
     */
    @ExceptionHandler(ForbiddenActionException.class)
    public ResponseEntity<ErrorResponseDTO> handleForbiddenAction(
            ForbiddenActionException ex, HttpServletRequest request) {

        ErrorResponseDTO errorResponse = ErrorResponseDTO.builder()
                .errorCode("FORBIDDEN_ACTION")
                .message(ex.getMessage())
                .details("The requested action is not allowed in the current state")
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(errorResponse);
    }

    /**
     * Handles DrawerAlreadyOpenException (409 Conflict).
     */
    @ExceptionHandler(DrawerAlreadyOpenException.class)
    public ResponseEntity<ErrorResponseDTO> handleDrawerAlreadyOpen(
            DrawerAlreadyOpenException ex, HttpServletRequest request) {

        ErrorResponseDTO errorResponse = ErrorResponseDTO.builder()
                .errorCode("DRAWER_ALREADY_OPEN")
                .message(ex.getMessage())
                .details("Close the open drawer session before opening a new one")
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(errorResponse);
    }

    /**
     * Handles UserNotFoundException (404 Not Found)
     */
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleUserNotFound(
            UserNotFoundException ex, HttpServletRequest request) {

        ErrorResponseDTO errorResponse = ErrorResponseDTO.builder()
                .errorCode("USER_NOT_FOUND")
                .message(ex.getMessage())
                .details("The requested user does not exist in the system")
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(errorResponse);
    }

    /**
     * Handles all other generic exceptions (500 Internal Server Error)
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleGenericException(
            Exception ex, HttpServletRequest request) {

        ErrorResponseDTO errorResponse = ErrorResponseDTO.builder()
                .errorCode("INTERNAL_SERVER_ERROR")
                .message("An unexpected error occurred")
                .details(ex.getMessage() != null ? ex.getMessage() : "Unknown error")
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(errorResponse);
    }
}