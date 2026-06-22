package com.example.timsanbong.utils;

public class Constants {
    public static final String BASE_URL = "http://10.0.2.2:8080/api/";

    public static final String PREFS_AUTH = "auth";
    public static final String KEY_TOKEN = "token";
    public static final String KEY_USER_JSON = "user_json";

    public static final String EXTRA_FIELD_ID = "extra_field_id";
    public static final String EXTRA_FIELD_NAME = "extra_field_name";
    
    public static final String EXTRA_MATCH_POST = "extra_match_post";
    public static final String EXTRA_MATCH_ID = "extra_match_id";
    
    public static final boolean MOCK_MODE = false;
    public static final String EXTRA_PRICE_PER_HOUR = "pricePerHour";
    public static final String EXTRA_BOOKING_ID = "extra_booking_id";
    public static final String EXTRA_PAYMENT_FIELD_NAME = "extra_payment_field_name";
    public static final String EXTRA_TOTAL_PRICE = "extra_total_price";
    public static final String EXTRA_PAYMENT_SUCCESS = "extra_payment_success";
    public static final String EXTRA_PAYMENT_MESSAGE = "extra_payment_message";
    public static final String EXTRA_DEPOSIT_AMOUNT = "extra_deposit_amount";
    public static final String EXTRA_REMAINDER_AMOUNT = "extra_remainder_amount";
    public static final String EXTRA_CONVERSATION = "extra_conversation";
    public static final String EXTRA_MATCH = "extra_match";

    public static final String KEY_USER_ID = "user_id";
    public static final String KEY_USER_ROLE = "user_role";
    public static final String KEY_USER_EMAIL = "user_email";
    
    // WebSockets
    public static final String WS_URL = "ws://10.0.2.2:8080/ws";

    public static final String[] TIME_SLOTS = {
            "06:00 - 07:30",
            "07:30 - 09:00",
            "09:00 - 10:30",
            "10:30 - 12:00",
            "13:00 - 14:30",
            "14:30 - 16:00",
            "16:00 - 17:30",
            "17:30 - 19:00",
            "19:00 - 20:30",
            "20:30 - 22:00",
            "22:30 - 00:00"
    };
}
