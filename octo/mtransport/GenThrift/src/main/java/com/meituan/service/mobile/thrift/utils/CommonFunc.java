package com.meituan.service.mobile.thrift.utils;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.StringTokenizer;

/**
 * Created with IntelliJ IDEA.
 * User: gaosheng
 * Date: 15-1-23
 * Time: 下午5:17
 */
public class CommonFunc {

    private static final Logger logger = LoggerFactory.getLogger(CommonFunc.class);

    /**
     * 根据文件路径读取文件内容
     * @param path 文件路径
     * @return 返回文件内容
     */
    public static String readFile(String path){
        File file = new File(path);
        InputStream in = null;
        BufferedReader br = null;
        try {
            in = new FileInputStream(file);
            br = new BufferedReader(new InputStreamReader(in, "UTF-8"));
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        String line;
        StringBuffer sb = new StringBuffer();
        try {
            while((line=br.readLine())!=null){
                sb.append(line);
                sb.append("\n");
            }
            br.close();
            in.close();
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        return sb.toString();
    }

    /**
     * 输出内容到指定文件
     * @param filename 文件名，绝对路径
     * @param content  文件内容
     */
    public static void writeFile(String filename, String content) {

        logger.info("Filename:" + filename);
        FileWriter fw;
        try {
            fw = new FileWriter(filename);
            fw.write(content, 0, content.length());
            fw.flush();
            fw.close();
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }

    /**
     * 向url发送http get请求
     * @param URL
     * @return
     */
    public static String httpRequest(String URL) {

        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(URL);
        CloseableHttpResponse response = null;
        InputStream in = null;
        InputStreamReader isr = null;
        BufferedReader br = null;
        StringBuffer sb = null;
        try {
            response = httpClient.execute(httpGet);
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                in = entity.getContent();
                isr = new InputStreamReader(in);
                br = new BufferedReader(isr);
                sb = new StringBuffer();
                String line;
                while ((line = br.readLine()) != null)
                    sb.append(line);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);

        } finally {
            try {
                in.close();
                isr.close();
                br.close();
                response.close();
            } catch (IOException e) {
                logger.error(e.getMessage());
            }
        }

        logger.info("URL:{}, HttpResponse: {}", URL, sb.toString());
        return sb.toString();
    }

    /**
     * html输出时把\n转为<br>
     * @param content
     * @return
     */
    public static String addBr(String content) {
        StringBuffer sb = new StringBuffer();
        StringTokenizer token = new StringTokenizer(content, "\n");
        while (token.hasMoreTokens()) {
            sb.append("<br>" + token.nextToken());
        }
        return sb.toString();
    }

    /**
     * 向url发送http get请求, ba认证
     * @param URL
     * @return
     */
    public static String getWithBA(String URL, String clientId, String secret) {

        CloseableHttpClient httpClient = HttpClients.createDefault();

        HttpGet httpGet = new HttpGet(URL);
        BaseAuthorizationUtils.generateAuthAndDateHeader(httpGet, clientId, secret);

        CloseableHttpResponse response = null;
        InputStream in = null;
        InputStreamReader isr = null;
        BufferedReader br = null;
        StringBuffer sb = null;
        try {
            response = httpClient.execute(httpGet);
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                in = entity.getContent();
                isr = new InputStreamReader(in);
                br = new BufferedReader(isr);
                sb = new StringBuffer();
                String line;
                while ((line = br.readLine()) != null)
                    sb.append(line);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return null;

        } finally {
            try {
                in.close();
                isr.close();
                br.close();
                response.close();
            } catch (IOException e) {
                logger.error(e.getMessage());
                return null;
            }
        }

        logger.info("URL:{}, HttpResponse: {}", URL, sb.toString());
        return sb.toString();
    }
}
