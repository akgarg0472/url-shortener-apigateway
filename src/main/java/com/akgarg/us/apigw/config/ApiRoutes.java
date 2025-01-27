package com.akgarg.us.apigw.config;

import java.util.List;

public final class ApiRoutes {

    public static final String AUTH_API_PATH = "/api/v1/auth/**";
    public static final String URL_SHORTENER_API_PATH = "/api/v1/urlshortener/**";
    public static final String STATISTICS_API_PATH = "/api/v1/statistics/**";
    public static final String PROFILE_API_PATH = "/api/v1/profiles/**";
    public static final String PAYMENT_API_PATH = "/api/v1/payments/**";
    public static final String SUBSCRIPTION_API_PATH = "/api/v1/subscriptions/**";
    public static final String SUBSCRIPTION_PACKS_API_PATH = "/api/v1/subscriptions/packs/**";
    public static final String PAYPAL_WEBHOOK_API_PATH = "/api/v1/payments/paypal/webhook/**";
    public static final String GENERIC_API_PATH = "/**";

    private ApiRoutes() {
        throw new IllegalAccessError("Utility class");
    }

    public static List<String> getApiPaths() {
        return List.of(
                AUTH_API_PATH,
                URL_SHORTENER_API_PATH,
                STATISTICS_API_PATH,
                PROFILE_API_PATH,
                PAYMENT_API_PATH,
                SUBSCRIPTION_API_PATH,
                GENERIC_API_PATH,
                SUBSCRIPTION_PACKS_API_PATH,
                PAYPAL_WEBHOOK_API_PATH
        );
    }

}
