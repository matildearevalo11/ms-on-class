package com.pragma.powerup.domain.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ExceptionMessages {
    INVALID_REQUEST("The request body is invalid."),
    CAPACITIES_NOT_FOUND("One or more capacities do not exist."),
    CAPACITY_SERVICE_UNAVAILABLE("The capacity service is unavailable."),
    CAPACITY_DETAILS_UNAVAILABLE("One or more associated capacities could not be loaded."),
    BOOTCAMP_NOT_FOUND("The bootcamp does not exist."),
    ALREADY_ENROLLED("The person is already enrolled in this bootcamp."),
    ENROLLMENT_OVERLAP("The bootcamp overlaps another enrollment."),
    ENROLLMENT_LIMIT("A person can have at most five current or future enrollments."),
    ENROLLMENT_CONFLICT("The enrollment changed concurrently. Please try again."),
    AUTHENTICATION_REQUIRED("A valid token is required to access this resource."),
    ACCESS_DENIED("The user does not have permission to perform this action."),
    DATABASE_UNAVAILABLE("The persistence operation could not be completed."),
    INTERNAL_ERROR("The request could not be completed.");
    private final String message;
}
