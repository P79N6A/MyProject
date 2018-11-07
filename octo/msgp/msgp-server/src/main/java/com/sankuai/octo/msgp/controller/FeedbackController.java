package com.sankuai.octo.msgp.controller;

import com.sankuai.octo.mworth.common.model.Worth;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by yves on 17/1/6.
 */

@Controller
@RequestMapping("/more")
public class FeedbackController {
    @Worth( model = Worth.Model.OTHER,function = "使用帮助")
    @RequestMapping(value = "doc", method = RequestMethod.GET)
    public String doc(HttpServletRequest request) {
        return "more/doc";
    }

}
