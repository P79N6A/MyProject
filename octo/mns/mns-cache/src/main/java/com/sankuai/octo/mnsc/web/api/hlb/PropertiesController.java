package com.sankuai.octo.mnsc.web.api.hlb;

import com.sankuai.octo.mnsc.model.Env;
import com.sankuai.octo.mnsc.service.mnscService;
import com.sankuai.octo.mnsc.utils.api;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Created by lhmily on 04/28/2017.
 */
@Controller
@RequestMapping("/api/hlb/properties")
public class PropertiesController {
    @RequestMapping(value = "", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public String getPropertiesByAppkey(@RequestParam("appkey") String appkey,
                                        @RequestParam("env") String env) {
        if (StringUtils.isEmpty(appkey) || !Env.isValid(env)) {
            return api.errorJsonArgInvalid("invalid params");
        }
        return mnscService.getHttpProperties4Api(appkey, Env.strConvertEnum(env).toString());
    }
}
