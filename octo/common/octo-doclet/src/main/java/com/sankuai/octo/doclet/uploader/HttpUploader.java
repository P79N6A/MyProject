package com.sankuai.octo.doclet.uploader;

import com.sankuai.octo.doclet.doc.OctoMethodDoc;
import com.sankuai.octo.doclet.doc.OctoTypeDoc;
import com.sankuai.octo.doclet.util.HttpUtil;
import com.sankuai.octo.doclet.util.JsonUtil;

import java.util.Collection;
import java.util.Map;

public class HttpUploader {
    private static final String OCTO_HOST = "http://octo.sankuai.com";

    public String uploadDocs(Map<String, Collection<OctoMethodDoc>> docs) {
        String url = OCTO_HOST + "/api/docs";
        return HttpUtil.post(url, JsonUtil.toJson(docs));
    }

    public String uploadTypes(Map<String, Collection<OctoTypeDoc>> types) {
        String url = OCTO_HOST + "/api/types";
        return HttpUtil.post(url, JsonUtil.toJson(types));
    }
}
