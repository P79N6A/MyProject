package com.sankuai.meituan.config.util;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class StreamUtil {
    public static String getLine(BufferedInputStream bufferedInputStream) {
        StringBuilder result = new StringBuilder();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(bufferedInputStream));
        try {
            String line = null;
            while ((line = bufferedReader.readLine()) != null)
                result.append(line);
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
        finally {
            try {
                bufferedReader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result.toString();
    }
}
