package com.sankuai.octo.statistic.metrics;

import java.util.EventListener;

/**
 * Listeners for events from the registry.  Listeners must be thread-safe.
 */
public interface MetricRegistryListener extends EventListener {
    /**
     * Called when a {@link SimpleCountHistogram} is added to the registry.
     *
     * @param name      the histogram's name
     * @param histogram the histogram
     */
    void onHistogramAdded(String name, SimpleCountHistogram histogram);

    /**
     * Called when a {@link SimpleCountHistogram} is removed from the registry.
     *
     * @param name the histogram's name
     */
    void onHistogramRemoved(String name);

    /**
     * A no-op implementation of {@link MetricRegistryListener}.
     */
    abstract class Base implements MetricRegistryListener {

        @Override
        public void onHistogramAdded(String name, SimpleCountHistogram histogram) {
        }

        @Override
        public void onHistogramRemoved(String name) {
        }

    }

}