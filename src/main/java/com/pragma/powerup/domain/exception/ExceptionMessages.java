package com.pragma.powerup.domain.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ExceptionMessages {
    INVALID_REQUEST("The request body is invalid."),
    BOOTCAMP_NAME_REQUIRED("Bootcamp name is required."),
    BOOTCAMP_DESCRIPTION_REQUIRED("Bootcamp description is required."),
    LAUNCH_DATE_REQUIRED("Launch date is required."),
    DURATION_INVALID("Duration must be greater than zero."),
    CAPACITY_COUNT_OUT_OF_RANGE("A bootcamp must have between 1 and 4 capacities."),
    CAPACITY_ID_INVALID("Capacity ID must be positive."),
    CAPACITIES_REPEATED("Capacities must not be repeated."),
    INVALID_SORT_FIELD("Sort by name or capacityCount."),
    INVALID_SORT_DIRECTION("Direction must be asc or desc."),
    CAPACITIES_NOT_FOUND("One or more capacities do not exist."),
    CAPACITY_SERVICE_UNAVAILABLE("The capacity service is unavailable."),
    CAPACITY_DETAILS_UNAVAILABLE("One or more associated capacities could not be loaded."),
    PERSON_DETAILS_UNAVAILABLE("One or more enrolled persons could not be loaded."),
    BOOTCAMP_NOT_FOUND("The bootcamp does not exist."),
    ALREADY_ENROLLED("The person is already enrolled in this bootcamp."),
    ENROLLMENT_OVERLAP("The bootcamp overlaps another enrollment."),
    ENROLLMENT_LIMIT("A person can have at most five current or future enrollments."),
    CONCURRENT_MODIFICATION("The data changed concurrently. Please try again."),
    AUTHENTICATION_REQUIRED("A valid token is required to access this resource."),
    ACCESS_DENIED("The user does not have permission to perform this action."),
    DATABASE_UNAVAILABLE("The persistence operation could not be completed."),
    INTERNAL_ERROR("The request could not be completed.");
    private final String message;
}
