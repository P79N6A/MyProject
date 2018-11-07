package com.sankuai.octo.aggregator;

import com.timgroup.statsd.StatsDClientErrorHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.charset.Charset;
import java.util.concurrent.*;

public class BatchUdpSender {
    private final static Logger LOG = LoggerFactory.getLogger(BatchUdpSender.class);

    private final Charset encoding;
    private final DatagramChannel clientSocket;
    private final ExecutorService executor;
    private StatsDClientErrorHandler handler;
    private static final int PACKET_SIZE_BYTES = 1500;
    private final BlockingQueue<String> queue = new LinkedBlockingQueue<String>();

    private class QueueConsumer implements Runnable {
        private final ByteBuffer buffer = ByteBuffer.allocateDirect(PACKET_SIZE_BYTES);

        @Override
        public void run() {
            while (!executor.isShutdown()) {
                try {
                    final String message = queue.poll(100, TimeUnit.MILLISECONDS);
                    if (null != message) {
                        final byte[] data = message.getBytes(encoding);
                        if (buffer.remaining() < (data.length + 1)) {
                            send();
                        }
                        if (buffer.position() > 0) {
                            buffer.put((byte) '\n');
                        }
                        buffer.put(data);
                        if (buffer.position() > PACKET_SIZE_BYTES / 2 && null == queue.peek()) {
                            send();
                        }
                    } else {
                        send();
                    }
                } catch (Exception e) {
                    handler.handle(e);
                }
            }
        }

        private void send() {
            //LOG.debug("batch send " + buffer.position());
            int sizeOfBuffer = buffer.position();
            buffer.flip();
            int sentBytes = blockingSend(buffer);
            buffer.limit(buffer.capacity());
            buffer.rewind();
            if (sizeOfBuffer != sentBytes) {
                handler.handle(
                        new IOException(
                                String.format("Could not send entirely stat %s. Only sent %d bytes out of %d bytes",
                                        buffer.toString(),
                                        sentBytes,
                                        sizeOfBuffer)));
            }
        }
    }

    public BatchUdpSender(String hostname, int port, Charset encoding, StatsDClientErrorHandler handler) throws IOException {
        this.encoding = encoding;
        this.handler = handler;
        this.clientSocket = DatagramChannel.open();
        this.clientSocket.connect(new InetSocketAddress(hostname, port));

        this.executor = Executors.newSingleThreadExecutor(new ThreadFactory() {
            final ThreadFactory delegate = Executors.defaultThreadFactory();

            @Override
            public Thread newThread(Runnable r) {
                Thread result = delegate.newThread(r);
                result.setName("StatsD-" + result.getName());
                result.setDaemon(true);
                return result;
            }
        });
        this.executor.submit(new QueueConsumer());
    }

    public void stop() {
        try {
            executor.shutdown();
            executor.awaitTermination(30, TimeUnit.SECONDS);
        } catch (Exception e) {
            handler.handle(e);
        } finally {
            if (clientSocket != null) {
                try {
                    clientSocket.close();
                } catch (Exception e) {
                    handler.handle(e);
                }
            }
        }
    }

    public void send(final String message) {
        queue.offer(message);
    }

    public void blockingSend(String message) {
        try {
            final byte[] sendData = message.getBytes(encoding);
            blockingSend(ByteBuffer.wrap(sendData));
        } catch (Exception e) {
            handler.handle(e);
        }
    }

    public int blockingSend(ByteBuffer data) {
        try {
            return clientSocket.write(data);
        } catch (Exception e) {
            handler.handle(e);
        }
        return -1;
    }
}