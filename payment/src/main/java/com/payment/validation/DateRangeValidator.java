package com.payment.validation;

import com.payment.dto.PaymentFilterRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class DateRangeValidator implements ConstraintValidator<ValidDateRange, PaymentFilterRequest> {

    @Override
    public boolean isValid(PaymentFilterRequest request, ConstraintValidatorContext context) {
        if (request.startDate() == null || request.endDate() == null) {
            return true;
        }
        return request.startDate().isBefore(request.endDate());
    }
}
