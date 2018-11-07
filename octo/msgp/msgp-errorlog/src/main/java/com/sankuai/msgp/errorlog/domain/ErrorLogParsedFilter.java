package com.sankuai.msgp.errorlog.domain;

import com.sankuai.msgp.errorlog.dao.*;
import com.sankuai.msgp.errorlog.pojo.ErrorLogFilter;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

 
public class ErrorLogParsedFilter extends ErrorLogFilter {
    private Map<String, List<Pattern>> parsedRules;


    public Map<String, List<Pattern>> getParsedRules() {
        return parsedRules;
    }

    public void setParsedRules(Map<String, List<Pattern>> parsedRules) {
        this.parsedRules = parsedRules;
    }
}
