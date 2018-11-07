package com.meituan.mtrace.http;

import org.apache.http.annotation.Immutable;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

/**
 * Factory methods for {@link CloseableHttpClient} instances.
 *
 * @since 4.3
 */
@Immutable
public class HttpClients {

    private HttpClients() {
        super();
    }

    /**
     * Creates builder object for construction of custom
     * {@link CloseableHttpClient} instances.
     */
    public static HttpClientBuilder custom() {
        HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
        httpClientBuilder.setHttpProcessor(new TraceHttpProcessor());
        return httpClientBuilder;
    }

    /**
     * Creates {@link CloseableHttpClient} instance with default
     * configuration.
     */
    public static CloseableHttpClient createDefault() {
        return custom().build();
    }

    /**
     * Creates {@link CloseableHttpClient} instance with default
     * configuration based on ssytem properties.
     */
    public static CloseableHttpClient createSystem() {
        return custom().useSystemProperties().build();
    }
}