package com.meituan.service.mobile.mtthrift.util;

import com.facebook.swift.generator.swift2thrift.Swift2ThriftGenerator;
import com.facebook.swift.generator.swift2thrift.Swift2ThriftGeneratorConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Annotation2IdlUtil {

    private final static Logger logger = LoggerFactory.getLogger(Annotation2IdlUtil.class);

    public static void main(final String... args) {

        //args[0]:包名  args[1]:service注解类名  args[...]:其他需要的注解类名
        if (args.length < 2) {
            logger.error("Error: Annotation2IdlUtil need more args!");
            return;
        }

        String output = new File(System.getProperty("user.dir")+"/src/main/resources", args[1]+".thrift").getAbsolutePath();
        Map<String, String> map = new HashMap<String, String>();
        Swift2ThriftGeneratorConfig config = Swift2ThriftGeneratorConfig.builder()
                .outputFile(new File(output))
                .allowMultiplePackages(args[0])
                .defaultPackage(args[0])
                .includeMap(map)
                .verbose(true)
                .build();

        List<String> list = new ArrayList<String>();
        int length = args.length;
        for(int i = 1; i < length; i++)
            list.add(args[i]);

        try {
            new Swift2ThriftGenerator(config).parse(list);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

}
