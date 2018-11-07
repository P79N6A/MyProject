package com.sankuai.meituan.config.web;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.sankuai.meituan.config.model.APIResponse;
import com.sankuai.meituan.config.service.UserService;
import com.sankuai.meituan.filter.util.UserUtils;
import com.sankuai.meituan.org.remote.vo.EmpSimpleVo;
import com.sankuai.meituan.org.remote.vo.OrgTreeNodeVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author yangguo03
 * @version 1.0
 * @created 14-5-7
 */
@Controller
@RequestMapping("/config")
public class UserController {
    private static final Logger LOG = LoggerFactory.getLogger(UserController.class);

    @Resource
    private UserService userService;

    @RequestMapping(value="/user/admin", method=RequestMethod.GET)
    public String userAdmin() {
        return "config/user";
    }

    @RequestMapping(value="/space/{spacename}/admins", method=RequestMethod.GET)
    public String spaceAdmin() {
        return "config/user";
    }

	@RequestMapping(value = "/employee", method = RequestMethod.GET)
	@ResponseBody
	public APIResponse queryEmployee(String q) {
        List<OrgTreeNodeVo> searchEmpList = userService.empListSearch(q);
        return APIResponse.newResponse(true, Lists.newArrayList(Iterables.transform(searchEmpList, new Function<OrgTreeNodeVo, EmpSimpleVo>() {
            @Override
            public EmpSimpleVo apply(OrgTreeNodeVo input) {
                EmpSimpleVo empSimpleVo = new EmpSimpleVo();
                empSimpleVo.setId(input.getDataId());
                empSimpleVo.setName(input.getName());
                empSimpleVo.setLogin(input.getEnName());
                return empSimpleVo;
            }
        })));
    }

    @RequestMapping(value = "/user/admin/get", method = RequestMethod.GET)
    @ResponseBody
    public APIResponse getAdmins() {
        return APIResponse.newResponse(userService.getConfigAdmins());
    }

    @RequestMapping(value = "/user/admin/add", method = RequestMethod.POST)
    @ResponseBody
    public APIResponse addAdmin(Integer id) {
        LOG.info("{} add admin {}", UserUtils.getUser().getId(), id);
        String msg = userService.addConfigAdmin(id);
        if (msg == null) {
            return APIResponse.newResponse(true);
        }
        return APIResponse.newResponse(false).withErrorMessage(msg);
    }

    @RequestMapping(value = "/user/admin/delete", method = RequestMethod.POST)
    @ResponseBody
    public APIResponse deleteAdmin(Integer id) {
        LOG.info("{} delete admin {}", UserUtils.getUser().getId(), id);
        String msg = userService.deleteConfigAdmin(id);
        if (msg == null) {
            return APIResponse.newResponse(true);
        }
        return APIResponse.newResponse(false).withErrorMessage(msg);
    }

    @RequestMapping(value = "/space/{spaceName}/user/get", method = RequestMethod.GET)
    @ResponseBody
    public APIResponse getSpaceAdmins(@PathVariable("spaceName") String spaceName) {
        return APIResponse.newResponse(userService.getSpaceAdmins(spaceName));
    }

    @RequestMapping(value = "/space/{spaceName}/user/add", method = RequestMethod.POST)
    @ResponseBody
    public APIResponse addSpaceAdmin(@PathVariable("spaceName") String spaceName, Integer id) {
        LOG.info("{} add admin {} to space {}", new Object[] {UserUtils.getUser().getId(), id, spaceName});
        String msg = userService.addSpaceAdmin(spaceName, id);
        if (msg == null) {
            return APIResponse.newResponse(true);
        }
        return APIResponse.newResponse(false).withErrorMessage(msg);
    }

    @RequestMapping(value = "/space/{spaceName}/user/delete", method = RequestMethod.POST)
    @ResponseBody
    public APIResponse deleteSpaceAdmin(@PathVariable("spaceName") String spaceName, Integer id) {
        LOG.info("{} delete admin {} of space {}", new Object[] {UserUtils.getUser().getId(), id, spaceName});
        String msg = userService.deleteSpaceAdmin(spaceName, id);
        if (msg == null) {
            return APIResponse.newResponse(true);
        }
        return APIResponse.newResponse(false).withErrorMessage(msg);
    }

}
