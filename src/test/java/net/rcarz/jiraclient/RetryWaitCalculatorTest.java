package net.rcarz.jiraclient;

import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.message.BasicStatusLine;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

public class RetryWaitCalculatorTest {

    @Test
    public void testCalculateWaitTimeMillisWithRetryAfter() throws IOException {

        BasicStatusLine statusLine = new BasicStatusLine( HttpVersion.HTTP_1_1,29,"Too Many Requests");
        HttpResponse response = new BasicHttpResponse(statusLine);
        response.addHeader("Retry-After", "6");

        long waitTime = RetryWaitCalculator.calculateWaitTimeMillis(response, 0);
        assertTrue(waitTime > 0 && waitTime <= 6000L);

        waitTime = RetryWaitCalculator.calculateWaitTimeMillis(response, 1);
        assertTrue(waitTime > 0 && waitTime <= 12000L);
    }

    @Test
    public void testCalculateWaitTimeMillisWithIntervalFillRate() throws IOException {

        BasicStatusLine statusLine = new BasicStatusLine( HttpVersion.HTTP_1_1,29,"Too Many Requests");
        HttpResponse response = new BasicHttpResponse(statusLine);
        response.addHeader("X-RateLimit-Interval-Seconds", "1");
        response.addHeader("X-RateLimit-FillRate", "10");

        long waitTime = RetryWaitCalculator.calculateWaitTimeMillis(response, 3);
        assertTrue(waitTime > 0 && waitTime <= 800L);

        waitTime = RetryWaitCalculator.calculateWaitTimeMillis(response, 5);
        assertTrue(waitTime > 0 && waitTime <= 3200L);
    }

    @Test
    public void testCalculateWaitTimeMillisWithoutHeaders() throws IOException {

        BasicStatusLine statusLine = new BasicStatusLine( HttpVersion.HTTP_1_1,29,"Too Many Requests");
        HttpResponse response = new BasicHttpResponse(statusLine);

        long waitTime = RetryWaitCalculator.calculateWaitTimeMillis(response, 3);
        assertTrue(waitTime > 0 && waitTime <= 8000L);

        waitTime = RetryWaitCalculator.calculateWaitTimeMillis(response, 5);
        assertTrue(waitTime > 0 && waitTime <= 32000L);
    }

    @Test
    public void testCalculateWaitTimeMillisWithFailFast() throws IOException {

        BasicStatusLine statusLine = new BasicStatusLine( HttpVersion.HTTP_1_1,29,"Too Many Requests");
        HttpResponse response = new BasicHttpResponse(statusLine);
        response.addHeader("X-RateLimit-Interval-Seconds", "60");
        response.addHeader("X-RateLimit-FillRate", "1");

        IOException exception = assertThrows(IOException.class, () -> {
            RetryWaitCalculator.calculateWaitTimeMillis(response, 2);
        });

        String expectedMessage = "Exponential backoff ceiling (240s) exceeds threshold";
        assertTrue(exception.getMessage().contains(expectedMessage));
    }

}
