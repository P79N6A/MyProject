package com.meituan.service.mobile.mtthrift.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;

import static com.meituan.service.mobile.mtthrift.util.Consts.defaultTimeoutInMills;

/**
 * Created by jiguang on 15/7/8.
 */

@Deprecated
public class HttpUtils {
    private static final Logger LOG = LoggerFactory.getLogger(HttpUtils.class);

    @Deprecated
    public static Object getJsonObj(String urlStr, String encoding, Class<?> clazz) {
        InputStreamReader reader = null;
        BufferedReader in = null;
        Object obj = null;
        try {
            URL url = new URL(urlStr);
            URLConnection connection = url.openConnection();
            connection.setConnectTimeout(defaultTimeoutInMills);
            reader = new InputStreamReader(connection.getInputStream(), encoding);
            in = new BufferedReader(reader);
            String line;        //每行内容
            int lineFlag = 0;        //标记: 判断有没有数据
            StringBuffer content = new StringBuffer();
            while ((line = in.readLine()) != null) {
                content.append(line);
                lineFlag++;
            }
            if (0 != lineFlag) {
                ObjectMapper objectMapper = new ObjectMapper();
                obj = objectMapper.readValue(content.toString(), clazz);
            }
        } catch (SocketTimeoutException e) {
            LOG.warn("get json time out:" + urlStr + e.getMessage());
        } catch (JsonProcessingException e) {
            LOG.warn("json format error:" + urlStr + e.getMessage());
        } catch (Exception e) {
            LOG.warn("Exception: " + urlStr + e.getMessage());
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    LOG.warn("关闭流出现异常!!!", e);
                }
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    LOG.warn("关闭流出现异常!!!");
                }
            }
        }
        return obj;
    }

    public static String postJson(String requestUrl, String content) {
        byte[] postData = content.getBytes();
        int postDataLength = postData.length;
        URL url = null;
        HttpURLConnection conn = null;
        String result = "";
        int code = -1;
        try {
            url = new URL(requestUrl);
            conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setInstanceFollowRedirects(false);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("charset", "utf-8");
            conn.setRequestProperty("Content-Length", Integer.toString(postDataLength));
            conn.setUseCaches(false);

            writeContent(conn, postData);
            result = readContent(conn);
            code = conn.getResponseCode();
        } catch (Exception e) {
            LOG.debug("post {} failed {} {}", new Object[]{url, code, e});
        } finally {
            if (conn != null) {
                try {
                    conn.disconnect();
                } catch (Exception e) {
                    LOG.debug("close connection failed... " + url, e);
                }
            }
        }

        return result;
    }

    private static void writeContent(HttpURLConnection conn, byte[] postData) {
        DataOutputStream wr = null;
        try {
            wr = new DataOutputStream(conn.getOutputStream());
            wr.write(postData);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (wr != null) {
                try {
                    wr.close();
                } catch (IOException e) {
                    LOG.debug("write content to {} failed {}", new Object[]{conn.getURL(), e});
                }
            }
        }
    }

    private static String readContent(HttpURLConnection conn) {
        conn.setConnectTimeout(defaultTimeoutInMills);
        conn.setReadTimeout(defaultTimeoutInMills);

        String result = "";
        StringBuilder sb = new StringBuilder();
        InputStream is = null;
        try {
            is = new BufferedInputStream(conn.getInputStream());
            BufferedReader br = new BufferedReader(new InputStreamReader(is, Charset.forName("utf-8")));
            String inputLine = "";
            while ((inputLine = br.readLine()) != null) {
                sb.append(inputLine);
            }
            result = sb.toString();
        } catch (Exception e) {
            LOG.debug("read content from {} failed {}", new Object[]{conn.getURL(), e});
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    LOG.debug("close connection failed... " + conn.getURL(), e);
                }
            }
        }
        return result;
    }

    public static String get(String url) {
        HttpURLConnection connection = null;
        String response = "";
        int code = -1;
        try {
            connection = (HttpURLConnection) new URL(url).openConnection();
            if (connection != null) {
                connection.setRequestMethod("GET");
                response = readContent(connection);
                code = connection.getResponseCode();
                LOG.debug("get {} {} {}", new Object[]{url, connection.getResponseCode(), response});
            } else {
                LOG.debug("can't connect to {}", url);
            }
        } catch (Exception e) {
            LOG.debug("get {} failed {} {}", new Object[]{url, code, e.getMessage()}, e);
        } finally {
            if (connection != null) {
                try {
                    connection.disconnect();
                } catch (Exception e) {
                    LOG.debug("close connection failed... {}", url, e);
                }
            }
        }
        return response;
    }
}
