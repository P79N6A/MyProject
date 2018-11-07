package com.sankuai.meituan.config.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * @author zhangxi
 * @created 13-12-6
 */
@Controller
@RequestMapping("/")
public class HomeController {

    @RequestMapping(value = "")
    public String home() {
        return "config/home";
    }

    @RequestMapping(value = "unauthorized", method = RequestMethod.GET)
    public String unauthorized() {
        return "unauthorized/unauthorized";
    }
}
