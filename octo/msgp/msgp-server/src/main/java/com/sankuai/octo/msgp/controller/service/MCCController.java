package com.sankuai.octo.msgp.controller.service;

import com.sankuai.meituan.auth.util.UserUtils;
import com.sankuai.meituan.auth.vo.User;
import com.sankuai.meituan.common.io.IOUtils;
import com.sankuai.msgp.common.model.Page;
import com.sankuai.msgp.common.utils.helper.CommonHelper;
import com.sankuai.msgp.common.utils.helper.JsonHelper;
import com.sankuai.octo.config.model.PRDetail;
import com.sankuai.octo.msgp.domain.MccConfigItem;
import com.sankuai.octo.msgp.serivce.AppkeyAuth;
import com.sankuai.octo.msgp.serivce.service.ServiceConfig;
import com.sankuai.octo.msgp.utils.Auth;
import com.sankuai.octo.msgp.utils.XMGroupPushUtil;
import com.sankuai.octo.msgp.utils.XMUtil;
import com.sankuai.octo.mworth.common.model.Worth;
import net.sf.json.JSONObject;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Controller
@RequestMapping("/serverOpt")
@Auth(level = Auth.Level.READ, responseMode = Auth.ResponseMode.JSON)
@Worth(project = Worth.Project.OCTO, model = Worth.Model.MCC)
public class MCCController {
    private static final Logger LOG = LoggerFactory.getLogger(MCCController.class);

