package com.sankuai.inf.octo.mns;

@Deprecated
public class FileUtil {
    private FileUtil() {
    }

    public static String readStringFromFile(String path) {
        return com.sankuai.inf.octo.mns.util.FileUtil.readStringFromFile(path);
    }

    public static void writeString2File(String path, String content) {
        com.sankuai.inf.octo.mns.util.FileUtil.writeString2File(path, content);
    }


}
