package com.meituan.service.mobile.thrift.service;

import com.meituan.service.mobile.thrift.domain.IdlVersionDO;
import com.meituan.service.mobile.thrift.manager.IdlVersionManager;
import com.meituan.service.mobile.thrift.param.Settings;
import com.meituan.service.mobile.thrift.utils.CommonFunc;
import com.meituan.service.mobile.thrift.utils.ShellProcess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Copyright (C) 2015 Meituan
 * All rights reserved
 * User: gaosheng
 * Date: 15-3-9
 * Time: 下午6:06
 */
public class IdlVersionService {

    private static final Logger logger = LoggerFactory.getLogger(IdlVersionService.class);

    private String timestamp;
    private String appkey;
    private String uid;
    private String filename;

    private String beforeFile;
    private String currentFile;

    private Settings settings;              //路径相关设置
    private IdlVersionManager idlVersionManager;

    public IdlVersionService(String timestamp, String appkey, String uid, String filename, Settings settings, IdlVersionManager idlVersionManager) {
        this.timestamp = timestamp;
        this.appkey = appkey;
        this.uid = uid;
        this.filename = filename;
        this.settings = settings;
        this.idlVersionManager = idlVersionManager;
        this.beforeFile = this.settings.getCommonDir() + "AppKeyFile/" + appkey + "/IDL.thrift";
        this.currentFile = this.settings.getCommonDir() + timestamp + "/" + this.filename;
    }


    /**
     * 检查与之前版本IDL文件的异同
     * @return 若数据库不存在有关该appkey的记录直接返回ok，有则返回diff比较的结果
     */
    public String checkDiff() {

        int num = idlVersionManager.existAppKey(appkey);

        //存在AppKey,进行diff操作,暂不考虑include的文件,返回两个文件的不同点
        if (num > 0) {

            //beforeFile之前版本，currentFile当前版本
            String diff = "diff -uwB " + beforeFile + " " + currentFile;

            Process process = null;
            try {
                process = Runtime.getRuntime().exec(diff);
                process.waitFor();
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
            logger.info(diff);

            //读取输出
            InputStreamReader isr = new InputStreamReader(process.getInputStream());
            BufferedReader br = new BufferedReader(isr);
            String line;
            StringBuffer sb = new StringBuffer();
            try {
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                    sb.append("\n");
                }
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }
            String result = sb.toString();
            return result;
        }
        //不存在AppKey,直接返回ok
        return "ok";
    }

    public void addIdlVersion(int version, String remark) {

        IdlVersionDO idlVersionDO = new IdlVersionDO();
        idlVersionDO.setAppkey(appkey);
        idlVersionDO.setUid(uid);
        idlVersionDO.setFile(timestamp);
        idlVersionDO.setVersion(version);
        idlVersionDO.setRemark(remark);

        String content = CommonFunc.readFile(currentFile);
        idlVersionDO.setContent(content);
        idlVersionManager.addIdlVersion(idlVersionDO);
        addAppKeyFile();
    }

    public void addAppKeyFile() {
        File file = new File(settings.getCommonDir() + "AppKeyFile/" + appkey);
        if (file.exists()) {
            String rm = "rm -rf " + settings.getCommonDir() + "AppKeyFile/" + appkey;
            ShellProcess.runShell(rm);
        }
        String mkdir = "mkdir " + settings.getCommonDir() + "AppKeyFile/" + appkey;
        ShellProcess.runShell(mkdir);
        String cp = "cp -r " + currentFile + " " + settings.getCommonDir() + "AppKeyFile/" + appkey + "/IDL.thrift";
        ShellProcess.runShell(cp);
    }

    public int getVersion() {
        return idlVersionManager.getVersionByAppkey(appkey);
    }
}
