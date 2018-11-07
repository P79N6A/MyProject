package com.sankuai.octo.log;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

import java.net.URISyntaxException;
import java.util.Arrays;

public class TestBrowser {

    public static void main(String[] args) throws URISyntaxException, InterruptedException {
        final Socket socket = IO.socket("http://localhost:8700");
        socket.on(Socket.EVENT_CONNECTING, new Emitter.Listener() {

            @Override
            public void call(Object... args) {
                System.out.println("connecting:" + Arrays.asList(args));
            }
        }).on(Socket.EVENT_CONNECT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                System.out.println("connect:" + Arrays.asList(args));
               /* BrowserStartWatch command = new BrowserStartWatch("test",Set("localhost"));
                command.setAppkey();
                command.setFile("/tmp/1");
                // 10.4.241.195
                command.setIps(Arrays.asList());
                socket.emit(LogEvent.BROWSER_START_WATCH.name(), command);*/
            }
        }).on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                System.out.println("disconnect:" + Arrays.asList(args));
            }
        }).on("news", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                System.out.println(Arrays.asList(args));
            }
        });
        socket.connect();

        while (true) {
            Thread.sleep(1000);
            socket.emit("my other event", "dfs");
            System.out.println("xxx");
        }
    }
}
