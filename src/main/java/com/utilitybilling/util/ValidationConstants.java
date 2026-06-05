package com.utilitybilling.util;

public final class ValidationConstants {

    private ValidationConstants() {}

    public static final String EMAIL_PATTERN =
            "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";

    public static final String DEFAULT_COUNTRY_CODE = "+250";

    public static final String COUNTRY_CODE_PATTERN = "^\\+[0-9]{1,4}$";

    /** Rwanda local mobile format: 07[2389] + 7 digits (without country code) */
    public static final String RWANDA_PHONE_PATTERN = "^(07[2389])[0-9]{7}$";

    public static final String PASSWORD_PATTERN =
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$";

    public static final String NATIONAL_ID_PATTERN = "^[0-9]{16}$";

    /** WM-10001 (water) or EM-10001 (electricity) */
    public static final String METER_NUMBER_PATTERN = "^(WM|EM)-[0-9]{5}$";

    public static final String EMAIL_MESSAGE = "Invalid email format";
    public static final String COUNTRY_CODE_MESSAGE = "Country code must start with + (e.g. +250 for Rwanda)";
    public static final String RWANDA_PHONE_MESSAGE =
            "Local phone must match Rwanda format: 07[2389] followed by 7 digits (e.g. 0788123456)";
    public static final String PASSWORD_MESSAGE =
            "Password must be at least 8 characters with uppercase, lowercase, digit, and special character";
    public static final String NATIONAL_ID_MESSAGE = "National ID must be exactly 16 digits";
    public static final String METER_NUMBER_MESSAGE =
            "Meter number must match format WM-10001 or EM-10001";
}
