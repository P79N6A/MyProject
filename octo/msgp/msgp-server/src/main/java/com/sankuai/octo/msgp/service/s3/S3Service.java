package com.sankuai.octo.msgp.service.s3;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.util.IOUtils;
import com.sankuai.msgp.common.utils.helper.CommonHelper;
import com.sankuai.octo.msgp.utils.s3.AmazonS3ClientProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.*;

/**
 * Created by nero on 2017/12/2
 */
@Service
public class S3Service {



    private AmazonS3 amazonS3Client = AmazonS3ClientProvider.CreateAmazonS3Conn();

    private static final Logger logger = LoggerFactory.getLogger(S3Service.class);

    private String dashboardBucketName = CommonHelper.isOffline()?"offline-dashboard":"online-dashboard";


    public S3Service() throws IOException {
    }

    /**
     * 创建一个bucket
     *
     * @param newBucketName
     */
    public void createBucket(String newBucketName) {
        try {
            amazonS3Client.createBucket(newBucketName);
        } catch (AmazonServiceException ase) {
            logAmazonServiceException(ase);
        } catch (AmazonClientException ace) {
            logAmazonClientException(ace);
        }
    }


    /**
     * @param bucketName bulkname
     * @param objectName 待上传文件的key
     * @param file       待上传文件
     */
    public void uploadObject(String bucketName, String objectName, File file) {
        try {
            logger.info("###begin uploadObject bucket"+bucketName+",objectname : "+objectName+", File : "+file.getAbsolutePath());
            amazonS3Client.putObject(new PutObjectRequest(bucketName, objectName, file));
        } catch (AmazonServiceException ase) {
            logAmazonServiceException(ase);
        } catch (AmazonClientException ace) {
            logAmazonClientException(ace);
        }
    }


    public void uploadDashboard(String path){
        uploadObject(dashboardBucketName,path.substring(path.lastIndexOf("/")+1,path.length()),new File(path));
    }

    public void downloadDashboard(String path){
        InputStream in = downloadObject(dashboardBucketName,path.substring(path.lastIndexOf("/")+1,path.length()));
        try {
            FileOutputStream fos = new FileOutputStream(path);
            int ch = 0;
            while ((ch=in.read()) != -1){
                fos.write(ch);
            }
            in.close();
            fos.close();
        } catch (Exception e) {
            logger.error("downloadDashboard error ",e);
        }
    }

    public InputStream downloadObject(String bucketName, String objectName) {
        try {
            S3Object s3object = amazonS3Client.getObject(new GetObjectRequest(bucketName, objectName));
            logger.info("Content-Type: " + s3object.getObjectMetadata().getContentType());
            InputStream objectData = s3object.getObjectContent();
            return objectData;
        } catch (AmazonServiceException ase) {
            logAmazonServiceException(ase);
        } catch (AmazonClientException ace) {
            logAmazonClientException(ace);
        }
        return null;
    }

    private void logAmazonServiceException(AmazonServiceException ase) {
        logger.info("Caught an AmazonServiceException, which" +
                " means your request made it to Amazon S3, but was rejected with an error response" +
                " for some reason." +
                "\nError Message:    " + ase.getMessage() +
                "\nHTTP Status Code: " + ase.getStatusCode() +
                "\nAWS Error Code:   " + ase.getErrorCode() +
                "\nError Type:       " + ase.getErrorType() +
                "\nRequest ID:       " + ase.getRequestId());
    }

    private void logAmazonClientException(AmazonClientException ace) {
        logger.error("Caught an AmazonClientException, which means" +
                " the client encountered " +
                "an internal error while trying to " +
                "communicate with S3, " +
                "such as not being able to access the network." +
                "\nError Message: " + ace.getMessage());
    }

}
