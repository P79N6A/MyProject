package com.sankuai.octo.benchmark;

import com.sankuai.octo.benchmark.utils.ThriftMsgGenerator;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.*;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.handler.codec.http.*;
import org.jboss.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;

import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.*;
import static org.jboss.netty.handler.codec.http.HttpHeaders.*;
import static org.jboss.netty.handler.codec.http.HttpResponseStatus.CONTINUE;
import static org.jboss.netty.handler.codec.http.HttpResponseStatus.OK;
import static org.jboss.netty.handler.codec.http.HttpVersion.HTTP_1_1;

/**
 * Copyright (C) 2016 Meituan
 * All rights reserved
 * User: xiongjiyuan
 * Date: 16/12/19
 * Time: 11:29
 */
public class HttpServer {
    private static final Logger logger = LoggerFactory.getLogger(HttpServer.class);
    private ServerBootstrap bootstrap;
    private ChannelGroup channelGroup = new DefaultChannelGroup();

    public static void main(String[] args) {
//        HttpServer server = new HttpServer();
//        server.start();

        ClassPathXmlApplicationContext applicationContext =
                new ClassPathXmlApplicationContext("classpath:server.xml");
    }

    public void start() {
        bootstrap = new ServerBootstrap(
                new NioServerSocketChannelFactory(Executors.newCachedThreadPool(), 20, Executors.newCachedThreadPool(),
                        150));
        // Enable TCP_NODELAY to handle pipelined requests without latency.
        bootstrap.setOption("child.tcpNoDelay", true);

        // Set up the event pipeline factory.
        bootstrap.setPipelineFactory(new HttpSnoopServerPipelineFactory());

        // Bind and start to accept incoming connections.
        channelGroup.add(bootstrap.bind(new InetSocketAddress(9090)));
    }

    public void shutdown() {
        channelGroup.close().awaitUninterruptibly();
        bootstrap.releaseExternalResources();
    }
    class HttpSnoopServerHandler extends SimpleChannelUpstreamHandler {
        private HttpRequest request;
        private boolean readingChunks;
        /**
         * Buffer that stores the response content
         */
        private final StringBuilder buf = new StringBuilder();

        @Override
        public void channelOpen(ChannelHandlerContext ctx, ChannelStateEvent e) {
            channelGroup.add(e.getChannel());
        }

