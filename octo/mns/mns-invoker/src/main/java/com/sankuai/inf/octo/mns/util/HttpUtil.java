package com.sankuai.inf.octo.mns.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;

public class HttpUtil {

    private static final Logger LOG = LoggerFactory.getLogger(HttpUtil.class);

    private HttpUtil() {

    }

    public static String get(String url) {
        HttpURLConnection connection = null;
        String response = "";
        int code = -1;
        try {
            connection = (HttpURLConnection) new URL(url).openConnection();
            if (connection != null) {
                connection.setRequestMethod("GET");
                response = readStream(connection);
                code = connection.getResponseCode();
                LOG.debug("get {} {} {}", new Object[]{url, connection.getResponseCode(), response});
            } else {
                LOG.debug("can't connect to {}" + url);
            }
        } catch (Exception e) {
            LOG.debug("get {} failed {} {}", new Object[]{url, code, e.getMessage()}, e);
        } finally {
            if (connection != null) {
                try {
                    connection.disconnect();
                } catch (Exception e) {
                    LOG.debug("close connection failed... " + url, e);
                }
            }
        }
        return response;
    }

    public static String post(String url, String content) {
        HttpURLConnection connection = null;
        String response = null;
        int code = -1;
        try {
            connection = (HttpURLConnection) new URL(url).openConnection();
            if (connection != null) {
                connection.setRequestMethod("POST");
                writeContent(connection, content);
                response = readStream(connection);
                code = connection.getResponseCode();
                LOG.debug("post {} {} {}", new Object[]{url, connection.getResponseCode(), response});
            } else {
                LOG.debug("can't connect to {}" + url);
            }
        } catch (Exception e) {
            LOG.debug("post {} failed {} {}", new Object[]{url, code, e});
        } finally {
            if (connection != null) {
                try {
                    connection.disconnect();
                } catch (Exception e) {
                    LOG.debug("close connection failed... " + url, e);
                }
            }
        }
        return response;
    }

    private static void writeContent(HttpURLConnection connection, String content) {
        OutputStreamWriter out = null;
        try {
            connection.setDoOutput(true);
            out = new OutputStreamWriter(connection.getOutputStream(), Charset.forName("utf-8"));
            out.write(content);
        } catch (Exception e) {
            LOG.debug("write content to {} failed {}", new Object[]{connection.getURL(), e});
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    LOG.debug("close connection failed... " + connection.getURL(), e);
                }
            }
        }
    }

    private static String readStream(HttpURLConnection connection) throws IOException {
        connection.setConnectTimeout(10000);
        connection.setReadTimeout(10000);

        String result = null;
        StringBuilder sb = new StringBuilder();
        InputStream is = null;
        try {
            is = new BufferedInputStream(connection.getInputStream());
            BufferedReader br = new BufferedReader(new InputStreamReader(is, Charset.forName("utf-8")));
            String inputLine = "";
            while ((inputLine = br.readLine()) != null) {
                sb.append(inputLine);
            }
            result = sb.toString();
        } catch (Exception e) {
            LOG.debug("read connection failed... " + connection.getURL(), e);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    LOG.debug("close connection failed... " + connection.getURL(), e);
                }
            }
        }
        return result;
    }
}