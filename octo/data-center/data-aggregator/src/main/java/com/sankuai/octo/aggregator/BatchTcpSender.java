package com.sankuai.octo.aggregator;

import com.timgroup.statsd.StatsDClientErrorHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.concurrent.*;

public class BatchTcpSender {
    private final static Logger LOG = LoggerFactory.getLogger(BatchTcpSender.class);

    private final Charset encoding;
    private final String host;
    private final int port;
    private SocketChannel channel;
    private OutputStream stream;
    private final ExecutorService executor;
    private StatsDClientErrorHandler handler;
    private static final int PACKET_SIZE_BYTES = 250;
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
                        stream.write(data);
                        stream.write((byte) '\n');
                        stream.flush();
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

    public BatchTcpSender(String hostname, int port, Charset encoding, StatsDClientErrorHandler handler) throws IOException {
        this.encoding = encoding;
        this.handler = handler;
        this.host = hostname;
        this.port = port;
        initChannel();
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
            if (channel != null) {
                try {
                    channel.close();
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
            handleException(e);
        }
    }

    public int blockingSend(ByteBuffer data) {
        try {
//            System.out.println(channel.isConnected());
//            int result = channel.write(data);
            stream.write(data.array());
//            System.out.println("send result " + result);
            return 0;
        } catch (Exception e) {
            handleException(e);
            initChannel();
        }
        return -1;
    }

    private void initChannel() {
        try {
            this.channel = SocketChannel.open(new InetSocketAddress(host, port));
            Socket socket = new Socket();
            socket.connect(new InetSocketAddress(host, port));
            stream = socket.getOutputStream();
        } catch (Exception e) {
            handleException(e);
        }
    }

    protected void handleException(Exception e) {
        LOG.error("", e);
        handler.handle(e);
    }
}