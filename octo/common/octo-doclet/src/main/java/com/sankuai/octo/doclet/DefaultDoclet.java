package com.sankuai.octo.doclet;

import com.sankuai.octo.doclet.convertor.OctoDocConvertor;
import com.sankuai.octo.doclet.doc.OctoMethodDoc;
import com.sankuai.octo.doclet.doc.OctoTypeDoc;
import com.sankuai.octo.doclet.uploader.HttpUploader;
import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.Doclet;
import com.sun.javadoc.LanguageVersion;
import com.sun.javadoc.RootDoc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Map;

public class DefaultDoclet extends Doclet {
    private static final Logger LOG = LoggerFactory.getLogger(DefaultDoclet.class);
    private static HttpUploader uploader = new HttpUploader();

    public static boolean start(RootDoc root) {
        OctoDocConvertor octoDocConvertor = new OctoDocConvertor();
        for (ClassDoc classDoc : root.classes()) {
            octoDocConvertor.handleClassDoc(classDoc);
        }
        Map<String, Collection<OctoMethodDoc>> appDocs = octoDocConvertor.getAppDocs();
        if (!appDocs.isEmpty()) {
            uploader.uploadDocs(appDocs);
            for (String appkey : appDocs.keySet()) {
                LOG.info("upload {} docs count {}", new Object[]{appkey, appDocs.get(appkey).size()});
            }
        }
        Map<String, Collection<OctoTypeDoc>> appTypes = octoDocConvertor.getAppTypes();
        if (!appTypes.isEmpty()) {
            uploader.uploadTypes(appTypes);
            for (String appkey : appTypes.keySet()) {
                LOG.info("upload {} types count {}", new Object[]{appkey, appTypes.get(appkey).size()});
            }
        }
        return true;
    }

    public static LanguageVersion languageVersion() {
        return LanguageVersion.JAVA_1_5;
    }

//    public static void main(String[] args) {
//        String[] docArgs = new String[]{"-doclet", DefaultDoclet.class.getName(),
//                System.getProperty("user.dir") + "/../octo-doclet-demo/src/main/java/com/sankuai/octo/demo/UserService.java"};
//        com.sun.tools.javadoc.Main.execute(docArgs);
//    }
}