package com.ruider.controller;


import net.sf.json.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.w3c.dom.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.lang.reflect.Array;
import java.net.URLDecoder;
import java.util.*;

@Controller
public class XacmlController {

    @RequestMapping(value = "/test")
    @ResponseBody
    public String test() {
        return "Test successful~~";
    }


    //与项目无关，纯属测试
    @RequestMapping(value = "/uploadForm")
    public ModelAndView uploadForm() {
        return new ModelAndView("uploadForm");
    }

    //处理用户上传的xacml文件
    @RequestMapping(value = "/upload", method = RequestMethod.POST)
    public ModelAndView upLoadXacmlFile(HttpServletRequest request,
                                        @RequestParam("file") MultipartFile file) {

        System.out.println("开始提交");

        Map<String, Object> map = new HashMap<String, Object>();

        ModelAndView mav = null;
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = null;
        Document doc = null;

        try {
            String path = request.getServletContext().getRealPath("/XML/");
            System.out.println(path);
            //上传文件名
            String filename = file.getOriginalFilename();
            File filepath = new File(path, filename);
            //判断路径是否存在，如果不存在就创建一个
            if (!filepath.getParentFile().exists()) {
                filepath.getParentFile().mkdirs();
            }
            //将上传文件保存到一个目标文件当中
            file.transferTo(new File(path + File.separator + filename));


            factory.setIgnoringElementContentWhitespace(true);
            db = factory.newDocumentBuilder();
            doc = db.parse(new File(path + File.separator + filename));   //从服务器获取XACML文件

            //获取根元素以及根元素的属性
            Element policy = doc.getDocumentElement();
            System.out.println("Policy");
            System.out.println("PolicyId>>   " + policy.getAttribute("PolicyId").substring(46));
            map.put("PolicyId", policy.getAttribute("PolicyId").substring(46));
            System.out.println("Version>>   " + policy.getAttribute("Version"));
            map.put("Version", policy.getAttribute("Version"));
            System.out.println("RuleCombiningAlgId>>   " + policy.getAttribute("RuleCombiningAlgId").trim());
            map.put("RuleCombiningAlgId", policy.getAttribute("RuleCombiningAlgId").trim());

            //获取根节点的子节点
            //Description
            NodeList nodes = policy.getChildNodes();
            String description1 = null;
            for (int i = 0; i < nodes.getLength(); i++) {
                if (nodes.item(i).getNodeName().equalsIgnoreCase("Description")) {
                    description1 = nodes.item(i).getTextContent();
                    break;
                }
            }

            System.out.println("Description>>   " + description1);
            map.put("Description1", description1);

            //子节点Target
            NodeList nodez = policy.getChildNodes();
            boolean hasTarget = false;
            boolean hasUser = false;
            boolean hasResource = false;
            boolean hasAction = false;

            //记录User,Resource,Action的数量
            int UserCount = 0, ResourceCount = 0, ActionCount = 0;

            //根据Target子节点AttributeDesignator的属性AttributeId结尾是否含有subject-id,resource-id,action-id判断是否含有User,Resource,Action
            for (int i = 0; i < nodez.getLength(); i++) {
                if (nodez.item(i).getNodeName().equalsIgnoreCase("Target")) {
                    Element target = (Element) policy.getElementsByTagName("Target").item(0);
                    hasTarget = true;
                    NodeList AttributeDesignators = target.getElementsByTagName("AttributeDesignator");
                    for (int j = 0; j < AttributeDesignators.getLength(); j++) {
                        NamedNodeMap attrs = AttributeDesignators.item(j).getAttributes();
                        for (int k = 0; k < attrs.getLength(); k++) {
                            if (attrs.item(k).getNodeName().equalsIgnoreCase("AttributeId")) {
                                String value = attrs.item(k).getNodeValue();
                                if (value.substring(29,36).equals("subject")) {
                                    UserCount++;
                                    hasUser = true;
                                } else if (value.substring(29,37).equals("resource")) {
                                    ResourceCount++;
                                    hasResource = true;
                                } else if (value.substring(29,35).equals("action")) {
                                    ActionCount++;
                                    hasAction = true;
                                }
                            }
                        }
                    }
                }
            }
            //获取Target的相关信息
            String Target = null;
            if (hasTarget) {
                Target = "Target";
                Element target = (Element) policy.getElementsByTagName("Target").item(0);
                NodeList matches = target.getElementsByTagName("Match");

                String User = null;
                if (hasUser) {
                    User = "User";
                    //User的Match属性
                    Element userMatch = (Element) matches.item(0);
                    String MatchId1 = userMatch.getAttribute("MatchId");
                    System.out.println("MatchId1>>   " + MatchId1);
                    map.put("MatchId1", MatchId1);

                    NodeList attributeValues = userMatch.getChildNodes();
                    NamedNodeMap attrs = userMatch.getAttributes();
                    for (int y = 0; y < attributeValues.getLength(); y++) {
                        if (attributeValues.item(y).getNodeName().equalsIgnoreCase("AttributeValue")) {
                            attrs = attributeValues.item(y).getAttributes();
                            for (int x = 0; x < attrs.getLength(); x++) {
                                if (attrs.item(x).getNodeName().equals("DataType")) {
                                    System.out.println("DataType1>>   " + attrs.item(x).getNodeValue());
                                    map.put("DataType1", attrs.item(x).getNodeValue());
                                }
                            }
                            System.out.println("value1>>   " + attributeValues.item(y).getTextContent());
                            map.put("value1", attributeValues.item(y).getTextContent());
                        } else if (attributeValues.item(y).getNodeName().equalsIgnoreCase("AttributeDesignator")) {
                            attrs = attributeValues.item(y).getAttributes();
                            for (int x = 0; x < attrs.getLength(); x++) {
                                if (attrs.item(x).getNodeName().equals("MustBePresent")) {
                                    System.out.println("MustBePresent1>>   " + attrs.item(x).getNodeValue());
                                    map.put("MustBePresent1", attrs.item(x).getNodeValue());
                                } else if (attrs.item(x).getNodeName().equals("AttributeId")) {
                                    System.out.println("AttributeId1>>   " + attrs.item(x).getNodeValue());
                                    map.put("AttributeId1", attrs.item(x).getNodeValue().substring(45));
                                }
                            }
                        }
                    }
                } else {
                    System.out.println("User>>  " + User);
                }
                map.put("User", User);                       //方便前端判断是否含有Target的User

                //Target的Resource
                String Resource = null;
                if (hasResource) {
                    Resource = "Resource";
                    Element resourceMatch = (Element) matches.item(1);
                    String MatchId2 = resourceMatch.getAttribute("MatchId");
                    System.out.println("MatchId2>>   " + MatchId2);
                    map.put("MatchId2", MatchId2);

                    NodeList attributeValues = resourceMatch.getChildNodes();
                    NamedNodeMap attrs = resourceMatch.getAttributes();
                    for (int y = 0; y < attributeValues.getLength(); y++) {
                        if (attributeValues.item(y).getNodeName().equalsIgnoreCase("AttributeValue")) {
                            attrs = attributeValues.item(y).getAttributes();
                            for (int x = 0; x < attrs.getLength(); x++) {
                                if (attrs.item(x).getNodeName().equals("DataType")) {
                                    System.out.println("DataType2>>   " + attrs.item(x).getNodeValue());
                                    map.put("DataType2", attrs.item(x).getNodeValue());
                                }
                            }

                            System.out.println("value2>>   " + attributeValues.item(y).getTextContent());
                            map.put("value2", attributeValues.item(y).getTextContent());
                        } else if (attributeValues.item(y).getNodeName().equalsIgnoreCase("AttributeDesignator")) {
                            attrs = attributeValues.item(y).getAttributes();
                            for (int x = 0; x < attrs.getLength(); x++) {
                                if (attrs.item(x).getNodeName().equals("MustBePresent")) {
                                    System.out.println("MustBePresent2>>   " + attrs.item(x).getNodeValue());
                                    map.put("MustBePresent2", attrs.item(x).getNodeValue());
                                } else if (attrs.item(x).getNodeName().equals("AttributeId")) {
                                    System.out.println("AttributeId2>>   " + attrs.item(x).getNodeValue());
                                    map.put("AttributeId2", attrs.item(x).getNodeValue().substring(47));
                                }
                            }
                        }

                    }
                } else
                    System.out.println("Resource>>   " + Resource);
                map.put("Resource", Resource);

                //Target的Action
                String Action = null;
                if (hasAction) {
                    Action = "Action";
                    Element actionMatch = (Element) matches.item(2);
                    String MatchId3 = actionMatch.getAttribute("MatchId");
                    System.out.println("MatchId3>>   " + MatchId3);
                    map.put("MatchId3", MatchId3);

                    NodeList attributeValues = actionMatch.getChildNodes();
                    NamedNodeMap attrs = actionMatch.getAttributes();
                    for (int y = 0; y < attributeValues.getLength(); y++) {
                        if (attributeValues.item(y).getNodeName().equalsIgnoreCase("AttributeValue")) {
                            attrs = attributeValues.item(y).getAttributes();
                            for (int x = 0; x < attrs.getLength(); x++) {
                                if (attrs.item(x).getNodeName().equals("DataType")) {
                                    System.out.println("DataType3>>   " + attrs.item(x).getNodeValue());
                                    map.put("DataType3", attrs.item(x).getNodeValue());
                                }
                            }
                            System.out.println("value3>>   " + attributeValues.item(y).getTextContent());
                            map.put("value3", attributeValues.item(y).getTextContent());
                        } else if (attributeValues.item(y).getNodeName().equalsIgnoreCase("AttributeDesignator")) {
                            attrs = attributeValues.item(y).getAttributes();
                            for (int x = 0; x < attrs.getLength(); x++) {
                                if (attrs.item(x).getNodeName().equals("MustBePresent")) {
                                    System.out.println("MustBePresent3>>   " + attrs.item(x).getNodeValue());
                                    map.put("MustBePresent3", attrs.item(x).getNodeValue());
                                } else if (attrs.item(x).getNodeName().equals("AttributeId")) {
                                    System.out.println("AttributeId3>>   " + attrs.item(x).getNodeValue());
                                    map.put("AttributeId3", attrs.item(x).getNodeValue().substring(43));
                                }
                            }
                        }
                    }
                } else
                    System.out.println("Action>>   " + Action);
                map.put("Action", Action);
            } else {
                System.out.println("Target>>   " + Target);
            }
            map.put("Target", Target);


            //Rule(s)
            int a = 0;              //记录xacml是否含有Target
            if (hasTarget)
                a = 1;
            //ArrayList<String> Rules=Rules=new ArrayList<String>();    //添加所有Ruels
            //System.out.println("长度>>>"+nodez.getLength());

            nodez = policy.getElementsByTagName("Rule");
            int RulesCount = nodez.getLength();          //记录Rule的数量
            map.put("RulesCount", RulesCount);

            TreeMap<Integer, TreeMap<String, String>> Rules = new TreeMap<Integer, TreeMap<String, String>>();      //有顺序存储rules


            for (int l = 0; l < nodez.getLength(); l++) {
                TreeMap<String, String> RulesInfo = new TreeMap<String, String>();                            //有顺序存放User,Resource,Action
                //if(nodez.item(i).getNodeName().equalsIgnoreCase("Rule")){
                boolean hasRule = false;
                boolean hasRuleUser = false;
                boolean hasRuleResource = false;
                boolean hasRuleAction = false;
                //Element rule=(Element) policy.getElementsByTagName("Rule").item(s);
                Element rule = (Element) nodez.item(l);
                hasRule = true;
                NodeList AttributeDesignators = rule.getElementsByTagName("AttributeDesignator");
                for (int j = 0; j < AttributeDesignators.getLength(); j++) {
                    NamedNodeMap attrs = AttributeDesignators.item(j).getAttributes();
                    for (int k = 0; k < attrs.getLength(); k++) {
                        if (attrs.item(k).getNodeName().equalsIgnoreCase("AttributeId")) {
                            String value = attrs.item(k).getNodeValue();
                            if (value.substring(29,36).equals("subject")) {
                                hasRuleUser = true;
                            } else if (value.substring(29,37).equals("resource")) {
                                hasRuleResource = true;
                            } else if (value.substring(29,35).equals("action")) {
                                hasRuleAction = true;
                            }
                        }
                    }
                }

                String Rule = null;
                if (hasRule) {
                    Rule = "Rule" + (l + 1);
                    System.out.println("Rule>>------------------------ " + Rule);
                    //RulesInfo.put("RuleName",Rule);
                    String RuleId = rule.getAttribute("RuleId").substring(46);
                    System.out.println("RuleId>>   " + RuleId);
                    RulesInfo.put("RuleId", RuleId);

                    String Effect = rule.getAttribute("Effect");
                    System.out.println("Effect>>   " + Effect);
                    RulesInfo.put("Effect", Effect);

                    //NodeList nodes=policy.getChildNodes();
                    String desc = null;
                    for (int i = 0; i < nodes.getLength(); i++) {
                        if (nodes.item(i).getNodeName().equalsIgnoreCase("Description")) {
                            desc = nodes.item(i).getTextContent();
                            break;
                        }
                    }
                    System.out.println("description>>   " + desc);
                    RulesInfo.put("description", desc);

                    String RuleUser = null;
                    NodeList matches = rule.getElementsByTagName("Match");
                    if (hasRuleUser) {
                        RuleUser = "RuleUser";
                        Element userMatch = (Element) matches.item(0);
                        String MatchId4 = userMatch.getAttribute("MatchId");
                        System.out.println("MatchId4>>   " + MatchId4);
                        RulesInfo.put("MatchId4", MatchId4);

                        NodeList attributeValues = userMatch.getChildNodes();
                        NamedNodeMap attrs = userMatch.getAttributes();
                        for (int y = 0; y < attributeValues.getLength(); y++) {
                            if (attributeValues.item(y).getNodeName().equalsIgnoreCase("AttributeValue")) {
                                attrs = attributeValues.item(y).getAttributes();
                                for (int x = 0; x < attrs.getLength(); x++) {
                                    if (attrs.item(x).getNodeName().equals("DataType")) {
                                        System.out.println("DataType4>>   " + attrs.item(x).getNodeValue());
                                        RulesInfo.put("DataType4", attrs.item(x).getNodeValue());
                                    }
                                }
                                System.out.println("value4>>   " + attributeValues.item(y).getTextContent());
                                RulesInfo.put("value4", attributeValues.item(y).getTextContent());
                            } else if (attributeValues.item(y).getNodeName().equalsIgnoreCase("AttributeDesignator")) {
                                attrs = attributeValues.item(y).getAttributes();
                                for (int x = 0; x < attrs.getLength(); x++) {
                                    if (attrs.item(x).getNodeName().equals("MustBePresent")) {
                                        System.out.println("MustBePresent4>>   " + attrs.item(x).getNodeValue());
                                        RulesInfo.put("MustBePresent4", attrs.item(x).getNodeValue());
                                    } else if (attrs.item(x).getNodeName().equals("AttributeId")) {
                                        System.out.println("AttributeId4>>   " + attrs.item(x).getNodeValue());
                                        RulesInfo.put("AttributeId4", attrs.item(x).getNodeValue().substring(45));
                                    }
                                }
                            }
                        }
                    } else
                        System.out.println("RuleUser>>  " + RuleUser);
                    RulesInfo.put("RuleUser", RuleUser);

                    int c = -1;

                    String RuleResource = null;
                    if (hasRuleResource) {
                        RuleResource = "RuleResource";
                        if (hasRuleUser)
                            c = 1;
                        else
                            c = 0;
                        Element resourceMatch = (Element) matches.item(c);
                        String MatchId5 = resourceMatch.getAttribute("MatchId");
                        System.out.println("MatchId5>>   " + MatchId5);
                        RulesInfo.put("MatchId5", MatchId5);

                        NodeList attributeValues = resourceMatch.getChildNodes();
                        NamedNodeMap attrs = resourceMatch.getAttributes();
                        for (int y = 0; y < attributeValues.getLength(); y++) {
                            if (attributeValues.item(y).getNodeName().equalsIgnoreCase("AttributeValue")) {
                                attrs = attributeValues.item(y).getAttributes();
                                for (int x = 0; x < attrs.getLength(); x++) {
                                    if (attrs.item(x).getNodeName().equals("DataType")) {
                                        System.out.println("DataType5>>   " + attrs.item(x).getNodeValue());
                                        RulesInfo.put("DataType5", attrs.item(x).getNodeValue());
                                    }
                                }
                                System.out.println("value5>>   " + attributeValues.item(y).getTextContent());
                                RulesInfo.put("value5", attributeValues.item(y).getTextContent());
                            } else if (attributeValues.item(y).getNodeName().equalsIgnoreCase("AttributeDesignator")) {
                                attrs = attributeValues.item(y).getAttributes();
                                for (int x = 0; x < attrs.getLength(); x++) {
                                    if (attrs.item(x).getNodeName().equals("MustBePresent")) {
                                        System.out.println("MustBePresent5>>   " + attrs.item(x).getNodeValue());
                                        RulesInfo.put("MustBePresent5", attrs.item(x).getNodeValue());
                                    } else if (attrs.item(x).getNodeName().equals("AttributeId")) {
                                        System.out.println("AttributeId5>>   " + attrs.item(x).getNodeValue());
                                        RulesInfo.put("AttributeId5", attrs.item(x).getNodeValue().substring(47));
                                    }
                                }
                            }
                        }
                    } else
                        System.out.println("RuleResource>>   " + RuleResource);
                    RulesInfo.put("RuleResource", RuleResource);

                    int z = -1;
                    String RuleAction = null;
                    if (hasRuleAction) {
                        RuleAction = "RuleAction";
                        if (hasRuleUser && hasRuleResource)
                            z = 2;
                        else if ((hasRuleUser && !hasRuleResource) || (!hasRuleUser && hasRuleResource))
                            z = 1;
                        else if (!hasRuleUser && !hasRuleResource)
                            z = 0;

                        Element actionMatch = (Element) matches.item(z);
                        String MatchId6 = actionMatch.getAttribute("MatchId");
                        System.out.println("MatchId6>>   " + MatchId6);
                        RulesInfo.put("MatchId6", MatchId6);

                        NodeList attributeValues = actionMatch.getChildNodes();
                        NamedNodeMap attrs = actionMatch.getAttributes();
                        for (int y = 0; y < attributeValues.getLength(); y++) {
                            if (attributeValues.item(y).getNodeName().equalsIgnoreCase("AttributeValue")) {
                                attrs = attributeValues.item(y).getAttributes();
                                for (int x = 0; x < attrs.getLength(); x++) {
                                    if (attrs.item(x).getNodeName().equals("DataType")) {
                                        System.out.println("DataType6>>   " + attrs.item(x).getNodeValue());
                                        RulesInfo.put("DataType6", attrs.item(x).getNodeValue());
                                    }
                                }
                                System.out.println("value6>>   " + attributeValues.item(y).getTextContent());
                                RulesInfo.put("value6", attributeValues.item(y).getTextContent());
                            } else if (attributeValues.item(y).getNodeName().equalsIgnoreCase("AttributeDesignator")) {
                                attrs = attributeValues.item(y).getAttributes();
                                for (int x = 0; x < attrs.getLength(); x++) {
                                    if (attrs.item(x).getNodeName().equals("MustBePresent")) {
                                        System.out.println("MustBePresent6>>   " + attrs.item(x).getNodeValue());
                                        RulesInfo.put("MustBePresent6", attrs.item(x).getNodeValue());
                                    } else if (attrs.item(x).getNodeName().equals("AttributeId")) {
                                        System.out.println("AttributeId6>>   " + attrs.item(x).getNodeValue());
                                        RulesInfo.put("AttributeId6", attrs.item(x).getNodeValue().substring(43));
                                    }
                                }
                            }
                        }
                    } else
                        System.out.println("RuleAction>>   " + RuleAction);
                    RulesInfo.put("RuleAction", RuleAction);
                    System.out.println(l + "  RulesInfo>>    " + RulesInfo);
                    Rules.put(l, RulesInfo);
                } else {
                }

                System.out.println("Rules>>    " + Rules);

            }
            System.out.println("----------------------------------");
            map.put("Rules", Rules);
            System.out.println("Rules>>    " + Rules);


            System.out.println("----------------------------------");
            //JSONObject result=JSONObject.fromObject(map);
            System.out.println("map:         /n" + map);
            //return result;

            mav = new ModelAndView("Update");
            mav.addObject("map", map);        //将map放到整个application中
            return mav;

            /*
            String path = request.getServletContext().getRealPath("/XML/");
            //上传文件名
            String filename = file.getOriginalFilename();
            File filepath = new File(path, filename);
            //判断路径是否存在，如果不存在就创建一个
            if (!filepath.getParentFile().exists()) {
                filepath.getParentFile().mkdirs();
            }
            //将上传文件保存到一个目标文件当中
            file.transferTo(new File(path + File.separator + filename));
             mav.addObject("doc",doc);
            mav.setViewName("Main1");
            */

        } catch (Exception e) {
            e.printStackTrace();
            return new ModelAndView("ERROR");
        }
    }


