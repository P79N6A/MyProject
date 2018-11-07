package com.sankuai.octo.test.web;

import com.sankuai.octo.test.utils.NetHelper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class HomeController {

    @RequestMapping("/")
    public String home(Model model) {
        model.addAttribute("host", NetHelper.getLocalHostName());
        return "home/index";
    }
}
