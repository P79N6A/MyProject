package com.sankuai.octo.aggregator;

import com.timgroup.statsd.ConvenienceMethodProvidingStatsDClient;
import com.timgroup.statsd.StatsDClientErrorHandler;
import com.timgroup.statsd.StatsDClientException;

import java.nio.charset.Charset;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * copy NonBlockingStatsDClient implement batch send use BatchUdpSender
 */
public class BatchStatsDClient extends ConvenienceMethodProvidingStatsDClient {
    private final BatchUdpSender sender;

    public BatchStatsDClient(String prefix, String hostname, int port, StatsDClientErrorHandler errorHandler) throws StatsDClientException {
        this.prefix = (prefix == null || prefix.trim().isEmpty()) ? "" : (prefix.trim() + ".");

        try {
            this.sender = new BatchUdpSender(hostname, port, STATS_D_ENCODING, errorHandler);
        } catch (Exception e) {
            throw new StatsDClientException("Failed to start StatsD client", e);
        }
    }

    /**
     * ***** original NonBlockingStatsDClient define code ******
     */

    private static final Charset STATS_D_ENCODING = Charset.forName("UTF-8");

    private static final StatsDClientErrorHandler NO_OP_HANDLER = new StatsDClientErrorHandler() {
        @Override
        public void handle(Exception e) { /* No-op */ }
    };

    private final String prefix;

    /**
     * Create a new StatsD client communicating with a StatsD instance on the
     * specified host and port. All messages send via this client will have
     * their keys prefixed with the specified string. The new client will
     * attempt to open a connection to the StatsD server immediately upon
     * instantiation, and may throw an exception if that a connection cannot
     * be established. Once a client has been instantiated in this way, all
     * exceptions thrown during subsequent usage are consumed, guaranteeing
     * that failures in metrics will not affect normal code execution.
     *
     * @param prefix   the prefix to apply to keys sent via this client (can be null or empty for no prefix)
     * @param hostname the host name of the targeted StatsD server
     * @param port     the port of the targeted StatsD server
     * @throws com.timgroup.statsd.StatsDClientException if the client could not be started
     */
    public BatchStatsDClient(String prefix, String hostname, int port) throws StatsDClientException {
        this(prefix, hostname, port, NO_OP_HANDLER);
    }

    /**
     * Cleanly shut down this StatsD client. This method may throw an exception if
     * the socket cannot be closed.
     */
    @Override
    public void stop() {
        sender.stop();
    }

    /**
     * Adjusts the specified counter by a given delta.
     * <p/>
     * <p>This method is non-blocking and is guaranteed not to throw an exception.</p>
     *
     * @param aspect     the name of the counter to adjust
     * @param delta      the amount to adjust the counter by
     * @param sampleRate the sampling rate being employed. For example, a rate of 0.1 would tell StatsD that this counter is being sent
     *                   sampled every 1/10th of the time.
     */
    @Override
    public void count(String aspect, long delta, double sampleRate) {
        send(messageFor(aspect, Long.toString(delta), "c", sampleRate));
    }

    /**
     * Records the latest fixed value for the specified named gauge.
     * <p/>
     * <p>This method is non-blocking and is guaranteed not to throw an exception.</p>
     *
     * @param aspect the name of the gauge
     * @param value  the new reading of the gauge
     */
    @Override
    public void recordGaugeValue(String aspect, long value) {
        recordGaugeCommon(aspect, Long.toString(value), value < 0, false);
    }

    @Override
    public void recordGaugeValue(String aspect, double value) {
        recordGaugeCommon(aspect, stringValueOf(value), value < 0, false);
    }

    @Override
    public void recordGaugeDelta(String aspect, long value) {
        recordGaugeCommon(aspect, Long.toString(value), value < 0, true);
    }

    @Override
    public void recordGaugeDelta(String aspect, double value) {
        recordGaugeCommon(aspect, stringValueOf(value), value < 0, true);
    }

    private void recordGaugeCommon(String aspect, String value, boolean negative, boolean delta) {
        final StringBuilder message = new StringBuilder();
        if (!delta && negative) {
            message.append(messageFor(aspect, "0", "g")).append('\n');
        }
        message.append(messageFor(aspect, (delta && !negative) ? ("+" + value) : value, "g"));
        send(message.toString());
    }

    /**
     * StatsD supports counting unique occurrences of events between flushes, Call this method to records an occurrence
     * of the specified named event.
     * <p/>
     * <p>This method is non-blocking and is guaranteed not to throw an exception.</p>
     *
     * @param aspect    the name of the set
     * @param eventName the value to be added to the set
     */
    @Override
    public void recordSetEvent(String aspect, String eventName) {
        send(messageFor(aspect, eventName, "s"));
    }

    /**
     * Records an execution time in milliseconds for the specified named operation.
     * <p/>
     * <p>This method is non-blocking and is guaranteed not to throw an exception.</p>
     *
     * @param aspect   the name of the timed operation
     * @param timeInMs the time in milliseconds
     */
    @Override
    public void recordExecutionTime(String aspect, long timeInMs, double sampleRate) {
        send(messageFor(aspect, Long.toString(timeInMs), "ms", sampleRate));
    }

    private String messageFor(String aspect, String value, String type) {
        return messageFor(aspect, value, type, 1.0);
    }

    private String messageFor(String aspect, String value, String type, double sampleRate) {
        final String message = prefix + aspect + ':' + value + '|' + type;
        return (sampleRate == 1.0)
                ? message
                : (message + "|@" + stringValueOf(sampleRate));
    }

    private void send(final String message) {
        sender.send(message);
    }

    private String stringValueOf(double value) {
        NumberFormat formatter = NumberFormat.getInstance(Locale.US);
        formatter.setGroupingUsed(false);
        formatter.setMaximumFractionDigits(19);
        return formatter.format(value);
    }
}