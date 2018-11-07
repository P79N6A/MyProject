package com.sankuai.octo.msgp.utils;

import com.sankuai.inf.octo.mns.util.ProcessInfoUtil;
import com.sankuai.meituan.auth.util.UserUtils;
import com.sankuai.msgp.common.utils.client.Messager;
import com.sankuai.octo.config.model.PRDetail;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class XMUtil {

    public static boolean isOnline(){
        return ProcessInfoUtil.isLocalHostOnline();
    }

    public static String getOctoUrl() {
        return isOnline() ? "http://octo.sankuai.com/" : "http://octo.test.sankuai.info/";
    }

    public static void sendMessage(String prID,String appkey,List<PRDetail> list,List<PRDetail> dbPRDetail){
        try {
            List<PRDetail> keys = new ArrayList<>(list);
            for(int i = 0; i < list.size(); i++){
                for(int j = 0; j < dbPRDetail.size(); j++){
                    if(list.get(i).key.equals(dbPRDetail.get(j).key) && list.get(i).newValue.equals(dbPRDetail.get(j).newValue) && list.get(i).newComment.equals(dbPRDetail.get(j).newComment)){
                        keys.remove(list.get(i));
                        dbPRDetail.remove(dbPRDetail.get(j));
                        break;
                    }
                }
            }

            StringBuilder sb = new StringBuilder();
            Date date = new Date();
            DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            sb.append(format.format(date))
                    .append("\nMCC Pull Request Update!(")
                    .append(XMUtil.isOnline() ? "线上" : "线下")
                    .append(")\n发起用户：")
                    .append(UserUtils.getUser().getLogin() + "\n");
            if(keys.size() != 0){
                for(int i = 0; i < keys.size(); i++){
                    sb.append("下列KEY的内容已经重新修改：\n")
                            .append(keys.get(i).getKey() + "\n");
                }
            }else{
                for(int i = 0; i < dbPRDetail.size(); i++){
                    sb.append("下列KEY的内容已经删除：\n")
                            .append(dbPRDetail.get(i).getKey() + "\n");
                }
            }
                    sb.append("邀请您审阅：[进入MCC|");
            String domain = XMUtil.getOctoUrl();

            sb.append(domain)
                    .append("serverOpt/operation?appkey=")
                    .append("com.sankuai.octo.tmy")
                    .append("&env=")
                    .append("&ismerge=0")
                    .append("&prID=")
                    .append(prID)
                    .append("#config")
                    .append("] PR ID#")
                    .append(prID)
                    .append("\n点击【Review管理】查看\n");
            String tempStr = sb.toString();
            Messager.sendXMAlarmByAppkey(appkey,tempStr);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
