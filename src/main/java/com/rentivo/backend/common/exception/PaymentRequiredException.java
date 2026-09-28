package com.rentivo.backend.common.exception;

import org.springframework.http.HttpStatus;

/** 402: the action needs an active subscription (or the plan's contact limit is used up). */
public class PaymentRequiredException extends ApiException {

    public PaymentRequiredException(String message) {
        super(HttpStatus.PAYMENT_REQUIRED, message);
    }
}
