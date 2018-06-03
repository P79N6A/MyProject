<%@ page import="java.util.HashMap" %>
<%@ page import="java.util.TreeMap" %>
<%@ page language="java" contentType="text/html; charset=utf-8"
         pageEncoding="utf-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<!DOCTYPE html>
<html lang="en">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf8">
    <title>xacml策略管理系统</title>

    <link rel="stylesheet" href="//cdn.bootcss.com/bootstrap/3.3.5/css/bootstrap.min.css">

    <script  src="http://libs.baidu.com/jquery/1.7.2/jquery.min.js"></script>
    <script src="http://cdn.bootcss.com/bootstrap/3.2.0/js/bootstrap.min.js"></script>


    <script type="text/javascript" src="https://code.jquery.com/jquery-3.3.1.min.js"></script>
    <script type="text/javascript">

    </script>
</head>
<body>
<%
    HashMap<String,Object> map = (HashMap<String,Object>)request.getAttribute("map");
    TreeMap<Integer,TreeMap<String,String>> map1=(TreeMap<Integer,TreeMap<String,String>>)map.get("Rules");
    Integer size=(Integer)map.get("RulesCount");

%>
<div class="container-fluid">
    <nav class="navbar navbar-default" role="navigation">
        <div class="container-fluid">
            <div class="navbar-header">
                <a class="navbar-brand" href="#">xacml策略管理系统</a>
            </div>
        </div>
    </nav>
    <div class="row">
        <div class="col-lg-2" style=" background: #005cbf; height: 600px; text-align: center;">
            <div style="margin-top: 20px;">
                <button class="btn btn-default"  id="save" onclick="save();">保存策略</button><br>
                <button class="btn btn-default" id="delete" onclick="delete2();" style="margin-top: 20px;">删除策略</button>
            </div>
        </div>
        <div class="col-lg-10" style="background-color: #0c5460; height: 600px; overflow: auto;">
            <div class="container">
                <div class="panel-group" id="accordion">
                    <div class="panel panel-default">
                        <div class="panel-heading">
                            <h4 class="panel-title">
                                <a data-toggle="collapse" data-parent="#accordion"
                                   href="#collapseOne">
                                    policy
                                </a>
                            </h4>
                        </div>
                        <div id="collapseOne" class="panel-collapse collapse in">
                            <div class="panel-body">
                                <div class="form-group">
                                    <label for="PolicyId" class="col-sm-2 control-label">PolicyId</label>
                                    <div class="col-sm-10">
                                        <input type="text" class="form-control" id="PolicyId"
                                               placeholder=<%=map.get("PolicyId")%>>
                                    </div>
                                </div>
                                <div class="form-group">
                                    <label for="Version" class="col-sm-2 control-label">Version</label>
                                    <div class="col-sm-10">
                                        <input type="text" class="form-control" id="Version"
                                               placeholder=<%=map.get("Version")%>>
                                    </div>
                                </div>
                                <div class="form-group">
                                    <label class="col-sm-2 control-label">RuleCombiningAlgId</label>
                                    <div class="col-sm-10">
                                        <select class="form-control" id="RuleCombiningAlgId">
                                            <option value=<%=map.get("RuleCombiningAlgId")%>><%=map.get("RuleCombiningAlgId")%></option>
                                            <option value="urn:oasis:names:tc:xacml:3.0:rule-combining-algorithm:deny-overrides">
                                                urn:oasis:names:tc:xacml:3.0:rule-combining-algorithm:deny-overrides
                                            </option>
                                            <option value="rn:oasis:names:tc:xacml:3.0:rule-combining-algorithm:permit-overrides">
                                                rn:oasis:names:tc:xacml:3.0:rule-combining-algorithm:permit-overrides
                                            </option>
                                            <option value="urn:oasis:names:tc:xacml:1.0:rule-combining-algorithm:first-applicable">
                                                urn:oasis:names:tc:xacml:1.0:rule-combining-algorithm:first-applicable
                                            </option>
                                            <option value="applicableurn:oasis:names:tc:xacml:3.0:rule-combining-algorithm:ordered-deny-overrides">
                                                applicableurn:oasis:names:tc:xacml:3.0:rule-combining-algorithm:ordered-deny-overrides
                                            </option>
                                            <option value="urn:oasis:names:tc:xacml:3.0:rule-combining-algorithm:ordered-permit-overrides">
                                                urn:oasis:names:tc:xacml:3.0:rule-combining-algorithm:ordered-permit-overrides
                                            </option>
                                            <option value="urn:oasis:names:tc:xacml:3.0:rule-combining-algorithm:deny-unless-permit">
                                                urn:oasis:names:tc:xacml:3.0:rule-combining-algorithm:deny-unless-permit
                                            </option>
                                            <option value="urn:oasis:names:tc:xacml:3.0:rule-combining-algorithm:permit-unless-deny">
                                                urn:oasis:names:tc:xacml:3.0:rule-combining-algorithm:permit-unless-deny
                                            </option>
                                        </select>
                                    </div>
                                </div>
                                <div class="form-group">
                                    <label for="description1" class="col-sm-2 control-label">description</label>
                                    <div class="col-sm-10">
                                        <textarea class="form-control" rows="3" id="description1"><%=map.get("Description1")%></textarea>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div class="panel panel-default">
                        <div class="panel-heading">
                            <h4 class="panel-title">
                                <a data-toggle="collapse" data-parent="#accordion"
                                   href="#collapseTwo">
                                    Target
                                </a>
                            </h4>
                        </div>
                        <div id="collapseTwo" class="panel-collapse collapse">
                            <div class="panel-body">
                                <div class="panel panel-default">
                                    <div class="panel-heading">
                                        <h3 class="panel-title">
                                            User
                                        </h3>
                                    </div>
                                    <div class="panel-body">
                                        <div class="form-group">
                                            <label class="col-sm-2 control-label">MatchId</label>
                                            <div class="col-sm-10">
                                                <select class="form-control" id="MatchId1">
                                                    <option value=<%=map.get("MatchId1")%>><%=map.get("MatchId1")%></option>
                                                    <option value="null">null</option>
                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:string-equal">urn:oasis:names:tc:xacml:1.0:function:string-equal</option>
                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:boolean-equal">urn:oasis:names:tc:xacml:1.0:function:boolean-equal</option>
                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:boolean-equa">urn:oasis:names:tc:xacml:1.0:function:boolean-equa</option>
                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:integer-equal">urn:oasis:names:tc:xacml:1.0:function:integer-equal</option>
                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:double-equal">urn:oasis:names:tc:xacml:1.0:function:double-equal</option>
                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:date-equal">urn:oasis:names:tc:xacml:1.0:function:date-equal</option>
                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:time-equal">urn:oasis:names:tc:xacml:1.0:function:time-equal</option>
                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:dateTime-equal">urn:oasis:names:tc:xacml:1.0:function:dateTime-equal</option>
                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-equal">urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-equal</option>
                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-equal">urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-equal</option>
                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:anyURI-equal">urn:oasis:names:tc:xacml:1.0:function:anyURI-equal</option>
                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:x500Name-equal">urn:oasis:names:tc:xacml:1.0:function:x500Name-equal</option>
                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:rfc822Name-equal">urn:oasis:names:tc:xacml:1.0:function:rfc822Name-equal</option>
                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:hexBinary-equal">urn:oasis:names:tc:xacml:1.0:function:hexBinary-equal</option>
                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:base64Binary-equal">urn:oasis:names:tc:xacml:1.0:function:base64Binary-equal</option>
                                                </select>
                                            </div>
                                        </div>
                                        <div class="form-group">
                                            <label class="col-sm-2 control-label">Data Type</label>
                                            <div class="col-sm-10">
                                                <select class="form-control" id="DataType1">
                                                    <option value=<%=map.get("DataType1")%>><%=map.get("DataType1")%></option>
                                                    <option value="null">null</option>
                                                    <option value="http://www.w3.org/2001/XMLSchema#string">http://www.w3.org/2001/XMLSchema#string</option>
                                                    <option value="http://www.w3.org/2001/XMLSchema#boolean">http://www.w3.org/2001/XMLSchema#boolean</option>
                                                    <option value="http://www.w3.org/2001/XMLSchema#integer">http://www.w3.org/2001/XMLSchema#integer</option>
                                                    <option value="http://www.w3.org/2001/XMLSchema#double">http://www.w3.org/2001/XMLSchema#double</option>
                                                    <option value="http://www.w3.org/2001/XMLSchema#time">http://www.w3.org/2001/XMLSchema#time</option>
                                                    <option value="http://www.w3.org/2001/XMLSchema#date">http://www.w3.org/2001/XMLSchema#date</option>
                                                    <option value="http://www.w3.org/2001/XMLSchema#dateTime">http://www.w3.org/2001/XMLSchema#dateTime</option>
                                                    <option value="http://www.w3.org/2001/XMLSchema#dayTimeDuration">http://www.w3.org/2001/XMLSchema#dayTimeDuration</option>
                                                    <option value="http://www.w3.org/2001/XMLSchema#yearMonthDuration">http://www.w3.org/2001/XMLSchema#yearMonthDuration</option>
                                                    <option value="http://www.w3.org/2001/XMLSchema#anyURI">http://www.w3.org/2001/XMLSchema#anyURI</option>
                                                    <option value="http://www.w3.org/2001/XMLSchema#hexBinary">http://www.w3.org/2001/XMLSchema#hexBinary</option>
                                                    <option value="http://www.w3.org/2001/XMLSchema#base64Binary">http://www.w3.org/2001/XMLSchema#base64Binary</option>
                                                    <option value="urn:oasis:names:tc:xacml:1.0:data-type:rfc822Name">urn:oasis:names:tc:xacml:1.0:data-type:rfc822Name</option>
                                                    <option value="urn:oasis:names:tc:xacml:1.0:data-type:x500Name">urn:oasis:names:tc:xacml:1.0:data-type:x500Name</option>
                                                    <option value="urn:oasis:names:tc:xacml:3.0:data-type:xpathExpression">urn:oasis:names:tc:xacml:3.0:data-type:xpathExpression
                                                    </option>
                                                    <option value="urn:oasis:names:tc:xacml:2.0:data-type:ipAddress">urn:oasis:names:tc:xacml:2.0:data-type:ipAddress</option>
                                                    <option value="urn:oasis:names:tc:xacml:2.0:data-type:dnsName">urn:oasis:names:tc:xacml:2.0:data-type:dnsName</option>
                                                </select>
                                            </div>
                                        </div>
                                        <div class="form-group">
                                            <label for="value1" class="col-sm-2 control-label">value</label>
                                            <div class="col-sm-10">
                                                <input type="text" class="form-control" id="value1"
                                                       placeholder=<%=map.get("value1")%>>
                                            </div>
                                        </div>
                                        <div class="form-group">
                                            <label for="attributeId1" class="col-sm-2 control-label">AttributeId</label>
                                            <div class="col-sm-10">
                                                <input type="text" class="form-control" id="AttributeId1"
                                                       placeholder=<%=map.get("AttributeId1")%>>
                                            </div>
                                        </div>
                                        <div class="form-group">
                                            <label  class="col-sm-2 control-label">MustBePresent</label>
                                            <div class="col-sm-10">
                                                <select class="form-control" id="MustBePresent1">
                                                    <option value=<%=map.get("MustBePresent1")%>><%=map.get("MustBePresent1")%></option>
                                                    <option value="true">true</option>
                                                    <option value="false">false</option>
                                                </select>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                                <div class="panel panel-default">
                                    <div class="panel-heading">
                                        <h3 class="panel-title">
                                            Resource
                                        </h3>
                                    </div>
                                    <div class="panel-body">
                                        <div class="form-group">
                                            <label class="col-sm-2 control-label">MatchId</label>
                                            <div class="col-sm-10">
                                                <select class="form-control" id="MatchId2">
                                                    <option value=<%=map.get("MatchId2")%>><%=map.get("MatchId2")%></option>
                                                    <option value="null">null</option>
                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:string-equal">urn:oasis:names:tc:xacml:1.0:function:string-equal
                                                    </option>
                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:boolean-equal">
                                                        urn:oasis:names:tc:xacml:1.0:function:boolean-equal
                                                    </option>
                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:boolean-equa">urn:oasis:names:tc:xacml:1.0:function:boolean-equa
                                                    </option>
                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:integer-equal">
                                                        urn:oasis:names:tc:xacml:1.0:function:integer-equal
                                                    </option>
                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:double-equal">urn:oasis:names:tc:xacml:1.0:function:double-equal
                                                    </option>
                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:date-equal">urn:oasis:names:tc:xacml:1.0:function:date-equal
                                                    </option>
                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:time-equal">urn:oasis:names:tc:xacml:1.0:function:time-equal
                                                    </option>
                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:dateTime-equal">
                                                        urn:oasis:names:tc:xacml:1.0:function:dateTime-equal
                                                    </option>
                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-equal">
                                                        urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-equal
                                                    </option>
                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-equal">
                                                        urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-equal
                                                    </option>
                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:anyURI-equal">urn:oasis:names:tc:xacml:1.0:function:anyURI-equal
                                                    </option>
                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:x500Name-equal">
                                                        urn:oasis:names:tc:xacml:1.0:function:x500Name-equal
                                                    </option>
                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:rfc822Name-equal">
                                                        urn:oasis:names:tc:xacml:1.0:function:rfc822Name-equal
                                                    </option>
                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:hexBinary-equal">
                                                        urn:oasis:names:tc:xacml:1.0:function:hexBinary-equal
                                                    </option>
                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:base64Binary-equal">
                                                        urn:oasis:names:tc:xacml:1.0:function:base64Binary-equal
                                                    </option>
                                                </select>
                                            </div>
                                        </div>
                                        <div class="form-group">
                                            <label class="col-sm-2 control-label">Data Type</label>
                                            <div class="col-sm-10">
                                                <select class="form-control" id="DataType2">
                                                    <option value=<%=map.get("DataType2")%>><%=map.get("DataType2")%></option>
                                                    <option value="null">null</option>
                                                    <option value="http://www.w3.org/2001/XMLSchema#string">http://www.w3.org/2001/XMLSchema#string</option>
                                                    <option value="http://www.w3.org/2001/XMLSchema#boolean">http://www.w3.org/2001/XMLSchema#boolean</option>
                                                    <option value="http://www.w3.org/2001/XMLSchema#integer">http://www.w3.org/2001/XMLSchema#integer</option>
                                                    <option value="http://www.w3.org/2001/XMLSchema#double">http://www.w3.org/2001/XMLSchema#double</option>
                                                    <option value="http://www.w3.org/2001/XMLSchema#time">http://www.w3.org/2001/XMLSchema#time</option>
                                                    <option value="http://www.w3.org/2001/XMLSchema#date">http://www.w3.org/2001/XMLSchema#date</option>
                                                    <option value="http://www.w3.org/2001/XMLSchema#dateTime">http://www.w3.org/2001/XMLSchema#dateTime</option>
                                                    <option value="http://www.w3.org/2001/XMLSchema#dayTimeDuration">http://www.w3.org/2001/XMLSchema#dayTimeDuration
                                                    </option>
                                                    <option value="http://www.w3.org/2001/XMLSchema#yearMonthDuration">http://www.w3.org/2001/XMLSchema#yearMonthDuration
                                                    </option>
                                                    <option value="http://www.w3.org/2001/XMLSchema#anyURI">http://www.w3.org/2001/XMLSchema#anyURI</option>
                                                    <option value="http://www.w3.org/2001/XMLSchema#hexBinary">http://www.w3.org/2001/XMLSchema#hexBinary</option>
                                                    <option value="http://www.w3.org/2001/XMLSchema#base64Binary">http://www.w3.org/2001/XMLSchema#base64Binary
                                                    </option>
                                                    <option value="urn:oasis:names:tc:xacml:1.0:data-type:rfc822Name">urn:oasis:names:tc:xacml:1.0:data-type:rfc822Name
                                                    </option>
                                                    <option value="urn:oasis:names:tc:xacml:1.0:data-type:x500Name">urn:oasis:names:tc:xacml:1.0:data-type:x500Name
                                                    </option>
                                                    <option value="urn:oasis:names:tc:xacml:3.0:data-type:xpathExpression">
                                                        urn:oasis:names:tc:xacml:3.0:data-type:xpathExpression
                                                    </option>
                                                    <option value="urn:oasis:names:tc:xacml:2.0:data-type:ipAddress">urn:oasis:names:tc:xacml:2.0:data-type:ipAddress
                                                    </option>
                                                    <option value="urn:oasis:names:tc:xacml:2.0:data-type:dnsName">urn:oasis:names:tc:xacml:2.0:data-type:dnsName
                                                    </option>
                                                </select>
                                            </div>
                                        </div>
                                        <div class="form-group">
                                            <label for="value2" class="col-sm-2 control-label">value</label>
                                            <div class="col-sm-10">
                                                <input type="text" class="form-control" id="value2"
                                                       placeholder=<%=map.get("value2")%>>
                                            </div>
                                        </div>
                                        <div class="form-group">
                                            <label for="AttributeId2" class="col-sm-2 control-label">AttributeId</label>
                                            <div class="col-sm-10">
                                                <input type="text" class="form-control" id="AttributeId2"
                                                       placeholder=<%=map.get("AttributeId2")%>>
                                            </div>
                                        </div>
                                        <div class="form-group">
                                            <label  class="col-sm-2 control-label">MustBePresent</label>
                                            <div class="col-sm-10">
                                                <select class="form-control" id="MustBePresent2">
                                                    <option value=<%=map.get("MustBePresent2")%>><%=map.get("MustBePresent2")%></option>
                                                    <option value="true">true</option>
                                                    <option value="false">false</option>
                                                </select>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                                <div class="panel panel-default">
                                    <div class="panel-heading">
                                        <h3 class="panel-title">
                                            Action
                                        </h3>
                                    </div>
                                    <div class="panel-body">
                                        <div class="form-group">
                                            <label class="col-sm-2 control-label">MatchId</label>
                                            <div class="col-sm-10">
                                                <select class="form-control" id="MatchId3">
                                                    <option value=<%=map.get("MatchId3")%>><%=map.get("MatchId3")%></option>
                                                    <option value="null">null</option>
                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:string-equal">urn:oasis:names:tc:xacml:1.0:function:string-equal
                                                    </option>
                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:boolean-equal">
                                                        urn:oasis:names:tc:xacml:1.0:function:boolean-equal
                                                    </option>
                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:boolean-equal">urn:oasis:names:tc:xacml:1.0:function:boolean-equal
                                                    </option>
                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:integer-equal">
                                                        urn:oasis:names:tc:xacml:1.0:function:integer-equal
                                                    </option>
                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:double-equal">urn:oasis:names:tc:xacml:1.0:function:double-equal
                                                    </option>
                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:date-equal">urn:oasis:names:tc:xacml:1.0:function:date-equal
                                                    </option>
                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:time-equal">urn:oasis:names:tc:xacml:1.0:function:time-equal
                                                    </option>
                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:dateTime-equal">
                                                        urn:oasis:names:tc:xacml:1.0:function:dateTime-equal
                                                    </option>
                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-equal">
                                                        urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-equal
                                                    </option>
                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-equal">
                                                        urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-equal
                                                    </option>
                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:anyURI-equal">urn:oasis:names:tc:xacml:1.0:function:anyURI-equal
                                                    </option>
                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:x500Name-equal">
                                                        urn:oasis:names:tc:xacml:1.0:function:x500Name-equal
                                                    </option>
                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:rfc822Name-equal">
                                                        urn:oasis:names:tc:xacml:1.0:function:rfc822Name-equal
                                                    </option>
                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:hexBinary-equal">
                                                        urn:oasis:names:tc:xacml:1.0:function:hexBinary-equal
                                                    </option>
                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:base64Binary-equal">
                                                        urn:oasis:names:tc:xacml:1.0:function:base64Binary-equal
                                                    </option>
                                                </select>
                                            </div>
                                        </div>
                                        <div class="form-group">
                                            <label class="col-sm-2 control-label">Data Type</label>
                                            <div class="col-sm-10">
                                                <select class="form-control" id="DataType3">
                                                    <option value=<%=map.get("DataType3")%>><%=map.get("DataType3")%></option>
                                                    <option value="null">null</option>
                                                    <option value="http://www.w3.org/2001/XMLSchema#string">http://www.w3.org/2001/XMLSchema#string</option>
                                                    <option value="http://www.w3.org/2001/XMLSchema#boolean">http://www.w3.org/2001/XMLSchema#boolean</option>
                                                    <option value="http://www.w3.org/2001/XMLSchema#integer">http://www.w3.org/2001/XMLSchema#integer</option>
                                                    <option value="ahttp://www.w3.org/2001/XMLSchema#double">ahttp://www.w3.org/2001/XMLSchema#double</option>
                                                    <option value="http://www.w3.org/2001/XMLSchema#time">http://www.w3.org/2001/XMLSchema#time</option>
                                                    <option value="http://www.w3.org/2001/XMLSchema#date">http://www.w3.org/2001/XMLSchema#date</option>
                                                    <option value="http://www.w3.org/2001/XMLSchema#dateTime">http://www.w3.org/2001/XMLSchema#dateTime</option>
                                                    <option value="http://www.w3.org/2001/XMLSchema#dayTimeDuration">http://www.w3.org/2001/XMLSchema#dayTimeDuration
                                                    </option>
                                                    <option value="http://www.w3.org/2001/XMLSchema#yearMonthDuration">http://www.w3.org/2001/XMLSchema#yearMonthDuration
                                                    </option>
                                                    <option value="http://www.w3.org/2001/XMLSchema#anyURI">http://www.w3.org/2001/XMLSchema#anyURI</option>
                                                    <option value="http://www.w3.org/2001/XMLSchema#hexBinary">http://www.w3.org/2001/XMLSchema#hexBinary</option>
                                                    <option value="http://www.w3.org/2001/XMLSchema#base64Binary">http://www.w3.org/2001/XMLSchema#base64Binary
                                                    </option>
                                                    <option value="urn:oasis:names:tc:xacml:1.0:data-type:rfc822Name">urn:oasis:names:tc:xacml:1.0:data-type:rfc822Name
                                                    </option>
                                                    <option value="urn:oasis:names:tc:xacml:1.0:data-type:x500Name">urn:oasis:names:tc:xacml:1.0:data-type:x500Name
                                                    </option>
                                                    <option value="urn:oasis:names:tc:xacml:3.0:data-type:xpathExpression">
                                                        urn:oasis:names:tc:xacml:3.0:data-type:xpathExpression
                                                    </option>
                                                    <option value="urn:oasis:names:tc:xacml:2.0:data-type:ipAddress">urn:oasis:names:tc:xacml:2.0:data-type:ipAddress
                                                    </option>
                                                    <option value="urn:oasis:names:tc:xacml:2.0:data-type:ipAddress">urn:oasis:names:tc:xacml:2.0:data-type:ipAddress
                                                    </option>
                                                </select>
                                            </div>
                                        </div>
                                        <div class="form-group">
                                            <label for="value3" class="col-sm-2 control-label">value</label>
                                            <div class="col-sm-10">
                                                <input type="text" class="form-control" id="value3"
                                                       placeholder=<%=map.get("value3")%>>
                                            </div>
                                        </div>
                                        <div class="form-group">
                                            <label for="AttributeId3" class="col-sm-2 control-label">AttributeId</label>
                                            <div class="col-sm-10">
                                                <input type="text" class="form-control" id="AttributeId3"
                                                       placeholder=<%=map.get("AttributeId3")%>>
                                            </div>
                                        </div>
                                        <div class="form-group">
                                            <label  class="col-sm-2 control-label">MustBePresent</label>
                                            <div class="col-sm-10">
                                                <select class="form-control" id="MustBePresent3">
                                                    <option value=<%=map.get("MustBePresent3")%>><%=map.get("MustBePresent3")%></option>
                                                    <option value="true">true</option>
                                                    <option value="false">false</option>
                                                </select>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>

                    <div class="panel panel-default">
                        <div class="panel-heading">
                            <h4 class="panel-title">
                                <a data-toggle="collapse" data-parent="#accordion"
                                   href="#collapseThree">
                                    Rule(s)
                                </a>
                            </h4>
                        </div>
                        <div id="collapseThree" class="panel-collapse collapse">
                            <div class="panel-body">
                                <div id="formpart">
                                    <%
                                        for(int i=0;i<(Integer) map.get("RulesCount");i++)
                                        {
                                    %>
                                    <div class="panel panel-default">
                                        <div class="panel-heading">
                                            <h4 class="panel-title">
                                                <a data-toggle="collapse" data-parent="#accordio"  HREF="#rule<%=i+1%>">
                                                    rule<%=i+1%>
                                                </a>
                                            </h4>
                                        </div>

                                        <div id="rule<%=i+1%>" class="panel-collapse collapse">
                                            <div class="panel-body">
                                                <div class="panel panel-default">
                                                    <div class="panel-body">
                                                        <div class="form-group">
                                                            <label for="RuleId<%=i+1%>"
                                                                   class="col-sm-2 control-label">RuleId</label>
                                                            <div class="col-sm-10">
                                                                <input type="text" class="form-control" id="RuleId<%=i+1%>" placeholder=<%=map1.get(i).get("RuleId")%>>
                                                            </div>
                                                        </div>
                                                        <div class="form-group">
                                                            <label class="col-sm-2 control-label">Effect</label>
                                                            <div class="col-sm-10">
                                                                <select class="form-control" id="Effect<%=i+1%>">
                                                                    <option value=<%=map1.get(i).get("Effect")%>><%=map1.get(i).get("Effect")%></option>
                                                                    <option value="permit">permit</option>
                                                                    <option value="deny">deny</option>
                                                                </select>
                                                            </div>
                                                        </div>
                                                        <div class="form-group">
                                                            <label class="col-sm-2 control-label">description</label>
                                                            <div class="col-sm-10">
                                                                <textarea class="form-control" rows="3" id="description2<%=i+1%>"><%=map1.get(i).get("description")%></textarea>
                                                            </div>
                                                        </div>
                                                    </div>
                                                </div>
                                                <div class="panel panel-default">
                                                    <div class="panel-heading">
                                                        <h3 class="panel-title">
                                                            User
                                                        </h3>
                                                    </div>
                                                    <div class="panel-body">
                                                        <div class="form-group">
                                                            <label class="col-sm-2 control-label">MatchId</label>
                                                            <div class="col-sm-10">
                                                                <select class="form-control" id="MatchId4<%=i+1%>">
                                                                    <option value=<%=map1.get(i).get("MatchId4")%>><%=map1.get(i).get("MatchId4")%></option>
                                                                    <option value="null">null</option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:string-equal">urn:oasis:names:tc:xacml:1.0:function:string-equal</option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:boolean-equal">urn:oasis:names:tc:xacml:1.0:function:boolean-equal </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:integer-equal">urn:oasis:names:tc:xacml:1.0:function:integer-equal </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:double-equal">urn:oasis:names:tc:xacml:1.0:function:double-equal </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:date-equal">urn:oasis:names:tc:xacml:1.0:function:date-equal</option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:time-equal">urn:oasis:names:tc:xacml:1.0:function:time-equal </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:dateTime-equal">urn:oasis:names:tc:xacml:1.0:function:dateTime-equal </option>
                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-equal">urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-equal </option>
                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-equal ">urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-equal </option>
                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:string-equal-ignore-case">urn:oasis:names:tc:xacml:3.0:function:string-equal-ignore-case </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:anyURI-equal">urn:oasis:names:tc:xacml:1.0:function:anyURI-equal </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:x500Name-equal">urn:oasis:names:tc:xacml:1.0:function:x500Name-equal </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:rfc822Name-equal">urn:oasis:names:tc:xacml:1.0:function:rfc822Name-equal </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:hexBinary-equal ">urn:oasis:names:tc:xacml:1.0:function:hexBinary-equal </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:base64Binary-equal">urn:oasis:names:tc:xacml:1.0:function:base64Binary-equal </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:integer-add ">urn:oasis:names:tc:xacml:1.0:function:integer-add </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:double-add ">urn:oasis:names:tc:xacml:1.0:function:double-add </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:integer-subtract">urn:oasis:names:tc:xacml:1.0:function:integer-subtract </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:double-subtract">urn:oasis:names:tc:xacml:1.0:function:double-subtract </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:integer-multiply">urn:oasis:names:tc:xacml:1.0:function:integer-multiply </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:double-multiply">urn:oasis:names:tc:xacml:1.0:function:double-multiply </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:integer-divide">urn:oasis:names:tc:xacml:1.0:function:integer-divide </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:double-divide">urn:oasis:names:tc:xacml:1.0:function:double-divide </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:integer-mod">urn:oasis:names:tc:xacml:1.0:function:integer-mod </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:integer-abs">urn:oasis:names:tc:xacml:1.0:function:integer-abs </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:double-abs">urn:oasis:names:tc:xacml:1.0:function:double-abs </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:round">urn:oasis:names:tc:xacml:1.0:function:round </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:floorurn:oasis:names:tc:xacml:1.0:function:string-normalize-space">urn:oasis:names:tc:xacml:1.0:function:floorurn:oasis:names:tc:xacml:1.0:function:string-normalize-space </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:string-normalize-to-lower-case">urn:oasis:names:tc:xacml:1.0:function:string-normalize-to-lower-case </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:double-to-integer ">urn:oasis:names:tc:xacml:1.0:function:double-to-integer </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:integer-to-double">urn:oasis:names:tc:xacml:1.0:function:integer-to-double </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:or">urn:oasis:names:tc:xacml:1.0:function:or </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:and">urn:oasis:names:tc:xacml:1.0:function:and </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:n-of">urn:oasis:names:tc:xacml:1.0:function:n-of </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:noturn:oasis:names:tc:xacml:1.0:function:integer-greater-than ">urn:oasis:names:tc:xacml:1.0:function:noturn:oasis:names:tc:xacml:1.0:function:integer-greater-than </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:integer-greater-than-or-equal ">urn:oasis:names:tc:xacml:1.0:function:integer-greater-than-or-equal </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:integer-greater-than-or-equal ">urn:oasis:names:tc:xacml:1.0:function:integer-greater-than-or-equal  </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:integer-less-than-or-equal">urn:oasis:names:tc:xacml:1.0:function:integer-less-than-or-equal </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:double-greater-than">urn:oasis:names:tc:xacml:1.0:function:double-greater-than </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:double-greater-than-or-equal">urn:oasis:names:tc:xacml:1.0:function:double-greater-than-or-equal </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:double-less-than">urn:oasis:names:tc:xacml:1.0:function:double-less-than </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:double-less-than-or-equal">urn:oasis:names:tc:xacml:1.0:function:double-less-than-or-equal </option>
                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:dateTime-add-dayTimeDuration">urn:oasis:names:tc:xacml:3.0:function:dateTime-add-dayTimeDuration </option>
                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:dateTime-add-yearMonthDuration ">urn:oasis:names:tc:xacml:3.0:function:dateTime-add-yearMonthDuration </option>
                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:dateTime-subtract-dayTimeDuration">urn:oasis:names:tc:xacml:3.0:function:dateTime-subtract-dayTimeDuration  </option>
                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:dateTime-subtractyearMonthDuration">urn:oasis:names:tc:xacml:3.0:function:dateTime-subtractyearMonthDuration </option>
                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:date-add-yearMonthDuration">urn:oasis:names:tc:xacml:3.0:function:date-add-yearMonthDuration </option>
                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:date-subtract-yearMonthDuration">urn:oasis:names:tc:xacml:3.0:function:date-subtract-yearMonthDuration </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:string-greater-than">urn:oasis:names:tc:xacml:1.0:function:string-greater-than </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:string-greater-than-or-equal">urn:oasis:names:tc:xacml:1.0:function:string-greater-than-or-equal </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:string-less-than">urn:oasis:names:tc:xacml:1.0:function:string-less-than </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:string-less-than-or-equal">urn:oasis:names:tc:xacml:1.0:function:string-less-than-or-equal </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:time-greater-than ">urn:oasis:names:tc:xacml:1.0:function:time-greater-than </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:time-greater-than-or-equal">urn:oasis:names:tc:xacml:1.0:function:time-greater-than-or-equal </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:time-less-than">urn:oasis:names:tc:xacml:1.0:function:time-less-than </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:time-less-than-or-equal">urn:oasis:names:tc:xacml:1.0:function:time-less-than-or-equal </option>
                                                                    <option value="urn:oasis:names:tc:xacml:2.0:function:time-in-range ">urn:oasis:names:tc:xacml:2.0:function:time-in-range </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:dateTime-greater-than">urn:oasis:names:tc:xacml:1.0:function:dateTime-greater-than </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:dateTime-greater-than-or-equal ">urn:oasis:names:tc:xacml:1.0:function:dateTime-greater-than-or-equal </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:dateTime-less-than">urn:oasis:names:tc:xacml:1.0:function:dateTime-less-than </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:dateTime-less-than-or-equal">urn:oasis:names:tc:xacml:1.0:function:dateTime-less-than-or-equal </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:date-greater-than ">urn:oasis:names:tc:xacml:1.0:function:date-greater-than </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:date-greater-than-or-equal">urn:oasis:names:tc:xacml:1.0:function:date-greater-than-or-equal </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:date-less-than">urn:oasis:names:tc:xacml:1.0:function:date-less-than </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:date-less-than-or-equal ">urn:oasis:names:tc:xacml:1.0:function:date-less-than-or-equal </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:string-one-and-only ">urn:oasis:names:tc:xacml:1.0:function:string-one-and-only </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:string-bag-size">urn:oasis:names:tc:xacml:1.0:function:string-bag-size </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:string-is-in ">urn:oasis:names:tc:xacml:1.0:function:string-is-in </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:string-bag">urn:oasis:names:tc:xacml:1.0:function:string-bag </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:boolean-one-and-only">urn:oasis:names:tc:xacml:1.0:function:boolean-one-and-only </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:boolean-bag-size">urn:oasis:names:tc:xacml:1.0:function:boolean-bag-size </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:boolean-is-in ">urn:oasis:names:tc:xacml:1.0:function:boolean-is-in </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:boolean-bag">urn:oasis:names:tc:xacml:1.0:function:boolean-bag </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:integer-one-and-only">urn:oasis:names:tc:xacml:1.0:function:integer-one-and-only </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:integer-bag-size">urn:oasis:names:tc:xacml:1.0:function:integer-bag-size</option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:integer-is-in">urn:oasis:names:tc:xacml:1.0:function:integer-is-in </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:integer-bag ">urn:oasis:names:tc:xacml:1.0:function:integer-bag </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:double-one-and-only">urn:oasis:names:tc:xacml:1.0:function:double-one-and-only </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:double-bag-size">urn:oasis:names:tc:xacml:1.0:function:double-bag-size </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:double-is-in">urn:oasis:names:tc:xacml:1.0:function:double-is-in </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:double-bag">urn:oasis:names:tc:xacml:1.0:function:double-bag </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:time-one-and-only ">urn:oasis:names:tc:xacml:1.0:function:time-one-and-only </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:time-bag-size">urn:oasis:names:tc:xacml:1.0:function:time-bag-size </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:time-is-in ">urn:oasis:names:tc:xacml:1.0:function:time-is-in </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:time-bag">urn:oasis:names:tc:xacml:1.0:function:time-bag </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:date-one-and-only ">urn:oasis:names:tc:xacml:1.0:function:date-one-and-only </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:date-bag-size ">urn:oasis:names:tc:xacml:1.0:function:date-bag-size </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:date-is-in ">urn:oasis:names:tc:xacml:1.0:function:date-is-in </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:date-bag ">urn:oasis:names:tc:xacml:1.0:function:date-bag </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:dateTime-one-and-only">urn:oasis:names:tc:xacml:1.0:function:dateTime-one-and-only </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:dateTime-bag-size">urn:oasis:names:tc:xacml:1.0:function:dateTime-bag-size </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:dateTime-is-in ">urn:oasis:names:tc:xacml:1.0:function:dateTime-is-in </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:dateTime-bag">urn:oasis:names:tc:xacml:1.0:function:dateTime-bag </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:anyURI-one-and-only">urn:oasis:names:tc:xacml:1.0:function:anyURI-one-and-only </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:anyURI-one-and-only">urn:oasis:names:tc:xacml:1.0:function:anyURI-one-and-only</option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:anyURI-is-in">urn:oasis:names:tc:xacml:1.0:function:anyURI-is-in </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:anyURI-bag">urn:oasis:names:tc:xacml:1.0:function:anyURI-bag </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:hexBinary-one-and-only">urn:oasis:names:tc:xacml:1.0:function:hexBinary-one-and-only </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:hexBinary-bag-size">urn:oasis:names:tc:xacml:1.0:function:hexBinary-bag-size </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:hexBinary-is-in">urn:oasis:names:tc:xacml:1.0:function:hexBinary-is-in </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:hexBinary-bag">urn:oasis:names:tc:xacml:1.0:function:hexBinary-bag </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:base64Binary-one-and-only">urn:oasis:names:tc:xacml:1.0:function:base64Binary-one-and-only </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:base64Binary-bag-size">urn:oasis:names:tc:xacml:1.0:function:base64Binary-bag-size </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:base64Binary-is-in">urn:oasis:names:tc:xacml:1.0:function:base64Binary-is-in </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:base64Binary-bag">urn:oasis:names:tc:xacml:1.0:function:base64Binary-bag </option>
                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-one-and-only">urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-one-and-only </option>
                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-bag-size">urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-bag-size </option>
                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-is-in ">urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-is-in </option>
                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-bag">urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-bag </option>
                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-one-and-only">urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-one-and-only </option>
                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-bag-size">urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-bag-size </option>
                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-is-in">urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-is-in </option>
                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-bag">urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-bag </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:x500Name-one-and-only">urn:oasis:names:tc:xacml:1.0:function:x500Name-one-and-only </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:x500Name-one-and-only">urn:oasis:names:tc:xacml:1.0:function:x500Name-one-and-only </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:x500Name-is-in">urn:oasis:names:tc:xacml:1.0:function:x500Name-is-in </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:x500Name-bag">urn:oasis:names:tc:xacml:1.0:function:x500Name-bag </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:rfc822Name-one-and-only">urn:oasis:names:tc:xacml:1.0:function:rfc822Name-one-and-only </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:rfc822Name-bag-size">urn:oasis:names:tc:xacml:1.0:function:rfc822Name-bag-size </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:rfc822Name-is-in">urn:oasis:names:tc:xacml:1.0:function:rfc822Name-is-in </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:rfc822Name-bag ">urn:oasis:names:tc:xacml:1.0:function:rfc822Name-bag </option>
                                                                    <option value="urn:oasis:names:tc:xacml:2.0:function:ipAddress-one-and-only">urn:oasis:names:tc:xacml:2.0:function:ipAddress-one-and-only </option>
                                                                    <option value="urn:oasis:names:tc:xacml:2.0:function:ipAddress-bag-size">urn:oasis:names:tc:xacml:2.0:function:ipAddress-bag-size </option>
                                                                    <option value="urn:oasis:names:tc:xacml:2.0:function:ipAddress-bag ">urn:oasis:names:tc:xacml:2.0:function:ipAddress-bag </option>
                                                                    <option value="urn:oasis:names:tc:xacml:2.0:function:dnsName-one-and-only">urn:oasis:names:tc:xacml:2.0:function:dnsName-one-and-only </option>
                                                                    <option value="urn:oasis:names:tc:xacml:2.0:function:dnsName-bag-size">urn:oasis:names:tc:xacml:2.0:function:dnsName-bag-size </option>
                                                                    <option value="urn:oasis:names:tc:xacml:2.0:function:dnsName-bag">urn:oasis:names:tc:xacml:2.0:function:dnsName-bag </option>
                                                                    <option value="urn:oasis:names:tc:xacml:2.0:function:string-concatenate">urn:oasis:names:tc:xacml:2.0:function:string-concatenate </option>
                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:boolean-from-string">urn:oasis:names:tc:xacml:3.0:function:boolean-from-string </option>
                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:string-from-boolean">urn:oasis:names:tc:xacml:3.0:function:string-from-boolean </option>
                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:integer-from-string ">urn:oasis:names:tc:xacml:3.0:function:integer-from-string </option>
                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:string-from-integer ">urn:oasis:names:tc:xacml:3.0:function:string-from-integer </option>
                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:string-from-double">urn:oasis:names:tc:xacml:3.0:function:string-from-double</option>
                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:string-from-double">urn:oasis:names:tc:xacml:3.0:function:string-from-double </option>
                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:time-from-string">urn:oasis:names:tc:xacml:3.0:function:time-from-string </option>
                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:string-from-time">urn:oasis:names:tc:xacml:3.0:function:string-from-time </option>
                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:date-from-string">urn:oasis:names:tc:xacml:3.0:function:date-from-string </option>
                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:string-from-date">urn:oasis:names:tc:xacml:3.0:function:string-from-date </option>
                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:dateTime-from-string">urn:oasis:names:tc:xacml:3.0:function:dateTime-from-string </option>
                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:string-from-dateTime">urn:oasis:names:tc:xacml:3.0:function:string-from-dateTime </option>
                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:anyURI-from-string ">urn:oasis:names:tc:xacml:3.0:function:anyURI-from-string </option>
                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:string-from-anyURI">urn:oasis:names:tc:xacml:3.0:function:string-from-anyURI </option>
                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-from-string">urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-from-string </option>
                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:string-from-dayTimeDuration ">urn:oasis:names:tc:xacml:3.0:function:string-from-dayTimeDuration </option>
                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-from-string ">urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-from-string </option>
                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:string-from-yearMonthDuration">urn:oasis:names:tc:xacml:3.0:function:string-from-yearMonthDuration </option>
                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:x500Name-from-string ">urn:oasis:names:tc:xacml:3.0:function:x500Name-from-string </option>
                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:string-from-x500Name">urn:oasis:names:tc:xacml:3.0:function:string-from-x500Name </option>
                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:rfc822Name-from-string">urn:oasis:names:tc:xacml:3.0:function:rfc822Name-from-string </option>
                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:string-from-rfc822Name">urn:oasis:names:tc:xacml:3.0:function:string-from-rfc822Name </option>
                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:ipAddress-from-string ">urn:oasis:names:tc:xacml:3.0:function:ipAddress-from-string </option>
                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:string-from-ipAddress">urn:oasis:names:tc:xacml:3.0:function:string-from-ipAddress </option>
                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:dnsName-from-string">urn:oasis:names:tc:xacml:3.0:function:dnsName-from-string </option>
                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:string-from-dnsName ">urn:oasis:names:tc:xacml:3.0:function:string-from-dnsName </option>
                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:string-starts-with">urn:oasis:names:tc:xacml:3.0:function:string-starts-with </option>
                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:anyURI-starts-with">urn:oasis:names:tc:xacml:3.0:function:anyURI-starts-with </option>
                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:string-ends-with ">urn:oasis:names:tc:xacml:3.0:function:string-ends-with </option>
                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:anyURI-ends-with">urn:oasis:names:tc:xacml:3.0:function:anyURI-ends-with </option>
                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:string-contains">urn:oasis:names:tc:xacml:3.0:function:string-contains </option>
                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:anyURI-contains">urn:oasis:names:tc:xacml:3.0:function:anyURI-contains </option>
                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:string-substring ">urn:oasis:names:tc:xacml:3.0:function:string-substring </option>
                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:anyURI-substring ">urn:oasis:names:tc:xacml:3.0:function:anyURI-substring </option>
                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:any-of ">urn:oasis:names:tc:xacml:3.0:function:any-of </option>
                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:all-of">urn:oasis:names:tc:xacml:3.0:function:all-of </option>
                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:any-of-any ">urn:oasis:names:tc:xacml:3.0:function:any-of-any </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:all-of-any">urn:oasis:names:tc:xacml:1.0:function:all-of-any </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:any-of-all">urn:oasis:names:tc:xacml:1.0:function:any-of-all  </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:all-of-all">urn:oasis:names:tc:xacml:1.0:function:all-of-all  </option>
                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:map">urn:oasis:names:tc:xacml:3.0:function:map </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:x500Name-match">urn:oasis:names:tc:xacml:1.0:function:x500Name-match  </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:rfc822Name-match">urn:oasis:names:tc:xacml:1.0:function:rfc822Name-match  </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:string-regexp-match">urn:oasis:names:tc:xacml:1.0:function:string-regexp-match  </option>
                                                                    <option value="urn:oasis:names:tc:xacml:2.0:function:anyURI-regexp-match ">urn:oasis:names:tc:xacml:2.0:function:anyURI-regexp-match  </option>
                                                                    <option value="urn:oasis:names:tc:xacml:2.0:function:ipAddress-regexp-match">urn:oasis:names:tc:xacml:2.0:function:ipAddress-regexp-match  </option>
                                                                    <option value="urn:oasis:names:tc:xacml:2.0:function:dnsName-regexp-match">urn:oasis:names:tc:xacml:2.0:function:dnsName-regexp-match  </option>
                                                                    <option value="urn:oasis:names:tc:xacml:2.0:function:rfc822Name-regexp-match ">urn:oasis:names:tc:xacml:2.0:function:rfc822Name-regexp-match  </option>
                                                                    <option value="urn:oasis:names:tc:xacml:2.0:function:x500Name-regexp-match">urn:oasis:names:tc:xacml:2.0:function:x500Name-regexp-match  </option>
                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:xpath-node-count">urn:oasis:names:tc:xacml:3.0:function:xpath-node-count </option>
                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:xpath-node-equal ">urn:oasis:names:tc:xacml:3.0:function:xpath-node-equal </option>
                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:xpath-node-match">urn:oasis:names:tc:xacml:3.0:function:xpath-node-match </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:string-intersection">urn:oasis:names:tc:xacml:1.0:function:string-intersection </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:string-at-least-one-member-of">urn:oasis:names:tc:xacml:1.0:function:string-at-least-one-member-of </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:string-union">urn:oasis:names:tc:xacml:1.0:function:string-union </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:string-subset">urn:oasis:names:tc:xacml:1.0:function:string-subset </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:string-set-equals">urn:oasis:names:tc:xacml:1.0:function:string-set-equals </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:boolean-intersection ">urn:oasis:names:tc:xacml:1.0:function:boolean-intersection </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:boolean-at-least-one-member-of">urn:oasis:names:tc:xacml:1.0:function:boolean-at-least-one-member-of </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:boolean-union">urn:oasis:names:tc:xacml:1.0:function:boolean-union </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:boolean-subset ">urn:oasis:names:tc:xacml:1.0:function:boolean-subset </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:boolean-set-equals ">urn:oasis:names:tc:xacml:1.0:function:boolean-set-equals </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:integer-intersection">urn:oasis:names:tc:xacml:1.0:function:integer-intersection</option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:integer-at-least-one-member-of ">urn:oasis:names:tc:xacml:1.0:function:integer-at-least-one-member-of  </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:integer-union ">urn:oasis:names:tc:xacml:1.0:function:integer-union  </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:integer-subset">urn:oasis:names:tc:xacml:1.0:function:integer-subset  </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:integer-set-equals">urn:oasis:names:tc:xacml:1.0:function:integer-set-equals  </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:double-intersection">urn:oasis:names:tc:xacml:1.0:function:double-intersection  </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:double-at-least-one-member-of">urn:oasis:names:tc:xacml:1.0:function:double-at-least-one-member-of  </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:double-union ">urn:oasis:names:tc:xacml:1.0:function:double-union  </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:double-subset">urn:oasis:names:tc:xacml:1.0:function:double-subset  </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:double-set-equals">urn:oasis:names:tc:xacml:1.0:function:double-set-equals  </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:time-intersection ">urn:oasis:names:tc:xacml:1.0:function:time-intersection  </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:time-at-least-one-member-of">urn:oasis:names:tc:xacml:1.0:function:time-at-least-one-member-of  </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:time-union ">urn:oasis:names:tc:xacml:1.0:function:time-union  </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:time-subset">urn:oasis:names:tc:xacml:1.0:function:time-subset  </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:time-set-equals">urn:oasis:names:tc:xacml:1.0:function:time-set-equals  </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:date-intersection">urn:oasis:names:tc:xacml:1.0:function:date-intersection  </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:date-at-least-one-member-of ">urn:oasis:names:tc:xacml:1.0:function:date-at-least-one-member-of  </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:date-union">urn:oasis:names:tc:xacml:1.0:function:date-union  </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:date-subset ">urn:oasis:names:tc:xacml:1.0:function:date-subset  </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:date-set-equals">urn:oasis:names:tc:xacml:1.0:function:date-set-equals  </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:dateTime-intersection">urn:oasis:names:tc:xacml:1.0:function:dateTime-intersection  </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:dateTime-at-least-one-member-of ">urn:oasis:names:tc:xacml:1.0:function:dateTime-at-least-one-member-of  </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:dateTime-union ">urn:oasis:names:tc:xacml:1.0:function:dateTime-union  </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:dateTime-subset ">urn:oasis:names:tc:xacml:1.0:function:dateTime-subset  </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:dateTime-set-equals ">urn:oasis:names:tc:xacml:1.0:function:dateTime-set-equals  </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:anyURI-intersection">urn:oasis:names:tc:xacml:1.0:function:anyURI-intersection  </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:anyURI-at-least-one-member-of ">urn:oasis:names:tc:xacml:1.0:function:anyURI-at-least-one-member-of  </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:anyURI-union ">urn:oasis:names:tc:xacml:1.0:function:anyURI-union  </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:anyURI-subset">urn:oasis:names:tc:xacml:1.0:function:anyURI-subset  </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:anyURI-set-equals ">urn:oasis:names:tc:xacml:1.0:function:anyURI-set-equals  </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:hexBinary-intersection ">urn:oasis:names:tc:xacml:1.0:function:hexBinary-intersection  </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:hexBinary-at-least-one-member-of ">urn:oasis:names:tc:xacml:1.0:function:hexBinary-at-least-one-member-of  </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:hexBinary-union ">urn:oasis:names:tc:xacml:1.0:function:hexBinary-union  </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:hexBinary-subset">urn:oasis:names:tc:xacml:1.0:function:hexBinary-subset  </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:hexBinary-set-equals">urn:oasis:names:tc:xacml:1.0:function:hexBinary-set-equals  </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:base64Binary-intersection ">urn:oasis:names:tc:xacml:1.0:function:base64Binary-intersection  </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:base64Binary-at-least-one-memberof">urn:oasis:names:tc:xacml:1.0:function:base64Binary-at-least-one-memberof  </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:base64Binary-union">urn:oasis:names:tc:xacml:1.0:function:base64Binary-union  </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:base64Binary-subset">urn:oasis:names:tc:xacml:1.0:function:base64Binary-subset  </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:base64Binary-set-equals ">urn:oasis:names:tc:xacml:1.0:function:base64Binary-set-equals  </option>
                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-intersection ">urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-intersection  </option>
                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-at-least-onemember-of">urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-at-least-onemember-of  </option>
                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-union  ">urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-union  </option>
                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-subset ">urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-subset  </option>
                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-set-equals">urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-set-equals  </option>
                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-intersection ">urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-intersection  </option>
                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-at-least-onemember-of">urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-at-least-onemember-of  </option>
                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-union  ">urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-union  </option>
                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-subset">urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-subset  </option>
                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-set-equals  ">urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-set-equals  </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:x500Name-intersection">urn:oasis:names:tc:xacml:1.0:function:x500Name-intersection  </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:x500Name-at-least-one-member-of">urn:oasis:names:tc:xacml:1.0:function:x500Name-at-least-one-member-of  </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:x500Name-union">urn:oasis:names:tc:xacml:1.0:function:x500Name-union  </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:x500Name-subset">urn:oasis:names:tc:xacml:1.0:function:x500Name-subset  </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:rfc822Name-intersection">urn:oasis:names:tc:xacml:1.0:function:rfc822Name-intersection </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:rfc822Name-at-least-one-member-of ">urn:oasis:names:tc:xacml:1.0:function:rfc822Name-at-least-one-member-of </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:rfc822Name-union ">urn:oasis:names:tc:xacml:1.0:function:rfc822Name-union </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:rfc822Name-subset">urn:oasis:names:tc:xacml:1.0:function:rfc822Name-subset </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:rfc822Name-set-equals">urn:oasis:names:tc:xacml:1.0:function:rfc822Name-set-equals </option>
                                                                </select>
                                                            </div>
                                                        </div>
                                                        <div class="form-group">
                                                            <label class="col-sm-2 control-label">Data Type</label>
                                                            <div class="col-sm-10">
                                                                <select class="form-control" id="DataType4<%=i+1%>">
                                                                    <option value=<%=map1.get(i).get("DataType4")%>><%=map1.get(i).get("DataType4")%></option>
                                                                    <option value="null">null</option>
                                                                    <option value="http://www.w3.org/2001/XMLSchema#string">http://www.w3.org/2001/XMLSchema#string
                                                                    </option>
                                                                    <option value="http://www.w3.org/2001/XMLSchema#boolean">http://www.w3.org/2001/XMLSchema#boolean
                                                                    </option>
                                                                    <option value="http://www.w3.org/2001/XMLSchema#integer">http://www.w3.org/2001/XMLSchema#integer
                                                                    </option>
                                                                    <option value="http://www.w3.org/2001/XMLSchema#double">http://www.w3.org/2001/XMLSchema#double
                                                                    </option>
                                                                    <option value="http://www.w3.org/2001/XMLSchema#time">http://www.w3.org/2001/XMLSchema#time
                                                                    </option>
                                                                    <option value="http://www.w3.org/2001/XMLSchema#date">http://www.w3.org/2001/XMLSchema#date
                                                                    </option>
                                                                    <option value="http://www.w3.org/2001/XMLSchema#dateTime">http://www.w3.org/2001/XMLSchema#dateTime
                                                                    </option>
                                                                    <option value="http://www.w3.org/2001/XMLSchema#dayTimeDuration">http://www.w3.org/2001/XMLSchema#dayTimeDuration
                                                                    </option>
                                                                    <option value="http://www.w3.org/2001/XMLSchema#yearMonthDuration">http://www.w3.org/2001/XMLSchema#yearMonthDuration
                                                                    </option>
                                                                    <option value="http://www.w3.org/2001/XMLSchema#anyURI">http://www.w3.org/2001/XMLSchema#anyURI
                                                                    </option>
                                                                    <option value="http://www.w3.org/2001/XMLSchema#hexBinary">http://www.w3.org/2001/XMLSchema#hexBinary
                                                                    </option>
                                                                    <option value="http://www.w3.org/2001/XMLSchema#base64Binary">http://www.w3.org/2001/XMLSchema#base64Binary
                                                                    </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:data-type:rfc822Name">urn:oasis:names:tc:xacml:1.0:data-type:rfc822Name
                                                                    </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:data-type:x500Name">urn:oasis:names:tc:xacml:1.0:data-type:x500Name
                                                                    </option>
                                                                    <option value="urn:oasis:names:tc:xacml:3.0:data-type:xpathExpression">urn:oasis:names:tc:xacml:3.0:data-type:xpathExpression
                                                                    </option>
                                                                    <option value="urn:oasis:names:tc:xacml:2.0:data-type:ipAddress">urn:oasis:names:tc:xacml:2.0:data-type:ipAddress
                                                                    </option>
                                                                    <option value="urn:oasis:names:tc:xacml:2.0:data-type:dnsName">urn:oasis:names:tc:xacml:2.0:data-type:dnsName
                                                                    </option>
                                                                </select>
                                                            </div>
                                                        </div>
                                                        <div class="form-group">
                                                            <label for="value4<%=i+1%>" class="col-sm-2 control-label">value</label>
                                                            <div class="col-sm-10">
                                                                <input type="text" class="form-control" id="value4<%=i+1%>" placeholder=<%=map1.get(i).get("value4")%>>
                                                            </div>
                                                        </div>
                                                        <div class="form-group">
                                                            <label for="AttributeId4<%=i+1%>" class="col-sm-2 control-label">AttributeId</label>
                                                            <div class="col-sm-10">
                                                                <input type="text" class="form-control" id="AttributeId4<%=i+1%>"
                                                                       placeholder=<%=map1.get(i).get("AttributeId4")%>>
                                                            </div>
                                                        </div>
                                                        <div class="form-group">
                                                            <label  class="col-sm-2 control-label">MustBePresent</label>
                                                            <div class="col-sm-10">
                                                                <select class="form-control" id="MustBePresent4<%=i+1%>">
                                                                    <option value=<%=map1.get(i).get("MustBePresent4")%>><%=map1.get(i).get("MustBePresent4")%></option>
                                                                    <option value="true">true</option>
                                                                    <option value="false">false</option>
                                                                </select>
                                                            </div>
                                                        </div>
                                                    </div>
                                                </div>
                                                <div class="panel panel-default">
                                                    <div class="panel-heading">
                                                        <h3 class="panel-title">
                                                            Resource
                                                        </h3>
                                                    </div>
                                                    <div class="panel-body">
                                                        <div class="form-group">
                                                            <label class="col-sm-2 control-label">MatchId</label>
                                                            <div class="col-sm-10">
                                                                <select class="form-control" id="MatchId5<%=i+1%>">
                                                                    <option value=<%=map1.get(i).get("MatchId5")%>><%=map1.get(i).get("MatchId5")%></option>
                                                                    <option value="null">null</option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:string-equal">urn:oasis:names:tc:xacml:1.0:function:string-equal</option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:boolean-equal">urn:oasis:names:tc:xacml:1.0:function:boolean-equal </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:integer-equal">urn:oasis:names:tc:xacml:1.0:function:integer-equal </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:double-equal">urn:oasis:names:tc:xacml:1.0:function:double-equal </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:date-equal">urn:oasis:names:tc:xacml:1.0:function:date-equal</option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:time-equal">urn:oasis:names:tc:xacml:1.0:function:time-equal </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:dateTime-equal">urn:oasis:names:tc:xacml:1.0:function:dateTime-equal </option>
                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-equal">urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-equal </option>
                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-equal ">urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-equal </option>
                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:string-equal-ignore-case">urn:oasis:names:tc:xacml:3.0:function:string-equal-ignore-case </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:anyURI-equal">urn:oasis:names:tc:xacml:1.0:function:anyURI-equal </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:x500Name-equal">urn:oasis:names:tc:xacml:1.0:function:x500Name-equal </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:rfc822Name-equal">urn:oasis:names:tc:xacml:1.0:function:rfc822Name-equal </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:hexBinary-equal ">urn:oasis:names:tc:xacml:1.0:function:hexBinary-equal </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:base64Binary-equal">urn:oasis:names:tc:xacml:1.0:function:base64Binary-equal </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:integer-add ">urn:oasis:names:tc:xacml:1.0:function:integer-add </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:double-add ">urn:oasis:names:tc:xacml:1.0:function:double-add </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:integer-subtract">urn:oasis:names:tc:xacml:1.0:function:integer-subtract </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:double-subtract">urn:oasis:names:tc:xacml:1.0:function:double-subtract </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:integer-multiply">urn:oasis:names:tc:xacml:1.0:function:integer-multiply </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:double-multiply">urn:oasis:names:tc:xacml:1.0:function:double-multiply </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:integer-divide">urn:oasis:names:tc:xacml:1.0:function:integer-divide </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:double-divide">urn:oasis:names:tc:xacml:1.0:function:double-divide </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:integer-mod">urn:oasis:names:tc:xacml:1.0:function:integer-mod </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:integer-abs">urn:oasis:names:tc:xacml:1.0:function:integer-abs </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:double-abs">urn:oasis:names:tc:xacml:1.0:function:double-abs </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:round">urn:oasis:names:tc:xacml:1.0:function:round </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:floorurn:oasis:names:tc:xacml:1.0:function:string-normalize-space">urn:oasis:names:tc:xacml:1.0:function:floorurn:oasis:names:tc:xacml:1.0:function:string-normalize-space </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:string-normalize-to-lower-case">urn:oasis:names:tc:xacml:1.0:function:string-normalize-to-lower-case </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:double-to-integer ">urn:oasis:names:tc:xacml:1.0:function:double-to-integer </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:integer-to-double">urn:oasis:names:tc:xacml:1.0:function:integer-to-double </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:or">urn:oasis:names:tc:xacml:1.0:function:or </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:and">urn:oasis:names:tc:xacml:1.0:function:and </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:n-of">urn:oasis:names:tc:xacml:1.0:function:n-of </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:noturn:oasis:names:tc:xacml:1.0:function:integer-greater-than ">urn:oasis:names:tc:xacml:1.0:function:noturn:oasis:names:tc:xacml:1.0:function:integer-greater-than </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:integer-greater-than-or-equal ">urn:oasis:names:tc:xacml:1.0:function:integer-greater-than-or-equal </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:integer-greater-than-or-equal ">urn:oasis:names:tc:xacml:1.0:function:integer-greater-than-or-equal  </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:integer-less-than-or-equal">urn:oasis:names:tc:xacml:1.0:function:integer-less-than-or-equal </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:double-greater-than">urn:oasis:names:tc:xacml:1.0:function:double-greater-than </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:double-greater-than-or-equal">urn:oasis:names:tc:xacml:1.0:function:double-greater-than-or-equal </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:double-less-than">urn:oasis:names:tc:xacml:1.0:function:double-less-than </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:double-less-than-or-equal">urn:oasis:names:tc:xacml:1.0:function:double-less-than-or-equal </option>
                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:dateTime-add-dayTimeDuration">urn:oasis:names:tc:xacml:3.0:function:dateTime-add-dayTimeDuration </option>
                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:dateTime-add-yearMonthDuration ">urn:oasis:names:tc:xacml:3.0:function:dateTime-add-yearMonthDuration </option>
                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:dateTime-subtract-dayTimeDuration">urn:oasis:names:tc:xacml:3.0:function:dateTime-subtract-dayTimeDuration  </option>
                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:dateTime-subtractyearMonthDuration">urn:oasis:names:tc:xacml:3.0:function:dateTime-subtractyearMonthDuration </option>
                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:date-add-yearMonthDuration">urn:oasis:names:tc:xacml:3.0:function:date-add-yearMonthDuration </option>
                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:date-subtract-yearMonthDuration">urn:oasis:names:tc:xacml:3.0:function:date-subtract-yearMonthDuration </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:string-greater-than">urn:oasis:names:tc:xacml:1.0:function:string-greater-than </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:string-greater-than-or-equal">urn:oasis:names:tc:xacml:1.0:function:string-greater-than-or-equal </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:string-less-than">urn:oasis:names:tc:xacml:1.0:function:string-less-than </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:string-less-than-or-equal">urn:oasis:names:tc:xacml:1.0:function:string-less-than-or-equal </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:time-greater-than ">urn:oasis:names:tc:xacml:1.0:function:time-greater-than </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:time-greater-than-or-equal">urn:oasis:names:tc:xacml:1.0:function:time-greater-than-or-equal </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:time-less-than">urn:oasis:names:tc:xacml:1.0:function:time-less-than </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:time-less-than-or-equal">urn:oasis:names:tc:xacml:1.0:function:time-less-than-or-equal </option>
                                                                    <option value="urn:oasis:names:tc:xacml:2.0:function:time-in-range ">urn:oasis:names:tc:xacml:2.0:function:time-in-range </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:dateTime-greater-than">urn:oasis:names:tc:xacml:1.0:function:dateTime-greater-than </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:dateTime-greater-than-or-equal ">urn:oasis:names:tc:xacml:1.0:function:dateTime-greater-than-or-equal </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:dateTime-less-than">urn:oasis:names:tc:xacml:1.0:function:dateTime-less-than </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:dateTime-less-than-or-equal">urn:oasis:names:tc:xacml:1.0:function:dateTime-less-than-or-equal </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:date-greater-than ">urn:oasis:names:tc:xacml:1.0:function:date-greater-than </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:date-greater-than-or-equal">urn:oasis:names:tc:xacml:1.0:function:date-greater-than-or-equal </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:date-less-than">urn:oasis:names:tc:xacml:1.0:function:date-less-than </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:date-less-than-or-equal ">urn:oasis:names:tc:xacml:1.0:function:date-less-than-or-equal </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:string-one-and-only ">urn:oasis:names:tc:xacml:1.0:function:string-one-and-only </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:string-bag-size">urn:oasis:names:tc:xacml:1.0:function:string-bag-size </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:string-is-in ">urn:oasis:names:tc:xacml:1.0:function:string-is-in </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:string-bag">urn:oasis:names:tc:xacml:1.0:function:string-bag </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:boolean-one-and-only">urn:oasis:names:tc:xacml:1.0:function:boolean-one-and-only </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:boolean-bag-size">urn:oasis:names:tc:xacml:1.0:function:boolean-bag-size </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:boolean-is-in ">urn:oasis:names:tc:xacml:1.0:function:boolean-is-in </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:boolean-bag">urn:oasis:names:tc:xacml:1.0:function:boolean-bag </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:integer-one-and-only">urn:oasis:names:tc:xacml:1.0:function:integer-one-and-only </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:integer-bag-size">urn:oasis:names:tc:xacml:1.0:function:integer-bag-size</option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:integer-is-in">urn:oasis:names:tc:xacml:1.0:function:integer-is-in </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:integer-bag ">urn:oasis:names:tc:xacml:1.0:function:integer-bag </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:double-one-and-only">urn:oasis:names:tc:xacml:1.0:function:double-one-and-only </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:double-bag-size">urn:oasis:names:tc:xacml:1.0:function:double-bag-size </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:double-is-in">urn:oasis:names:tc:xacml:1.0:function:double-is-in </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:double-bag">urn:oasis:names:tc:xacml:1.0:function:double-bag </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:time-one-and-only ">urn:oasis:names:tc:xacml:1.0:function:time-one-and-only </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:time-bag-size">urn:oasis:names:tc:xacml:1.0:function:time-bag-size </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:time-is-in ">urn:oasis:names:tc:xacml:1.0:function:time-is-in </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:time-bag">urn:oasis:names:tc:xacml:1.0:function:time-bag </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:date-one-and-only ">urn:oasis:names:tc:xacml:1.0:function:date-one-and-only </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:date-bag-size ">urn:oasis:names:tc:xacml:1.0:function:date-bag-size </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:date-is-in ">urn:oasis:names:tc:xacml:1.0:function:date-is-in </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:date-bag ">urn:oasis:names:tc:xacml:1.0:function:date-bag </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:dateTime-one-and-only">urn:oasis:names:tc:xacml:1.0:function:dateTime-one-and-only </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:dateTime-bag-size">urn:oasis:names:tc:xacml:1.0:function:dateTime-bag-size </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:dateTime-is-in ">urn:oasis:names:tc:xacml:1.0:function:dateTime-is-in </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:dateTime-bag">urn:oasis:names:tc:xacml:1.0:function:dateTime-bag </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:anyURI-one-and-only">urn:oasis:names:tc:xacml:1.0:function:anyURI-one-and-only </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:anyURI-one-and-only">urn:oasis:names:tc:xacml:1.0:function:anyURI-one-and-only</option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:anyURI-is-in">urn:oasis:names:tc:xacml:1.0:function:anyURI-is-in </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:anyURI-bag">urn:oasis:names:tc:xacml:1.0:function:anyURI-bag </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:hexBinary-one-and-only">urn:oasis:names:tc:xacml:1.0:function:hexBinary-one-and-only </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:hexBinary-bag-size">urn:oasis:names:tc:xacml:1.0:function:hexBinary-bag-size </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:hexBinary-is-in">urn:oasis:names:tc:xacml:1.0:function:hexBinary-is-in </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:hexBinary-bag">urn:oasis:names:tc:xacml:1.0:function:hexBinary-bag </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:base64Binary-one-and-only">urn:oasis:names:tc:xacml:1.0:function:base64Binary-one-and-only </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:base64Binary-bag-size">urn:oasis:names:tc:xacml:1.0:function:base64Binary-bag-size </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:base64Binary-is-in">urn:oasis:names:tc:xacml:1.0:function:base64Binary-is-in </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:base64Binary-bag">urn:oasis:names:tc:xacml:1.0:function:base64Binary-bag </option>
                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-one-and-only">urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-one-and-only </option>
                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-bag-size">urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-bag-size </option>
                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-is-in ">urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-is-in </option>
                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-bag">urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-bag </option>
                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-one-and-only">urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-one-and-only </option>
                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-bag-size">urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-bag-size </option>
                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-is-in">urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-is-in </option>
                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-bag">urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-bag </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:x500Name-one-and-only">urn:oasis:names:tc:xacml:1.0:function:x500Name-one-and-only </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:x500Name-one-and-only">urn:oasis:names:tc:xacml:1.0:function:x500Name-one-and-only </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:x500Name-is-in">urn:oasis:names:tc:xacml:1.0:function:x500Name-is-in </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:x500Name-bag">urn:oasis:names:tc:xacml:1.0:function:x500Name-bag </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:rfc822Name-one-and-only">urn:oasis:names:tc:xacml:1.0:function:rfc822Name-one-and-only </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:rfc822Name-bag-size">urn:oasis:names:tc:xacml:1.0:function:rfc822Name-bag-size </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:rfc822Name-is-in">urn:oasis:names:tc:xacml:1.0:function:rfc822Name-is-in </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:rfc822Name-bag ">urn:oasis:names:tc:xacml:1.0:function:rfc822Name-bag </option>
                                                                    <option value="urn:oasis:names:tc:xacml:2.0:function:ipAddress-one-and-only">urn:oasis:names:tc:xacml:2.0:function:ipAddress-one-and-only </option>
                                                                    <option value="urn:oasis:names:tc:xacml:2.0:function:ipAddress-bag-size">urn:oasis:names:tc:xacml:2.0:function:ipAddress-bag-size </option>
                                                                    <option value="urn:oasis:names:tc:xacml:2.0:function:ipAddress-bag ">urn:oasis:names:tc:xacml:2.0:function:ipAddress-bag </option>
                                                                    <option value="urn:oasis:names:tc:xacml:2.0:function:dnsName-one-and-only">urn:oasis:names:tc:xacml:2.0:function:dnsName-one-and-only </option>
                                                                    <option value="urn:oasis:names:tc:xacml:2.0:function:dnsName-bag-size">urn:oasis:names:tc:xacml:2.0:function:dnsName-bag-size </option>
                                                                    <option value="urn:oasis:names:tc:xacml:2.0:function:dnsName-bag">urn:oasis:names:tc:xacml:2.0:function:dnsName-bag </option>
                                                                    <option value="urn:oasis:names:tc:xacml:2.0:function:string-concatenate">urn:oasis:names:tc:xacml:2.0:function:string-concatenate </option>
                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:boolean-from-string">urn:oasis:names:tc:xacml:3.0:function:boolean-from-string </option>
                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:string-from-boolean">urn:oasis:names:tc:xacml:3.0:function:string-from-boolean </option>
                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:integer-from-string ">urn:oasis:names:tc:xacml:3.0:function:integer-from-string </option>
                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:string-from-integer ">urn:oasis:names:tc:xacml:3.0:function:string-from-integer </option>
                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:string-from-double">urn:oasis:names:tc:xacml:3.0:function:string-from-double</option>
                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:string-from-double">urn:oasis:names:tc:xacml:3.0:function:string-from-double </option>
                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:time-from-string">urn:oasis:names:tc:xacml:3.0:function:time-from-string </option>
                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:string-from-time">urn:oasis:names:tc:xacml:3.0:function:string-from-time </option>
                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:date-from-string">urn:oasis:names:tc:xacml:3.0:function:date-from-string </option>
                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:string-from-date">urn:oasis:names:tc:xacml:3.0:function:string-from-date </option>
                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:dateTime-from-string">urn:oasis:names:tc:xacml:3.0:function:dateTime-from-string </option>
                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:string-from-dateTime">urn:oasis:names:tc:xacml:3.0:function:string-from-dateTime </option>
                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:anyURI-from-string ">urn:oasis:names:tc:xacml:3.0:function:anyURI-from-string </option>
                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:string-from-anyURI">urn:oasis:names:tc:xacml:3.0:function:string-from-anyURI </option>
                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-from-string">urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-from-string </option>
                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:string-from-dayTimeDuration ">urn:oasis:names:tc:xacml:3.0:function:string-from-dayTimeDuration </option>
                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-from-string ">urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-from-string </option>
                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:string-from-yearMonthDuration">urn:oasis:names:tc:xacml:3.0:function:string-from-yearMonthDuration </option>
                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:x500Name-from-string ">urn:oasis:names:tc:xacml:3.0:function:x500Name-from-string </option>
                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:string-from-x500Name">urn:oasis:names:tc:xacml:3.0:function:string-from-x500Name </option>
                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:rfc822Name-from-string">urn:oasis:names:tc:xacml:3.0:function:rfc822Name-from-string </option>
                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:string-from-rfc822Name">urn:oasis:names:tc:xacml:3.0:function:string-from-rfc822Name </option>
                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:ipAddress-from-string ">urn:oasis:names:tc:xacml:3.0:function:ipAddress-from-string </option>
                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:string-from-ipAddress">urn:oasis:names:tc:xacml:3.0:function:string-from-ipAddress </option>
                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:dnsName-from-string">urn:oasis:names:tc:xacml:3.0:function:dnsName-from-string </option>
                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:string-from-dnsName ">urn:oasis:names:tc:xacml:3.0:function:string-from-dnsName </option>
                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:string-starts-with">urn:oasis:names:tc:xacml:3.0:function:string-starts-with </option>
                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:anyURI-starts-with">urn:oasis:names:tc:xacml:3.0:function:anyURI-starts-with </option>
                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:string-ends-with ">urn:oasis:names:tc:xacml:3.0:function:string-ends-with </option>
                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:anyURI-ends-with">urn:oasis:names:tc:xacml:3.0:function:anyURI-ends-with </option>
                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:string-contains">urn:oasis:names:tc:xacml:3.0:function:string-contains </option>
                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:anyURI-contains">urn:oasis:names:tc:xacml:3.0:function:anyURI-contains </option>
                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:string-substring ">urn:oasis:names:tc:xacml:3.0:function:string-substring </option>
                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:anyURI-substring ">urn:oasis:names:tc:xacml:3.0:function:anyURI-substring </option>
                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:any-of ">urn:oasis:names:tc:xacml:3.0:function:any-of </option>
                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:all-of">urn:oasis:names:tc:xacml:3.0:function:all-of </option>
                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:any-of-any ">urn:oasis:names:tc:xacml:3.0:function:any-of-any </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:all-of-any">urn:oasis:names:tc:xacml:1.0:function:all-of-any </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:any-of-all">urn:oasis:names:tc:xacml:1.0:function:any-of-all  </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:all-of-all">urn:oasis:names:tc:xacml:1.0:function:all-of-all  </option>
                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:map">urn:oasis:names:tc:xacml:3.0:function:map </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:x500Name-match">urn:oasis:names:tc:xacml:1.0:function:x500Name-match  </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:rfc822Name-match">urn:oasis:names:tc:xacml:1.0:function:rfc822Name-match  </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:string-regexp-match">urn:oasis:names:tc:xacml:1.0:function:string-regexp-match  </option>
                                                                    <option value="urn:oasis:names:tc:xacml:2.0:function:anyURI-regexp-match ">urn:oasis:names:tc:xacml:2.0:function:anyURI-regexp-match  </option>
                                                                    <option value="urn:oasis:names:tc:xacml:2.0:function:ipAddress-regexp-match">urn:oasis:names:tc:xacml:2.0:function:ipAddress-regexp-match  </option>
                                                                    <option value="urn:oasis:names:tc:xacml:2.0:function:dnsName-regexp-match">urn:oasis:names:tc:xacml:2.0:function:dnsName-regexp-match  </option>
                                                                    <option value="urn:oasis:names:tc:xacml:2.0:function:rfc822Name-regexp-match ">urn:oasis:names:tc:xacml:2.0:function:rfc822Name-regexp-match  </option>
                                                                    <option value="urn:oasis:names:tc:xacml:2.0:function:x500Name-regexp-match">urn:oasis:names:tc:xacml:2.0:function:x500Name-regexp-match  </option>
                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:xpath-node-count">urn:oasis:names:tc:xacml:3.0:function:xpath-node-count </option>
                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:xpath-node-equal ">urn:oasis:names:tc:xacml:3.0:function:xpath-node-equal </option>
                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:xpath-node-match">urn:oasis:names:tc:xacml:3.0:function:xpath-node-match </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:string-intersection">urn:oasis:names:tc:xacml:1.0:function:string-intersection </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:string-at-least-one-member-of">urn:oasis:names:tc:xacml:1.0:function:string-at-least-one-member-of </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:string-union">urn:oasis:names:tc:xacml:1.0:function:string-union </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:string-subset">urn:oasis:names:tc:xacml:1.0:function:string-subset </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:string-set-equals">urn:oasis:names:tc:xacml:1.0:function:string-set-equals </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:boolean-intersection ">urn:oasis:names:tc:xacml:1.0:function:boolean-intersection </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:boolean-at-least-one-member-of">urn:oasis:names:tc:xacml:1.0:function:boolean-at-least-one-member-of </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:boolean-union">urn:oasis:names:tc:xacml:1.0:function:boolean-union </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:boolean-subset ">urn:oasis:names:tc:xacml:1.0:function:boolean-subset </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:boolean-set-equals ">urn:oasis:names:tc:xacml:1.0:function:boolean-set-equals </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:integer-intersection">urn:oasis:names:tc:xacml:1.0:function:integer-intersection</option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:integer-at-least-one-member-of ">urn:oasis:names:tc:xacml:1.0:function:integer-at-least-one-member-of  </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:integer-union ">urn:oasis:names:tc:xacml:1.0:function:integer-union  </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:integer-subset">urn:oasis:names:tc:xacml:1.0:function:integer-subset  </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:integer-set-equals">urn:oasis:names:tc:xacml:1.0:function:integer-set-equals  </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:double-intersection">urn:oasis:names:tc:xacml:1.0:function:double-intersection  </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:double-at-least-one-member-of">urn:oasis:names:tc:xacml:1.0:function:double-at-least-one-member-of  </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:double-union ">urn:oasis:names:tc:xacml:1.0:function:double-union  </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:double-subset">urn:oasis:names:tc:xacml:1.0:function:double-subset  </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:double-set-equals">urn:oasis:names:tc:xacml:1.0:function:double-set-equals  </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:time-intersection ">urn:oasis:names:tc:xacml:1.0:function:time-intersection  </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:time-at-least-one-member-of">urn:oasis:names:tc:xacml:1.0:function:time-at-least-one-member-of  </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:time-union ">urn:oasis:names:tc:xacml:1.0:function:time-union  </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:time-subset">urn:oasis:names:tc:xacml:1.0:function:time-subset  </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:time-set-equals">urn:oasis:names:tc:xacml:1.0:function:time-set-equals  </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:date-intersection">urn:oasis:names:tc:xacml:1.0:function:date-intersection  </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:date-at-least-one-member-of ">urn:oasis:names:tc:xacml:1.0:function:date-at-least-one-member-of  </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:date-union">urn:oasis:names:tc:xacml:1.0:function:date-union  </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:date-subset ">urn:oasis:names:tc:xacml:1.0:function:date-subset  </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:date-set-equals">urn:oasis:names:tc:xacml:1.0:function:date-set-equals  </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:dateTime-intersection">urn:oasis:names:tc:xacml:1.0:function:dateTime-intersection  </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:dateTime-at-least-one-member-of ">urn:oasis:names:tc:xacml:1.0:function:dateTime-at-least-one-member-of  </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:dateTime-union ">urn:oasis:names:tc:xacml:1.0:function:dateTime-union  </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:dateTime-subset ">urn:oasis:names:tc:xacml:1.0:function:dateTime-subset  </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:dateTime-set-equals ">urn:oasis:names:tc:xacml:1.0:function:dateTime-set-equals  </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:anyURI-intersection">urn:oasis:names:tc:xacml:1.0:function:anyURI-intersection  </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:anyURI-at-least-one-member-of ">urn:oasis:names:tc:xacml:1.0:function:anyURI-at-least-one-member-of  </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:anyURI-union ">urn:oasis:names:tc:xacml:1.0:function:anyURI-union  </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:anyURI-subset">urn:oasis:names:tc:xacml:1.0:function:anyURI-subset  </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:anyURI-set-equals ">urn:oasis:names:tc:xacml:1.0:function:anyURI-set-equals  </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:hexBinary-intersection ">urn:oasis:names:tc:xacml:1.0:function:hexBinary-intersection  </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:hexBinary-at-least-one-member-of ">urn:oasis:names:tc:xacml:1.0:function:hexBinary-at-least-one-member-of  </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:hexBinary-union ">urn:oasis:names:tc:xacml:1.0:function:hexBinary-union  </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:hexBinary-subset">urn:oasis:names:tc:xacml:1.0:function:hexBinary-subset  </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:hexBinary-set-equals">urn:oasis:names:tc:xacml:1.0:function:hexBinary-set-equals  </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:base64Binary-intersection ">urn:oasis:names:tc:xacml:1.0:function:base64Binary-intersection  </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:base64Binary-at-least-one-memberof">urn:oasis:names:tc:xacml:1.0:function:base64Binary-at-least-one-memberof  </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:base64Binary-union">urn:oasis:names:tc:xacml:1.0:function:base64Binary-union  </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:base64Binary-subset">urn:oasis:names:tc:xacml:1.0:function:base64Binary-subset  </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:base64Binary-set-equals ">urn:oasis:names:tc:xacml:1.0:function:base64Binary-set-equals  </option>
                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-intersection ">urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-intersection  </option>
                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-at-least-onemember-of">urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-at-least-onemember-of  </option>
                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-union  ">urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-union  </option>
                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-subset ">urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-subset  </option>
                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-set-equals">urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-set-equals  </option>
                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-intersection ">urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-intersection  </option>
                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-at-least-onemember-of">urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-at-least-onemember-of  </option>
                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-union  ">urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-union  </option>
                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-subset">urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-subset  </option>
                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-set-equals  ">urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-set-equals  </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:x500Name-intersection">urn:oasis:names:tc:xacml:1.0:function:x500Name-intersection  </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:x500Name-at-least-one-member-of">urn:oasis:names:tc:xacml:1.0:function:x500Name-at-least-one-member-of  </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:x500Name-union">urn:oasis:names:tc:xacml:1.0:function:x500Name-union  </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:x500Name-subset">urn:oasis:names:tc:xacml:1.0:function:x500Name-subset  </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:rfc822Name-intersection">urn:oasis:names:tc:xacml:1.0:function:rfc822Name-intersection </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:rfc822Name-at-least-one-member-of ">urn:oasis:names:tc:xacml:1.0:function:rfc822Name-at-least-one-member-of </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:rfc822Name-union ">urn:oasis:names:tc:xacml:1.0:function:rfc822Name-union </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:rfc822Name-subset">urn:oasis:names:tc:xacml:1.0:function:rfc822Name-subset </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:rfc822Name-set-equals">urn:oasis:names:tc:xacml:1.0:function:rfc822Name-set-equals </option>
                                                                </select>
                                                            </div>
                                                        </div>
                                                        <div class="form-group">
                                                            <label class="col-sm-2 control-label">Data
                                                                Type</label>
                                                            <div class="col-sm-10">
                                                                <select class="form-control" id="DataType5<%=i+1%>">
                                                                    <option value=<%=map1.get(i).get("DataType5")%>><%=map1.get(i).get("DataType5")%></option>
                                                                    <option value="null">null</option>
                                                                    <option value="http://www.w3.org/2001/XMLSchema#string">
                                                                        http://www.w3.org/2001/XMLSchema#string
                                                                    </option>
                                                                    <option value=" http://www.w3.org/2001/XMLSchema#boolean">
                                                                        http://www.w3.org/2001/XMLSchema#boolean
                                                                    </option>
                                                                    <option value="http://www.w3.org/2001/XMLSchema#integer">
                                                                        http://www.w3.org/2001/XMLSchema#integer
                                                                    </option>
                                                                    <option value="ahttp://www.w3.org/2001/XMLSchema#double">
                                                                        ahttp://www.w3.org/2001/XMLSchema#double
                                                                    </option>
                                                                    <option value="http://www.w3.org/2001/XMLSchema#time">
                                                                        http://www.w3.org/2001/XMLSchema#time
                                                                    </option>
                                                                    <option value="http://www.w3.org/2001/XMLSchema#date">
                                                                        http://www.w3.org/2001/XMLSchema#date
                                                                    </option>
                                                                    <option value="http://www.w3.og/2001/XMLSchema#dateTime">
                                                                        http://www.w3.og/2001/XMLSchema#dateTime
                                                                    </option>
                                                                    <option value="http://www.w3.org/2001/XMLSchema#dayTimeDuration">
                                                                        http://www.w3.org/2001/XMLSchema#dayTimeDuration
                                                                    </option>
                                                                    <option value="http://www.w3.org/2001/XMLSchema#yearMonthDuration">
                                                                        http://www.w3.org/2001/XMLSchema#yearMonthDuration
                                                                    </option>
                                                                    <option value="http://www.w3.org/2001/XMLSchema#anyURI">
                                                                        http://www.w3.org/2001/XMLSchema#anyURI
                                                                    </option>
                                                                    <option value="http://www.w3.org/2001/XMLSchema#hexBinary">
                                                                        http://www.w3.org/2001/XMLSchema#hexBinary
                                                                    </option>
                                                                    <option value="http://www.w3.org/2001/XMLSchema#base64Binary">
                                                                        http://www.w3.org/2001/XMLSchema#base64Binary
                                                                    </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:data-type:rfc822Name">
                                                                        urn:oasis:names:tc:xacml:1.0:data-type:rfc822Name
                                                                    </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:data-type:x500Name">
                                                                        urn:oasis:names:tc:xacml:1.0:data-type:x500Name
                                                                    </option>
                                                                    <option value="urn:oasis:names:tc:xacml:3.0:data-type:xpathExpression">
                                                                        urn:oasis:names:tc:xacml:3.0:data-type:xpathExpression
                                                                    </option>
                                                                    <option value="urn:oasis:names:tc:xacml:2.0:data-type:ipAddress">
                                                                        urn:oasis:names:tc:xacml:2.0:data-type:ipAddress
                                                                    </option>
                                                                    <option value="urn:oasis:names:tc:xacml:2.0:data-type:dnsName">
                                                                        urn:oasis:names:tc:xacml:2.0:data-type:dnsName
                                                                    </option>
                                                                </select>
                                                            </div>
                                                        </div>
                                                        <div class="form-group">
                                                            <label for="value5<%=i+1%>" class="col-sm-2 control-label">value</label>
                                                            <div class="col-sm-10">
                                                                <input type="text" class="form-control" id="value5<%=i+1%>" placeholder=<%=map1.get(i).get("value5")%>>
                                                            </div>
                                                        </div>
                                                        <div class="form-group">
                                                            <label for="AttributeId5<%=i+1%>" class="col-sm-2 control-label">AttributeId</label>
                                                            <div class="col-sm-10">
                                                                <input type="text" class="form-control" id="AttributeId5<%=i+1%>"
                                                                       placeholder=<%=map1.get(i).get("AttributeId5")%>>
                                                            </div>
                                                        </div>
                                                        <div class="form-group">
                                                            <label  class="col-sm-2 control-label">MustBePresent</label>
                                                            <div class="col-sm-10">
                                                                <select class="form-control" id="MustBePresent5<%=i+1%>">
                                                                    <option value=<%=map1.get(i).get("MustBePresent5")%>><%=map1.get(i).get("MustBePresent5")%></option>
                                                                    <option value="true">true</option>
                                                                    <option value="false">false</option>
                                                                </select>
                                                            </div>
                                                        </div>
                                                    </div>
                                                </div>
                                                <div class="panel panel-default">
                                                    <div class="panel-heading">
                                                        <h3 class="panel-title">
                                                            Action
                                                        </h3>
                                                    </div>
                                                    <div class="panel-body">
                                                        <div class="form-group">
                                                            <label class="col-sm-2 control-label">MatchId</label>
                                                            <div class="col-sm-10">
                                                                <select class="form-control" id="MatchId6<%=i+1%>">
                                                                    <option value=<%=map1.get(i).get("MatchId6")%>><%=map1.get(i).get("MatchId6")%></option>
                                                                    <option value="null">null</option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:string-equal">urn:oasis:names:tc:xacml:1.0:function:string-equal</option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:boolean-equal">urn:oasis:names:tc:xacml:1.0:function:boolean-equal </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:integer-equal">urn:oasis:names:tc:xacml:1.0:function:integer-equal </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:double-equal">urn:oasis:names:tc:xacml:1.0:function:double-equal </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:date-equal">urn:oasis:names:tc:xacml:1.0:function:date-equal</option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:time-equal">urn:oasis:names:tc:xacml:1.0:function:time-equal </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:dateTime-equal">urn:oasis:names:tc:xacml:1.0:function:dateTime-equal </option>
                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-equal">urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-equal </option>
                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-equal ">urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-equal </option>
                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:string-equal-ignore-case">urn:oasis:names:tc:xacml:3.0:function:string-equal-ignore-case </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:anyURI-equal">urn:oasis:names:tc:xacml:1.0:function:anyURI-equal </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:x500Name-equal">urn:oasis:names:tc:xacml:1.0:function:x500Name-equal </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:rfc822Name-equal">urn:oasis:names:tc:xacml:1.0:function:rfc822Name-equal </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:hexBinary-equal ">urn:oasis:names:tc:xacml:1.0:function:hexBinary-equal </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:base64Binary-equal">urn:oasis:names:tc:xacml:1.0:function:base64Binary-equal </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:integer-add ">urn:oasis:names:tc:xacml:1.0:function:integer-add </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:double-add ">urn:oasis:names:tc:xacml:1.0:function:double-add </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:integer-subtract">urn:oasis:names:tc:xacml:1.0:function:integer-subtract </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:double-subtract">urn:oasis:names:tc:xacml:1.0:function:double-subtract </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:integer-multiply">urn:oasis:names:tc:xacml:1.0:function:integer-multiply </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:double-multiply">urn:oasis:names:tc:xacml:1.0:function:double-multiply </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:integer-divide">urn:oasis:names:tc:xacml:1.0:function:integer-divide </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:double-divide">urn:oasis:names:tc:xacml:1.0:function:double-divide </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:integer-mod">urn:oasis:names:tc:xacml:1.0:function:integer-mod </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:integer-abs">urn:oasis:names:tc:xacml:1.0:function:integer-abs </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:double-abs">urn:oasis:names:tc:xacml:1.0:function:double-abs </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:round">urn:oasis:names:tc:xacml:1.0:function:round </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:floorurn:oasis:names:tc:xacml:1.0:function:string-normalize-space">urn:oasis:names:tc:xacml:1.0:function:floorurn:oasis:names:tc:xacml:1.0:function:string-normalize-space </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:string-normalize-to-lower-case">urn:oasis:names:tc:xacml:1.0:function:string-normalize-to-lower-case </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:double-to-integer ">urn:oasis:names:tc:xacml:1.0:function:double-to-integer </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:integer-to-double">urn:oasis:names:tc:xacml:1.0:function:integer-to-double </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:or">urn:oasis:names:tc:xacml:1.0:function:or </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:and">urn:oasis:names:tc:xacml:1.0:function:and </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:n-of">urn:oasis:names:tc:xacml:1.0:function:n-of </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:noturn:oasis:names:tc:xacml:1.0:function:integer-greater-than ">urn:oasis:names:tc:xacml:1.0:function:noturn:oasis:names:tc:xacml:1.0:function:integer-greater-than </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:integer-greater-than-or-equal ">urn:oasis:names:tc:xacml:1.0:function:integer-greater-than-or-equal </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:integer-greater-than-or-equal ">urn:oasis:names:tc:xacml:1.0:function:integer-greater-than-or-equal  </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:integer-less-than-or-equal">urn:oasis:names:tc:xacml:1.0:function:integer-less-than-or-equal </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:double-greater-than">urn:oasis:names:tc:xacml:1.0:function:double-greater-than </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:double-greater-than-or-equal">urn:oasis:names:tc:xacml:1.0:function:double-greater-than-or-equal </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:double-less-than">urn:oasis:names:tc:xacml:1.0:function:double-less-than </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:double-less-than-or-equal">urn:oasis:names:tc:xacml:1.0:function:double-less-than-or-equal </option>
                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:dateTime-add-dayTimeDuration">urn:oasis:names:tc:xacml:3.0:function:dateTime-add-dayTimeDuration </option>
                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:dateTime-add-yearMonthDuration ">urn:oasis:names:tc:xacml:3.0:function:dateTime-add-yearMonthDuration </option>
                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:dateTime-subtract-dayTimeDuration">urn:oasis:names:tc:xacml:3.0:function:dateTime-subtract-dayTimeDuration  </option>
                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:dateTime-subtractyearMonthDuration">urn:oasis:names:tc:xacml:3.0:function:dateTime-subtractyearMonthDuration </option>
                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:date-add-yearMonthDuration">urn:oasis:names:tc:xacml:3.0:function:date-add-yearMonthDuration </option>
                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:date-subtract-yearMonthDuration">urn:oasis:names:tc:xacml:3.0:function:date-subtract-yearMonthDuration </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:string-greater-than">urn:oasis:names:tc:xacml:1.0:function:string-greater-than </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:string-greater-than-or-equal">urn:oasis:names:tc:xacml:1.0:function:string-greater-than-or-equal </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:string-less-than">urn:oasis:names:tc:xacml:1.0:function:string-less-than </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:string-less-than-or-equal">urn:oasis:names:tc:xacml:1.0:function:string-less-than-or-equal </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:time-greater-than ">urn:oasis:names:tc:xacml:1.0:function:time-greater-than </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:time-greater-than-or-equal">urn:oasis:names:tc:xacml:1.0:function:time-greater-than-or-equal </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:time-less-than">urn:oasis:names:tc:xacml:1.0:function:time-less-than </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:time-less-than-or-equal">urn:oasis:names:tc:xacml:1.0:function:time-less-than-or-equal </option>
                                                                    <option value="urn:oasis:names:tc:xacml:2.0:function:time-in-range ">urn:oasis:names:tc:xacml:2.0:function:time-in-range </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:dateTime-greater-than">urn:oasis:names:tc:xacml:1.0:function:dateTime-greater-than </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:dateTime-greater-than-or-equal ">urn:oasis:names:tc:xacml:1.0:function:dateTime-greater-than-or-equal </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:dateTime-less-than">urn:oasis:names:tc:xacml:1.0:function:dateTime-less-than </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:dateTime-less-than-or-equal">urn:oasis:names:tc:xacml:1.0:function:dateTime-less-than-or-equal </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:date-greater-than ">urn:oasis:names:tc:xacml:1.0:function:date-greater-than </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:date-greater-than-or-equal">urn:oasis:names:tc:xacml:1.0:function:date-greater-than-or-equal </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:date-less-than">urn:oasis:names:tc:xacml:1.0:function:date-less-than </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:date-less-than-or-equal ">urn:oasis:names:tc:xacml:1.0:function:date-less-than-or-equal </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:string-one-and-only ">urn:oasis:names:tc:xacml:1.0:function:string-one-and-only </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:string-bag-size">urn:oasis:names:tc:xacml:1.0:function:string-bag-size </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:string-is-in ">urn:oasis:names:tc:xacml:1.0:function:string-is-in </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:string-bag">urn:oasis:names:tc:xacml:1.0:function:string-bag </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:boolean-one-and-only">urn:oasis:names:tc:xacml:1.0:function:boolean-one-and-only </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:boolean-bag-size">urn:oasis:names:tc:xacml:1.0:function:boolean-bag-size </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:boolean-is-in ">urn:oasis:names:tc:xacml:1.0:function:boolean-is-in </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:boolean-bag">urn:oasis:names:tc:xacml:1.0:function:boolean-bag </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:integer-one-and-only">urn:oasis:names:tc:xacml:1.0:function:integer-one-and-only </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:integer-bag-size">urn:oasis:names:tc:xacml:1.0:function:integer-bag-size</option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:integer-is-in">urn:oasis:names:tc:xacml:1.0:function:integer-is-in </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:integer-bag ">urn:oasis:names:tc:xacml:1.0:function:integer-bag </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:double-one-and-only">urn:oasis:names:tc:xacml:1.0:function:double-one-and-only </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:double-bag-size">urn:oasis:names:tc:xacml:1.0:function:double-bag-size </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:double-is-in">urn:oasis:names:tc:xacml:1.0:function:double-is-in </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:double-bag">urn:oasis:names:tc:xacml:1.0:function:double-bag </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:time-one-and-only ">urn:oasis:names:tc:xacml:1.0:function:time-one-and-only </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:time-bag-size">urn:oasis:names:tc:xacml:1.0:function:time-bag-size </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:time-is-in ">urn:oasis:names:tc:xacml:1.0:function:time-is-in </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:time-bag">urn:oasis:names:tc:xacml:1.0:function:time-bag </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:date-one-and-only ">urn:oasis:names:tc:xacml:1.0:function:date-one-and-only </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:date-bag-size ">urn:oasis:names:tc:xacml:1.0:function:date-bag-size </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:date-is-in ">urn:oasis:names:tc:xacml:1.0:function:date-is-in </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:date-bag ">urn:oasis:names:tc:xacml:1.0:function:date-bag </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:dateTime-one-and-only">urn:oasis:names:tc:xacml:1.0:function:dateTime-one-and-only </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:dateTime-bag-size">urn:oasis:names:tc:xacml:1.0:function:dateTime-bag-size </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:dateTime-is-in ">urn:oasis:names:tc:xacml:1.0:function:dateTime-is-in </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:dateTime-bag">urn:oasis:names:tc:xacml:1.0:function:dateTime-bag </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:anyURI-one-and-only">urn:oasis:names:tc:xacml:1.0:function:anyURI-one-and-only </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:anyURI-one-and-only">urn:oasis:names:tc:xacml:1.0:function:anyURI-one-and-only</option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:anyURI-is-in">urn:oasis:names:tc:xacml:1.0:function:anyURI-is-in </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:anyURI-bag">urn:oasis:names:tc:xacml:1.0:function:anyURI-bag </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:hexBinary-one-and-only">urn:oasis:names:tc:xacml:1.0:function:hexBinary-one-and-only </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:hexBinary-bag-size">urn:oasis:names:tc:xacml:1.0:function:hexBinary-bag-size </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:hexBinary-is-in">urn:oasis:names:tc:xacml:1.0:function:hexBinary-is-in </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:hexBinary-bag">urn:oasis:names:tc:xacml:1.0:function:hexBinary-bag </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:base64Binary-one-and-only">urn:oasis:names:tc:xacml:1.0:function:base64Binary-one-and-only </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:base64Binary-bag-size">urn:oasis:names:tc:xacml:1.0:function:base64Binary-bag-size </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:base64Binary-is-in">urn:oasis:names:tc:xacml:1.0:function:base64Binary-is-in </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:base64Binary-bag">urn:oasis:names:tc:xacml:1.0:function:base64Binary-bag </option>
                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-one-and-only">urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-one-and-only </option>
                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-bag-size">urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-bag-size </option>
                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-is-in ">urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-is-in </option>
                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-bag">urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-bag </option>
                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-one-and-only">urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-one-and-only </option>
                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-bag-size">urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-bag-size </option>
                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-is-in">urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-is-in </option>
                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-bag">urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-bag </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:x500Name-one-and-only">urn:oasis:names:tc:xacml:1.0:function:x500Name-one-and-only </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:x500Name-one-and-only">urn:oasis:names:tc:xacml:1.0:function:x500Name-one-and-only </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:x500Name-is-in">urn:oasis:names:tc:xacml:1.0:function:x500Name-is-in </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:x500Name-bag">urn:oasis:names:tc:xacml:1.0:function:x500Name-bag </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:rfc822Name-one-and-only">urn:oasis:names:tc:xacml:1.0:function:rfc822Name-one-and-only </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:rfc822Name-bag-size">urn:oasis:names:tc:xacml:1.0:function:rfc822Name-bag-size </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:rfc822Name-is-in">urn:oasis:names:tc:xacml:1.0:function:rfc822Name-is-in </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:rfc822Name-bag ">urn:oasis:names:tc:xacml:1.0:function:rfc822Name-bag </option>
                                                                    <option value="urn:oasis:names:tc:xacml:2.0:function:ipAddress-one-and-only">urn:oasis:names:tc:xacml:2.0:function:ipAddress-one-and-only </option>
                                                                    <option value="urn:oasis:names:tc:xacml:2.0:function:ipAddress-bag-size">urn:oasis:names:tc:xacml:2.0:function:ipAddress-bag-size </option>
                                                                    <option value="urn:oasis:names:tc:xacml:2.0:function:ipAddress-bag ">urn:oasis:names:tc:xacml:2.0:function:ipAddress-bag </option>
                                                                    <option value="urn:oasis:names:tc:xacml:2.0:function:dnsName-one-and-only">urn:oasis:names:tc:xacml:2.0:function:dnsName-one-and-only </option>
                                                                    <option value="urn:oasis:names:tc:xacml:2.0:function:dnsName-bag-size">urn:oasis:names:tc:xacml:2.0:function:dnsName-bag-size </option>
                                                                    <option value="urn:oasis:names:tc:xacml:2.0:function:dnsName-bag">urn:oasis:names:tc:xacml:2.0:function:dnsName-bag </option>
                                                                    <option value="urn:oasis:names:tc:xacml:2.0:function:string-concatenate">urn:oasis:names:tc:xacml:2.0:function:string-concatenate </option>
                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:boolean-from-string">urn:oasis:names:tc:xacml:3.0:function:boolean-from-string </option>
                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:string-from-boolean">urn:oasis:names:tc:xacml:3.0:function:string-from-boolean </option>
                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:integer-from-string ">urn:oasis:names:tc:xacml:3.0:function:integer-from-string </option>
                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:string-from-integer ">urn:oasis:names:tc:xacml:3.0:function:string-from-integer </option>
                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:string-from-double">urn:oasis:names:tc:xacml:3.0:function:string-from-double</option>
                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:string-from-double">urn:oasis:names:tc:xacml:3.0:function:string-from-double </option>
                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:time-from-string">urn:oasis:names:tc:xacml:3.0:function:time-from-string </option>
                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:string-from-time">urn:oasis:names:tc:xacml:3.0:function:string-from-time </option>
                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:date-from-string">urn:oasis:names:tc:xacml:3.0:function:date-from-string </option>
                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:string-from-date">urn:oasis:names:tc:xacml:3.0:function:string-from-date </option>
                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:dateTime-from-string">urn:oasis:names:tc:xacml:3.0:function:dateTime-from-string </option>
                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:string-from-dateTime">urn:oasis:names:tc:xacml:3.0:function:string-from-dateTime </option>
                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:anyURI-from-string ">urn:oasis:names:tc:xacml:3.0:function:anyURI-from-string </option>
                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:string-from-anyURI">urn:oasis:names:tc:xacml:3.0:function:string-from-anyURI </option>
                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-from-string">urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-from-string </option>
                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:string-from-dayTimeDuration ">urn:oasis:names:tc:xacml:3.0:function:string-from-dayTimeDuration </option>
                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-from-string ">urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-from-string </option>
                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:string-from-yearMonthDuration">urn:oasis:names:tc:xacml:3.0:function:string-from-yearMonthDuration </option>
                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:x500Name-from-string ">urn:oasis:names:tc:xacml:3.0:function:x500Name-from-string </option>
                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:string-from-x500Name">urn:oasis:names:tc:xacml:3.0:function:string-from-x500Name </option>
                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:rfc822Name-from-string">urn:oasis:names:tc:xacml:3.0:function:rfc822Name-from-string </option>
                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:string-from-rfc822Name">urn:oasis:names:tc:xacml:3.0:function:string-from-rfc822Name </option>
                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:ipAddress-from-string ">urn:oasis:names:tc:xacml:3.0:function:ipAddress-from-string </option>
                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:string-from-ipAddress">urn:oasis:names:tc:xacml:3.0:function:string-from-ipAddress </option>
                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:dnsName-from-string">urn:oasis:names:tc:xacml:3.0:function:dnsName-from-string </option>
                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:string-from-dnsName ">urn:oasis:names:tc:xacml:3.0:function:string-from-dnsName </option>
                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:string-starts-with">urn:oasis:names:tc:xacml:3.0:function:string-starts-with </option>
                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:anyURI-starts-with">urn:oasis:names:tc:xacml:3.0:function:anyURI-starts-with </option>
                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:string-ends-with ">urn:oasis:names:tc:xacml:3.0:function:string-ends-with </option>
                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:anyURI-ends-with">urn:oasis:names:tc:xacml:3.0:function:anyURI-ends-with </option>
                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:string-contains">urn:oasis:names:tc:xacml:3.0:function:string-contains </option>
                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:anyURI-contains">urn:oasis:names:tc:xacml:3.0:function:anyURI-contains </option>
                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:string-substring ">urn:oasis:names:tc:xacml:3.0:function:string-substring </option>
                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:anyURI-substring ">urn:oasis:names:tc:xacml:3.0:function:anyURI-substring </option>
                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:any-of ">urn:oasis:names:tc:xacml:3.0:function:any-of </option>
                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:all-of">urn:oasis:names:tc:xacml:3.0:function:all-of </option>
                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:any-of-any ">urn:oasis:names:tc:xacml:3.0:function:any-of-any </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:all-of-any">urn:oasis:names:tc:xacml:1.0:function:all-of-any </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:any-of-all">urn:oasis:names:tc:xacml:1.0:function:any-of-all  </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:all-of-all">urn:oasis:names:tc:xacml:1.0:function:all-of-all  </option>
                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:map">urn:oasis:names:tc:xacml:3.0:function:map </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:x500Name-match">urn:oasis:names:tc:xacml:1.0:function:x500Name-match  </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:rfc822Name-match">urn:oasis:names:tc:xacml:1.0:function:rfc822Name-match  </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:string-regexp-match">urn:oasis:names:tc:xacml:1.0:function:string-regexp-match  </option>
                                                                    <option value="urn:oasis:names:tc:xacml:2.0:function:anyURI-regexp-match ">urn:oasis:names:tc:xacml:2.0:function:anyURI-regexp-match  </option>
                                                                    <option value="urn:oasis:names:tc:xacml:2.0:function:ipAddress-regexp-match">urn:oasis:names:tc:xacml:2.0:function:ipAddress-regexp-match  </option>
                                                                    <option value="urn:oasis:names:tc:xacml:2.0:function:dnsName-regexp-match">urn:oasis:names:tc:xacml:2.0:function:dnsName-regexp-match  </option>
                                                                    <option value="urn:oasis:names:tc:xacml:2.0:function:rfc822Name-regexp-match ">urn:oasis:names:tc:xacml:2.0:function:rfc822Name-regexp-match  </option>
                                                                    <option value="urn:oasis:names:tc:xacml:2.0:function:x500Name-regexp-match">urn:oasis:names:tc:xacml:2.0:function:x500Name-regexp-match  </option>
                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:xpath-node-count">urn:oasis:names:tc:xacml:3.0:function:xpath-node-count </option>
                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:xpath-node-equal ">urn:oasis:names:tc:xacml:3.0:function:xpath-node-equal </option>
                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:xpath-node-match">urn:oasis:names:tc:xacml:3.0:function:xpath-node-match </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:string-intersection">urn:oasis:names:tc:xacml:1.0:function:string-intersection </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:string-at-least-one-member-of">urn:oasis:names:tc:xacml:1.0:function:string-at-least-one-member-of </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:string-union">urn:oasis:names:tc:xacml:1.0:function:string-union </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:string-subset">urn:oasis:names:tc:xacml:1.0:function:string-subset </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:string-set-equals">urn:oasis:names:tc:xacml:1.0:function:string-set-equals </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:boolean-intersection ">urn:oasis:names:tc:xacml:1.0:function:boolean-intersection </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:boolean-at-least-one-member-of">urn:oasis:names:tc:xacml:1.0:function:boolean-at-least-one-member-of </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:boolean-union">urn:oasis:names:tc:xacml:1.0:function:boolean-union </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:boolean-subset ">urn:oasis:names:tc:xacml:1.0:function:boolean-subset </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:boolean-set-equals ">urn:oasis:names:tc:xacml:1.0:function:boolean-set-equals </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:integer-intersection">urn:oasis:names:tc:xacml:1.0:function:integer-intersection</option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:integer-at-least-one-member-of ">urn:oasis:names:tc:xacml:1.0:function:integer-at-least-one-member-of  </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:integer-union ">urn:oasis:names:tc:xacml:1.0:function:integer-union  </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:integer-subset">urn:oasis:names:tc:xacml:1.0:function:integer-subset  </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:integer-set-equals">urn:oasis:names:tc:xacml:1.0:function:integer-set-equals  </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:double-intersection">urn:oasis:names:tc:xacml:1.0:function:double-intersection  </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:double-at-least-one-member-of">urn:oasis:names:tc:xacml:1.0:function:double-at-least-one-member-of  </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:double-union ">urn:oasis:names:tc:xacml:1.0:function:double-union  </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:double-subset">urn:oasis:names:tc:xacml:1.0:function:double-subset  </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:double-set-equals">urn:oasis:names:tc:xacml:1.0:function:double-set-equals  </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:time-intersection ">urn:oasis:names:tc:xacml:1.0:function:time-intersection  </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:time-at-least-one-member-of">urn:oasis:names:tc:xacml:1.0:function:time-at-least-one-member-of  </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:time-union ">urn:oasis:names:tc:xacml:1.0:function:time-union  </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:time-subset">urn:oasis:names:tc:xacml:1.0:function:time-subset  </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:time-set-equals">urn:oasis:names:tc:xacml:1.0:function:time-set-equals  </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:date-intersection">urn:oasis:names:tc:xacml:1.0:function:date-intersection  </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:date-at-least-one-member-of ">urn:oasis:names:tc:xacml:1.0:function:date-at-least-one-member-of  </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:date-union">urn:oasis:names:tc:xacml:1.0:function:date-union  </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:date-subset ">urn:oasis:names:tc:xacml:1.0:function:date-subset  </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:date-set-equals">urn:oasis:names:tc:xacml:1.0:function:date-set-equals  </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:dateTime-intersection">urn:oasis:names:tc:xacml:1.0:function:dateTime-intersection  </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:dateTime-at-least-one-member-of ">urn:oasis:names:tc:xacml:1.0:function:dateTime-at-least-one-member-of  </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:dateTime-union ">urn:oasis:names:tc:xacml:1.0:function:dateTime-union  </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:dateTime-subset ">urn:oasis:names:tc:xacml:1.0:function:dateTime-subset  </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:dateTime-set-equals ">urn:oasis:names:tc:xacml:1.0:function:dateTime-set-equals  </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:anyURI-intersection">urn:oasis:names:tc:xacml:1.0:function:anyURI-intersection  </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:anyURI-at-least-one-member-of ">urn:oasis:names:tc:xacml:1.0:function:anyURI-at-least-one-member-of  </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:anyURI-union ">urn:oasis:names:tc:xacml:1.0:function:anyURI-union  </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:anyURI-subset">urn:oasis:names:tc:xacml:1.0:function:anyURI-subset  </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:anyURI-set-equals ">urn:oasis:names:tc:xacml:1.0:function:anyURI-set-equals  </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:hexBinary-intersection ">urn:oasis:names:tc:xacml:1.0:function:hexBinary-intersection  </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:hexBinary-at-least-one-member-of ">urn:oasis:names:tc:xacml:1.0:function:hexBinary-at-least-one-member-of  </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:hexBinary-union ">urn:oasis:names:tc:xacml:1.0:function:hexBinary-union  </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:hexBinary-subset">urn:oasis:names:tc:xacml:1.0:function:hexBinary-subset  </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:hexBinary-set-equals">urn:oasis:names:tc:xacml:1.0:function:hexBinary-set-equals  </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:base64Binary-intersection ">urn:oasis:names:tc:xacml:1.0:function:base64Binary-intersection  </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:base64Binary-at-least-one-memberof">urn:oasis:names:tc:xacml:1.0:function:base64Binary-at-least-one-memberof  </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:base64Binary-union">urn:oasis:names:tc:xacml:1.0:function:base64Binary-union  </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:base64Binary-subset">urn:oasis:names:tc:xacml:1.0:function:base64Binary-subset  </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:base64Binary-set-equals ">urn:oasis:names:tc:xacml:1.0:function:base64Binary-set-equals  </option>
                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-intersection ">urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-intersection  </option>
                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-at-least-onemember-of">urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-at-least-onemember-of  </option>
                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-union  ">urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-union  </option>
                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-subset ">urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-subset  </option>
                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-set-equals">urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-set-equals  </option>
                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-intersection ">urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-intersection  </option>
                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-at-least-onemember-of">urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-at-least-onemember-of  </option>
                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-union  ">urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-union  </option>
                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-subset">urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-subset  </option>
                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-set-equals  ">urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-set-equals  </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:x500Name-intersection">urn:oasis:names:tc:xacml:1.0:function:x500Name-intersection  </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:x500Name-at-least-one-member-of">urn:oasis:names:tc:xacml:1.0:function:x500Name-at-least-one-member-of  </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:x500Name-union">urn:oasis:names:tc:xacml:1.0:function:x500Name-union  </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:x500Name-subset">urn:oasis:names:tc:xacml:1.0:function:x500Name-subset  </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:rfc822Name-intersection">urn:oasis:names:tc:xacml:1.0:function:rfc822Name-intersection </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:rfc822Name-at-least-one-member-of ">urn:oasis:names:tc:xacml:1.0:function:rfc822Name-at-least-one-member-of </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:rfc822Name-union ">urn:oasis:names:tc:xacml:1.0:function:rfc822Name-union </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:rfc822Name-subset">urn:oasis:names:tc:xacml:1.0:function:rfc822Name-subset </option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:rfc822Name-set-equals">urn:oasis:names:tc:xacml:1.0:function:rfc822Name-set-equals </option>
                                                                </select>
                                                            </div>
                                                        </div>
                                                        <div class="form-group">
                                                            <label class="col-sm-2 control-label">Data
                                                                Type</label>
                                                            <div class="col-sm-10">
                                                                <select class="form-control" id="DataType6<%=i+1%>">
                                                                    <option value=<%=map1.get(i).get("DataType6")%>><%=map1.get(i).get("DataType6")%></option>
                                                                    <option value="null">null</option>
                                                                    <option value="http://www.w3.org/2001/XMLSchema#string">http://www.w3.org/2001/XMLSchema#string</option>
                                                                    <option value="http://www.w3.org/2001/XMLSchema#boolean">http://www.w3.org/2001/XMLSchema#boolean</option>
                                                                    <option value="http://www.w3.org/2001/XMLSchema#integer">http://www.w3.org/2001/XMLSchema#integer</option>
                                                                    <option value="ahttp://www.w3.org/2001/XMLSchema#double">ahttp://www.w3.org/2001/XMLSchema#double</option>
                                                                    <option value="http://www.w3.org/2001/XMLSchema#time">http://www.w3.org/2001/XMLSchema#time</option>
                                                                    <option value="http://www.w3.org/2001/XMLSchema#date">http://www.w3.org/2001/XMLSchema#date</option>
                                                                    <option value="http://www.w3.org/2001/XMLSchema#dateTime">http://www.w3.org/2001/XMLSchema#dateTime</option>
                                                                    <option value="http://www.w3.org/2001/XMLSchema#dayTimeDuration">http://www.w3.org/2001/XMLSchema#dayTimeDuration</option>
                                                                    <option value="http://www.w3.org/2001/XMLSchema#yearMonthDuration">http://www.w3.org/2001/XMLSchema#yearMonthDuration</option>
                                                                    <option value="http://www.w3.org/2001/XMLSchema#anyURI">http://www.w3.org/2001/XMLSchema#anyURI</option>
                                                                    <option value="http://www.w3.org/2001/XMLSchema#hexBinary">http://www.w3.org/2001/XMLSchema#hexBinary</option>
                                                                    <option value="http://www.w3.org/2001/XMLSchema#base64Binary">http://www.w3.org/2001/XMLSchema#base64Binary</option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:data-type:rfc822Name">urn:oasis:names:tc:xacml:1.0:data-type:rfc822Name</option>
                                                                    <option value="urn:oasis:names:tc:xacml:1.0:data-type:x500Name">urn:oasis:names:tc:xacml:1.0:data-type:x500Name</option>
                                                                    <option value="urn:oasis:names:tc:xacml:3.0:data-type:xpathExpression">urn:oasis:names:tc:xacml:3.0:data-type:xpathExpression</option>
                                                                    <option value="urn:oasis:names:tc:xacml:2.0:data-type:ipAddress">urn:oasis:names:tc:xacml:2.0:data-type:ipAddress</option>
                                                                    <option value="urn:oasis:names:tc:xacml:2.0:data-type:dnsName">urn:oasis:names:tc:xacml:2.0:data-type:dnsName</option>
                                                                </select>
                                                            </div>
                                                        </div>
                                                        <div class="form-group">
                                                            <label for="value6<%=i+1%>" class="col-sm-2 control-label">value</label>
                                                            <div class="col-sm-10">
                                                                <input type="text" class="form-control" id="value6<%=i+1%>" placeholder=<%=map1.get(i).get("DataType6")%>>
                                                            </div>
                                                        </div>
                                                        <div class="form-group">
                                                            <label for="AttributeId6<%=i+1%>" class="col-sm-2 control-label">AttributeId</label>
                                                            <div class="col-sm-10">
                                                                <input type="text" class="form-control" id="AttributeId6<%=i+1%>"
                                                                       placeholder=<%=map1.get(i).get("AttributeId6")%>>
                                                            </div>
                                                        </div>
                                                        <div class="form-group">
                                                            <label  class="col-sm-2 control-label">MustBePresent</label>
                                                            <div class="col-sm-10">
                                                                <select class="form-control" id="MustBePresent6<%=i+1%>">
                                                                    <option value=<%=map1.get(i).get("MustBePresent6")%>><%=map1.get(i).get("MustBePresent6")%></option>
                                                                    <option value="true">true</option>
                                                                    <option value="false">false</option>
                                                                </select>
                                                            </div>
                                                        </div>
                                                    </div>
                                                </div>
                                            </div><br><br>

                                        </div>
                                    </div>
                                <%
                                    }
                                %>

                                </div>

                            </div>
                            <button class="btn btn-primary" id="addform">添加规则</button>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>
