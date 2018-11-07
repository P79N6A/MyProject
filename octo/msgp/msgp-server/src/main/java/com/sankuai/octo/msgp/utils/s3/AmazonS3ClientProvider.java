package com.sankuai.octo.msgp.utils.s3;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.Protocol;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.S3ClientOptions;
import com.sankuai.msgp.common.utils.helper.CommonHelper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.net.URL;

/**
 * Created by nero on 2017/12/2
 */
@Configuration
public class AmazonS3ClientProvider {
    private static String accessKey = CommonHelper.isOffline()?"d672e493065b4af380e23599483baccb":"467a4d0cf8cc4e7994e9635f6701ba6e";
    private static String secretKey = CommonHelper.isOffline()?"1e17e09d342d41a59cd8d3e2e812f33e":"ceba159a3fb747a183256f3d03247c3a";
    private static String url = CommonHelper.isOffline()?"http://msstest.vip.sankuai.com":"http://mss.vip.sankuai.com";
    static AmazonS3Client s3conn;

    @Bean
    public static AmazonS3 CreateAmazonS3Conn()
            throws IOException {
        AWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);
        ClientConfiguration clientConfig = new ClientConfiguration();
        //clientConfig.setSignerOverride("S3SignerType");

        URL ep = new URL(url);
        if (ep.getProtocol().equalsIgnoreCase("http")) {
            clientConfig.setProtocol(Protocol.HTTP);
        } else if (ep.getProtocol().equalsIgnoreCase("https")) {
            clientConfig.setProtocol(Protocol.HTTPS);
        } else {
            throw new IOException("Unsupported protocol");
        }
        String endpoint = ep.getHost();
        if (ep.getPort() > 0) {
            endpoint += ":" + ep.getPort();
        }

        S3ClientOptions s3ClientOptions = new S3ClientOptions();
        s3ClientOptions.setPathStyleAccess(true);
        s3conn = new AmazonS3Client(credentials, clientConfig);
        s3conn.setS3ClientOptions(s3ClientOptions);
        s3conn.setEndpoint(endpoint);
        return s3conn;
    }
}
