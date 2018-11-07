package com.sankuai.octo.msgp.utils;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Map;

/**
 * Created by zava on 16/4/28.
 */
@Component
public class FreeMarkerTemplate {
    private static final Logger log = LoggerFactory.getLogger(FreeMarkerTemplate.class);
    @Resource
    private Configuration freemarkerConfiguration;


    public String processTemplateIntoString(String fileName, Map<String, Object> data) {
        try {
            Template t = freemarkerConfiguration.getTemplate(fileName);
            String body = FreeMarkerTemplateUtils.processTemplateIntoString(t, data);
            return body;
        } catch (IOException e) {
            log.error("error,fileName"+fileName, e);
        } catch (TemplateException e) {
            log.error("error,fileName"+fileName, e);
        }
        return null;
    }
}