    //打开新建xacml界面
    @RequestMapping("/toBuild")
    public ModelAndView test1() {
        return new ModelAndView("Build");
    }


    //新建xml文件
    @RequestMapping(value = "/main")
    @ResponseBody
    Map<String, String> main(HttpServletRequest request, HttpServletResponse response) throws IOException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        String[] RuleIds = request.getParameterValues("RuleId[]");
        String[] Effects = request.getParameterValues("Effect[]");
        String[] description2s = request.getParameterValues("description2[]");
        String[] MatchId4s = request.getParameterValues("MatchId4[]");
        String[] MatchId5s = request.getParameterValues("MatchId5[]");
        String[] MatchId6s = request.getParameterValues("MatchId6[]");
        String[] DataType4s = request.getParameterValues("DataType4[]");
        String[] DataType5s = request.getParameterValues("DataType5[]");
        String[] DataType6s = request.getParameterValues("DataType6[]");
        String[] value4s = request.getParameterValues("value4[]");
        String[] value5s = request.getParameterValues("value5[]");
        String[] value6s = request.getParameterValues("value6[]");
        String[] AttributeId4s = request.getParameterValues("AttributeId4[]");
        String[] AttributeId5s = request.getParameterValues("AttributeId5[]");
        String[] AttributeId6s = request.getParameterValues("AttributeId6[]");
        String[] MustBePresent4s = request.getParameterValues("MustBePresent4[]");
        String[] MustBePresent5s = request.getParameterValues("MustBePresent5[]");
        String[] MustBePresent6s = request.getParameterValues("MustBePresent6[]");


