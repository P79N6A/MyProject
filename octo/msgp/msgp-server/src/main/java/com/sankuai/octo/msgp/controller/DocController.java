package com.sankuai.octo.msgp.controller;

import com.sankuai.meituan.common.io.IOUtils;
import com.sankuai.msgp.common.utils.helper.JsonHelper;
import com.sankuai.octo.msgp.serivce.doc.DocParser;
import com.sankuai.octo.msgp.serivce.service.ServiceCommon;
import com.sankuai.octo.mworth.common.model.Worth;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;

@Controller
@RequestMapping("/")
public class DocController {
    private static final Logger LOG = LoggerFactory.getLogger(DocController.class);

    @Worth( model = Worth.Model.DOC,function = "更新文档")
    @RequestMapping(value = "api/docs", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String uploadDocs(HttpServletRequest request) {
        try {
            String text = IOUtils.copyToString(request.getReader());
            return DocParser.parseDocs(text);
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
            return JsonHelper.errorJson(e.getMessage());
        }
    }

    @RequestMapping(value = "api/types", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String uploadTypes(HttpServletRequest request) {
        try {
            String text = IOUtils.copyToString(request.getReader());
            return DocParser.parseTypes(text);
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
            return JsonHelper.errorJson(e.getMessage());
        }
    }

}