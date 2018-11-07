package com.sankuai.octo.log;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.URISyntaxException;

public class MyAgent {
    public static void main(String[] args) throws InterruptedException, URISyntaxException, IOException {

        ServerSocket socket = new ServerSocket(9876);
        socket.getReceiveBufferSize();
        while (true)
            Thread.sleep(100000);
    }
}