    //获取节点数据：data、children
    @RequestMapping(value = "config/space/node/get", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @Worth(model = Worth.Model.MCC, function = "查询动态配置")
    @Auth(level = Auth.Level.LOGIN, responseMode = Auth.ResponseMode.JSON)
    @ResponseBody
    public String getNode(@RequestParam("appkey") String appkey,
                          @RequestParam("nodeName") String nodeName,
                          HttpServletRequest request) {
        return ServiceConfig.getNodeData(appkey, nodeName, request.getCookies());
    }

    //新增一个节点
    @RequestMapping(value = "config/space/{appkey}/node/add", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    @Worth(model = Worth.Model.MCC, function = "修改动态配置")
    @Auth(level = Auth.Level.ADMIN, responseMode = Auth.ResponseMode.JSON)
    @ResponseBody
    public String addNode(@PathVariable("appkey") String appkey, HttpServletRequest request) {
        try {
            String json = IOUtils.copyToString(request.getReader());
            LOG.info(json);
            return ServiceConfig.addNode(appkey, json, request.getCookies());
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return JsonHelper.errorJson(e.getMessage());
        }
    }

    //删除一个节点
    @RequestMapping(value = "config/space/{appkey}/node/delete", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    @Worth(model = Worth.Model.MCC, function = "修改动态配置")
    @Auth(level = Auth.Level.ADMIN, responseMode = Auth.ResponseMode.JSON)
    @ResponseBody
    public String delNode(@PathVariable("appkey") String appkey, HttpServletRequest request) {
        try {
            String json = IOUtils.copyToString(request.getReader());
            LOG.info(json);
            return ServiceConfig.delNode(appkey, json, request.getCookies());
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return JsonHelper.errorJson(e.getMessage());
        }
    }

    //更新节点中的data
    @RequestMapping(value = "config/space/{appkey}/node/update", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    @Worth(model = Worth.Model.MCC, function = "修改动态配置")
    @Auth(level = Auth.Level.ADMIN, responseMode = Auth.ResponseMode.JSON)
    @ResponseBody
    public String updateNode(@PathVariable("appkey") String appkey,@RequestBody MccConfigItem item,
                             HttpServletRequest request) {
        try {
            String json = IOUtils.copyToString(request.getReader());
            MccConfigItem   configItem = JsonHelper.toObject(json,MccConfigItem.class);
            LOG.info(json);
            //onfigItem.isRollback()：判断是否是回滚，回滚不走pr流程；AppkeyAuth.isInSpecialOwt(appkey)：非回滚时，判断该appkey的owt是否为金融部门，是：走pr流程，否：直接修改。
            if(!configItem.isRollback() && !CommonHelper.isOffline() && StringUtils.equals("prod", ServiceConfig.getEnvByNodename(configItem.getNodeName(), configItem.getSpaceName())) && AppkeyAuth.isInSpecialOwt(appkey)){
                return JsonHelper.errorJson("该服务不支持直接修改配置，请发起PR流程。");
            }
            return ServiceConfig.updateNodeData(appkey, configItem, request.getCookies());
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return JsonHelper.errorJson(e.getMessage());
        }
    }

    /**
     * 回滚动态配置
     * @param appkey
     * @param request
     * @return
     */
    @RequestMapping(value = "config/space/{appkey}/node/configrollback", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    @Worth(model = Worth.Model.MCC, function = "回滚动态配置")
    @Auth(level = Auth.Level.ADMIN, responseMode = Auth.ResponseMode.JSON)
    @ResponseBody
    public String configRollback(@PathVariable("appkey") String appkey, HttpServletRequest request) {
        try {
            String json = IOUtils.copyToString(request.getReader());
            return ServiceConfig.configRollback(appkey, json, request.getCookies());
        } catch (IOException e) {
            return JsonHelper.errorJson("服务器异常：回滚失败");
        }
    }

    //客户端同步记录
    @RequestMapping(value = "config/space/{appkey}/node/clientsynclog", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @Worth(model = Worth.Model.MCC, function = "查看同步记录")
    @Auth(level = Auth.Level.LOGIN, responseMode = Auth.ResponseMode.JSON)
    @ResponseBody
    public String syncLog(@PathVariable("appkey") String appkey, HttpServletRequest request) {
        return ServiceConfig.syncLog(appkey, request.getQueryString(), request.getCookies());
    }

    /*操作日志*/
    //配置文件：客户端同步记录
    @RequestMapping(value = "config/filelog/{appkey}/clientsyncfilelog", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @Worth(model = Worth.Model.MCC, function = "查看配置文件的同步记录")
    @Auth(level = Auth.Level.LOGIN, responseMode = Auth.ResponseMode.JSON)
    @ResponseBody
    public String syncFileLog(@PathVariable("appkey") String appkey, HttpServletRequest request) {
        return ServiceConfig.syncFileLog(appkey, request.getQueryString(), request.getCookies());
    }


    //注册旧的appkey到mtconfig
    //TODO 执行过后删除
    @RequestMapping(value = "config/add/spaces", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @Auth(level = Auth.Level.LOGIN, responseMode = Auth.ResponseMode.JSON)
    @ResponseBody
    public String addSpaceForAllApp(HttpServletRequest request) {
        ServiceConfig.addSpaceForAllApp(request.getCookies());
        return "ok";
    }

    /**
     * 上传文件到数据库
     *
     * @param appkey
     * @param env
     * @param filename
     * @param filepath
     * @param file
     * @return
     */
    @RequestMapping(value = "{appkey}/config/file/upload", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    @Worth(model = Worth.Model.MCC, function = "修改文件内容")
    @Auth(level = Auth.Level.ADMIN, responseMode = Auth.ResponseMode.JSON)
    @ResponseBody
    public String uploadFileConfig(@PathVariable("appkey") String appkey,
                                   @RequestParam("env") int env,
                                   @RequestParam("filename") String filename,
                                   @RequestParam("filepath") String filepath,
                                   @RequestParam("groupID") String groupID,
                                   @RequestParam(value = "file", required = true) MultipartFile file) {
        try {
            InputStream fileStream = file.getInputStream();
            byte[] filecontent = IOUtils.copyToByteArray(fileStream);
            if (ArrayUtils.isEmpty(filecontent)) {
                return JsonHelper.errorJson("文件不能为空");
            }
            return ServiceConfig.uploadFileConfig(appkey, env, groupID, filename, filepath, filecontent);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return JsonHelper.errorJson("服务器异常：上传失败");
        }
    }

    /**
     * 根据appkey和env来获取文件列表
     *
     * @param appkey
     * @param env
     * @return
     */
    @RequestMapping(value = "{appkey}/config/file/filenames", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @Worth(model = Worth.Model.MCC, function = "查询文件配置")
    @Auth(level = Auth.Level.LOGIN, responseMode = Auth.ResponseMode.JSON)
    @ResponseBody
    public String getFilenameList(@PathVariable("appkey") String appkey,
                                  @RequestParam("env") int env,
                                  @RequestParam("groupID") String groupID) {
        return ServiceConfig.getFilenameList(appkey, env, groupID);
    }

    /**
     * 获取文件内容
     *
     * @param appkey
     * @param env
     * @param request
     * @return
     */
    @RequestMapping(value = "{appkey}/config/file/filecontent", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @Worth(model = Worth.Model.MCC, function = "查看文件内容")
    @Auth(level = Auth.Level.LOGIN, responseMode = Auth.ResponseMode.JSON)
    @ResponseBody
    public String getFileContent(@PathVariable("appkey") String appkey,
                                 @RequestParam("env") int env,
                                 @RequestParam("fileName") String fileName,
                                 @RequestParam("groupID") String groupID,
                                 HttpServletRequest request) {
        return ServiceConfig.getFileContent(appkey, env, fileName, groupID);
    }

    /**
     * 文件下发
     *
     * @param appkey
     * @return
     */
    @RequestMapping(value = "{appkey}/config/file/downFile2IPs", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    @Worth(model = Worth.Model.MCC, function = "下发文件配置")
    @Auth(level = Auth.Level.ADMIN, responseMode = Auth.ResponseMode.JSON)
    @ResponseBody
    public String downFileToIPs(@PathVariable(value = "appkey") String appkey,
                                HttpServletRequest request) {
        try {
            String json = IOUtils.copyToString(request.getReader());
            return ServiceConfig.pushFile(json, appkey);
        } catch (IOException e) {
            return JsonHelper.errorJson("服务器异常：文件下发失败");
        }
    }

    @RequestMapping(value = "{appkey}/config/file/saveFileContent", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    @Worth(model = Worth.Model.MCC, function = "修改文件内容")
    @Auth(level = Auth.Level.ADMIN, responseMode = Auth.ResponseMode.JSON)
    @ResponseBody
    public String saveFileContent(@PathVariable("appkey") String appkey,
                                  HttpServletRequest request) {
        try {
            String json = IOUtils.copyToString(request.getReader());
            return ServiceConfig.saveFileContent(json, appkey);
        } catch (IOException e) {
            return JsonHelper.errorJson("服务器异常：文件保存失败");
        }
    }

    @RequestMapping(value = "{appkey}/config/file/deleteFile", method = RequestMethod.DELETE, produces = "application/json;charset=utf-8")
    @Worth(model = Worth.Model.MCC, function = "删除文件")
    @Auth(level = Auth.Level.ADMIN, responseMode = Auth.ResponseMode.JSON)
    @ResponseBody
    public String deleteFile(@PathVariable("appkey") String appkey,
                             @RequestParam("env") Integer env,
                             @RequestParam("groupID") String groupID,
                             @RequestParam("fileName") String fileName,
                             HttpServletRequest request) {
        return ServiceConfig.deleteConfigFile(appkey, env, groupID, fileName) ? JsonHelper.dataJson("删除成功") : JsonHelper.errorJson("删除失败");
    }

    @RequestMapping(value = "{appkey}/config/file/groups", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @Worth(model = Worth.Model.MCC, function = "查看分组")
    @Auth(level = Auth.Level.LOGIN, responseMode = Auth.ResponseMode.JSON)
    @ResponseBody
    public String fileConfigGroups(@PathVariable("appkey") String appkey,
                                   @RequestParam("env") int env,
                                   @RequestParam("pageNo") int pageNo) {
        Page page = new Page(pageNo);
        return JsonHelper.dataJson(ServiceConfig.getFileConfigGroups(appkey, env, page), page);
    }

    @RequestMapping(value = "{appkey}/config/providerIP/all", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @Worth(model = Worth.Model.MCC, function = "查看分组")
    @Auth(level = Auth.Level.LOGIN, responseMode = Auth.ResponseMode.JSON)
    @ResponseBody
    public String allProviderIPs(@PathVariable("appkey") String appkey, @RequestParam(value = "env") int env) {
        return JsonHelper.dataJson(ServiceConfig.getAllProviderIPs(appkey, env));
    }

    @RequestMapping(value = "{appkey}/config/file/addGroup", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    @Worth(model = Worth.Model.MCC, function = "修改分组")
    @Auth(level = Auth.Level.ADMIN, responseMode = Auth.ResponseMode.JSON)
    @ResponseBody
    public String addGroup(@PathVariable("appkey") String appkey, HttpServletRequest request) {
        try {
            String json = IOUtils.copyToString(request.getReader());
            return ServiceConfig.saveGroup(appkey, json, true) ? JsonHelper.dataJson("保存成功") : JsonHelper.errorJson("保存失败");
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
            return JsonHelper.errorJson("内部异常");
        }
    }

    @RequestMapping(value = "{appkey}/config/file/updateGroup", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    @Worth(model = Worth.Model.MCC, function = "修改分组")
    @Auth(level = Auth.Level.ADMIN, responseMode = Auth.ResponseMode.JSON)
    @ResponseBody
    public String updateGroup(@PathVariable("appkey") String appkey, HttpServletRequest request) {
        try {
            String json = IOUtils.copyToString(request.getReader());
            return ServiceConfig.saveGroup(appkey, json, false) ? JsonHelper.dataJson("保存成功") : JsonHelper.errorJson("保存失败");
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
            return JsonHelper.errorJson("内部异常");
        }
    }

    @RequestMapping(value = "{appkey}/config/file/group", method = RequestMethod.DELETE, produces = "application/json;charset=utf-8")
    @Worth(model = Worth.Model.MCC, function = "修改分组")
    @Auth(level = Auth.Level.ADMIN, responseMode = Auth.ResponseMode.JSON)
    @ResponseBody
    public String delGroup(@PathVariable("appkey") String appkey,
                           @RequestParam("env") int env,
                           @RequestParam("groupID") String groupID) {
        return ServiceConfig.delFileGroup(appkey, env, groupID) ? JsonHelper.dataJson("删除成功") : JsonHelper.errorJson("删除失败");
    }

    @RequestMapping(value = "{appkey}/config/file/group", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @Worth(model = Worth.Model.MCC, function = "查看分组")
    @Auth(level = Auth.Level.LOGIN, responseMode = Auth.ResponseMode.JSON)
    @ResponseBody
    public String getGroupInfo(@PathVariable("appkey") String appkey,
                               @RequestParam("env") int env,
                               @RequestParam("groupID") String groupID) {
        return JsonHelper.dataJson(ServiceConfig.getFileGroupInfo(appkey, env, groupID));
    }

    @RequestMapping(value = "{appkey}/config/file/allGroupsIPs", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @Worth(model = Worth.Model.MCC, function = "查看分组")
    @Auth(level = Auth.Level.LOGIN, responseMode = Auth.ResponseMode.JSON)
    @ResponseBody
    public String getAllGroupsIPs(@PathVariable("appkey") String appkey,
                                  @RequestParam("env") int env) {
        return JsonHelper.dataJson(ServiceConfig.getGroupsIPs(appkey, env));
    }

    @RequestMapping(value = "{appkey}/config/file/domainIPByGroupID", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @Worth(model = Worth.Model.MCC, function = "查看分组")
    @Auth(level = Auth.Level.LOGIN, responseMode = Auth.ResponseMode.JSON)
    @ResponseBody
    public String domainIPByGroupID(@PathVariable("appkey") String appkey,
                                    @RequestParam("env") int env,
                                    @RequestParam("groupID") String groupID) {
        return JsonHelper.dataJson(ServiceConfig.domainIPByGroupID(appkey, env, groupID));
    }

    @RequestMapping(value = "{appkey}/config/file/existGroupIPs", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @Auth(level = Auth.Level.LOGIN, responseMode = Auth.ResponseMode.JSON)
    @ResponseBody
    public String existGroupIPs(@PathVariable("appkey") String appkey,
                                @RequestParam("env") int env,
                                @RequestParam("groupID") String groupID) {
        return JsonHelper.dataJson(ServiceConfig.existGroupIPs(appkey, env, groupID));
    }

    @RequestMapping(value = "{appkey}/config/file/existGroupNames", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @Auth(level = Auth.Level.LOGIN, responseMode = Auth.ResponseMode.JSON)
    @ResponseBody
    public String existGroupNames(@PathVariable("appkey") String appkey,
                                  @RequestParam("env") int env) {
        return JsonHelper.dataJson(ServiceConfig.existGroupNames(appkey, env));
    }

    /**
     * 是否为服务负责人
     * @param appkey
     * @return
     */
    @RequestMapping(value = "{appkey}/config/auth", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String getAuth(@PathVariable("appkey") String appkey) {
        Integer level = Auth.Level.ADMIN.getValue();
        // 获取用户信息和appkey
        User user = UserUtils.getUser();
        Boolean hasAuth = AppkeyAuth.hasAuth(appkey, level, user);
        return JsonHelper.dataJson(hasAuth);
    }

    /**
     * 计算修改鉴权的token签名
     * @param appkey
     * @param token
     * @return
     */
    @RequestMapping(value = "{appkey}/config/token", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @Auth(level = Auth.Level.ADMIN, responseMode = Auth.ResponseMode.JSON)
    @ResponseBody
    public String makeToken(@PathVariable("appkey") String appkey, @RequestParam("token") String token, HttpServletRequest request) {
        String authPath = "/" + appkey;
        return ServiceConfig.getAuthToken(token, authPath,request.getCookies());
    }

    @RequestMapping(value = "config/sgconfig", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @Auth(level = Auth.Level.LOGIN, responseMode = Auth.ResponseMode.JSON)
    @ResponseBody
    public String sgconfig(@RequestParam("app") String app,
                           @RequestParam("env") int env,
                           @RequestParam("iswaimai") boolean isWaimai) {
        return ServiceConfig.getWaimaiSgconfig(app, env, isWaimai);
    }

    @RequestMapping(value = "{appkey}/config/sgconfig/migration", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @Auth(level = Auth.Level.LOGIN, responseMode = Auth.ResponseMode.JSON)
    @ResponseBody
    public String sgconfigMigration(@PathVariable("appkey") String appkey,
                                    @RequestParam("app") String app,
                                    @RequestParam("env") int env,
                                    @RequestParam("iswaimai") boolean isWaimai) {
        return ServiceConfig.waimaiSgconfigMigration(appkey, app, env, isWaimai);
    }

    @RequestMapping(value = "{appkey}/config/pullrequest", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    @Worth(model = Worth.Model.MCC, function = "创建MCC PR")
    @Auth(level = Auth.Level.ADMIN, responseMode = Auth.ResponseMode.JSON)
    @ResponseBody
    public String createPR(@PathVariable("appkey") String appkey, HttpServletRequest request) {
        try {
            String json = IOUtils.copyToString(request.getReader());
            return ServiceConfig.createPR(json);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return JsonHelper.errorJson(e.getMessage());
        }
    }

    @RequestMapping(value = "{appkey}/config/pullrequest", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @Worth(model = Worth.Model.MCC, function = "获取MCC PR")
    @Auth(level = Auth.Level.LOGIN, responseMode = Auth.ResponseMode.JSON)
    @ResponseBody
    public String getPR(@PathVariable("appkey") String appkey,
                        @RequestParam("env") int env,
                        @RequestParam("status") int status,
                        @RequestParam("pageNo") int pageNo) {
        try {
            Page page = new Page(pageNo);
            return JsonHelper.dataJson(ServiceConfig.getPR(appkey, env, status, page), page);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return JsonHelper.errorJson("获取失败");
        }
    }

    @RequestMapping(value = "config/updateprdetail", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    @Worth(model = Worth.Model.MCC, function = "更新MCC PR Detail")
    @Auth(level = Auth.Level.ADMIN, responseMode = Auth.ResponseMode.JSON)
    @ResponseBody
    public String updatePRDetail(HttpServletRequest request) {
        try {
            String json = IOUtils.copyToString(request.getReader());
            JSONObject jsonObj = JSONObject.fromObject(json);
            String appkey =  jsonObj.getString("appkey");
            jsonObj.remove("appkey");
            return (String)ServiceConfig.updatePRDetail(jsonObj.toString(),appkey);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return JsonHelper.errorJson("更新失败");
        }
    }

    @RequestMapping(value = "config/prdetail", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @Worth(model = Worth.Model.MCC, function = "获取MCC PR")
    @Auth(level = Auth.Level.LOGIN, responseMode = Auth.ResponseMode.JSON)
    @ResponseBody
    public String getPRDetail(@RequestParam("prID") int prID) {
        try {
            return JsonHelper.dataJson(ServiceConfig.getPRDetailAndReview(prID));
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return JsonHelper.errorJson("获取失败");
        }

    }

    @RequestMapping(value = "config/prauthor", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @Worth(model = Worth.Model.MCC, function = "获取MCC PR的发起人")
    @ResponseBody
    public String isPRAuthor(@RequestParam("prID") int prID) {
        try {
            return JsonHelper.dataJson(ServiceConfig.isPrAuthor(prID));
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return JsonHelper.errorJson("获取失败");
        }

    }

    @RequestMapping(value = "{appkey1}/config/review", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    @Worth(model = Worth.Model.MCC, function = "创建MCC review")
    @Auth(level = Auth.Level.ADMIN, responseMode = Auth.ResponseMode.JSON)
    @ResponseBody
    public String createReview(@PathVariable("appkey1") String appkey1, HttpServletRequest request) {
        try {
            String json = IOUtils.copyToString(request.getReader());
            return ServiceConfig.createReview(json);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return JsonHelper.errorJson("创建review失败");
        }
    }

    @RequestMapping(value = "{appkey1}/config/pullrequest", method = RequestMethod.DELETE, produces = "application/json;charset=utf-8")
    @Worth(model = Worth.Model.MCC, function = "删除MCC PR")
    @Auth(level = Auth.Level.ADMIN, responseMode = Auth.ResponseMode.JSON)
    @ResponseBody
    public String delPR(@PathVariable("appkey1") String appkey1, @RequestParam("prID") int prID, @RequestParam("appkey") String appkey) {
        return ServiceConfig.delPR(prID) ? JsonHelper.dataJson("删除成功") : JsonHelper.errorJson("删除失败");
    }


    @RequestMapping(value = "{appkey}/config/mergepr", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @Worth(model = Worth.Model.MCC, function = "merge PR")
    @Auth(level = Auth.Level.ADMIN, responseMode = Auth.ResponseMode.JSON)
    @ResponseBody
    public String mergepr(@RequestParam("prID") int prID, @RequestParam("appkey") String appkey) {
        boolean r = ServiceConfig.merge(prID);
        return  r ? JsonHelper.dataJson("merge成功") : JsonHelper.errorJson("merge失败");
    }

    @RequestMapping(value = "config/reopenpr", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @Worth(model = Worth.Model.MCC, function = "reopen PR")
    @Auth(level = Auth.Level.ADMIN, responseMode = Auth.ResponseMode.JSON)
    @ResponseBody
    public String reopenPR(@RequestParam("appkey") String appkey,
                           @RequestParam("env") int env,
                           @RequestParam("prID") int prID) {
        try {
            return ServiceConfig.reopenPR(appkey, env, prID) ? JsonHelper.dataJson("Reopen成功") : JsonHelper.errorJson("Reopen成功");
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return JsonHelper.errorJson("Reopen失败");
        }
    }

    @RequestMapping(value = "config/settings/{appkey}/settings/update", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    @Worth(model = Worth.Model.MCC, function = "MCC设置")
    @ResponseBody
    public String updateMCCSettings(@PathVariable("appkey") String appkey,
                                    HttpServletRequest request) {
        try {
            String json = IOUtils.copyToString(request.getReader());
            return ServiceConfig.updateMCCSettings(appkey, json, request.getCookies());
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return JsonHelper.errorJson("MCC设置失败");
        }
    }

    @RequestMapping(value = "config/settings/{appkey}/settings/data", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @Worth(model = Worth.Model.MCC, function = "获取MCC设置")
    @ResponseBody
    public String settingsData(@PathVariable("appkey") String appkey, HttpServletRequest request) {
        try {
            return ServiceConfig.settingsData(appkey, request.getCookies());
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return JsonHelper.errorJson("获取MCC设置失败");
        }
    }

    @RequestMapping(value = "config/space/{appkey}/node/syncprod", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    @Worth(model = Worth.Model.MCC, function = "同步Stage动态配置至Prod")
    @Auth(level = Auth.Level.ADMIN, responseMode = Auth.ResponseMode.JSON)
    @ResponseBody
    public String syncDynamicCfg2Prod(@PathVariable("appkey") String appkey,
                                      @RequestParam("nodeName") String nodeName,
                                      HttpServletRequest request) {
        try {
            if (!CommonHelper.isOffline() && StringUtils.equals("stage", ServiceConfig.getEnvByNodename(nodeName, appkey))  && AppkeyAuth.isInSpecialOwt(appkey)) {
                return JsonHelper.errorJson("该服务不支持直接修改配置，请发起PR流程。");
            } else {
                return ServiceConfig.syncDynamicCfg2Prod(appkey, nodeName, request.getCookies());
            }

        } catch (Exception e) {
            LOG.error("MCCControler syncDynamicCfg2Prod fail.", e);
            return JsonHelper.errorJson(e.getMessage());
        }
    }
}

