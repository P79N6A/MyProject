package com.sankuai.octo.msgp.service;
import com.sankuai.inf.octo.mns.util.ProcessInfoUtil;
import com.sankuai.meituan.config.listener.IConfigChangeListener;
import com.sankuai.msgp.common.config.MnsapiConfig;
import com.sankuai.msgp.common.config.db.msgp.Tables;
import com.sankuai.octo.msgp.dao.self.HttpAuthDao;
import com.sankuai.octo.msgp.domain.HttpAuthItemNode;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
/**
 * @author uu
 * @description
 * @date Created in 11:53 2018/5/2
 * @modified
 */
public class BootstrapService {
    private static final Logger log = LoggerFactory.getLogger(BootstrapService.class);
    private static final String defaultIP = ProcessInfoUtil.isLocalHostOnline() ? "10.5.204.179" : "10.21.60.133";
    private static final Map<String, HttpAuthItemNode> userTokenMap = new ConcurrentHashMap<String, HttpAuthItemNode>();
    static{
        //将http接口鉴权的内容一次性加载到内存
        initTokenMap();
    }

    private static void initTokenMap() {
        loadUserTokenMap();
        MnsapiConfig.addListener("refreshHttpAuth", new IConfigChangeListener() {
            @Override
            public void changed(String s, String s1, String s2) {
                if (null != s && null != s2) {
                    loadUserTokenMap();
                }
            }
        });
    }

    private static void loadUserTokenMap() {
        List<Tables.MnsapiAuthRow> items = scala.collection.JavaConversions.seqAsJavaList(HttpAuthDao.getAllItems());
        log.info("old http auth info = {}", userTokenMap.toString());
        if (null != items) {
            for (Tables.MnsapiAuthRow item : items) {
                String[] patterns = item.appkeyPattern().split("\n");
                List<String> patternList = new ArrayList<String>();
                if (null != patterns && patterns.length > 0) {
                    for (String pattern: patterns) {
                        patternList.add(pattern.trim());
                    }
                }
                HttpAuthItemNode node = new HttpAuthItemNode(item.id(), item.username(), item.token(), patternList, item.owtPattern());
                if (!StringUtils.isEmpty(item.username())) {
                    userTokenMap.put(item.username().trim(), node);
                }
            }
        }
        log.info("new http auth info = {}", userTokenMap.toString());
    }

    public static Map<String, HttpAuthItemNode> getUserTokenMap() {
        return userTokenMap;
    }
}