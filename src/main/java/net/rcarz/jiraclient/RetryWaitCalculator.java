package net.rcarz.jiraclient;

import org.apache.http.Header;
import org.apache.http.HttpResponse;

import java.io.IOException;
import java.util.Random;

public class RetryWaitCalculator {

    private static final long MAX_WAIT_TIME_MS = 120000;
    private static final Random RANDOM = new Random();

    private RetryWaitCalculator() {
    }

    /**
     * Calculates the wait time (in milliseconds) for a retry attempt following a 429 (Too Many Requests) response.
     * This method implements an "Informed Full Jitter" strategy, which is designed to prevent the "Thundering Herd"
     * problem by spreading retry attempts randomly across an exponentially growing time window.
     *
     * @param response  The HttpResponse object, used to read rate-limiting headers.
     * @param attempt   The current retry attempt number (starting from 0 for the first retry).
     * @return A value representing the randomized wait time in milliseconds.
     * @throws IOException If the "Fail Fast" policy is triggered because the calculated
     * backoff ceiling exceeds the MAX_WAIT_TIME_MS threshold.
     */
    public static long calculateWaitTimeMillis(HttpResponse response, int attempt) throws IOException {
        long base = 1000L;

        // If we have value in Retry-After header we use exactly this value (is the direct indication from Jira) plus the jitter
        long retryAfter = getNumericValueForHeader(response, "Retry-After");
        if (retryAfter > 0) {
            base = retryAfter * 1000L;
        }
        else {
            // Otherwise, we calculate intervalSeconds / fillRate, to try to find out the following moment in which we will have
            //  an available request
            long intervalSeconds = getNumericValueForHeader(response, "X-RateLimit-Interval-Seconds");
            long fillRate = getNumericValueForHeader(response,"X-RateLimit-FillRate");
            if (intervalSeconds > 0 && fillRate > 0) {
                base = (intervalSeconds * 1000L) / fillRate;
            }
        }

        // For small bases, we prefer to space out API calls. This way, we avoid stressing Jira, and we stay more away from its rate limit.
        if (base <= 100L) {
            base *=2L;
        }

        // Just in case base is 0 because (intervalSeconds * 1000L) / fillRate is 0
        if (base <= 0L) {
            base =1L;
        }

        // The ceiling grows exponentially with the number of attempt
        long ceiling = (long) Math.pow(2, attempt) * base;

        // If this calculated ceiling exceeds the MAX_WAIT_TIME_MS constant, this method throws an IOException to abort
        // the retry and protect the thread from being busy too long.
        if (ceiling > MAX_WAIT_TIME_MS) {
            String headerDetails = formatRateLimitHeaders(response);
            throw new IOException(String.format(
                    "Fail-fast: Exponential backoff ceiling (%ds) exceeds threshold. %s", ceiling / 1000, headerDetails));
        }

        return RANDOM.nextInt((int)ceiling) + 1L;
    }

    private static String formatRateLimitHeaders(HttpResponse response) {
        String retryAfter = getValueForHeader(response, "Retry-After");
        String interval = getValueForHeader(response, "X-RateLimit-Interval-Seconds");
        String fillRate = getValueForHeader(response, "X-RateLimit-FillRate");

        return String.format(
                "Relevant headers: Retry-After: [%s], X-RateLimit-Interval-Seconds: [%s], X-RateLimit-FillRate: [%s]",
                retryAfter, interval, fillRate);
    }

    private static Long getNumericValueForHeader(HttpResponse response, String name) {
        String value = getValueForHeader(response, name);
        if (value == null || value.isEmpty()) {
            return 0L;
        }
        try {
            return Long.parseLong(value);
        } catch(NumberFormatException ignored) {
            return 0L;
        }
    }

    private static String getValueForHeader(HttpResponse response, String name) {
        if (name == null || name.isEmpty() ) {
            return "";
        }
        Header header = response.getFirstHeader(name);
        if (header == null) {
            return "";
        }
        return header.getValue();
    }
}
