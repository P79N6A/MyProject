package com.sankuai.octo.errorlog.model;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * @author yangguo03
 * @version 1.0
 * @created 13-11-29
 */
public class ErrorLogParsedFilter extends ErrorLogFilter {
    private Map<String, List<Pattern>> parsedRules;


    public Map<String, List<Pattern>> getParsedRules() {
        return parsedRules;
    }

    public void setParsedRules(Map<String, List<Pattern>> parsedRules) {
        this.parsedRules = parsedRules;
    }
}
