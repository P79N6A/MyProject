package com.sankuai.meituan.config.web;

import com.sankuai.meituan.common.io.IOUtils;
import com.sankuai.meituan.config.anno.OperationRecord;
import com.sankuai.meituan.config.model.APIResponse;
import com.sankuai.meituan.config.service.SpaceConfigService;
import com.sankuai.meituan.config.util.ZKPathBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Map;

@Controller
@RequestMapping("/config")
public class SettingController {
    private static final Logger logger = LoggerFactory.getLogger(SettingController.class);

    @Resource
    private SpaceConfigService spaceConfigService;

    @RequestMapping(value = "/spaces/{spaceName}/settings", method = RequestMethod.GET)
    public ModelAndView spaceSetting(@PathVariable String spaceName) {
        Map<String, String> spaceConfig = spaceConfigService.getAll(spaceName);
        return new ModelAndView("config/settings").addAllObjects(spaceConfig).addObject("spaceName", spaceName);
    }

    @OperationRecord(type = "updateSpace", desc = "修改配置空间配置")
    @RequestMapping(value = "/spaces/{spaceName}/settings/set/third_level/{canUseThirdLevel}", method = RequestMethod.GET)
    public ModelAndView setThirdLevel(@PathVariable String spaceName, @PathVariable boolean canUseThirdLevel) {
        String spacePath = ZKPathBuilder.newBuilder().appendSpace(spaceName).toPath();
        spaceConfigService.setCanUseThirdLevel(spacePath, canUseThirdLevel);
        return new ModelAndView(new RedirectView("/"));
    }

    @RequestMapping(value = "/spaces/{spaceName}/settings/data", method = RequestMethod.GET)
    @ResponseBody
    public APIResponse getConfigData(@PathVariable("spaceName") String spaceName) {
        String spacePath = ZKPathBuilder.newBuilder().appendSpace(spaceName).toPath();
        return APIResponse.newResponse(true,spaceConfigService.getData(spacePath));
    }

    @RequestMapping(value = "/spaces/{spaceName}/settings/update", method = RequestMethod.POST)
    @ResponseBody
    public APIResponse updateSettings(@PathVariable("spaceName") String spaceName,
                                  HttpServletRequest request) {
        APIResponse ret = null;
        try{
            String json = IOUtils.copyToString(request.getReader());
            String spacePath = ZKPathBuilder.newBuilder().appendSpace(spaceName).toPath();
            ret = APIResponse.newResponse(spaceConfigService.setConfig(spacePath,json));
        }catch (IOException e){
            logger.debug("invalid parameter",e);
            ret= APIResponse.newResponse(false,"invalid parameter");
        }
        return ret;
    }
}
