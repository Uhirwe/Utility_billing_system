package com.utilitybilling.util;

import com.utilitybilling.exception.ValidationException;

import java.time.LocalDate;
import java.time.Period;

public final class AgeValidator {

    private AgeValidator() {}

    public static void validateMinimumAge(LocalDate dateOfBirth, int minimumAge) {
        int age = Period.between(dateOfBirth, LocalDate.now()).getYears();
        if (age < minimumAge) {
            throw new ValidationException("Customer must be at least " + minimumAge + " years old");
        }
    }
}
