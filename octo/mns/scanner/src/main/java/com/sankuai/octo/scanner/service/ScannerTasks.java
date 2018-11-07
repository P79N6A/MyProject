package com.sankuai.octo.scanner.service;

import com.sankuai.octo.scanner.model.report.DuplicateRegistryReport;

import java.util.HashSet;
import java.util.Map;

public class ScannerTasks {

    public static void checkDuplicateRegistry(Map<String, HashSet<String>> ipPortMap, String identifierString, String ipPort) {
        if (ipPortMap.containsKey(ipPort)) {
            HashSet<String> set = ipPortMap.get(ipPort);
            set.add(identifierString);
            ipPortMap.put(ipPort, set);
            SendReport.send(new DuplicateRegistryReport(1, "DuplicateRegistry", set.toString(), ipPort, set.size()));
        } else {
            HashSet<String> set = new HashSet<>();
            set.add(identifierString);
            ipPortMap.put(ipPort, set);
        }
    }
}