        System.out.println("===============");
        for (String x : value4s)
            System.out.println(URLDecoder.decode(x));


        Map<String, String> map = new HashMap<String, String>();

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

        String fileName = URLDecoder.decode(request.getParameter("PolicyId"));                 //名称为policyId,
        if (fileName.equals("")) {
            fileName = "PolicyId";                                                                //默认名称为PolicyId
        }

        System.out.println("PolicyId>>     " + fileName);
        try {
            DocumentBuilder documentBuilder = factory.newDocumentBuilder();
            Document doc = documentBuilder.newDocument();

            //创建结点
            Element policy = doc.createElement("Policy");
            //添加属性
            policy.setAttribute("xmlns", "urn:oasis:names:tc:xacml:3.0:core:schema:wd-17");
            policy.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
            policy.setAttribute("PolicyId", "urn:oasis:names:tc:xacml:2.0:conformance-test:" + fileName);
            policy.setAttribute("RuleCombiningAlgId", request.getParameter("RuleCombiningAlgId"));
            String Version = request.getParameter("Version");

            if (Version.equals(""))
                Version = "1.0";                                                               //Version默认为1.0
            policy.setAttribute("Version", Version);
            policy.setAttribute("xsi:schemaLocation", "urn:oasis:names:tc:xacml:3.0:" +
                    "policy:schema:os access_control-xacml-2.0-policy-schema-os.xsd");

            //创建子节点Description
            Element description = doc.createElement("Description");
            description.setTextContent(URLDecoder.decode(request.getParameter("description1")));

            String str1 = URLDecoder.decode(request.getParameter("description1"));
            System.out.println(str1);

            String str2 = request.getParameter("RuleCombiningAlgId");
            System.out.println(str2);
            //绑定父子结点关系
            policy.appendChild(description);


            //policy子节点Target
            String MatchId1 = request.getParameter("MatchId1");
            String MatchId2 = request.getParameter("MatchId2");
            String MatchId3 = request.getParameter("MatchId3");
            String DataType1 = request.getParameter("DataType1");
            String DataType2 = request.getParameter("DataType2");
            String DataType3 = request.getParameter("DataType3");
            String value1 = URLDecoder.decode(request.getParameter("value1"));
            String value2 = URLDecoder.decode(request.getParameter("value2"));
            String value3 = URLDecoder.decode(request.getParameter("value3"));
            String AttributeId1 = URLDecoder.decode(request.getParameter("AttributeId1"));
            String AttributeId2 = URLDecoder.decode(request.getParameter("AttributeId2"));
            String AttributeId3 = URLDecoder.decode(request.getParameter("AttributeId3"));

            //为空处理
            if(AttributeId1.equals("")){
                AttributeId1="id";
            }
            if(AttributeId2.equals("")){
                AttributeId2="id";
            }if(AttributeId3.equals("")){
                AttributeId3="id";
            }


            String MustBePresent1 = request.getParameter("MustBePresent1");
            String MustBePresent2 = request.getParameter("MustBePresent2");
            String MustBePresent3 = request.getParameter("MustBePresent3");

            Element target = doc.createElement("Target");
            //为空不显示结点
            if (!(MatchId1.equals("null") && MatchId2.equals("null") && MatchId3.equals("null"))) {


                //Target子节点User
                Element anyof = doc.createElement("Anyof");
                Element allof = doc.createElement("Allof");

                //创建Allof子节点Match
                Element match1 = doc.createElement("Match");
                match1.setAttribute("MatchId", MatchId1);

                Element attributeValue1 = doc.createElement("AttributeValue");
                attributeValue1.setAttribute("DataType", DataType1);
                attributeValue1.setTextContent(value1);

                Element attributeDesignator1 = doc.createElement("AttributeDesignator");
                attributeDesignator1.setAttribute("AttributeId", "urn:oasis:names:tc:xacml:3.0:subject:subject-"+AttributeId1);
                attributeDesignator1.setAttribute("Catagory", "urn:oasis:names:tc:xacml:3.0:subject-catagory:access-subject");
                attributeDesignator1.setAttribute("DataType", DataType1);
                attributeDesignator1.setAttribute("MustBePresent", MustBePresent1);

                match1.appendChild(attributeValue1);
                match1.appendChild(attributeDesignator1);

                //为空不显示结点
                if (!(MatchId1.equals("null"))) {
                    allof.appendChild(match1);
                    anyof.appendChild(allof);
                    target.appendChild(anyof);
                }


                //Target子节点Resource
                anyof = doc.createElement("Anyof");
                allof = doc.createElement("Allof");
                //创建User子节点Match
                Element match2 = doc.createElement("Match");
                match2.setAttribute("MatchId", MatchId2);

                Element attributeValue2 = doc.createElement("AttributeValue");
                attributeValue2.setAttribute("DataType", DataType2);
                attributeValue2.setTextContent(value2);

                Element attributeDesignator2 = doc.createElement("AttributeDesignator");
                attributeDesignator2.setAttribute("AttributeId", "urn:oasis:names:tc:xacml:3.0:resource:resource-"+AttributeId2);
                attributeDesignator2.setAttribute("Catagory", "urn:oasis:names:tc:xacml:3.0:attribute-catagory:resource");
                attributeDesignator2.setAttribute("DataType", DataType2);
                attributeDesignator2.setAttribute("MustBePresent", MustBePresent2);

                match2.appendChild(attributeValue2);
                match2.appendChild(attributeDesignator2);

                //为空不显示结点
                if (!MatchId2.equals("null")) {
                    allof.appendChild(match2);
                    anyof.appendChild(allof);
                    target.appendChild(anyof);
                }

                //Target子节点Action
                Element anyof4 = doc.createElement("Anyof");
                Element allof4 = doc.createElement("Allof");
                //创建User子节点Match
                Element match3 = doc.createElement("Match");
                match3.setAttribute("MatchId", MatchId3);

                Element attributeValue3 = doc.createElement("AttributeValue");
                attributeValue3 = doc.createElement("AttributeValue");
                attributeValue3.setAttribute("DataType", DataType3);
                attributeValue3.setTextContent(value3);

                Element attributeDesignator3 = doc.createElement("AttributeDesignator");
                attributeDesignator3.setAttribute("AttributeId", "urn:oasis:names:tc:xacml:3.0:action:action-"+AttributeId3);
                attributeDesignator3.setAttribute("Catagory", "urn:oasis:names:tc:xacml:3.0:attribute-catagory:action");
                attributeDesignator3.setAttribute("DataType", DataType3);
                attributeDesignator3.setAttribute("MustBePresent", MustBePresent3);

                match3.appendChild(attributeValue3);
                match3.appendChild(attributeDesignator3);

                //为空不显示结点
                if (!MatchId3.equals("null")) {
                    allof4.appendChild(match3);
                    anyof4.appendChild(allof4);
                    target.appendChild(anyof4);
                }

            } else {
                target.setTextContent("");
            }
            policy.appendChild(target);


            for (int i = 0; i < RuleIds.length; i++) {

                String RuleId = URLDecoder.decode(RuleIds[i]);
                String Effect = Effects[i];
                String description2 = description2s[i];
                String MatchId4 = URLDecoder.decode(MatchId4s[i]);
                String MatchId5 = URLDecoder.decode(MatchId5s[i]);
                String MatchId6 = URLDecoder.decode(MatchId6s[i]);

                String DataType4 = URLDecoder.decode(DataType4s[i]);
                String DataType5 = URLDecoder.decode(DataType5s[i]);
                String DataType6 = URLDecoder.decode(DataType6s[i]);

                String value4 = URLDecoder.decode(value4s[i]);
                String value5 = URLDecoder.decode(value5s[i]);
                String value6 = URLDecoder.decode(value6s[i]);

                String AttributeId4 = URLDecoder.decode(AttributeId4s[i]);
                String AttributeId5 = URLDecoder.decode(AttributeId5s[i]);
                String AttributeId6 = URLDecoder.decode(AttributeId6s[i]);

                //为空处理
                if(AttributeId4.equals("")){
                    AttributeId4="id";
                }
                if(AttributeId5.equals("")){
                    AttributeId5="id";
                }if(AttributeId6.equals("")){
                    AttributeId6="id";
                }

                String MustBePresent4 = URLDecoder.decode(MustBePresent4s[i]);
                String MustBePresent5 = URLDecoder.decode(MustBePresent5s[i]);
                String MustBePresent6 = URLDecoder.decode(MustBePresent6s[i]);
                if (RuleId.equals("")) {
                    RuleId = "RuleId" + (i+1);
                }

                Element rule = doc.createElement("Rule");
                if (!(RuleId.equals("") && MatchId4.equals("null") && MatchId5.equals("null") && MatchId6.equals("null"))) {
                    //数组接受新增rule,循环添加即可

                    //policy子节点Rule

                    rule.setAttribute("RuleId", "urn:oasis:names:tc:xacml:2.0:conformance-test:" + RuleId);
                    rule.setAttribute("Effect", "Effect");

                    //Rule子节点description
                    description = doc.createElement("description");
                    description.setTextContent(description2);
                    rule.appendChild(description);

                    //Rule子节点Target
                    Element target1 = doc.createElement("Target");


                    //Rule的子节点User
                    Element anyof1 = doc.createElement("Anyof");
                    Element allof1 = doc.createElement("Allof");

                    Element match4 = doc.createElement("Match");
                    match4.setAttribute("MatchId", MatchId4);


                    Element attributeValue4 = doc.createElement("AttributeValue");
                    attributeValue4.setAttribute("DataType", DataType4);
                    attributeValue4.setTextContent(value4);                          //只改value

                    Element attributeDesignator4 = doc.createElement("AttributeDesignator");
                    attributeDesignator4.setAttribute("AttributeId", "urn:oasis:names:tc:xacml:3.0:subject:subject-"+AttributeId4);

                    attributeDesignator4.setAttribute("Catagory", "urn:oasis:names:tc:xacml:3.0:attribute-catagory:access-subject");
                    attributeDesignator4.setAttribute("DataType", DataType4);
                    attributeDesignator4.setAttribute("MustBePresent", MustBePresent4);

                    match4.appendChild(attributeValue4);
                    match4.appendChild(attributeDesignator4);

                    if (!MatchId4.equals("null")) {
                        allof1.appendChild(match4);
                        anyof1.appendChild(allof1);
                        target1.appendChild(anyof1);
                    }

                    //Rule的子节点Resource
                    Element anyof2 = doc.createElement("Anyof");
                    Element allof2 = doc.createElement("Allof");
                    //创建User子节点Match
                    Element match5 = doc.createElement("Match");
                    match5.setAttribute("MatchId", MatchId5);

                    Element attributeValue5 = doc.createElement("AttributeValue");
                    attributeValue5.setAttribute("DataType", DataType5);
                    attributeValue5.setTextContent(value5);

                    Element attributeDesignator5 = doc.createElement("AttributeDesignator");
                    attributeDesignator5.setAttribute("AttributeId", "urn:oasis:names:tc:xacml:3.0:resource:resource-"+AttributeId5);
                    attributeDesignator5.setAttribute("Catagory", "urn:oasis:names:tc:xacml:3.0:attribute-catagory:resource");
                    attributeDesignator5.setAttribute("DataType", DataType5);
                    attributeDesignator5.setAttribute("MustBePresent", MustBePresent5);

                    match5.appendChild(attributeValue5);
                    match5.appendChild(attributeDesignator5);

                    //非空处理
                    if (!MatchId5.equals("null")) {
                        allof2.appendChild(match5);
                        anyof2.appendChild(allof2);
                        target1.appendChild(anyof2);
                    }

                    //Target子节点Action
                    Element anyof3 = doc.createElement("Anyof");
                    Element allof3 = doc.createElement("Allof");
                    //创建User子节点Match
                    Element match6 = doc.createElement("Match");
                    match6.setAttribute("MatchId", MatchId6);

                    Element attributeValue6 = doc.createElement("AttributeValue");
                    attributeValue6 = doc.createElement("AttributeValue");
                    attributeValue6.setAttribute("DataType", DataType6);
                    attributeValue6.setTextContent(value6);

                    Element attributeDesignator6 = doc.createElement("AttributeDesignator");
                    attributeDesignator6.setAttribute("AttributeId", "urn:oasis:names:tc:xacml:3.0:action:action-"+AttributeId6);
                    attributeDesignator6.setAttribute("Catagory", "urn:oasis:names:tc:xacml:3.0:attribute-catagory:action");
                    attributeDesignator6.setAttribute("DataType", DataType6);
                    attributeDesignator6.setAttribute("MustBePresent", MustBePresent6);

                    match6.appendChild(attributeValue6);
                    match6.appendChild(attributeDesignator6);

                    if (!MatchId6.equals("null")) {
                        allof3.appendChild(match6);
                        anyof3.appendChild(allof3);
                        target1.appendChild(anyof3);
                    }

                    rule.appendChild(target1);
                } else {
                    rule.setTextContent("");
                }

                policy.appendChild(rule);
            }

            //根节点
            doc.appendChild(policy);


            //存储文件
            TransformerFactory transFactory = TransformerFactory.newInstance();

            Transformer transformer = transFactory.newTransformer();
            transformer.setOutputProperty("indent", "yes");

            DOMSource source = new DOMSource();
            source.setNode(doc);
            StreamResult result = new StreamResult();
            result.setOutputStream(new FileOutputStream("E://" + fileName + ".xml"));

            transformer.transform(source, result);
            System.out.println("successful");
            map.put("msg", "成功");
            return map;

        } catch (Exception e) {
            File file = new File("E://" + fileName + ".xml");
            file.delete();
            e.printStackTrace();
            map.put("msg", "失败");
            return map;
        }


    }

    //删除文件
    @RequestMapping("/delete")
    public @ResponseBody
    Map<String, String> delete(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Map<String, String> map = new HashMap<String, String>();
        try {
            String fileName = URLDecoder.decode(request.getParameter("PolicyId"));
            if (fileName.equals("")) {
                fileName = "PolicyId";
            }
            File file = new File("E://" + fileName + ".xml");
            if (!file.exists()) {
                map.put("msg", "删除文件失败：文件不存在");
                return map;
            }
            file.delete();
            map.put("msg", "成功删除文件");
            return map;
        } catch (Exception e) {
            e.printStackTrace();
            map.put("msg", "删除文件失败");
            return map;
        }

    }

    //用于测试SpringMVC与Ajax的交互
    @RequestMapping(value = "/login")
    public @ResponseBody
    Map<String, Object> login(HttpServletRequest request, HttpServletResponse response) throws IOException {

        System.out.println(request.getParameter("name"));
        Map<String, Object> map = new HashMap<String, Object>();

        if (request.getParameter("name").equals("123")) {
            System.out.println("成功");
            map.put("msg", "成功");
        } else {
            System.out.println("失败");
            map.put("msg", "失败");
        }
        //JSONObject result=JSONObject.fromObject(map);
        //response.getWriter().println(map);
        JSONObject result = JSONObject.fromObject(map);
        System.out.println(map);
        return result;
    }

    //用于测试上传文件功能
    @RequestMapping(value = "/uploadTest")
    public ModelAndView multiUpload(@RequestParam("file") MultipartFile file, HttpServletRequest request, HttpServletResponse response)
            throws IllegalStateException, IOException {
        String path = request.getServletContext().getRealPath("/XML/");
        //上传文件名
        String filename = file.getOriginalFilename();
        File filepath = new File(path, filename);
        //判断路径是否存在，如果不存在就创建一个
        if (!filepath.getParentFile().exists()) {
            filepath.getParentFile().mkdirs();
        }
        //将上传文件保存到一个目标文件当中
        file.transferTo(new File(path + File.separator + filename));
        return new ModelAndView("SUCCESS");
    }
}


