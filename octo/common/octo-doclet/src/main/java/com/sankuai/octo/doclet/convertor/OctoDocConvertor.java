package com.sankuai.octo.doclet.convertor;

import com.sankuai.octo.doclet.doc.OctoClassDoc;
import com.sankuai.octo.doclet.doc.OctoMethodDoc;
import com.sankuai.octo.doclet.doc.OctoTypeDoc;
import com.sankuai.octo.doclet.util.StringUtil;
import com.sun.javadoc.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class OctoDocConvertor {
    private static final Logger LOG = LoggerFactory.getLogger(OctoDocConvertor.class);
    private static final String OCTO_TAG_APPKEY = "@octo.appkey";
    private static final String OCTO_TAG_API = "@api";
    private static final String OCTO_TAG_GROUP = "@group";
    private static final String OCTO_TAG_NAME = "@name";
    private static final String OCTO_TAG_DESC = "@desc";
    private static final String OCTO_TAG_PARAM = "@param";
    private static final String OCTO_TAG_RETURN = "@return";
    private static final String OCTO_TAG_THROWS = "@throws";
    private static final String OCTO_TAG_PERMISSION = "@permission";
    private static final String OCTO_TAG_STATUS = "@status";
    private static final String OCTO_TAG_VERSION = "@version";
    private static final String OCTO_TAG_LINK = "@link";
    private static final String OCTO_TAG_AUTHOR = "@author";

    // check docs duplicate appkey + api, warn application
    private static final Map<String, Map<String, OctoMethodDoc>> methodDocs = new HashMap<String, Map<String, OctoMethodDoc>>();
    // different appkey for types;
    private static final Map<String, Map<String, OctoTypeDoc>> typeDocs = new HashMap<String, Map<String, OctoTypeDoc>>();

    public void handleClassDoc(ClassDoc classDoc) {
        OctoClassDoc octoClassDoc = getClassDoc(classDoc, classDoc.tags());
        for (MethodDoc methodDoc : classDoc.methods()) {
            OctoMethodDoc octoMethodDoc = getMethodDoc(octoClassDoc, classDoc, methodDoc);
            if (octoMethodDoc != null) {
                addMethodDoc(octoMethodDoc);
            }
        }
        LOG.debug("after {} find docs {} types {}", new Object[]{classDoc.name(), methodDocs.size(), typeDocs.size()});
    }

    private void addMethodDoc(OctoMethodDoc doc) {
        Map<String, OctoMethodDoc> apis = methodDocs.get(doc.getAppkey());
        if (apis == null) {
            apis = new HashMap<String, OctoMethodDoc>();
            methodDocs.put(doc.getAppkey(), apis);
        }
        if (apis.containsKey(doc.getApi())) {
            OctoMethodDoc dupDoc = apis.get(doc.getApi());
            LOG.warn("api {}duplicate with {}", new Object[]{doc, dupDoc});
        } else {
            apis.put(doc.getApi(), doc);
        }
    }

    private static void addTypes(String appkey, OctoTypeDoc typeDoc) {
        Map<String, OctoTypeDoc> types = typeDocs.get(appkey);
        if (types == null) {
            types = new HashMap<String, OctoTypeDoc>();
            typeDocs.put(appkey, types);
        }
        types.put(typeDoc.getType(), typeDoc);
    }

    public Map<String, Collection<OctoMethodDoc>> getAppDocs() {
        Map<String, Collection<OctoMethodDoc>> appDocs = new HashMap<String, Collection<OctoMethodDoc>>();
        for (String appkey : methodDocs.keySet()) {
            appDocs.put(appkey, methodDocs.get(appkey).values());
        }
        return appDocs;
    }

    public Map<String, Collection<OctoTypeDoc>> getAppTypes() {
        Map<String, Collection<OctoTypeDoc>> appTypes = new HashMap<String, Collection<OctoTypeDoc>>();
        for (String appkey : typeDocs.keySet()) {
            appTypes.put(appkey, typeDocs.get(appkey).values());
        }
        return appTypes;
    }

    private OctoMethodDoc getMethodDoc(OctoClassDoc octoClassDoc, ClassDoc classDoc, MethodDoc methodDoc) {
        if (!methodDoc.isPublic()) {
            return null;
        }
        if (octoClassDoc == null) {
            octoClassDoc = getClassDoc(classDoc, methodDoc.tags());
            if (octoClassDoc == null) {
                return null;
            }
        }
        // init field from octoClassDoc，then override by methodDoc
        OctoMethodDoc octoMethodDoc = initMethodDoc(octoClassDoc, classDoc, methodDoc);
        if (octoMethodDoc != null) {
            return octoMethodDoc;
        }
        return null;
    }

    private OctoClassDoc getClassDoc(ClassDoc classDoc, Tag[] tags) {
        String appkey = getFirstTagText(tags, OCTO_TAG_APPKEY);
        if (StringUtil.isBlank(appkey)) {
            return null;
        }
        OctoClassDoc octoClassDoc = new OctoClassDoc(appkey);
        String group = getFirstTagText(tags, OCTO_TAG_GROUP);
        if (StringUtil.isBlank(group)) {
            group = classDoc.name().replaceAll("\\.Iface", "");;
        }
        octoClassDoc.setGroup(group);
        octoClassDoc.setPermission(getFirstTagText(tags, OCTO_TAG_PERMISSION));
        octoClassDoc.setStatus(getFirstTagText(tags, OCTO_TAG_STATUS));
        octoClassDoc.setVersion(getFirstTagText(tags, OCTO_TAG_VERSION));
        octoClassDoc.setLink(getFirstTagText(tags, OCTO_TAG_LINK));
        octoClassDoc.setAuthor(getFirstTagText(tags, OCTO_TAG_AUTHOR));
        return octoClassDoc;
    }

    private OctoMethodDoc initMethodDoc(OctoClassDoc octoClassDoc, ClassDoc classDoc, MethodDoc methodDoc) {
        String api = getFirstTagText(methodDoc.tags(), OCTO_TAG_API);
        if (StringUtil.isBlank(api)) {
            // default use className.methodName as api
            api = (classDoc.name() + "." + methodDoc.name()).replaceAll("\\.Iface", "");
        }
        OctoMethodDoc octoMethodDoc = new OctoMethodDoc(octoClassDoc, api);
        String group = getFirstTagText(methodDoc.tags(), OCTO_TAG_GROUP);
        if (StringUtil.isNotBlank(group)) {
            octoMethodDoc.setGroup(group);
        }
        String permission = getFirstTagText(methodDoc.tags(), OCTO_TAG_PERMISSION);
        if (StringUtil.isNotBlank(permission)) {
            octoMethodDoc.setPermission(permission);
        }
        String use = getFirstTagText(methodDoc.tags(), OCTO_TAG_STATUS);
        if (StringUtil.isNotBlank(use)) {
            octoMethodDoc.setStatus(use);
        }
        String version = getFirstTagText(methodDoc.tags(), OCTO_TAG_VERSION);
        if (StringUtil.isNotBlank(version)) {
            octoMethodDoc.setVersion(version);
        }
        String link = getFirstTagText(methodDoc.tags(), OCTO_TAG_LINK);
        if (StringUtil.isNotBlank(link)) {
            octoMethodDoc.setLink(link);
        }
        String author = getFirstTagText(methodDoc.tags(), OCTO_TAG_AUTHOR);
        if (StringUtil.isNotBlank(author)) {
            octoMethodDoc.setAuthor(author);
        }
        octoMethodDoc.setName(getFirstTagText(methodDoc.tags(), OCTO_TAG_NAME));
        octoMethodDoc.setDesc(getFirstTagText(methodDoc.tags(), OCTO_TAG_DESC));
        octoMethodDoc.setParams(getParams(octoMethodDoc.getAppkey(), methodDoc));
        octoMethodDoc.setResult(getSuccess(octoMethodDoc.getAppkey(), methodDoc));
        octoMethodDoc.setExceptions(getExceptions(octoMethodDoc.getAppkey(), methodDoc));
        return octoMethodDoc;
    }

    private Map<String, OctoTypeDoc> getParams(String appkey, MethodDoc methodDoc) {
        Map<String, OctoTypeDoc> map = new LinkedHashMap<String, OctoTypeDoc>();
        for (Parameter p : methodDoc.parameters()) {
            OctoTypeDoc octoTypeDoc = formatTypeDoc(appkey, p.type());
            map.put(p.name(), octoTypeDoc);
        }
        Tag[] tags = methodDoc.tags(OCTO_TAG_PARAM);
        if (tags.length > 0) {
            for (Tag tag : tags) {
                String text = tag.text();
                if (StringUtil.isNotBlank(text)) {
                    Integer flag = text.indexOf(" ");
                    if (flag > 0) {
                        String paramName = text.substring(0, flag);
                        String paramDesc = text.substring(flag);
                        OctoTypeDoc octoTypeDoc = map.get(paramName);
                        if (octoTypeDoc != null && StringUtil.isNotBlank(paramDesc)) {
                            octoTypeDoc.setCommentText(paramDesc);
                        }
                    }
                }
            }
        }
        return map;
    }

    private OctoTypeDoc getSuccess(String appkey, MethodDoc methodDoc) {
        OctoTypeDoc octoTypeDoc = formatTypeDoc(appkey, methodDoc.returnType());
        Tag[] tags = methodDoc.tags(OCTO_TAG_RETURN);
        String desc = getFirstTagText(tags, OCTO_TAG_RETURN);
        if (StringUtil.isNotBlank(desc)) {
            octoTypeDoc.setCommentText(desc);
        }
        return octoTypeDoc;
    }

    private static OctoTypeDoc formatTypeDoc(String appkey, Type type) {
        OctoTypeDoc octoTypeDoc = new OctoTypeDoc();
        octoTypeDoc.setType(type.qualifiedTypeName());
        octoTypeDoc.setSimpleType(type.simpleTypeName());
        // 容器及泛型
        ParameterizedType paramType = type.asParameterizedType();
        if (paramType != null) {
            Type[] args = paramType.typeArguments();
            for (Type arg : args) {
                OctoTypeDoc argDoc = formatTypeDoc(appkey, arg);
                octoTypeDoc.putParamType(argDoc);
            }
        }
        if (isExcludeType(type)) {
            return octoTypeDoc;
        } else {
            ClassDoc classDoc = type.asClassDoc();
            octoTypeDoc.setCommentText(classDoc.commentText());
            for (FieldDoc fd : classDoc.fields(false)) {
                if (!isExcludeField(fd)) {
                    OctoTypeDoc fieldDoc = formatTypeDoc(appkey, fd.type());
                    if (StringUtil.isNotBlank(fd.commentText())) {
                        fieldDoc.setCommentText(fd.commentText());
                    }
                    octoTypeDoc.putField(fd.name(), fieldDoc);
                }
            }
            addTypes(appkey, octoTypeDoc);
        }
        return octoTypeDoc;
    }

    private Map<String, OctoTypeDoc> getExceptions(String appkey, MethodDoc methodDoc) {
        Map<String, String> exceptionDescMap = new HashMap<String, String>();
        for (Tag tag : methodDoc.tags(OCTO_TAG_THROWS)) {
            int indexOf = tag.text().indexOf(" ");
            if (indexOf > 0) {
                // could be simple name or quli name
                String name = tag.text().substring(0, indexOf);
                exceptionDescMap.put(name, tag.text().substring(indexOf + 1));
            }
        }
        Map<String, OctoTypeDoc> map = new LinkedHashMap<String, OctoTypeDoc>();
        Type[] exceptions = methodDoc.thrownExceptionTypes();
        for (Type type : exceptions) {
            OctoTypeDoc octoTypeDoc = formatTypeDoc(appkey, type);
            String desc = getDesc(exceptionDescMap, octoTypeDoc);
            if (StringUtil.isNotBlank(desc)) {
                octoTypeDoc.setCommentText(desc);
            }
            map.put(octoTypeDoc.getSimpleType(), octoTypeDoc);
        }
        return map;
    }

    private String getDesc(Map<String, String> exceptionDescMap, OctoTypeDoc octoTypeDoc) {
        String desc = exceptionDescMap.get(octoTypeDoc.getType());
        if (StringUtil.isBlank(desc)) {
            desc = exceptionDescMap.get(octoTypeDoc.getSimpleType());
        }
        return desc;
    }

    private static boolean isExcludeType(Type type) {
        ClassDoc classDoc = type.asClassDoc();
        return type.isPrimitive() || type.qualifiedTypeName().startsWith("java.") || classDoc == null;
    }

    private static boolean isExcludeField(FieldDoc fd) {
        return fd.isStatic() || fd.name().equalsIgnoreCase("__isset_bit_vector");
    }

    private static String getFirstTagText(Tag[] tags, String tagName) {
        for (Tag tag : tags) {
            if ((tagName).equalsIgnoreCase(tag.name())) {
                return tag.text();
            }
        }
        return null;
    }
}