</body>
<script>

    $("#addform").click(function(){
        //没点击一次button rule数量加一
        var curr_size = document.getElementById("formpart").parentNode.children.length+<%=size%>;
        //添加的内容
        var addform =

'                    <div class="panel panel-default">\n' +
'                        <div class="panel-heading">\n' +
'                            <h4 class="panel-title">\n' +
'                                <a data-toggle="collapse" \n' +
'                                   href="#rule'+curr_size +'">\n' +
'                                    rule'+curr_size+'\n' +
'                                </a>\n' +
'                            </h4>\n' +
'                        </div>\n' +
'                                        <div id="rule'+curr_size +'" class="panel-collapse collapse">\n' +
'                                            <div class="panel-body">\n' +
'                                                <div class="panel panel-default">\n' +
'                                                    <div class="panel-body">\n' +
'                                                        <div class="form-group">\n' +
'                                                            <label for="RuleId'+curr_size +'"    \n' +
'                                                                   class="col-sm-2 control-label">RuleId</label>\n' +
'                                                            <div class="col-sm-10">\n' +
'                                                                <input type="text" class="form-control" id="RuleId'+curr_size +'" placeholder="RuleId">\n' +
'                                                            </div>\n' +
'                                                        </div>\n' +
'                                                        <div class="form-group">\n' +
'                                                            <label class="col-sm-2 control-label">Effect</label>\n' +
'                                                            <div class="col-sm-10">\n' +
'                                                                <select class="form-control" id="Effect'+curr_size +'">\n' +
'                                                                    <option value="permit">permit</option>\n' +
'                                                                    <option value="deny">deny</option>\n' +
'                                                                </select>\n' +
'                                                            </div>\n' +
'                                                        </div>\n' +
'                                                        <div class="form-group">\n' +
'                                                            <label class="col-sm-2 control-label">description</label>\n' +
'                                                            <div class="col-sm-10">\n' +
'                                                                <textarea class="form-control" rows="3" id="description2'+curr_size +'">null</textarea>\n' +
'                                                            </div>\n' +
'                                                        </div>\n' +
'                                                    </div>\n' +
'                                                </div>\n' +
'                                                <div class="panel panel-default">\n' +
'                                                    <div class="panel-heading">\n' +
'                                                        <h3 class="panel-title">\n' +
'                                                            User\n' +
'                                                        </h3>\n' +
'                                                    </div>\n' +
'                                                    <div class="panel-body">\n' +
'                                                        <div class="form-group">\n' +
'                                                            <label class="col-sm-2 control-label">MatchId</label>\n' +
'                                                            <div class="col-sm-10">\n' +
'                                                                <select class="form-control" id="MatchId4'+curr_size +'">\n' +
'                                                                    <option value="null">null</option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:string-equal">urn:oasis:names:tc:xacml:1.0:function:string-equal</option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:boolean-equal">urn:oasis:names:tc:xacml:1.0:function:boolean-equal </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:integer-equal">urn:oasis:names:tc:xacml:1.0:function:integer-equal </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:double-equal">urn:oasis:names:tc:xacml:1.0:function:double-equal </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:date-equal">urn:oasis:names:tc:xacml:1.0:function:date-equal</option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:time-equal">urn:oasis:names:tc:xacml:1.0:function:time-equal </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:dateTime-equal">urn:oasis:names:tc:xacml:1.0:function:dateTime-equal </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-equal">urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-equal </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-equal ">urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-equal </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:string-equal-ignore-case">urn:oasis:names:tc:xacml:3.0:function:string-equal-ignore-case </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:anyURI-equal">urn:oasis:names:tc:xacml:1.0:function:anyURI-equal </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:x500Name-equal">urn:oasis:names:tc:xacml:1.0:function:x500Name-equal </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:rfc822Name-equal">urn:oasis:names:tc:xacml:1.0:function:rfc822Name-equal </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:hexBinary-equal ">urn:oasis:names:tc:xacml:1.0:function:hexBinary-equal </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:base64Binary-equal">urn:oasis:names:tc:xacml:1.0:function:base64Binary-equal </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:integer-add ">urn:oasis:names:tc:xacml:1.0:function:integer-add </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:double-add ">urn:oasis:names:tc:xacml:1.0:function:double-add </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:integer-subtract">urn:oasis:names:tc:xacml:1.0:function:integer-subtract </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:double-subtract">urn:oasis:names:tc:xacml:1.0:function:double-subtract </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:integer-multiply">urn:oasis:names:tc:xacml:1.0:function:integer-multiply </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:double-multiply">urn:oasis:names:tc:xacml:1.0:function:double-multiply </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:integer-divide">urn:oasis:names:tc:xacml:1.0:function:integer-divide </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:double-divide">urn:oasis:names:tc:xacml:1.0:function:double-divide </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:integer-mod">urn:oasis:names:tc:xacml:1.0:function:integer-mod </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:integer-abs">urn:oasis:names:tc:xacml:1.0:function:integer-abs </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:double-abs">urn:oasis:names:tc:xacml:1.0:function:double-abs </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:round">urn:oasis:names:tc:xacml:1.0:function:round </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:floorurn:oasis:names:tc:xacml:1.0:function:string-normalize-space">urn:oasis:names:tc:xacml:1.0:function:floorurn:oasis:names:tc:xacml:1.0:function:string-normalize-space </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:string-normalize-to-lower-case">urn:oasis:names:tc:xacml:1.0:function:string-normalize-to-lower-case </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:double-to-integer ">urn:oasis:names:tc:xacml:1.0:function:double-to-integer </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:integer-to-double">urn:oasis:names:tc:xacml:1.0:function:integer-to-double </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:or">urn:oasis:names:tc:xacml:1.0:function:or </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:and">urn:oasis:names:tc:xacml:1.0:function:and </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:n-of">urn:oasis:names:tc:xacml:1.0:function:n-of </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:noturn:oasis:names:tc:xacml:1.0:function:integer-greater-than ">urn:oasis:names:tc:xacml:1.0:function:noturn:oasis:names:tc:xacml:1.0:function:integer-greater-than </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:integer-greater-than-or-equal ">urn:oasis:names:tc:xacml:1.0:function:integer-greater-than-or-equal </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:integer-greater-than-or-equal ">urn:oasis:names:tc:xacml:1.0:function:integer-greater-than-or-equal  </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:integer-less-than-or-equal">urn:oasis:names:tc:xacml:1.0:function:integer-less-than-or-equal </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:double-greater-than">urn:oasis:names:tc:xacml:1.0:function:double-greater-than </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:double-greater-than-or-equal">urn:oasis:names:tc:xacml:1.0:function:double-greater-than-or-equal </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:double-less-than">urn:oasis:names:tc:xacml:1.0:function:double-less-than </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:double-less-than-or-equal">urn:oasis:names:tc:xacml:1.0:function:double-less-than-or-equal </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:dateTime-add-dayTimeDuration">urn:oasis:names:tc:xacml:3.0:function:dateTime-add-dayTimeDuration </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:dateTime-add-yearMonthDuration ">urn:oasis:names:tc:xacml:3.0:function:dateTime-add-yearMonthDuration </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:dateTime-subtract-dayTimeDuration">urn:oasis:names:tc:xacml:3.0:function:dateTime-subtract-dayTimeDuration  </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:dateTime-subtractyearMonthDuration">urn:oasis:names:tc:xacml:3.0:function:dateTime-subtractyearMonthDuration </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:date-add-yearMonthDuration">urn:oasis:names:tc:xacml:3.0:function:date-add-yearMonthDuration </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:date-subtract-yearMonthDuration">urn:oasis:names:tc:xacml:3.0:function:date-subtract-yearMonthDuration </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:string-greater-than">urn:oasis:names:tc:xacml:1.0:function:string-greater-than </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:string-greater-than-or-equal">urn:oasis:names:tc:xacml:1.0:function:string-greater-than-or-equal </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:string-less-than">urn:oasis:names:tc:xacml:1.0:function:string-less-than </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:string-less-than-or-equal">urn:oasis:names:tc:xacml:1.0:function:string-less-than-or-equal </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:time-greater-than ">urn:oasis:names:tc:xacml:1.0:function:time-greater-than </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:time-greater-than-or-equal">urn:oasis:names:tc:xacml:1.0:function:time-greater-than-or-equal </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:time-less-than">urn:oasis:names:tc:xacml:1.0:function:time-less-than </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:time-less-than-or-equal">urn:oasis:names:tc:xacml:1.0:function:time-less-than-or-equal </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:2.0:function:time-in-range ">urn:oasis:names:tc:xacml:2.0:function:time-in-range </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:dateTime-greater-than">urn:oasis:names:tc:xacml:1.0:function:dateTime-greater-than </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:dateTime-greater-than-or-equal ">urn:oasis:names:tc:xacml:1.0:function:dateTime-greater-than-or-equal </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:dateTime-less-than">urn:oasis:names:tc:xacml:1.0:function:dateTime-less-than </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:dateTime-less-than-or-equal">urn:oasis:names:tc:xacml:1.0:function:dateTime-less-than-or-equal </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:date-greater-than ">urn:oasis:names:tc:xacml:1.0:function:date-greater-than </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:date-greater-than-or-equal">urn:oasis:names:tc:xacml:1.0:function:date-greater-than-or-equal </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:date-less-than">urn:oasis:names:tc:xacml:1.0:function:date-less-than </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:date-less-than-or-equal ">urn:oasis:names:tc:xacml:1.0:function:date-less-than-or-equal </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:string-one-and-only ">urn:oasis:names:tc:xacml:1.0:function:string-one-and-only </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:string-bag-size">urn:oasis:names:tc:xacml:1.0:function:string-bag-size </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:string-is-in ">urn:oasis:names:tc:xacml:1.0:function:string-is-in </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:string-bag">urn:oasis:names:tc:xacml:1.0:function:string-bag </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:boolean-one-and-only">urn:oasis:names:tc:xacml:1.0:function:boolean-one-and-only </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:boolean-bag-size">urn:oasis:names:tc:xacml:1.0:function:boolean-bag-size </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:boolean-is-in ">urn:oasis:names:tc:xacml:1.0:function:boolean-is-in </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:boolean-bag">urn:oasis:names:tc:xacml:1.0:function:boolean-bag </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:integer-one-and-only">urn:oasis:names:tc:xacml:1.0:function:integer-one-and-only </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:integer-bag-size">urn:oasis:names:tc:xacml:1.0:function:integer-bag-size</option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:integer-is-in">urn:oasis:names:tc:xacml:1.0:function:integer-is-in </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:integer-bag ">urn:oasis:names:tc:xacml:1.0:function:integer-bag </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:double-one-and-only">urn:oasis:names:tc:xacml:1.0:function:double-one-and-only </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:double-bag-size">urn:oasis:names:tc:xacml:1.0:function:double-bag-size </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:double-is-in">urn:oasis:names:tc:xacml:1.0:function:double-is-in </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:double-bag">urn:oasis:names:tc:xacml:1.0:function:double-bag </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:time-one-and-only ">urn:oasis:names:tc:xacml:1.0:function:time-one-and-only </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:time-bag-size">urn:oasis:names:tc:xacml:1.0:function:time-bag-size </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:time-is-in ">urn:oasis:names:tc:xacml:1.0:function:time-is-in </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:time-bag">urn:oasis:names:tc:xacml:1.0:function:time-bag </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:date-one-and-only ">urn:oasis:names:tc:xacml:1.0:function:date-one-and-only </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:date-bag-size ">urn:oasis:names:tc:xacml:1.0:function:date-bag-size </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:date-is-in ">urn:oasis:names:tc:xacml:1.0:function:date-is-in </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:date-bag ">urn:oasis:names:tc:xacml:1.0:function:date-bag </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:dateTime-one-and-only">urn:oasis:names:tc:xacml:1.0:function:dateTime-one-and-only </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:dateTime-bag-size">urn:oasis:names:tc:xacml:1.0:function:dateTime-bag-size </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:dateTime-is-in ">urn:oasis:names:tc:xacml:1.0:function:dateTime-is-in </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:dateTime-bag">urn:oasis:names:tc:xacml:1.0:function:dateTime-bag </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:anyURI-one-and-only">urn:oasis:names:tc:xacml:1.0:function:anyURI-one-and-only </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:anyURI-one-and-only">urn:oasis:names:tc:xacml:1.0:function:anyURI-one-and-only</option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:anyURI-is-in">urn:oasis:names:tc:xacml:1.0:function:anyURI-is-in </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:anyURI-bag">urn:oasis:names:tc:xacml:1.0:function:anyURI-bag </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:hexBinary-one-and-only">urn:oasis:names:tc:xacml:1.0:function:hexBinary-one-and-only </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:hexBinary-bag-size">urn:oasis:names:tc:xacml:1.0:function:hexBinary-bag-size </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:hexBinary-is-in">urn:oasis:names:tc:xacml:1.0:function:hexBinary-is-in </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:hexBinary-bag">urn:oasis:names:tc:xacml:1.0:function:hexBinary-bag </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:base64Binary-one-and-only">urn:oasis:names:tc:xacml:1.0:function:base64Binary-one-and-only </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:base64Binary-bag-size">urn:oasis:names:tc:xacml:1.0:function:base64Binary-bag-size </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:base64Binary-is-in">urn:oasis:names:tc:xacml:1.0:function:base64Binary-is-in </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:base64Binary-bag">urn:oasis:names:tc:xacml:1.0:function:base64Binary-bag </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-one-and-only">urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-one-and-only </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-bag-size">urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-bag-size </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-is-in ">urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-is-in </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-bag">urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-bag </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-one-and-only">urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-one-and-only </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-bag-size">urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-bag-size </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-is-in">urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-is-in </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-bag">urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-bag </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:x500Name-one-and-only">urn:oasis:names:tc:xacml:1.0:function:x500Name-one-and-only </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:x500Name-one-and-only">urn:oasis:names:tc:xacml:1.0:function:x500Name-one-and-only </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:x500Name-is-in">urn:oasis:names:tc:xacml:1.0:function:x500Name-is-in </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:x500Name-bag">urn:oasis:names:tc:xacml:1.0:function:x500Name-bag </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:rfc822Name-one-and-only">urn:oasis:names:tc:xacml:1.0:function:rfc822Name-one-and-only </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:rfc822Name-bag-size">urn:oasis:names:tc:xacml:1.0:function:rfc822Name-bag-size </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:rfc822Name-is-in">urn:oasis:names:tc:xacml:1.0:function:rfc822Name-is-in </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:rfc822Name-bag ">urn:oasis:names:tc:xacml:1.0:function:rfc822Name-bag </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:2.0:function:ipAddress-one-and-only">urn:oasis:names:tc:xacml:2.0:function:ipAddress-one-and-only </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:2.0:function:ipAddress-bag-size">urn:oasis:names:tc:xacml:2.0:function:ipAddress-bag-size </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:2.0:function:ipAddress-bag ">urn:oasis:names:tc:xacml:2.0:function:ipAddress-bag </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:2.0:function:dnsName-one-and-only">urn:oasis:names:tc:xacml:2.0:function:dnsName-one-and-only </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:2.0:function:dnsName-bag-size">urn:oasis:names:tc:xacml:2.0:function:dnsName-bag-size </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:2.0:function:dnsName-bag">urn:oasis:names:tc:xacml:2.0:function:dnsName-bag </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:2.0:function:string-concatenate">urn:oasis:names:tc:xacml:2.0:function:string-concatenate </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:boolean-from-string">urn:oasis:names:tc:xacml:3.0:function:boolean-from-string </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:string-from-boolean">urn:oasis:names:tc:xacml:3.0:function:string-from-boolean </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:integer-from-string ">urn:oasis:names:tc:xacml:3.0:function:integer-from-string </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:string-from-integer ">urn:oasis:names:tc:xacml:3.0:function:string-from-integer </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:string-from-double">urn:oasis:names:tc:xacml:3.0:function:string-from-double</option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:string-from-double">urn:oasis:names:tc:xacml:3.0:function:string-from-double </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:time-from-string">urn:oasis:names:tc:xacml:3.0:function:time-from-string </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:string-from-time">urn:oasis:names:tc:xacml:3.0:function:string-from-time </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:date-from-string">urn:oasis:names:tc:xacml:3.0:function:date-from-string </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:string-from-date">urn:oasis:names:tc:xacml:3.0:function:string-from-date </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:dateTime-from-string">urn:oasis:names:tc:xacml:3.0:function:dateTime-from-string </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:string-from-dateTime">urn:oasis:names:tc:xacml:3.0:function:string-from-dateTime </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:anyURI-from-string ">urn:oasis:names:tc:xacml:3.0:function:anyURI-from-string </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:string-from-anyURI">urn:oasis:names:tc:xacml:3.0:function:string-from-anyURI </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-from-string">urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-from-string </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:string-from-dayTimeDuration ">urn:oasis:names:tc:xacml:3.0:function:string-from-dayTimeDuration </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-from-string ">urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-from-string </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:string-from-yearMonthDuration">urn:oasis:names:tc:xacml:3.0:function:string-from-yearMonthDuration </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:x500Name-from-string ">urn:oasis:names:tc:xacml:3.0:function:x500Name-from-string </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:string-from-x500Name">urn:oasis:names:tc:xacml:3.0:function:string-from-x500Name </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:rfc822Name-from-string">urn:oasis:names:tc:xacml:3.0:function:rfc822Name-from-string </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:string-from-rfc822Name">urn:oasis:names:tc:xacml:3.0:function:string-from-rfc822Name </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:ipAddress-from-string ">urn:oasis:names:tc:xacml:3.0:function:ipAddress-from-string </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:string-from-ipAddress">urn:oasis:names:tc:xacml:3.0:function:string-from-ipAddress </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:dnsName-from-string">urn:oasis:names:tc:xacml:3.0:function:dnsName-from-string </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:string-from-dnsName ">urn:oasis:names:tc:xacml:3.0:function:string-from-dnsName </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:string-starts-with">urn:oasis:names:tc:xacml:3.0:function:string-starts-with </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:anyURI-starts-with">urn:oasis:names:tc:xacml:3.0:function:anyURI-starts-with </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:string-ends-with ">urn:oasis:names:tc:xacml:3.0:function:string-ends-with </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:anyURI-ends-with">urn:oasis:names:tc:xacml:3.0:function:anyURI-ends-with </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:string-contains">urn:oasis:names:tc:xacml:3.0:function:string-contains </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:anyURI-contains">urn:oasis:names:tc:xacml:3.0:function:anyURI-contains </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:string-substring ">urn:oasis:names:tc:xacml:3.0:function:string-substring </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:anyURI-substring ">urn:oasis:names:tc:xacml:3.0:function:anyURI-substring </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:any-of ">urn:oasis:names:tc:xacml:3.0:function:any-of </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:all-of">urn:oasis:names:tc:xacml:3.0:function:all-of </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:any-of-any ">urn:oasis:names:tc:xacml:3.0:function:any-of-any </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:all-of-any">urn:oasis:names:tc:xacml:1.0:function:all-of-any </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:any-of-all">urn:oasis:names:tc:xacml:1.0:function:any-of-all  </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:all-of-all">urn:oasis:names:tc:xacml:1.0:function:all-of-all  </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:map">urn:oasis:names:tc:xacml:3.0:function:map </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:x500Name-match">urn:oasis:names:tc:xacml:1.0:function:x500Name-match  </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:rfc822Name-match">urn:oasis:names:tc:xacml:1.0:function:rfc822Name-match  </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:string-regexp-match">urn:oasis:names:tc:xacml:1.0:function:string-regexp-match  </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:2.0:function:anyURI-regexp-match ">urn:oasis:names:tc:xacml:2.0:function:anyURI-regexp-match  </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:2.0:function:ipAddress-regexp-match">urn:oasis:names:tc:xacml:2.0:function:ipAddress-regexp-match  </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:2.0:function:dnsName-regexp-match">urn:oasis:names:tc:xacml:2.0:function:dnsName-regexp-match  </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:2.0:function:rfc822Name-regexp-match ">urn:oasis:names:tc:xacml:2.0:function:rfc822Name-regexp-match  </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:2.0:function:x500Name-regexp-match">urn:oasis:names:tc:xacml:2.0:function:x500Name-regexp-match  </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:xpath-node-count">urn:oasis:names:tc:xacml:3.0:function:xpath-node-count </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:xpath-node-equal ">urn:oasis:names:tc:xacml:3.0:function:xpath-node-equal </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:xpath-node-match">urn:oasis:names:tc:xacml:3.0:function:xpath-node-match </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:string-intersection">urn:oasis:names:tc:xacml:1.0:function:string-intersection </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:string-at-least-one-member-of">urn:oasis:names:tc:xacml:1.0:function:string-at-least-one-member-of </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:string-union">urn:oasis:names:tc:xacml:1.0:function:string-union </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:string-subset">urn:oasis:names:tc:xacml:1.0:function:string-subset </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:string-set-equals">urn:oasis:names:tc:xacml:1.0:function:string-set-equals </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:boolean-intersection ">urn:oasis:names:tc:xacml:1.0:function:boolean-intersection </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:boolean-at-least-one-member-of">urn:oasis:names:tc:xacml:1.0:function:boolean-at-least-one-member-of </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:boolean-union">urn:oasis:names:tc:xacml:1.0:function:boolean-union </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:boolean-subset ">urn:oasis:names:tc:xacml:1.0:function:boolean-subset </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:boolean-set-equals ">urn:oasis:names:tc:xacml:1.0:function:boolean-set-equals </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:integer-intersection">urn:oasis:names:tc:xacml:1.0:function:integer-intersection</option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:integer-at-least-one-member-of ">urn:oasis:names:tc:xacml:1.0:function:integer-at-least-one-member-of  </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:integer-union ">urn:oasis:names:tc:xacml:1.0:function:integer-union  </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:integer-subset">urn:oasis:names:tc:xacml:1.0:function:integer-subset  </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:integer-set-equals">urn:oasis:names:tc:xacml:1.0:function:integer-set-equals  </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:double-intersection">urn:oasis:names:tc:xacml:1.0:function:double-intersection  </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:double-at-least-one-member-of">urn:oasis:names:tc:xacml:1.0:function:double-at-least-one-member-of  </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:double-union ">urn:oasis:names:tc:xacml:1.0:function:double-union  </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:double-subset">urn:oasis:names:tc:xacml:1.0:function:double-subset  </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:double-set-equals">urn:oasis:names:tc:xacml:1.0:function:double-set-equals  </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:time-intersection ">urn:oasis:names:tc:xacml:1.0:function:time-intersection  </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:time-at-least-one-member-of">urn:oasis:names:tc:xacml:1.0:function:time-at-least-one-member-of  </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:time-union ">urn:oasis:names:tc:xacml:1.0:function:time-union  </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:time-subset">urn:oasis:names:tc:xacml:1.0:function:time-subset  </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:time-set-equals">urn:oasis:names:tc:xacml:1.0:function:time-set-equals  </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:date-intersection">urn:oasis:names:tc:xacml:1.0:function:date-intersection  </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:date-at-least-one-member-of ">urn:oasis:names:tc:xacml:1.0:function:date-at-least-one-member-of  </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:date-union">urn:oasis:names:tc:xacml:1.0:function:date-union  </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:date-subset ">urn:oasis:names:tc:xacml:1.0:function:date-subset  </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:date-set-equals">urn:oasis:names:tc:xacml:1.0:function:date-set-equals  </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:dateTime-intersection">urn:oasis:names:tc:xacml:1.0:function:dateTime-intersection  </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:dateTime-at-least-one-member-of ">urn:oasis:names:tc:xacml:1.0:function:dateTime-at-least-one-member-of  </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:dateTime-union ">urn:oasis:names:tc:xacml:1.0:function:dateTime-union  </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:dateTime-subset ">urn:oasis:names:tc:xacml:1.0:function:dateTime-subset  </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:dateTime-set-equals ">urn:oasis:names:tc:xacml:1.0:function:dateTime-set-equals  </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:anyURI-intersection">urn:oasis:names:tc:xacml:1.0:function:anyURI-intersection  </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:anyURI-at-least-one-member-of ">urn:oasis:names:tc:xacml:1.0:function:anyURI-at-least-one-member-of  </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:anyURI-union ">urn:oasis:names:tc:xacml:1.0:function:anyURI-union  </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:anyURI-subset">urn:oasis:names:tc:xacml:1.0:function:anyURI-subset  </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:anyURI-set-equals ">urn:oasis:names:tc:xacml:1.0:function:anyURI-set-equals  </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:hexBinary-intersection ">urn:oasis:names:tc:xacml:1.0:function:hexBinary-intersection  </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:hexBinary-at-least-one-member-of ">urn:oasis:names:tc:xacml:1.0:function:hexBinary-at-least-one-member-of  </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:hexBinary-union ">urn:oasis:names:tc:xacml:1.0:function:hexBinary-union  </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:hexBinary-subset">urn:oasis:names:tc:xacml:1.0:function:hexBinary-subset  </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:hexBinary-set-equals">urn:oasis:names:tc:xacml:1.0:function:hexBinary-set-equals  </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:base64Binary-intersection ">urn:oasis:names:tc:xacml:1.0:function:base64Binary-intersection  </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:base64Binary-at-least-one-memberof">urn:oasis:names:tc:xacml:1.0:function:base64Binary-at-least-one-memberof  </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:base64Binary-union">urn:oasis:names:tc:xacml:1.0:function:base64Binary-union  </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:base64Binary-subset">urn:oasis:names:tc:xacml:1.0:function:base64Binary-subset  </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:base64Binary-set-equals ">urn:oasis:names:tc:xacml:1.0:function:base64Binary-set-equals  </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-intersection ">urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-intersection  </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-at-least-onemember-of">urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-at-least-onemember-of  </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-union  ">urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-union  </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-subset ">urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-subset  </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-set-equals">urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-set-equals  </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-intersection ">urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-intersection  </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-at-least-onemember-of">urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-at-least-onemember-of  </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-union  ">urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-union  </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-subset">urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-subset  </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-set-equals  ">urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-set-equals  </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:x500Name-intersection">urn:oasis:names:tc:xacml:1.0:function:x500Name-intersection  </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:x500Name-at-least-one-member-of">urn:oasis:names:tc:xacml:1.0:function:x500Name-at-least-one-member-of  </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:x500Name-union">urn:oasis:names:tc:xacml:1.0:function:x500Name-union  </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:x500Name-subset">urn:oasis:names:tc:xacml:1.0:function:x500Name-subset  </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:rfc822Name-intersection">urn:oasis:names:tc:xacml:1.0:function:rfc822Name-intersection </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:rfc822Name-at-least-one-member-of ">urn:oasis:names:tc:xacml:1.0:function:rfc822Name-at-least-one-member-of </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:rfc822Name-union ">urn:oasis:names:tc:xacml:1.0:function:rfc822Name-union </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:rfc822Name-subset">urn:oasis:names:tc:xacml:1.0:function:rfc822Name-subset </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:rfc822Name-set-equals">urn:oasis:names:tc:xacml:1.0:function:rfc822Name-set-equals </option>\n' +
'                                                                </select>\n' +
'                                                            </div>\n' +
'                                                        </div>\n' +
'                                                        <div class="form-group">\n' +
'                                                            <label class="col-sm-2 control-label">Data Type</label>\n' +
'                                                            <div class="col-sm-10">\n' +
'                                                                <select class="form-control" id="DataType4'+curr_size +'">\n' +
'                                                                    <option value="null">null</option>\n' +
'                                                                    <option value="http://www.w3.org/2001/XMLSchema#string">http://www.w3.org/2001/XMLSchema#string\n' +
'                                                                    </option>\n' +
'                                                                    <option value="http://www.w3.org/2001/XMLSchema#boolean">http://www.w3.org/2001/XMLSchema#boolean\n' +
'                                                                    </option>\n' +
'                                                                    <option value="http://www.w3.org/2001/XMLSchema#integer">http://www.w3.org/2001/XMLSchema#integer\n' +
'                                                                    </option>\n' +
'                                                                    <option value="http://www.w3.org/2001/XMLSchema#double">http://www.w3.org/2001/XMLSchema#double\n' +
'                                                                    </option>\n' +
'                                                                    <option value="http://www.w3.org/2001/XMLSchema#time">http://www.w3.org/2001/XMLSchema#time\n' +
'                                                                    </option>\n' +
'                                                                    <option value="http://www.w3.org/2001/XMLSchema#date">http://www.w3.org/2001/XMLSchema#date\n' +
'                                                                    </option>\n' +
'                                                                    <option value="http://www.w3.org/2001/XMLSchema#dateTime">http://www.w3.org/2001/XMLSchema#dateTime\n' +
'                                                                    </option>\n' +
'                                                                    <option value="http://www.w3.org/2001/XMLSchema#dayTimeDuration">http://www.w3.org/2001/XMLSchema#dayTimeDuration\n' +
'                                                                    </option>\n' +
'                                                                    <option value="http://www.w3.org/2001/XMLSchema#yearMonthDuration">http://www.w3.org/2001/XMLSchema#yearMonthDuration\n' +
'                                                                    </option>\n' +
'                                                                    <option value="http://www.w3.org/2001/XMLSchema#anyURI">http://www.w3.org/2001/XMLSchema#anyURI\n' +
'                                                                    </option>\n' +
'                                                                    <option value="http://www.w3.org/2001/XMLSchema#hexBinary">http://www.w3.org/2001/XMLSchema#hexBinary\n' +
'                                                                    </option>\n' +
'                                                                    <option value="http://www.w3.org/2001/XMLSchema#base64Binary">http://www.w3.org/2001/XMLSchema#base64Binary\n' +
'                                                                    </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:data-type:rfc822Name">urn:oasis:names:tc:xacml:1.0:data-type:rfc822Name\n' +
'                                                                    </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:data-type:x500Name">urn:oasis:names:tc:xacml:1.0:data-type:x500Name\n' +
'                                                                    </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:3.0:data-type:xpathExpression">urn:oasis:names:tc:xacml:3.0:data-type:xpathExpression\n' +
'                                                                    </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:2.0:data-type:ipAddress">urn:oasis:names:tc:xacml:2.0:data-type:ipAddress\n' +
'                                                                    </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:2.0:data-type:dnsName">urn:oasis:names:tc:xacml:2.0:data-type:dnsName\n' +
'                                                                    </option>\n' +
'                                                                </select>\n' +
'                                                            </div>\n' +
'                                                        </div>\n' +
'                                                        <div class="form-group">\n' +
'                                                            <label for="value4'+curr_size +'" class="col-sm-2 control-label">value</label>\n' +
'                                                            <div class="col-sm-10">\n' +
'                                                                <input type="text" class="form-control" id="value4'+curr_size +'" placeholder="value">\n' +
'                                                            </div>\n' +
'                                                        </div>\n' +
'                                                        <div class="form-group">\n' +
'                                                            <label for="AttributeId4'+curr_size +'" class="col-sm-2 control-label">AttributeId</label>\n' +
'                                                            <div class="col-sm-10">\n' +
'                                                                <input type="text" class="form-control" id="AttributeId4'+curr_size +'"\n' +
'                                                                       placeholder="null">\n' +
'                                                            </div>\n' +
'                                                        </div>\n' +
'                                                        <div class="form-group">\n' +
'                                                            <label  class="col-sm-2 control-label">MustBePresent</label>\n' +
'                                                            <div class="col-sm-10">\n' +
'                                                                <select class="form-control" id="MustBePresent4'+curr_size +'">\n' +
'                                                                    <option value="true">true</option>\n' +
'                                                                    <option value="false">false</option>\n' +
'                                                                </select>\n' +
'                                                            </div>\n' +
'                                                        </div>\n' +
'                                                    </div>\n' +
'                                                </div>\n' +
'                                                <div class="panel panel-default">\n' +
'                                                    <div class="panel-heading">\n' +
'                                                        <h3 class="panel-title">\n' +
'                                                            Resource\n' +
'                                                        </h3>\n' +
'                                                    </div>\n' +
'                                                    <div class="panel-body">\n' +
'                                                        <div class="form-group">\n' +
'                                                            <label class="col-sm-2 control-label">MatchId</label>\n' +
'                                                            <div class="col-sm-10">\n' +
'                                                                <select class="form-control" id="MatchId5'+curr_size +'">\n' +
'                                                                    <option value="null">null</option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:string-equal">urn:oasis:names:tc:xacml:1.0:function:string-equal</option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:boolean-equal">urn:oasis:names:tc:xacml:1.0:function:boolean-equal </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:integer-equal">urn:oasis:names:tc:xacml:1.0:function:integer-equal </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:double-equal">urn:oasis:names:tc:xacml:1.0:function:double-equal </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:date-equal">urn:oasis:names:tc:xacml:1.0:function:date-equal</option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:time-equal">urn:oasis:names:tc:xacml:1.0:function:time-equal </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:dateTime-equal">urn:oasis:names:tc:xacml:1.0:function:dateTime-equal </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-equal">urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-equal </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-equal ">urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-equal </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:string-equal-ignore-case">urn:oasis:names:tc:xacml:3.0:function:string-equal-ignore-case </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:anyURI-equal">urn:oasis:names:tc:xacml:1.0:function:anyURI-equal </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:x500Name-equal">urn:oasis:names:tc:xacml:1.0:function:x500Name-equal </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:rfc822Name-equal">urn:oasis:names:tc:xacml:1.0:function:rfc822Name-equal </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:hexBinary-equal ">urn:oasis:names:tc:xacml:1.0:function:hexBinary-equal </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:base64Binary-equal">urn:oasis:names:tc:xacml:1.0:function:base64Binary-equal </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:integer-add ">urn:oasis:names:tc:xacml:1.0:function:integer-add </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:double-add ">urn:oasis:names:tc:xacml:1.0:function:double-add </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:integer-subtract">urn:oasis:names:tc:xacml:1.0:function:integer-subtract </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:double-subtract">urn:oasis:names:tc:xacml:1.0:function:double-subtract </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:integer-multiply">urn:oasis:names:tc:xacml:1.0:function:integer-multiply </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:double-multiply">urn:oasis:names:tc:xacml:1.0:function:double-multiply </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:integer-divide">urn:oasis:names:tc:xacml:1.0:function:integer-divide </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:double-divide">urn:oasis:names:tc:xacml:1.0:function:double-divide </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:integer-mod">urn:oasis:names:tc:xacml:1.0:function:integer-mod </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:integer-abs">urn:oasis:names:tc:xacml:1.0:function:integer-abs </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:double-abs">urn:oasis:names:tc:xacml:1.0:function:double-abs </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:round">urn:oasis:names:tc:xacml:1.0:function:round </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:floorurn:oasis:names:tc:xacml:1.0:function:string-normalize-space">urn:oasis:names:tc:xacml:1.0:function:floorurn:oasis:names:tc:xacml:1.0:function:string-normalize-space </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:string-normalize-to-lower-case">urn:oasis:names:tc:xacml:1.0:function:string-normalize-to-lower-case </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:double-to-integer ">urn:oasis:names:tc:xacml:1.0:function:double-to-integer </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:integer-to-double">urn:oasis:names:tc:xacml:1.0:function:integer-to-double </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:or">urn:oasis:names:tc:xacml:1.0:function:or </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:and">urn:oasis:names:tc:xacml:1.0:function:and </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:n-of">urn:oasis:names:tc:xacml:1.0:function:n-of </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:noturn:oasis:names:tc:xacml:1.0:function:integer-greater-than ">urn:oasis:names:tc:xacml:1.0:function:noturn:oasis:names:tc:xacml:1.0:function:integer-greater-than </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:integer-greater-than-or-equal ">urn:oasis:names:tc:xacml:1.0:function:integer-greater-than-or-equal </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:integer-greater-than-or-equal ">urn:oasis:names:tc:xacml:1.0:function:integer-greater-than-or-equal  </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:integer-less-than-or-equal">urn:oasis:names:tc:xacml:1.0:function:integer-less-than-or-equal </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:double-greater-than">urn:oasis:names:tc:xacml:1.0:function:double-greater-than </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:double-greater-than-or-equal">urn:oasis:names:tc:xacml:1.0:function:double-greater-than-or-equal </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:double-less-than">urn:oasis:names:tc:xacml:1.0:function:double-less-than </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:double-less-than-or-equal">urn:oasis:names:tc:xacml:1.0:function:double-less-than-or-equal </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:dateTime-add-dayTimeDuration">urn:oasis:names:tc:xacml:3.0:function:dateTime-add-dayTimeDuration </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:dateTime-add-yearMonthDuration ">urn:oasis:names:tc:xacml:3.0:function:dateTime-add-yearMonthDuration </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:dateTime-subtract-dayTimeDuration">urn:oasis:names:tc:xacml:3.0:function:dateTime-subtract-dayTimeDuration  </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:dateTime-subtractyearMonthDuration">urn:oasis:names:tc:xacml:3.0:function:dateTime-subtractyearMonthDuration </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:date-add-yearMonthDuration">urn:oasis:names:tc:xacml:3.0:function:date-add-yearMonthDuration </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:date-subtract-yearMonthDuration">urn:oasis:names:tc:xacml:3.0:function:date-subtract-yearMonthDuration </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:string-greater-than">urn:oasis:names:tc:xacml:1.0:function:string-greater-than </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:string-greater-than-or-equal">urn:oasis:names:tc:xacml:1.0:function:string-greater-than-or-equal </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:string-less-than">urn:oasis:names:tc:xacml:1.0:function:string-less-than </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:string-less-than-or-equal">urn:oasis:names:tc:xacml:1.0:function:string-less-than-or-equal </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:time-greater-than ">urn:oasis:names:tc:xacml:1.0:function:time-greater-than </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:time-greater-than-or-equal">urn:oasis:names:tc:xacml:1.0:function:time-greater-than-or-equal </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:time-less-than">urn:oasis:names:tc:xacml:1.0:function:time-less-than </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:time-less-than-or-equal">urn:oasis:names:tc:xacml:1.0:function:time-less-than-or-equal </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:2.0:function:time-in-range ">urn:oasis:names:tc:xacml:2.0:function:time-in-range </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:dateTime-greater-than">urn:oasis:names:tc:xacml:1.0:function:dateTime-greater-than </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:dateTime-greater-than-or-equal ">urn:oasis:names:tc:xacml:1.0:function:dateTime-greater-than-or-equal </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:dateTime-less-than">urn:oasis:names:tc:xacml:1.0:function:dateTime-less-than </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:dateTime-less-than-or-equal">urn:oasis:names:tc:xacml:1.0:function:dateTime-less-than-or-equal </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:date-greater-than ">urn:oasis:names:tc:xacml:1.0:function:date-greater-than </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:date-greater-than-or-equal">urn:oasis:names:tc:xacml:1.0:function:date-greater-than-or-equal </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:date-less-than">urn:oasis:names:tc:xacml:1.0:function:date-less-than </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:date-less-than-or-equal ">urn:oasis:names:tc:xacml:1.0:function:date-less-than-or-equal </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:string-one-and-only ">urn:oasis:names:tc:xacml:1.0:function:string-one-and-only </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:string-bag-size">urn:oasis:names:tc:xacml:1.0:function:string-bag-size </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:string-is-in ">urn:oasis:names:tc:xacml:1.0:function:string-is-in </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:string-bag">urn:oasis:names:tc:xacml:1.0:function:string-bag </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:boolean-one-and-only">urn:oasis:names:tc:xacml:1.0:function:boolean-one-and-only </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:boolean-bag-size">urn:oasis:names:tc:xacml:1.0:function:boolean-bag-size </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:boolean-is-in ">urn:oasis:names:tc:xacml:1.0:function:boolean-is-in </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:boolean-bag">urn:oasis:names:tc:xacml:1.0:function:boolean-bag </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:integer-one-and-only">urn:oasis:names:tc:xacml:1.0:function:integer-one-and-only </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:integer-bag-size">urn:oasis:names:tc:xacml:1.0:function:integer-bag-size</option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:integer-is-in">urn:oasis:names:tc:xacml:1.0:function:integer-is-in </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:integer-bag ">urn:oasis:names:tc:xacml:1.0:function:integer-bag </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:double-one-and-only">urn:oasis:names:tc:xacml:1.0:function:double-one-and-only </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:double-bag-size">urn:oasis:names:tc:xacml:1.0:function:double-bag-size </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:double-is-in">urn:oasis:names:tc:xacml:1.0:function:double-is-in </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:double-bag">urn:oasis:names:tc:xacml:1.0:function:double-bag </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:time-one-and-only ">urn:oasis:names:tc:xacml:1.0:function:time-one-and-only </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:time-bag-size">urn:oasis:names:tc:xacml:1.0:function:time-bag-size </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:time-is-in ">urn:oasis:names:tc:xacml:1.0:function:time-is-in </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:time-bag">urn:oasis:names:tc:xacml:1.0:function:time-bag </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:date-one-and-only ">urn:oasis:names:tc:xacml:1.0:function:date-one-and-only </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:date-bag-size ">urn:oasis:names:tc:xacml:1.0:function:date-bag-size </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:date-is-in ">urn:oasis:names:tc:xacml:1.0:function:date-is-in </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:date-bag ">urn:oasis:names:tc:xacml:1.0:function:date-bag </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:dateTime-one-and-only">urn:oasis:names:tc:xacml:1.0:function:dateTime-one-and-only </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:dateTime-bag-size">urn:oasis:names:tc:xacml:1.0:function:dateTime-bag-size </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:dateTime-is-in ">urn:oasis:names:tc:xacml:1.0:function:dateTime-is-in </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:dateTime-bag">urn:oasis:names:tc:xacml:1.0:function:dateTime-bag </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:anyURI-one-and-only">urn:oasis:names:tc:xacml:1.0:function:anyURI-one-and-only </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:anyURI-one-and-only">urn:oasis:names:tc:xacml:1.0:function:anyURI-one-and-only</option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:anyURI-is-in">urn:oasis:names:tc:xacml:1.0:function:anyURI-is-in </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:anyURI-bag">urn:oasis:names:tc:xacml:1.0:function:anyURI-bag </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:hexBinary-one-and-only">urn:oasis:names:tc:xacml:1.0:function:hexBinary-one-and-only </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:hexBinary-bag-size">urn:oasis:names:tc:xacml:1.0:function:hexBinary-bag-size </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:hexBinary-is-in">urn:oasis:names:tc:xacml:1.0:function:hexBinary-is-in </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:hexBinary-bag">urn:oasis:names:tc:xacml:1.0:function:hexBinary-bag </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:base64Binary-one-and-only">urn:oasis:names:tc:xacml:1.0:function:base64Binary-one-and-only </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:base64Binary-bag-size">urn:oasis:names:tc:xacml:1.0:function:base64Binary-bag-size </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:base64Binary-is-in">urn:oasis:names:tc:xacml:1.0:function:base64Binary-is-in </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:base64Binary-bag">urn:oasis:names:tc:xacml:1.0:function:base64Binary-bag </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-one-and-only">urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-one-and-only </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-bag-size">urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-bag-size </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-is-in ">urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-is-in </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-bag">urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-bag </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-one-and-only">urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-one-and-only </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-bag-size">urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-bag-size </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-is-in">urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-is-in </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-bag">urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-bag </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:x500Name-one-and-only">urn:oasis:names:tc:xacml:1.0:function:x500Name-one-and-only </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:x500Name-one-and-only">urn:oasis:names:tc:xacml:1.0:function:x500Name-one-and-only </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:x500Name-is-in">urn:oasis:names:tc:xacml:1.0:function:x500Name-is-in </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:x500Name-bag">urn:oasis:names:tc:xacml:1.0:function:x500Name-bag </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:rfc822Name-one-and-only">urn:oasis:names:tc:xacml:1.0:function:rfc822Name-one-and-only </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:rfc822Name-bag-size">urn:oasis:names:tc:xacml:1.0:function:rfc822Name-bag-size </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:rfc822Name-is-in">urn:oasis:names:tc:xacml:1.0:function:rfc822Name-is-in </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:rfc822Name-bag ">urn:oasis:names:tc:xacml:1.0:function:rfc822Name-bag </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:2.0:function:ipAddress-one-and-only">urn:oasis:names:tc:xacml:2.0:function:ipAddress-one-and-only </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:2.0:function:ipAddress-bag-size">urn:oasis:names:tc:xacml:2.0:function:ipAddress-bag-size </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:2.0:function:ipAddress-bag ">urn:oasis:names:tc:xacml:2.0:function:ipAddress-bag </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:2.0:function:dnsName-one-and-only">urn:oasis:names:tc:xacml:2.0:function:dnsName-one-and-only </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:2.0:function:dnsName-bag-size">urn:oasis:names:tc:xacml:2.0:function:dnsName-bag-size </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:2.0:function:dnsName-bag">urn:oasis:names:tc:xacml:2.0:function:dnsName-bag </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:2.0:function:string-concatenate">urn:oasis:names:tc:xacml:2.0:function:string-concatenate </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:boolean-from-string">urn:oasis:names:tc:xacml:3.0:function:boolean-from-string </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:string-from-boolean">urn:oasis:names:tc:xacml:3.0:function:string-from-boolean </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:integer-from-string ">urn:oasis:names:tc:xacml:3.0:function:integer-from-string </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:string-from-integer ">urn:oasis:names:tc:xacml:3.0:function:string-from-integer </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:string-from-double">urn:oasis:names:tc:xacml:3.0:function:string-from-double</option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:string-from-double">urn:oasis:names:tc:xacml:3.0:function:string-from-double </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:time-from-string">urn:oasis:names:tc:xacml:3.0:function:time-from-string </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:string-from-time">urn:oasis:names:tc:xacml:3.0:function:string-from-time </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:date-from-string">urn:oasis:names:tc:xacml:3.0:function:date-from-string </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:string-from-date">urn:oasis:names:tc:xacml:3.0:function:string-from-date </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:dateTime-from-string">urn:oasis:names:tc:xacml:3.0:function:dateTime-from-string </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:string-from-dateTime">urn:oasis:names:tc:xacml:3.0:function:string-from-dateTime </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:anyURI-from-string ">urn:oasis:names:tc:xacml:3.0:function:anyURI-from-string </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:string-from-anyURI">urn:oasis:names:tc:xacml:3.0:function:string-from-anyURI </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-from-string">urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-from-string </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:string-from-dayTimeDuration ">urn:oasis:names:tc:xacml:3.0:function:string-from-dayTimeDuration </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-from-string ">urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-from-string </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:string-from-yearMonthDuration">urn:oasis:names:tc:xacml:3.0:function:string-from-yearMonthDuration </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:x500Name-from-string ">urn:oasis:names:tc:xacml:3.0:function:x500Name-from-string </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:string-from-x500Name">urn:oasis:names:tc:xacml:3.0:function:string-from-x500Name </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:rfc822Name-from-string">urn:oasis:names:tc:xacml:3.0:function:rfc822Name-from-string </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:string-from-rfc822Name">urn:oasis:names:tc:xacml:3.0:function:string-from-rfc822Name </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:ipAddress-from-string ">urn:oasis:names:tc:xacml:3.0:function:ipAddress-from-string </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:string-from-ipAddress">urn:oasis:names:tc:xacml:3.0:function:string-from-ipAddress </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:dnsName-from-string">urn:oasis:names:tc:xacml:3.0:function:dnsName-from-string </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:string-from-dnsName ">urn:oasis:names:tc:xacml:3.0:function:string-from-dnsName </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:string-starts-with">urn:oasis:names:tc:xacml:3.0:function:string-starts-with </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:anyURI-starts-with">urn:oasis:names:tc:xacml:3.0:function:anyURI-starts-with </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:string-ends-with ">urn:oasis:names:tc:xacml:3.0:function:string-ends-with </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:anyURI-ends-with">urn:oasis:names:tc:xacml:3.0:function:anyURI-ends-with </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:string-contains">urn:oasis:names:tc:xacml:3.0:function:string-contains </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:anyURI-contains">urn:oasis:names:tc:xacml:3.0:function:anyURI-contains </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:string-substring ">urn:oasis:names:tc:xacml:3.0:function:string-substring </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:anyURI-substring ">urn:oasis:names:tc:xacml:3.0:function:anyURI-substring </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:any-of ">urn:oasis:names:tc:xacml:3.0:function:any-of </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:all-of">urn:oasis:names:tc:xacml:3.0:function:all-of </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:any-of-any ">urn:oasis:names:tc:xacml:3.0:function:any-of-any </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:all-of-any">urn:oasis:names:tc:xacml:1.0:function:all-of-any </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:any-of-all">urn:oasis:names:tc:xacml:1.0:function:any-of-all  </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:all-of-all">urn:oasis:names:tc:xacml:1.0:function:all-of-all  </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:map">urn:oasis:names:tc:xacml:3.0:function:map </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:x500Name-match">urn:oasis:names:tc:xacml:1.0:function:x500Name-match  </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:rfc822Name-match">urn:oasis:names:tc:xacml:1.0:function:rfc822Name-match  </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:string-regexp-match">urn:oasis:names:tc:xacml:1.0:function:string-regexp-match  </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:2.0:function:anyURI-regexp-match ">urn:oasis:names:tc:xacml:2.0:function:anyURI-regexp-match  </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:2.0:function:ipAddress-regexp-match">urn:oasis:names:tc:xacml:2.0:function:ipAddress-regexp-match  </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:2.0:function:dnsName-regexp-match">urn:oasis:names:tc:xacml:2.0:function:dnsName-regexp-match  </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:2.0:function:rfc822Name-regexp-match ">urn:oasis:names:tc:xacml:2.0:function:rfc822Name-regexp-match  </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:2.0:function:x500Name-regexp-match">urn:oasis:names:tc:xacml:2.0:function:x500Name-regexp-match  </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:xpath-node-count">urn:oasis:names:tc:xacml:3.0:function:xpath-node-count </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:xpath-node-equal ">urn:oasis:names:tc:xacml:3.0:function:xpath-node-equal </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:xpath-node-match">urn:oasis:names:tc:xacml:3.0:function:xpath-node-match </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:string-intersection">urn:oasis:names:tc:xacml:1.0:function:string-intersection </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:string-at-least-one-member-of">urn:oasis:names:tc:xacml:1.0:function:string-at-least-one-member-of </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:string-union">urn:oasis:names:tc:xacml:1.0:function:string-union </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:string-subset">urn:oasis:names:tc:xacml:1.0:function:string-subset </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:string-set-equals">urn:oasis:names:tc:xacml:1.0:function:string-set-equals </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:boolean-intersection ">urn:oasis:names:tc:xacml:1.0:function:boolean-intersection </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:boolean-at-least-one-member-of">urn:oasis:names:tc:xacml:1.0:function:boolean-at-least-one-member-of </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:boolean-union">urn:oasis:names:tc:xacml:1.0:function:boolean-union </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:boolean-subset ">urn:oasis:names:tc:xacml:1.0:function:boolean-subset </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:boolean-set-equals ">urn:oasis:names:tc:xacml:1.0:function:boolean-set-equals </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:integer-intersection">urn:oasis:names:tc:xacml:1.0:function:integer-intersection</option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:integer-at-least-one-member-of ">urn:oasis:names:tc:xacml:1.0:function:integer-at-least-one-member-of  </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:integer-union ">urn:oasis:names:tc:xacml:1.0:function:integer-union  </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:integer-subset">urn:oasis:names:tc:xacml:1.0:function:integer-subset  </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:integer-set-equals">urn:oasis:names:tc:xacml:1.0:function:integer-set-equals  </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:double-intersection">urn:oasis:names:tc:xacml:1.0:function:double-intersection  </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:double-at-least-one-member-of">urn:oasis:names:tc:xacml:1.0:function:double-at-least-one-member-of  </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:double-union ">urn:oasis:names:tc:xacml:1.0:function:double-union  </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:double-subset">urn:oasis:names:tc:xacml:1.0:function:double-subset  </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:double-set-equals">urn:oasis:names:tc:xacml:1.0:function:double-set-equals  </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:time-intersection ">urn:oasis:names:tc:xacml:1.0:function:time-intersection  </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:time-at-least-one-member-of">urn:oasis:names:tc:xacml:1.0:function:time-at-least-one-member-of  </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:time-union ">urn:oasis:names:tc:xacml:1.0:function:time-union  </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:time-subset">urn:oasis:names:tc:xacml:1.0:function:time-subset  </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:time-set-equals">urn:oasis:names:tc:xacml:1.0:function:time-set-equals  </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:date-intersection">urn:oasis:names:tc:xacml:1.0:function:date-intersection  </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:date-at-least-one-member-of ">urn:oasis:names:tc:xacml:1.0:function:date-at-least-one-member-of  </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:date-union">urn:oasis:names:tc:xacml:1.0:function:date-union  </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:date-subset ">urn:oasis:names:tc:xacml:1.0:function:date-subset  </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:date-set-equals">urn:oasis:names:tc:xacml:1.0:function:date-set-equals  </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:dateTime-intersection">urn:oasis:names:tc:xacml:1.0:function:dateTime-intersection  </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:dateTime-at-least-one-member-of ">urn:oasis:names:tc:xacml:1.0:function:dateTime-at-least-one-member-of  </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:dateTime-union ">urn:oasis:names:tc:xacml:1.0:function:dateTime-union  </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:dateTime-subset ">urn:oasis:names:tc:xacml:1.0:function:dateTime-subset  </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:dateTime-set-equals ">urn:oasis:names:tc:xacml:1.0:function:dateTime-set-equals  </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:anyURI-intersection">urn:oasis:names:tc:xacml:1.0:function:anyURI-intersection  </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:anyURI-at-least-one-member-of ">urn:oasis:names:tc:xacml:1.0:function:anyURI-at-least-one-member-of  </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:anyURI-union ">urn:oasis:names:tc:xacml:1.0:function:anyURI-union  </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:anyURI-subset">urn:oasis:names:tc:xacml:1.0:function:anyURI-subset  </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:anyURI-set-equals ">urn:oasis:names:tc:xacml:1.0:function:anyURI-set-equals  </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:hexBinary-intersection ">urn:oasis:names:tc:xacml:1.0:function:hexBinary-intersection  </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:hexBinary-at-least-one-member-of ">urn:oasis:names:tc:xacml:1.0:function:hexBinary-at-least-one-member-of  </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:hexBinary-union ">urn:oasis:names:tc:xacml:1.0:function:hexBinary-union  </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:hexBinary-subset">urn:oasis:names:tc:xacml:1.0:function:hexBinary-subset  </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:hexBinary-set-equals">urn:oasis:names:tc:xacml:1.0:function:hexBinary-set-equals  </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:base64Binary-intersection ">urn:oasis:names:tc:xacml:1.0:function:base64Binary-intersection  </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:base64Binary-at-least-one-memberof">urn:oasis:names:tc:xacml:1.0:function:base64Binary-at-least-one-memberof  </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:base64Binary-union">urn:oasis:names:tc:xacml:1.0:function:base64Binary-union  </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:base64Binary-subset">urn:oasis:names:tc:xacml:1.0:function:base64Binary-subset  </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:base64Binary-set-equals ">urn:oasis:names:tc:xacml:1.0:function:base64Binary-set-equals  </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-intersection ">urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-intersection  </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-at-least-onemember-of">urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-at-least-onemember-of  </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-union  ">urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-union  </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-subset ">urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-subset  </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-set-equals">urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-set-equals  </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-intersection ">urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-intersection  </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-at-least-onemember-of">urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-at-least-onemember-of  </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-union  ">urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-union  </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-subset">urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-subset  </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-set-equals  ">urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-set-equals  </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:x500Name-intersection">urn:oasis:names:tc:xacml:1.0:function:x500Name-intersection  </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:x500Name-at-least-one-member-of">urn:oasis:names:tc:xacml:1.0:function:x500Name-at-least-one-member-of  </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:x500Name-union">urn:oasis:names:tc:xacml:1.0:function:x500Name-union  </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:x500Name-subset">urn:oasis:names:tc:xacml:1.0:function:x500Name-subset  </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:rfc822Name-intersection">urn:oasis:names:tc:xacml:1.0:function:rfc822Name-intersection </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:rfc822Name-at-least-one-member-of ">urn:oasis:names:tc:xacml:1.0:function:rfc822Name-at-least-one-member-of </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:rfc822Name-union ">urn:oasis:names:tc:xacml:1.0:function:rfc822Name-union </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:rfc822Name-subset">urn:oasis:names:tc:xacml:1.0:function:rfc822Name-subset </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:rfc822Name-set-equals">urn:oasis:names:tc:xacml:1.0:function:rfc822Name-set-equals </option>\n' +
'                                                                </select>\n' +
'                                                            </div>\n' +
'                                                        </div>\n' +
'                                                        <div class="form-group">\n' +
'                                                            <label class="col-sm-2 control-label">Data\n' +
'                                                                Type</label>\n' +
'                                                            <div class="col-sm-10">\n' +
'                                                                <select class="form-control" id="DataType5'+curr_size +'">\n' +
'                                                                    <option value="null">null</option>\n' +
'                                                                    <option value="http://www.w3.org/2001/XMLSchema#string">\n' +
'                                                                        http://www.w3.org/2001/XMLSchema#string\n' +
'                                                                    </option>\n' +
'                                                                    <option value=" http://www.w3.org/2001/XMLSchema#boolean">\n' +
'                                                                        http://www.w3.org/2001/XMLSchema#boolean\n' +
'                                                                    </option>\n' +
'                                                                    <option value="http://www.w3.org/2001/XMLSchema#integer">\n' +
'                                                                        http://www.w3.org/2001/XMLSchema#integer\n' +
'                                                                    </option>\n' +
'                                                                    <option value="ahttp://www.w3.org/2001/XMLSchema#double">\n' +
'                                                                        ahttp://www.w3.org/2001/XMLSchema#double\n' +
'                                                                    </option>\n' +
'                                                                    <option value="http://www.w3.org/2001/XMLSchema#time">\n' +
'                                                                        http://www.w3.org/2001/XMLSchema#time\n' +
'                                                                    </option>\n' +
'                                                                    <option value="http://www.w3.org/2001/XMLSchema#date">\n' +
'                                                                        http://www.w3.org/2001/XMLSchema#date\n' +
'                                                                    </option>\n' +
'                                                                    <option value="http://www.w3.og/2001/XMLSchema#dateTime">\n' +
'                                                                        http://www.w3.og/2001/XMLSchema#dateTime\n' +
'                                                                    </option>\n' +
'                                                                    <option value="http://www.w3.org/2001/XMLSchema#dayTimeDuration">\n' +
'                                                                        http://www.w3.org/2001/XMLSchema#dayTimeDuration\n' +
'                                                                    </option>\n' +
'                                                                    <option value="http://www.w3.org/2001/XMLSchema#yearMonthDuration">\n' +
'                                                                        http://www.w3.org/2001/XMLSchema#yearMonthDuration\n' +
'                                                                    </option>\n' +
'                                                                    <option value="http://www.w3.org/2001/XMLSchema#anyURI">\n' +
'                                                                        http://www.w3.org/2001/XMLSchema#anyURI\n' +
'                                                                    </option>\n' +
'                                                                    <option value="http://www.w3.org/2001/XMLSchema#hexBinary">\n' +
'                                                                        http://www.w3.org/2001/XMLSchema#hexBinary\n' +
'                                                                    </option>\n' +
'                                                                    <option value="http://www.w3.org/2001/XMLSchema#base64Binary">\n' +
'                                                                        http://www.w3.org/2001/XMLSchema#base64Binary\n' +
'                                                                    </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:data-type:rfc822Name">\n' +
'                                                                        urn:oasis:names:tc:xacml:1.0:data-type:rfc822Name\n' +
'                                                                    </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:data-type:x500Name">\n' +
'                                                                        urn:oasis:names:tc:xacml:1.0:data-type:x500Name\n' +
'                                                                    </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:3.0:data-type:xpathExpression">\n' +
'                                                                        urn:oasis:names:tc:xacml:3.0:data-type:xpathExpression\n' +
'                                                                    </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:2.0:data-type:ipAddress">\n' +
'                                                                        urn:oasis:names:tc:xacml:2.0:data-type:ipAddress\n' +
'                                                                    </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:2.0:data-type:dnsName">\n' +
'                                                                        urn:oasis:names:tc:xacml:2.0:data-type:dnsName\n' +
'                                                                    </option>\n' +
'                                                                </select>\n' +
'                                                            </div>\n' +
'                                                        </div>\n' +
'                                                        <div class="form-group">\n' +
'                                                            <label for="value5'+curr_size +'" class="col-sm-2 control-label">value</label>\n' +
'                                                            <div class="col-sm-10">\n' +
'                                                                <input type="text" class="form-control" id="value5'+curr_size +'" placeholder="null">\n' +
'                                                            </div>\n' +
'                                                        </div>\n' +
'                                                        <div class="form-group">\n' +
'                                                            <label for="AttributeId5'+curr_size +'" class="col-sm-2 control-label">AttributeId</label>\n' +
'                                                            <div class="col-sm-10">\n' +
'                                                                <input type="text" class="form-control" id="AttributeId5'+curr_size +'"\n' +
'                                                                       placeholder="null">\n' +
'                                                            </div>\n' +
'                                                        </div>\n' +
'                                                        <div class="form-group">\n' +
'                                                            <label  class="col-sm-2 control-label">MustBePresent</label>\n' +
'                                                            <div class="col-sm-10">\n' +
'                                                                <select class="form-control" id="MustBePresent5'+curr_size +'">\n' +
'                                                                    <option value="true">true</option>\n' +
'                                                                    <option value="false">false</option>\n' +
'                                                                </select>\n' +
'                                                            </div>\n' +
'                                                        </div>\n' +
'                                                    </div>\n' +
'                                                </div>\n' +
'                                                <div class="panel panel-default">\n' +
'                                                    <div class="panel-heading">\n' +
'                                                        <h3 class="panel-title">\n' +
'                                                            Action\n' +
'                                                        </h3>\n' +
'                                                    </div>\n' +
'                                                    <div class="panel-body">\n' +
'                                                        <div class="form-group">\n' +
'                                                            <label class="col-sm-2 control-label">MatchId</label>\n' +
'                                                            <div class="col-sm-10">\n' +
'                                                                <select class="form-control" id="MatchId6'+curr_size +'">\n' +
'                                                                    <option value="null">null</option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:string-equal">urn:oasis:names:tc:xacml:1.0:function:string-equal</option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:boolean-equal">urn:oasis:names:tc:xacml:1.0:function:boolean-equal </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:integer-equal">urn:oasis:names:tc:xacml:1.0:function:integer-equal </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:double-equal">urn:oasis:names:tc:xacml:1.0:function:double-equal </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:date-equal">urn:oasis:names:tc:xacml:1.0:function:date-equal</option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:time-equal">urn:oasis:names:tc:xacml:1.0:function:time-equal </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:dateTime-equal">urn:oasis:names:tc:xacml:1.0:function:dateTime-equal </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-equal">urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-equal </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-equal ">urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-equal </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:string-equal-ignore-case">urn:oasis:names:tc:xacml:3.0:function:string-equal-ignore-case </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:anyURI-equal">urn:oasis:names:tc:xacml:1.0:function:anyURI-equal </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:x500Name-equal">urn:oasis:names:tc:xacml:1.0:function:x500Name-equal </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:rfc822Name-equal">urn:oasis:names:tc:xacml:1.0:function:rfc822Name-equal </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:hexBinary-equal ">urn:oasis:names:tc:xacml:1.0:function:hexBinary-equal </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:base64Binary-equal">urn:oasis:names:tc:xacml:1.0:function:base64Binary-equal </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:integer-add ">urn:oasis:names:tc:xacml:1.0:function:integer-add </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:double-add ">urn:oasis:names:tc:xacml:1.0:function:double-add </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:integer-subtract">urn:oasis:names:tc:xacml:1.0:function:integer-subtract </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:double-subtract">urn:oasis:names:tc:xacml:1.0:function:double-subtract </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:integer-multiply">urn:oasis:names:tc:xacml:1.0:function:integer-multiply </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:double-multiply">urn:oasis:names:tc:xacml:1.0:function:double-multiply </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:integer-divide">urn:oasis:names:tc:xacml:1.0:function:integer-divide </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:double-divide">urn:oasis:names:tc:xacml:1.0:function:double-divide </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:integer-mod">urn:oasis:names:tc:xacml:1.0:function:integer-mod </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:integer-abs">urn:oasis:names:tc:xacml:1.0:function:integer-abs </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:double-abs">urn:oasis:names:tc:xacml:1.0:function:double-abs </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:round">urn:oasis:names:tc:xacml:1.0:function:round </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:floorurn:oasis:names:tc:xacml:1.0:function:string-normalize-space">urn:oasis:names:tc:xacml:1.0:function:floorurn:oasis:names:tc:xacml:1.0:function:string-normalize-space </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:string-normalize-to-lower-case">urn:oasis:names:tc:xacml:1.0:function:string-normalize-to-lower-case </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:double-to-integer ">urn:oasis:names:tc:xacml:1.0:function:double-to-integer </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:integer-to-double">urn:oasis:names:tc:xacml:1.0:function:integer-to-double </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:or">urn:oasis:names:tc:xacml:1.0:function:or </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:and">urn:oasis:names:tc:xacml:1.0:function:and </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:n-of">urn:oasis:names:tc:xacml:1.0:function:n-of </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:noturn:oasis:names:tc:xacml:1.0:function:integer-greater-than ">urn:oasis:names:tc:xacml:1.0:function:noturn:oasis:names:tc:xacml:1.0:function:integer-greater-than </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:integer-greater-than-or-equal ">urn:oasis:names:tc:xacml:1.0:function:integer-greater-than-or-equal </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:integer-greater-than-or-equal ">urn:oasis:names:tc:xacml:1.0:function:integer-greater-than-or-equal  </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:integer-less-than-or-equal">urn:oasis:names:tc:xacml:1.0:function:integer-less-than-or-equal </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:double-greater-than">urn:oasis:names:tc:xacml:1.0:function:double-greater-than </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:double-greater-than-or-equal">urn:oasis:names:tc:xacml:1.0:function:double-greater-than-or-equal </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:double-less-than">urn:oasis:names:tc:xacml:1.0:function:double-less-than </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:double-less-than-or-equal">urn:oasis:names:tc:xacml:1.0:function:double-less-than-or-equal </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:dateTime-add-dayTimeDuration">urn:oasis:names:tc:xacml:3.0:function:dateTime-add-dayTimeDuration </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:dateTime-add-yearMonthDuration ">urn:oasis:names:tc:xacml:3.0:function:dateTime-add-yearMonthDuration </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:dateTime-subtract-dayTimeDuration">urn:oasis:names:tc:xacml:3.0:function:dateTime-subtract-dayTimeDuration  </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:dateTime-subtractyearMonthDuration">urn:oasis:names:tc:xacml:3.0:function:dateTime-subtractyearMonthDuration </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:date-add-yearMonthDuration">urn:oasis:names:tc:xacml:3.0:function:date-add-yearMonthDuration </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:date-subtract-yearMonthDuration">urn:oasis:names:tc:xacml:3.0:function:date-subtract-yearMonthDuration </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:string-greater-than">urn:oasis:names:tc:xacml:1.0:function:string-greater-than </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:string-greater-than-or-equal">urn:oasis:names:tc:xacml:1.0:function:string-greater-than-or-equal </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:string-less-than">urn:oasis:names:tc:xacml:1.0:function:string-less-than </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:string-less-than-or-equal">urn:oasis:names:tc:xacml:1.0:function:string-less-than-or-equal </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:time-greater-than ">urn:oasis:names:tc:xacml:1.0:function:time-greater-than </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:time-greater-than-or-equal">urn:oasis:names:tc:xacml:1.0:function:time-greater-than-or-equal </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:time-less-than">urn:oasis:names:tc:xacml:1.0:function:time-less-than </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:time-less-than-or-equal">urn:oasis:names:tc:xacml:1.0:function:time-less-than-or-equal </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:2.0:function:time-in-range ">urn:oasis:names:tc:xacml:2.0:function:time-in-range </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:dateTime-greater-than">urn:oasis:names:tc:xacml:1.0:function:dateTime-greater-than </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:dateTime-greater-than-or-equal ">urn:oasis:names:tc:xacml:1.0:function:dateTime-greater-than-or-equal </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:dateTime-less-than">urn:oasis:names:tc:xacml:1.0:function:dateTime-less-than </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:dateTime-less-than-or-equal">urn:oasis:names:tc:xacml:1.0:function:dateTime-less-than-or-equal </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:date-greater-than ">urn:oasis:names:tc:xacml:1.0:function:date-greater-than </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:date-greater-than-or-equal">urn:oasis:names:tc:xacml:1.0:function:date-greater-than-or-equal </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:date-less-than">urn:oasis:names:tc:xacml:1.0:function:date-less-than </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:date-less-than-or-equal ">urn:oasis:names:tc:xacml:1.0:function:date-less-than-or-equal </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:string-one-and-only ">urn:oasis:names:tc:xacml:1.0:function:string-one-and-only </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:string-bag-size">urn:oasis:names:tc:xacml:1.0:function:string-bag-size </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:string-is-in ">urn:oasis:names:tc:xacml:1.0:function:string-is-in </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:string-bag">urn:oasis:names:tc:xacml:1.0:function:string-bag </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:boolean-one-and-only">urn:oasis:names:tc:xacml:1.0:function:boolean-one-and-only </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:boolean-bag-size">urn:oasis:names:tc:xacml:1.0:function:boolean-bag-size </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:boolean-is-in ">urn:oasis:names:tc:xacml:1.0:function:boolean-is-in </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:boolean-bag">urn:oasis:names:tc:xacml:1.0:function:boolean-bag </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:integer-one-and-only">urn:oasis:names:tc:xacml:1.0:function:integer-one-and-only </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:integer-bag-size">urn:oasis:names:tc:xacml:1.0:function:integer-bag-size</option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:integer-is-in">urn:oasis:names:tc:xacml:1.0:function:integer-is-in </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:integer-bag ">urn:oasis:names:tc:xacml:1.0:function:integer-bag </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:double-one-and-only">urn:oasis:names:tc:xacml:1.0:function:double-one-and-only </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:double-bag-size">urn:oasis:names:tc:xacml:1.0:function:double-bag-size </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:double-is-in">urn:oasis:names:tc:xacml:1.0:function:double-is-in </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:double-bag">urn:oasis:names:tc:xacml:1.0:function:double-bag </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:time-one-and-only ">urn:oasis:names:tc:xacml:1.0:function:time-one-and-only </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:time-bag-size">urn:oasis:names:tc:xacml:1.0:function:time-bag-size </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:time-is-in ">urn:oasis:names:tc:xacml:1.0:function:time-is-in </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:time-bag">urn:oasis:names:tc:xacml:1.0:function:time-bag </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:date-one-and-only ">urn:oasis:names:tc:xacml:1.0:function:date-one-and-only </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:date-bag-size ">urn:oasis:names:tc:xacml:1.0:function:date-bag-size </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:date-is-in ">urn:oasis:names:tc:xacml:1.0:function:date-is-in </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:date-bag ">urn:oasis:names:tc:xacml:1.0:function:date-bag </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:dateTime-one-and-only">urn:oasis:names:tc:xacml:1.0:function:dateTime-one-and-only </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:dateTime-bag-size">urn:oasis:names:tc:xacml:1.0:function:dateTime-bag-size </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:dateTime-is-in ">urn:oasis:names:tc:xacml:1.0:function:dateTime-is-in </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:dateTime-bag">urn:oasis:names:tc:xacml:1.0:function:dateTime-bag </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:anyURI-one-and-only">urn:oasis:names:tc:xacml:1.0:function:anyURI-one-and-only </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:anyURI-one-and-only">urn:oasis:names:tc:xacml:1.0:function:anyURI-one-and-only</option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:anyURI-is-in">urn:oasis:names:tc:xacml:1.0:function:anyURI-is-in </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:anyURI-bag">urn:oasis:names:tc:xacml:1.0:function:anyURI-bag </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:hexBinary-one-and-only">urn:oasis:names:tc:xacml:1.0:function:hexBinary-one-and-only </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:hexBinary-bag-size">urn:oasis:names:tc:xacml:1.0:function:hexBinary-bag-size </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:hexBinary-is-in">urn:oasis:names:tc:xacml:1.0:function:hexBinary-is-in </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:hexBinary-bag">urn:oasis:names:tc:xacml:1.0:function:hexBinary-bag </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:base64Binary-one-and-only">urn:oasis:names:tc:xacml:1.0:function:base64Binary-one-and-only </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:base64Binary-bag-size">urn:oasis:names:tc:xacml:1.0:function:base64Binary-bag-size </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:base64Binary-is-in">urn:oasis:names:tc:xacml:1.0:function:base64Binary-is-in </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:base64Binary-bag">urn:oasis:names:tc:xacml:1.0:function:base64Binary-bag </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-one-and-only">urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-one-and-only </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-bag-size">urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-bag-size </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-is-in ">urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-is-in </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-bag">urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-bag </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-one-and-only">urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-one-and-only </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-bag-size">urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-bag-size </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-is-in">urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-is-in </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-bag">urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-bag </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:x500Name-one-and-only">urn:oasis:names:tc:xacml:1.0:function:x500Name-one-and-only </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:x500Name-one-and-only">urn:oasis:names:tc:xacml:1.0:function:x500Name-one-and-only </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:x500Name-is-in">urn:oasis:names:tc:xacml:1.0:function:x500Name-is-in </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:x500Name-bag">urn:oasis:names:tc:xacml:1.0:function:x500Name-bag </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:rfc822Name-one-and-only">urn:oasis:names:tc:xacml:1.0:function:rfc822Name-one-and-only </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:rfc822Name-bag-size">urn:oasis:names:tc:xacml:1.0:function:rfc822Name-bag-size </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:rfc822Name-is-in">urn:oasis:names:tc:xacml:1.0:function:rfc822Name-is-in </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:rfc822Name-bag ">urn:oasis:names:tc:xacml:1.0:function:rfc822Name-bag </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:2.0:function:ipAddress-one-and-only">urn:oasis:names:tc:xacml:2.0:function:ipAddress-one-and-only </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:2.0:function:ipAddress-bag-size">urn:oasis:names:tc:xacml:2.0:function:ipAddress-bag-size </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:2.0:function:ipAddress-bag ">urn:oasis:names:tc:xacml:2.0:function:ipAddress-bag </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:2.0:function:dnsName-one-and-only">urn:oasis:names:tc:xacml:2.0:function:dnsName-one-and-only </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:2.0:function:dnsName-bag-size">urn:oasis:names:tc:xacml:2.0:function:dnsName-bag-size </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:2.0:function:dnsName-bag">urn:oasis:names:tc:xacml:2.0:function:dnsName-bag </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:2.0:function:string-concatenate">urn:oasis:names:tc:xacml:2.0:function:string-concatenate </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:boolean-from-string">urn:oasis:names:tc:xacml:3.0:function:boolean-from-string </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:string-from-boolean">urn:oasis:names:tc:xacml:3.0:function:string-from-boolean </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:integer-from-string ">urn:oasis:names:tc:xacml:3.0:function:integer-from-string </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:string-from-integer ">urn:oasis:names:tc:xacml:3.0:function:string-from-integer </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:string-from-double">urn:oasis:names:tc:xacml:3.0:function:string-from-double</option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:string-from-double">urn:oasis:names:tc:xacml:3.0:function:string-from-double </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:time-from-string">urn:oasis:names:tc:xacml:3.0:function:time-from-string </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:string-from-time">urn:oasis:names:tc:xacml:3.0:function:string-from-time </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:date-from-string">urn:oasis:names:tc:xacml:3.0:function:date-from-string </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:string-from-date">urn:oasis:names:tc:xacml:3.0:function:string-from-date </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:dateTime-from-string">urn:oasis:names:tc:xacml:3.0:function:dateTime-from-string </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:string-from-dateTime">urn:oasis:names:tc:xacml:3.0:function:string-from-dateTime </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:anyURI-from-string ">urn:oasis:names:tc:xacml:3.0:function:anyURI-from-string </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:string-from-anyURI">urn:oasis:names:tc:xacml:3.0:function:string-from-anyURI </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-from-string">urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-from-string </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:string-from-dayTimeDuration ">urn:oasis:names:tc:xacml:3.0:function:string-from-dayTimeDuration </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-from-string ">urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-from-string </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:string-from-yearMonthDuration">urn:oasis:names:tc:xacml:3.0:function:string-from-yearMonthDuration </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:x500Name-from-string ">urn:oasis:names:tc:xacml:3.0:function:x500Name-from-string </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:string-from-x500Name">urn:oasis:names:tc:xacml:3.0:function:string-from-x500Name </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:rfc822Name-from-string">urn:oasis:names:tc:xacml:3.0:function:rfc822Name-from-string </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:string-from-rfc822Name">urn:oasis:names:tc:xacml:3.0:function:string-from-rfc822Name </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:ipAddress-from-string ">urn:oasis:names:tc:xacml:3.0:function:ipAddress-from-string </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:string-from-ipAddress">urn:oasis:names:tc:xacml:3.0:function:string-from-ipAddress </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:dnsName-from-string">urn:oasis:names:tc:xacml:3.0:function:dnsName-from-string </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:string-from-dnsName ">urn:oasis:names:tc:xacml:3.0:function:string-from-dnsName </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:string-starts-with">urn:oasis:names:tc:xacml:3.0:function:string-starts-with </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:anyURI-starts-with">urn:oasis:names:tc:xacml:3.0:function:anyURI-starts-with </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:string-ends-with ">urn:oasis:names:tc:xacml:3.0:function:string-ends-with </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:anyURI-ends-with">urn:oasis:names:tc:xacml:3.0:function:anyURI-ends-with </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:string-contains">urn:oasis:names:tc:xacml:3.0:function:string-contains </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:anyURI-contains">urn:oasis:names:tc:xacml:3.0:function:anyURI-contains </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:string-substring ">urn:oasis:names:tc:xacml:3.0:function:string-substring </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:anyURI-substring ">urn:oasis:names:tc:xacml:3.0:function:anyURI-substring </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:any-of ">urn:oasis:names:tc:xacml:3.0:function:any-of </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:all-of">urn:oasis:names:tc:xacml:3.0:function:all-of </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:any-of-any ">urn:oasis:names:tc:xacml:3.0:function:any-of-any </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:all-of-any">urn:oasis:names:tc:xacml:1.0:function:all-of-any </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:any-of-all">urn:oasis:names:tc:xacml:1.0:function:any-of-all  </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:all-of-all">urn:oasis:names:tc:xacml:1.0:function:all-of-all  </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:map">urn:oasis:names:tc:xacml:3.0:function:map </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:x500Name-match">urn:oasis:names:tc:xacml:1.0:function:x500Name-match  </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:rfc822Name-match">urn:oasis:names:tc:xacml:1.0:function:rfc822Name-match  </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:string-regexp-match">urn:oasis:names:tc:xacml:1.0:function:string-regexp-match  </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:2.0:function:anyURI-regexp-match ">urn:oasis:names:tc:xacml:2.0:function:anyURI-regexp-match  </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:2.0:function:ipAddress-regexp-match">urn:oasis:names:tc:xacml:2.0:function:ipAddress-regexp-match  </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:2.0:function:dnsName-regexp-match">urn:oasis:names:tc:xacml:2.0:function:dnsName-regexp-match  </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:2.0:function:rfc822Name-regexp-match ">urn:oasis:names:tc:xacml:2.0:function:rfc822Name-regexp-match  </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:2.0:function:x500Name-regexp-match">urn:oasis:names:tc:xacml:2.0:function:x500Name-regexp-match  </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:xpath-node-count">urn:oasis:names:tc:xacml:3.0:function:xpath-node-count </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:xpath-node-equal ">urn:oasis:names:tc:xacml:3.0:function:xpath-node-equal </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:xpath-node-match">urn:oasis:names:tc:xacml:3.0:function:xpath-node-match </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:string-intersection">urn:oasis:names:tc:xacml:1.0:function:string-intersection </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:string-at-least-one-member-of">urn:oasis:names:tc:xacml:1.0:function:string-at-least-one-member-of </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:string-union">urn:oasis:names:tc:xacml:1.0:function:string-union </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:string-subset">urn:oasis:names:tc:xacml:1.0:function:string-subset </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:string-set-equals">urn:oasis:names:tc:xacml:1.0:function:string-set-equals </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:boolean-intersection ">urn:oasis:names:tc:xacml:1.0:function:boolean-intersection </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:boolean-at-least-one-member-of">urn:oasis:names:tc:xacml:1.0:function:boolean-at-least-one-member-of </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:boolean-union">urn:oasis:names:tc:xacml:1.0:function:boolean-union </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:boolean-subset ">urn:oasis:names:tc:xacml:1.0:function:boolean-subset </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:boolean-set-equals ">urn:oasis:names:tc:xacml:1.0:function:boolean-set-equals </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:integer-intersection">urn:oasis:names:tc:xacml:1.0:function:integer-intersection</option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:integer-at-least-one-member-of ">urn:oasis:names:tc:xacml:1.0:function:integer-at-least-one-member-of  </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:integer-union ">urn:oasis:names:tc:xacml:1.0:function:integer-union  </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:integer-subset">urn:oasis:names:tc:xacml:1.0:function:integer-subset  </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:integer-set-equals">urn:oasis:names:tc:xacml:1.0:function:integer-set-equals  </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:double-intersection">urn:oasis:names:tc:xacml:1.0:function:double-intersection  </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:double-at-least-one-member-of">urn:oasis:names:tc:xacml:1.0:function:double-at-least-one-member-of  </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:double-union ">urn:oasis:names:tc:xacml:1.0:function:double-union  </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:double-subset">urn:oasis:names:tc:xacml:1.0:function:double-subset  </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:double-set-equals">urn:oasis:names:tc:xacml:1.0:function:double-set-equals  </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:time-intersection ">urn:oasis:names:tc:xacml:1.0:function:time-intersection  </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:time-at-least-one-member-of">urn:oasis:names:tc:xacml:1.0:function:time-at-least-one-member-of  </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:time-union ">urn:oasis:names:tc:xacml:1.0:function:time-union  </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:time-subset">urn:oasis:names:tc:xacml:1.0:function:time-subset  </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:time-set-equals">urn:oasis:names:tc:xacml:1.0:function:time-set-equals  </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:date-intersection">urn:oasis:names:tc:xacml:1.0:function:date-intersection  </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:date-at-least-one-member-of ">urn:oasis:names:tc:xacml:1.0:function:date-at-least-one-member-of  </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:date-union">urn:oasis:names:tc:xacml:1.0:function:date-union  </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:date-subset ">urn:oasis:names:tc:xacml:1.0:function:date-subset  </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:date-set-equals">urn:oasis:names:tc:xacml:1.0:function:date-set-equals  </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:dateTime-intersection">urn:oasis:names:tc:xacml:1.0:function:dateTime-intersection  </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:dateTime-at-least-one-member-of ">urn:oasis:names:tc:xacml:1.0:function:dateTime-at-least-one-member-of  </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:dateTime-union ">urn:oasis:names:tc:xacml:1.0:function:dateTime-union  </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:dateTime-subset ">urn:oasis:names:tc:xacml:1.0:function:dateTime-subset  </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:dateTime-set-equals ">urn:oasis:names:tc:xacml:1.0:function:dateTime-set-equals  </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:anyURI-intersection">urn:oasis:names:tc:xacml:1.0:function:anyURI-intersection  </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:anyURI-at-least-one-member-of ">urn:oasis:names:tc:xacml:1.0:function:anyURI-at-least-one-member-of  </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:anyURI-union ">urn:oasis:names:tc:xacml:1.0:function:anyURI-union  </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:anyURI-subset">urn:oasis:names:tc:xacml:1.0:function:anyURI-subset  </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:anyURI-set-equals ">urn:oasis:names:tc:xacml:1.0:function:anyURI-set-equals  </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:hexBinary-intersection ">urn:oasis:names:tc:xacml:1.0:function:hexBinary-intersection  </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:hexBinary-at-least-one-member-of ">urn:oasis:names:tc:xacml:1.0:function:hexBinary-at-least-one-member-of  </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:hexBinary-union ">urn:oasis:names:tc:xacml:1.0:function:hexBinary-union  </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:hexBinary-subset">urn:oasis:names:tc:xacml:1.0:function:hexBinary-subset  </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:hexBinary-set-equals">urn:oasis:names:tc:xacml:1.0:function:hexBinary-set-equals  </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:base64Binary-intersection ">urn:oasis:names:tc:xacml:1.0:function:base64Binary-intersection  </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:base64Binary-at-least-one-memberof">urn:oasis:names:tc:xacml:1.0:function:base64Binary-at-least-one-memberof  </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:base64Binary-union">urn:oasis:names:tc:xacml:1.0:function:base64Binary-union  </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:base64Binary-subset">urn:oasis:names:tc:xacml:1.0:function:base64Binary-subset  </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:base64Binary-set-equals ">urn:oasis:names:tc:xacml:1.0:function:base64Binary-set-equals  </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-intersection ">urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-intersection  </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-at-least-onemember-of">urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-at-least-onemember-of  </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-union  ">urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-union  </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-subset ">urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-subset  </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-set-equals">urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-set-equals  </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-intersection ">urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-intersection  </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-at-least-onemember-of">urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-at-least-onemember-of  </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-union  ">urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-union  </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-subset">urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-subset  </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-set-equals  ">urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-set-equals  </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:x500Name-intersection">urn:oasis:names:tc:xacml:1.0:function:x500Name-intersection  </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:x500Name-at-least-one-member-of">urn:oasis:names:tc:xacml:1.0:function:x500Name-at-least-one-member-of  </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:x500Name-union">urn:oasis:names:tc:xacml:1.0:function:x500Name-union  </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:x500Name-subset">urn:oasis:names:tc:xacml:1.0:function:x500Name-subset  </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:rfc822Name-intersection">urn:oasis:names:tc:xacml:1.0:function:rfc822Name-intersection </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:rfc822Name-at-least-one-member-of ">urn:oasis:names:tc:xacml:1.0:function:rfc822Name-at-least-one-member-of </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:rfc822Name-union ">urn:oasis:names:tc:xacml:1.0:function:rfc822Name-union </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:rfc822Name-subset">urn:oasis:names:tc:xacml:1.0:function:rfc822Name-subset </option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:function:rfc822Name-set-equals">urn:oasis:names:tc:xacml:1.0:function:rfc822Name-set-equals </option>\n' +
'                                                                </select>\n' +
'                                                            </div>\n' +
'                                                        </div>\n' +
'                                                        <div class="form-group">\n' +
'                                                            <label class="col-sm-2 control-label">Data\n' +
'                                                                Type</label>\n' +
'                                                            <div class="col-sm-10">\n' +
'                                                                <select class="form-control" id="DataType6'+curr_size +'">\n' +
'                                                                    <option value="null">null</option>\n' +
'                                                                    <option value="http://www.w3.org/2001/XMLSchema#string">http://www.w3.org/2001/XMLSchema#string</option>\n' +
'                                                                    <option value="http://www.w3.org/2001/XMLSchema#boolean">http://www.w3.org/2001/XMLSchema#boolean</option>\n' +
'                                                                    <option value="http://www.w3.org/2001/XMLSchema#integer">http://www.w3.org/2001/XMLSchema#integer</option>\n' +
'                                                                    <option value="ahttp://www.w3.org/2001/XMLSchema#double">ahttp://www.w3.org/2001/XMLSchema#double</option>\n' +
'                                                                    <option value="http://www.w3.org/2001/XMLSchema#time">http://www.w3.org/2001/XMLSchema#time</option>\n' +
'                                                                    <option value="http://www.w3.org/2001/XMLSchema#date">http://www.w3.org/2001/XMLSchema#date</option>\n' +
'                                                                    <option value="http://www.w3.org/2001/XMLSchema#dateTime">http://www.w3.org/2001/XMLSchema#dateTime</option>\n' +
'                                                                    <option value="http://www.w3.org/2001/XMLSchema#dayTimeDuration">http://www.w3.org/2001/XMLSchema#dayTimeDuration</option>\n' +
'                                                                    <option value="http://www.w3.org/2001/XMLSchema#yearMonthDuration">http://www.w3.org/2001/XMLSchema#yearMonthDuration</option>\n' +
'                                                                    <option value="http://www.w3.org/2001/XMLSchema#anyURI">http://www.w3.org/2001/XMLSchema#anyURI</option>\n' +
'                                                                    <option value="http://www.w3.org/2001/XMLSchema#hexBinary">http://www.w3.org/2001/XMLSchema#hexBinary</option>\n' +
'                                                                    <option value="http://www.w3.org/2001/XMLSchema#base64Binary">http://www.w3.org/2001/XMLSchema#base64Binary</option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:data-type:rfc822Name">urn:oasis:names:tc:xacml:1.0:data-type:rfc822Name</option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:1.0:data-type:x500Name">urn:oasis:names:tc:xacml:1.0:data-type:x500Name</option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:3.0:data-type:xpathExpression">urn:oasis:names:tc:xacml:3.0:data-type:xpathExpression</option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:2.0:data-type:ipAddress">urn:oasis:names:tc:xacml:2.0:data-type:ipAddress</option>\n' +
'                                                                    <option value="urn:oasis:names:tc:xacml:2.0:data-type:dnsName">urn:oasis:names:tc:xacml:2.0:data-type:dnsName</option>\n' +
'                                                                </select>\n' +
'                                                            </div>\n' +
'                                                        </div>\n' +
'                                                        <div class="form-group">\n' +
'                                                            <label for="value6'+curr_size +'" class="col-sm-2 control-label">value</label>\n' +
'                                                            <div class="col-sm-10">\n' +
'                                                                <input type="text" class="form-control" id="value6\'+curr_size +\'" placeholder="null">\n' +
'                                                            </div>\n' +
'                                                        </div>\n' +
'                                                        <div class="form-group">\n' +
'                                                            <label for="AttributeId6'+curr_size +'" class="col-sm-2 control-label">AttributeId</label>\n' +
'                                                            <div class="col-sm-10">\n' +
'                                                                <input type="text" class="form-control" id="AttributeId6'+curr_size +'"\n' +
'                                                                       placeholder="null">\n' +
'                                                            </div>\n' +
'                                                        </div>\n' +
'                                                        <div class="form-group">\n' +
'                                                            <label  class="col-sm-2 control-label">MustBePresent</label>\n' +
'                                                            <div class="col-sm-10">\n' +
'                                                                <select class="form-control" id="MustBePresent6'+curr_size +'">\n' +
'                                                                    <option value="true">true</option>\n' +
'                                                                    <option value="false">false</option>\n' +
'                                                                </select>\n' +
'                                                            </div>\n' +
'                                                        </div>\n' +
'                                                    </div>\n' +
'                                                </div>';

        $("#formpart")[0].parentNode.innerHTML+=addform;
        //删除表单
        $("#removeform").click(function(){
            $("#addformbody").remove();
        });

    });
    function save(){
        var RuleId=[];
        var Effect=[];
        var description2=[];
        var MatchId4=[];
        var MatchId5=[];
        var MatchId6=[];
        var DataType4=[];
        var DataType5=[];
        var DataType6=[];
        var value4=[];
        var value5=[];
        var value6=[];
        var AttributeId4=[];
        var AttributeId5=[];
        var AttributeId6=[];
        var MustBePresent4=[];
        var MustBePresent5=[];
        var MustBePresent6=[];
        var curr_size = document.getElementById("formpart").parentNode.children.length+<%=size%>;     //Rules数量
        for(var i = 1;i < curr_size;i++){
            RuleId[i-1]=encodeURI($("#RuleId"+i+"").val());
            Effect[i-1]=$("#Effect"+i+"").val();
            description2[i-1]=$("#description2"+i+"").val();
            MatchId4[i-1]=encodeURI($("#MatchId4"+i+"").val());
            MatchId5[i-1]=encodeURI($("#MatchId5"+i+"").val());
            MatchId6[i-1]=encodeURI($("#MatchId6"+i+"").val());
            DataType4[i-1]=encodeURI($("#DataType4"+i+"").val());
            DataType5[i-1]=encodeURI($("#DataType5"+i+"").val());
            DataType6[i-1]=encodeURI($("#DataType6"+i+"").val());
            value4[i-1]=encodeURI($("#value4"+i+"").val());
            value5[i-1]=encodeURI($("#value5"+i+"").val());
            value6[i-1]=encodeURI($("#value6"+i+"").val());
            AttributeId4[i-1]=encodeURI($("#AttributeId4"+i+"").val());
            AttributeId5[i-1]=encodeURI($("#AttributeId5"+i+"").val());
            AttributeId6[i-1]=encodeURI($("#AttributeId6"+i+"").val());
            MustBePresent4[i-1]=encodeURI($("#MustBePresent4"+i+"").val());
            MustBePresent5[i-1]=encodeURI($("#MustBePresent5"+i+"").val());
            MustBePresent6[i-1]=encodeURI($("#MustBePresent6"+i+"").val());

        }

        //var array = ["1", "2"];
        //console.log(array);
        console.log(RuleId);
        console.log(Effect);
        console.log(description2);
        console.log(MatchId4);
        console.log(DataType4);
        console.log(value6);
        console.log(AttributeId4);
        console.log(MustBePresent4);

        $.ajax({
            data:{
                //"rules":rules,
                //"array":array,
                "PolicyId":encodeURI($("#PolicyId").val()),
                "Version":$("#Version").val(),
                "RuleCombiningAlgId":$("#RuleCombiningAlgId option:selected").text(),
                "description1":encodeURI($("#description1").val()),
                "MatchId1":$("#MatchId1").val(),
                "DataType1":$("#DataType1").val(),
                "value1":encodeURI($("#value1").val()),
                "AttributeId1":encodeURI($("#AttributeId1").val()),
                "MustBePresent1":$("#MustBePresent1").val(),

                "MatchId2":$("#MatchId2").val(),
                "DataType2":$("#DataType2").val(),
                "value2":encodeURI($("#value3").val()),
                "AttributeId2":encodeURI($("#AttributeId2").val()),
                "MustBePresent2":$("#MustBePresent2").val(),

                "MatchId3":$("#MatchId3").val(),
                "DataType3":$("#DataType3").val(),
                "value3":encodeURI($("#value3").val()),
                "AttributeId3":encodeURI($("#AttributeId3").val()),
                "MustBePresent3":$("#MustBePresent3").val(),


                "RuleId":RuleId,
                "Effect":Effect,
                "description2":description2,


                "MatchId4":MatchId4,
                "DataType4":DataType4,
                "value4":value4,
                "AttributeId4":AttributeId4,
                "MustBePresent4":MustBePresent4,

                "MatchId5":MatchId5,
                "DataType5":DataType5,
                "value5":value5,
                "AttributeId5":AttributeId5,
                "MustBePresent5":MustBePresent5,

                "MatchId6":MatchId6,
                "DataType6":DataType6,
                "value6":value6,
                "AttributeId6":AttributeId6,
                "MustBePresent6":MustBePresent6,

            },
            type:"GET",
            contentType: "application/json; charset=utf-8",
            dataType: "json",
            url:"/main",
            error:function(data){
                alert(data["msg"]);
            },
            success:function(data){
                alert(data["msg"]);
                //$("#result").html(data) ;
            }
        });
    }

    function delete2(){
        $.ajax({
            data:{"PolicyId":encodeURI($("#PolicyId").val()),},
            type: "GET",
            dataType: "json",
            contentType: "application/json; charset=utf-8",
            url: "/delete",
            error:function(data){
                alert("成功删除Xacml文件");
            },
            success:function(data){
                alert("成功删除Xacml文件");
            }
        });
    }
</script>
</html>