        @Override public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws InterruptedException {
            if (!readingChunks) {
                HttpRequest request = this.request = (HttpRequest) e.getMessage();
                if (is100ContinueExpected(request)) {
                    send100Continue(e);
                }

                buf.setLength(0);
                buf.append("WELCOME TO THE WILD WILD WEB SERVER\r\n");
                buf.append("===================================\r\n");
                buf.append("VERSION: " + request.getProtocolVersion() + "\r\n");
                buf.append("HOSTNAME: " + getHost(request, "unknown") + "\r\n");
                buf.append("REQUEST_URI: " + request.getUri() + "\r\n\r\n");
                buf.append("\r\n");

                QueryStringDecoder queryStringDecoder = new QueryStringDecoder(request.getUri());
                Map<String, List<String>> params = queryStringDecoder.getParameters();
                if (!params.isEmpty()) {
                    for (Map.Entry<String, List<String>> p : params.entrySet()) {
                        String key = p.getKey();
                        List<String> vals = p.getValue();
                        for (String val : vals) {
                            buf.append("PARAM: " + key + " = " + val + "\r\n");
                        }
                    }
                    buf.append("\r\n");
                }

                int param = params.get("param") != null && !params.get("param").isEmpty() ?
                        Integer.parseInt(params.get("param").get(0)) :
                        0;
                if (param > 0) {
                    boolean keepAlive = isKeepAlive(request);
                    String result = ThriftMsgGenerator.getRandomString(param);
                    HttpResponse response = new DefaultHttpResponse(HTTP_1_1, OK);
                    String text = "{\"result\":\"" + result + "\"}";
                    response.setContent(ChannelBuffers.copiedBuffer(text, CharsetUtil.UTF_8));
                    response.setHeader(CONTENT_TYPE, "application/json; charset=UTF-8");
                    if (keepAlive) {
                        // Add 'Content-Length' header only for a keep-alive connection.
                        response.setHeader(CONTENT_LENGTH, response.getContent().readableBytes());
                        // Add keep alive header as per:
                        // - http://www.w3.org/Protocols/HTTP/1.1/draft-ietf-http-v11-spec-01.html#Connection
                        response.setHeader(CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
                    }
                    ChannelFuture future = e.getChannel().write(response);
                    // Close the non-keep-alive connection after the write operation is done.
                    if (!keepAlive) {
                        future.addListener(ChannelFutureListener.CLOSE);
                    }
                    return;
                }
            } else {
                HttpChunk chunk = (HttpChunk) e.getMessage();
                if (chunk.isLast()) {
                    readingChunks = false;
                    buf.append("END OF CONTENT\r\n");

                    HttpChunkTrailer trailer = (HttpChunkTrailer) chunk;
                    if (!trailer.getHeaderNames().isEmpty()) {
                        buf.append("\r\n");
                        for (String name : trailer.getHeaderNames()) {
                            for (String value : trailer.getHeaders(name)) {
                                buf.append("TRAILING HEADER: " + name + " = " + value + "\r\n");
                            }
                        }
                        buf.append("\r\n");
                    }
                    writeResponse(e);
                } else {
                    buf.append("CHUNK: " + chunk.getContent().toString(CharsetUtil.UTF_8) + "\r\n");
                }
            }
        }

        private void writeResponse(MessageEvent e) {
            // Decide whether to close the connection or not.
            boolean keepAlive = isKeepAlive(request);

            // Build the response object.
            HttpResponse response = new DefaultHttpResponse(HTTP_1_1, OK);
            response.setContent(ChannelBuffers.copiedBuffer(buf.toString(), CharsetUtil.UTF_8));
            response.setHeader(CONTENT_TYPE, "text/plain; charset=UTF-8");

            if (keepAlive) {
                // Add 'Content-Length' header only for a keep-alive connection.
                response.setHeader(CONTENT_LENGTH, response.getContent().readableBytes());
                // Add keep alive header as per:
                // - http://www.w3.org/Protocols/HTTP/1.1/draft-ietf-http-v11-spec-01.html#Connection
                response.setHeader(CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
            }

            // Encode the cookie.
            String cookieString = request.getHeader(COOKIE);
            if (cookieString != null) {
                CookieDecoder cookieDecoder = new CookieDecoder();
                Set<Cookie> cookies = cookieDecoder.decode(cookieString);
                if (!cookies.isEmpty()) {
                    // Reset the cookies if necessary.
                    CookieEncoder cookieEncoder = new CookieEncoder(true);
                    for (Cookie cookie : cookies) {
                        cookieEncoder.addCookie(cookie);
                        response.addHeader(SET_COOKIE, cookieEncoder.encode());
                    }
                }
            } else {
                // Browser sent no cookie.  Add some.
                CookieEncoder cookieEncoder = new CookieEncoder(true);
                cookieEncoder.addCookie("key1", "value1");
                response.addHeader(SET_COOKIE, cookieEncoder.encode());
                cookieEncoder.addCookie("key2", "value2");
                response.addHeader(SET_COOKIE, cookieEncoder.encode());
            }

            // Write the response.
            ChannelFuture future = e.getChannel().write(response);

            // Close the non-keep-alive connection after the write operation is done.
            if (!keepAlive) {
                future.addListener(ChannelFutureListener.CLOSE);
            }
        }

        private void send100Continue(MessageEvent e) {
            HttpResponse response = new DefaultHttpResponse(HTTP_1_1, CONTINUE);
            e.getChannel().write(response);
        }

        @Override public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {
            e.getCause().printStackTrace();
            e.getChannel().close();
        }

    }

    class HttpSnoopServerPipelineFactory implements ChannelPipelineFactory {

        @Override public ChannelPipeline getPipeline() throws Exception {
            ChannelPipeline pipeline = Channels.pipeline();
            pipeline.addLast("decoder", new HttpRequestDecoder());
            // Uncomment the following line if you don't want to handle HttpChunks.
            pipeline.addLast("aggregator", new HttpChunkAggregator(1048576));
            pipeline.addLast("encoder", new HttpResponseEncoder());
            // Remove the following line if you don't want automatic content compression.
            pipeline.addLast("deflater", new HttpContentCompressor());
            pipeline.addLast("handler", new HttpSnoopServerHandler());
            return pipeline;
        }
    }
}



