package com.pragma.powerup.infrastructure.constants;

public final class ApiPaths {
    public static final String BOOTCAMPS = "/bootcamps";
    public static final String BOOTCAMP_BY_ID = BOOTCAMPS + "/{id}";
    public static final String MOST_ENROLLED_BOOTCAMP = BOOTCAMPS + "/most-enrolled";
    public static final String BOOTCAMP_ENROLLMENTS = BOOTCAMP_BY_ID + "/enrollments";
    public static final String CAPACITIES = "/capacities";
    public static final String CAPACITY_IDS = CAPACITIES + "/ids";
    public static final String CAPACITY_DETAILS = CAPACITIES + "/details";
    public static final String PERSONS = "/persons";
    public static final String BOOTCAMP_REPORTS = "/internal/bootcamp-reports";
    public static final String BOOTCAMP_REPORT_PERSON_COUNT = BOOTCAMP_REPORTS + "/{id}/person-count";
    public static final String OPENAPI = "/openapi.yaml";
    public static final String OPENAPI_FILE = "openapi.yaml";

    private ApiPaths() {
    }

    public static String[] documentation() {
        return new String[] {OPENAPI, "/v3/api-docs", "/v3/api-docs/swagger-config", "/webjars/swagger-ui/**",
                "/swagger-ui/**", "/swagger-ui.html"};
    }
}
