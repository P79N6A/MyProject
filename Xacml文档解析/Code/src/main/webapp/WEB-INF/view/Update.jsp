<%@ page import="javax.swing.text.Document" %>
<%@ page import="java.util.TreeMap" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.HashMap" %>
<%@ page language="java" contentType="text/html; charset=utf-8"
         pageEncoding="utf-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>xacml策略管理系统</title>
    <link rel="stylesheet" href="css/bootstrap.css">
    <script src="js/jquery-3.3.1.js"></script>
    <script src="js/bootstrap.js"></script>
</head>
<body>

<%
    HashMap<String,Object> map=(HashMap<String,Object>)request.getAttribute("map");
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
                <button class="btn btn-default"  id="save">保存策略</button><br>
                <button class="btn btn-default" id="delete" style="margin-top: 20px;">删除策略</button>
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
                                    <label for="vresion" class="col-sm-2 control-label">vresion</label>
                                    <div class="col-sm-10">
                                        <input type="text" class="form-control" id="vresion"
                                               placeholder=<%=map.get("Version")%>>
                                    </div>
                                </div>
                                <div class="form-group">
                                    <label class="col-sm-2 control-label">RulecombiningAigId</label>
                                    <div class="col-sm-10">
                                        <select class="form-control">
                                            <option><%=map.get("RuleCombiningAlgId")%></option>
                                            <option> </option>
                                            <option>
                                                urn:oasis:names:tc:xacml:3.0:rule-combining-algorithm:deny-overrides
                                            </option>
                                            <option>
                                                rn:oasis:names:tc:xacml:3.0:rule-combining-algorithm:permit-overrides
                                            </option>
                                            <option>
                                                urn:oasis:names:tc:xacml:1.0:rule-combining-algorithm:first-applicable
                                            </option>
                                            <option>
                                                applicableurn:oasis:names:tc:xacml:3.0:rule-combining-algorithm:ordered-deny-overrides
                                            </option>
                                            <option>
                                                urn:oasis:names:tc:xacml:3.0:rule-combining-algorithm:ordered-permit-overrides
                                            </option>
                                            <option>
                                                urn:oasis:names:tc:xacml:3.0:rule-combining-algorithm:deny-unless-permit
                                            </option>
                                            <option>
                                                urn:oasis:names:tc:xacml:3.0:rule-combining-algorithm:permit-unless-deny
                                            </option>
                                        </select>
                                    </div>
                                </div>
                                <div class="form-group">
                                    <label for="description" class="col-sm-2 control-label">description</label>
                                    <div class="col-sm-10">
                                        <textarea class="form-control" rows="3" id="description"><%=map.get("Description1")%>></textarea>
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
                                                <select class="form-control">
                                                    <option><%=map.get("MatchId1")%></option>
                                                    <option> </option>
                                                    <option>urn:oasis:names:tc:xacml:1.0:function:string-equal</option>
                                                    <option>urn:oasis:names:tc:xacml:1.0:function:boolean-equal</option>
                                                    <option>urn:oasis:names:tc:xacml:1.0:function:boolean-equa</option>
                                                    <option>urn:oasis:names:tc:xacml:1.0:function:integer-equal</option>
                                                    <option>urn:oasis:names:tc:xacml:1.0:function:double-equal</option>
                                                    <option>urn:oasis:names:tc:xacml:1.0:function:date-equal</option>
                                                    <option>urn:oasis:names:tc:xacml:1.0:function:time-equal</option>
                                                    <option>urn:oasis:names:tc:xacml:1.0:function:dateTime-equal</option>
                                                    <option>urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-equal</option>
                                                    <option>urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-equal</option>
                                                    <option>urn:oasis:names:tc:xacml:1.0:function:anyURI-equal</option>
                                                    <option>urn:oasis:names:tc:xacml:1.0:function:x500Name-equal</option>
                                                    <option>urn:oasis:names:tc:xacml:1.0:function:rfc822Name-equal</option>
                                                    <option>urn:oasis:names:tc:xacml:1.0:function:hexBinary-equal</option>
                                                    <option>urn:oasis:names:tc:xacml:1.0:function:base64Binary-equal</option>
                                                </select>
                                            </div>
                                        </div>
                                        <div class="form-group">
                                            <label class="col-sm-2 control-label">Data Type</label>
                                            <div class="col-sm-10">
                                                <select class="form-control">
                                                    <option><%=map.get("DataType1")%></option>
                                                    <option> </option>
                                                    <option>http://www.w3.org/2001/XMLSchema#string</option>
                                                    <option>http://www.w3.org/2001/XMLSchema#boolean</option>
                                                    <option>http://www.w3.org/2001/XMLSchema#integer</option>
                                                    <option>ahttp://www.w3.org/2001/XMLSchema#double</option>
                                                    <option>http://www.w3.org/2001/XMLSchema#time</option>
                                                    <option>http://www.w3.org/2001/XMLSchema#date</option>
                                                    <option>http://www.w3.org/2001/XMLSchema#dateTime</option>
                                                    <option>http://www.w3.org/2001/XMLSchema#dayTimeDuration</option>
                                                    <option>http://www.w3.org/2001/XMLSchema#yearMonthDuration</option>
                                                    <option>http://www.w3.org/2001/XMLSchema#anyURI</option>
                                                    <option>http://www.w3.org/2001/XMLSchema#hexBinary</option>
                                                    <option>http://www.w3.org/2001/XMLSchema#base64Binary</option>
                                                    <option>urn:oasis:names:tc:xacml:1.0:data-type:rfc822Name</option>
                                                    <option>urn:oasis:names:tc:xacml:1.0:data-type:x500Name</option>
                                                    <option>urn:oasis:names:tc:xacml:3.0:data-type:xpathExpression
                                                    </option>
                                                    <option>urn:oasis:names:tc:xacml:2.0:data-type:ipAddress</option>
                                                    <option>urn:oasis:names:tc:xacml:2.0:data-type:dnsName</option>
                                                </select>
                                            </div>
                                        </div>
                                        <div class="form-group">
                                            <label for="value" class="col-sm-2 control-label">value</label>
                                            <div class="col-sm-10">
                                                <input type="text" class="form-control" id="value"
                                                       placeholder=<%=map.get("value1")%>>
                                            </div>
                                        </div>
                                        <div class="form-group">
                                            <label for="attributeId" class="col-sm-2 control-label">attributeId</label>
                                            <div class="col-sm-10">
                                                <input type="text" class="form-control" id="attributeId"
                                                       placeholder="attributeId">
                                            </div>
                                        </div>
                                        <div class="form-group">
                                            <label  class="col-sm-2 control-label">mustbepresent</label>
                                            <div class="col-sm-10">
                                                <select class="form-control">
                                                    <option>true</option>
                                                    <option>false</option>
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
                                                <select class="form-control">
                                                    <option><%=map.get("MatchId2")%></option>
                                                    <option> </option>
                                                    <option>urn:oasis:names:tc:xacml:1.0:function:string-equal
                                                    </option>
                                                    <option>
                                                        urn:oasis:names:tc:xacml:1.0:function:boolean-equal
                                                    </option>
                                                    <option>urn:oasis:names:tc:xacml:1.0:function:boolean-equa
                                                    </option>
                                                    <option>
                                                        urn:oasis:names:tc:xacml:1.0:function:integer-equal
                                                    </option>
                                                    <option>urn:oasis:names:tc:xacml:1.0:function:double-equal
                                                    </option>
                                                    <option>urn:oasis:names:tc:xacml:1.0:function:date-equal
                                                    </option>
                                                    <option>urn:oasis:names:tc:xacml:1.0:function:time-equal
                                                    </option>
                                                    <option>
                                                        urn:oasis:names:tc:xacml:1.0:function:dateTime-equal
                                                    </option>
                                                    <option>
                                                        urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-equal
                                                    </option>
                                                    <option>
                                                        urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-equal
                                                    </option>
                                                    <option>urn:oasis:names:tc:xacml:1.0:function:anyURI-equal
                                                    </option>
                                                    <option>
                                                        urn:oasis:names:tc:xacml:1.0:function:x500Name-equal
                                                    </option>
                                                    <option>
                                                        urn:oasis:names:tc:xacml:1.0:function:rfc822Name-equal
                                                    </option>
                                                    <option>
                                                        urn:oasis:names:tc:xacml:1.0:function:hexBinary-equal
                                                    </option>
                                                    <option>
                                                        urn:oasis:names:tc:xacml:1.0:function:base64Binary-equal
                                                    </option>
                                                </select>
                                            </div>
                                        </div>
                                        <div class="form-group">
                                            <label class="col-sm-2 control-label">Data Type</label>
                                            <div class="col-sm-10">
                                                <select class="form-control">
                                                    <option><%=map.get("DataType2")%></option>
                                                    <option> </option>
                                                    <option>http://www.w3.org/2001/XMLSchema#string</option>
                                                    <option>http://www.w3.org/2001/XMLSchema#boolean</option>
                                                    <option>http://www.w3.org/2001/XMLSchema#integer</option>
                                                    <option>ahttp://www.w3.org/2001/XMLSchema#double</option>
                                                    <option>http://www.w3.org/2001/XMLSchema#time</option>
                                                    <option>http://www.w3.org/2001/XMLSchema#date</option>
                                                    <option>http://www.w3.org/2001/XMLSchema#dateTime</option>
                                                    <option>http://www.w3.org/2001/XMLSchema#dayTimeDuration
                                                    </option>
                                                    <option>http://www.w3.org/2001/XMLSchema#yearMonthDuration
                                                    </option>
                                                    <option>http://www.w3.org/2001/XMLSchema#anyURI</option>
                                                    <option>http://www.w3.org/2001/XMLSchema#hexBinary</option>
                                                    <option>http://www.w3.org/2001/XMLSchema#base64Binary
                                                    </option>
                                                    <option>urn:oasis:names:tc:xacml:1.0:data-type:rfc822Name
                                                    </option>
                                                    <option>urn:oasis:names:tc:xacml:1.0:data-type:x500Name
                                                    </option>
                                                    <option>
                                                        urn:oasis:names:tc:xacml:3.0:data-type:xpathExpression
                                                    </option>
                                                    <option>urn:oasis:names:tc:xacml:2.0:data-type:ipAddress
                                                    </option>
                                                    <option>urn:oasis:names:tc:xacml:2.0:data-type:dnsName
                                                    </option>
                                                </select>
                                            </div>
                                        </div>
                                        <div class="form-group">
                                            <label for="value" class="col-sm-2 control-label">value</label>
                                            <div class="col-sm-10">
                                                <input type="text" class="form-control" id="value"
                                                       placeholder=<%=map.get("value2")%>>
                                            </div>
                                        </div>
                                        <div class="form-group">
                                            <label for="attributeId" class="col-sm-2 control-label">attributeId</label>
                                            <div class="col-sm-10">
                                                <input type="text" class="form-control" id="attributeId"
                                                       placeholder="attributeId">
                                            </div>
                                        </div>
                                        <div class="form-group">
                                            <label  class="col-sm-2 control-label">mustbepresent</label>
                                            <div class="col-sm-10">
                                                <select class="form-control">
                                                    <option>true</option>
                                                    <option>false</option>
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
                                                <select class="form-control">
                                                    <option><%=map.get("MatchId3")%></option>
                                                    <option> </option>
                                                    <option>urn:oasis:names:tc:xacml:1.0:function:string-equal
                                                    </option>
                                                    <option>
                                                        urn:oasis:names:tc:xacml:1.0:function:boolean-equal
                                                    </option>
                                                    <option>urn:oasis:names:tc:xacml:1.0:function:boolean-equa
                                                    </option>
                                                    <option>
                                                        urn:oasis:names:tc:xacml:1.0:function:integer-equal
                                                    </option>
                                                    <option>urn:oasis:names:tc:xacml:1.0:function:double-equal
                                                    </option>
                                                    <option>urn:oasis:names:tc:xacml:1.0:function:date-equal
                                                    </option>
                                                    <option>urn:oasis:names:tc:xacml:1.0:function:time-equal
                                                    </option>
                                                    <option>
                                                        urn:oasis:names:tc:xacml:1.0:function:dateTime-equal
                                                    </option>
                                                    <option>
                                                        urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-equal
                                                    </option>
                                                    <option>
                                                        urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-equal
                                                    </option>
                                                    <option>urn:oasis:names:tc:xacml:1.0:function:anyURI-equal
                                                    </option>
                                                    <option>
                                                        urn:oasis:names:tc:xacml:1.0:function:x500Name-equal
                                                    </option>
                                                    <option>
                                                        urn:oasis:names:tc:xacml:1.0:function:rfc822Name-equal
                                                    </option>
                                                    <option>
                                                        urn:oasis:names:tc:xacml:1.0:function:hexBinary-equal
                                                    </option>
                                                    <option>
                                                        urn:oasis:names:tc:xacml:1.0:function:base64Binary-equal
                                                    </option>
                                                </select>
                                            </div>
                                        </div>
                                        <div class="form-group">
                                            <label class="col-sm-2 control-label">Data Type</label>
                                            <div class="col-sm-10">
                                                <select class="form-control">
                                                    <option><%=map.get("DataType3")%></option>
                                                    <option> </option>
                                                    <option>http://www.w3.org/2001/XMLSchema#string</option>
                                                    <option>http://www.w3.org/2001/XMLSchema#boolean</option>
                                                    <option>http://www.w3.org/2001/XMLSchema#integer</option>
                                                    <option>ahttp://www.w3.org/2001/XMLSchema#double</option>
                                                    <option>http://www.w3.org/2001/XMLSchema#time</option>
                                                    <option>http://www.w3.org/2001/XMLSchema#date</option>
                                                    <option>http://www.w3.org/2001/XMLSchema#dateTime</option>
                                                    <option>http://www.w3.org/2001/XMLSchema#dayTimeDuration
                                                    </option>
                                                    <option>http://www.w3.org/2001/XMLSchema#yearMonthDuration
                                                    </option>
                                                    <option>http://www.w3.org/2001/XMLSchema#anyURI</option>
                                                    <option>http://www.w3.org/2001/XMLSchema#hexBinary</option>
                                                    <option>http://www.w3.org/2001/XMLSchema#base64Binary
                                                    </option>
                                                    <option>urn:oasis:names:tc:xacml:1.0:data-type:rfc822Name
                                                    </option>
                                                    <option>urn:oasis:names:tc:xacml:1.0:data-type:x500Name
                                                    </option>
                                                    <option>
                                                        urn:oasis:names:tc:xacml:3.0:data-type:xpathExpression
                                                    </option>
                                                    <option>urn:oasis:names:tc:xacml:2.0:data-type:ipAddress
                                                    </option>
                                                    <option>urn:oasis:names:tc:xacml:2.0:data-type:dnsName
                                                    </option>
                                                </select>
                                            </div>
                                        </div>
                                        <div class="form-group">
                                            <label for="value" class="col-sm-2 control-label">value</label>
                                            <div class="col-sm-10">
                                                <input type="text" class="form-control" id="value"
                                                       placeholder=<%=map.get("value3")%>>
                                            </div>
                                        </div>
                                        <div class="form-group">
                                            <label for="attributeId" class="col-sm-2 control-label">attributeId</label>
                                            <div class="col-sm-10">
                                                <input type="text" class="form-control" id="attributeId"
                                                       placeholder="attributeId">
                                            </div>
                                        </div>
                                        <div class="form-group">
                                            <label  class="col-sm-2 control-label">mustbepresent</label>
                                            <div class="col-sm-10">
                                                <select class="form-control">
                                                    <option>true</option>
                                                    <option>false</option>
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
                                <div id="formpart"></div><br><br>
                                <button class="btn btn-primary" id="addform">添加规则</button>
                            </div>
                        </div>
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
        //添加的内容
        var addform ='<div class="panel panel-default">\n' +
            '\n' +
            '    <div class="panel-heading">\n' +
            '         <h4 class="panel-title">\n' +
            '         <a data-toggle="collapse" data-parent="#accordio" href="#rule1">\n' +
            '         rule1\n' +
            '         </a>\n' +
            '         </h4>\n' +
            '\n' +
            '    </div>\n' +
            '\n' +
            '    <div id="rule1" class="panel-collapse collapse">\n' +
            '        <div class="panel-body">\n' +
            '            <div class="panel panel-default">\n' +
            '                <div class="panel-body">\n' +
            '                    <div class="form-group">\n' +
            '                         <label for="RuleId"\n' +
            '                         class="col-sm-2 control-label">RuleId</label>\n' +
            '                        <div class="col-sm-10">\n' +
            '                            <input type="text" class="form-control" id="RuleId" placeholder="RuleId">\n' +
            '                        </div>\n' +
            '                    </div>\n' +
            '                    <div class="form-group">\n' +
            '                        <label class="col-sm-2 control-label">Effect</label>\n' +
            '                        <div class="col-sm-10">\n' +
            '                            <select class="form-control">\n' +
            '                            <option>permit</option>\n' +
            '                            <option>deny</option>\n' +
            '                            </select>\n' +
            '                        </div>\n' +
            '                    </div>\n' +
            '                    <div class="form-group">\n' +
            '                         <label class="col-sm-2 control-label">description</label>\n' +
            '                        <div class="col-sm-10">\n' +
            '                             <textarea class="form-control" rows="3"></textarea>\n' +
            '                        </div>\n' +
            '                    </div>\n' +
            '                </div>\n' +
            '            </div>\n' +
            '            <div class="panel panel-default">\n' +
            '                <div class="panel-heading">\n' +
            '                     <h3 class="panel-title">\n' +
            '                     User\n' +
            '                    </h3>\n' +
            '                </div>\n' +
            '                <div class="panel-body">\n' +
            '                    <div class="form-group">\n' +
            '                        <label class="col-sm-2 control-label">MatchId</label>\n' +
            '                        <div class="col-sm-10">\n' +
            '                             <select class="form-control">\n' +
            '                                 <option>urn:oasis:names:tc:xacml:1.0:function:string-equal</option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:1.0:function:boolean-equal </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:1.0:function:integer-equal </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:1.0:function:double-equal </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:1.0:function:date-equal</option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:1.0:function:time-equal </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:1.0:function:dateTime-equal </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-equal </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-equal </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:3.0:function:string-equal-ignore-case </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:1.0:function:anyURI-equal </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:1.0:function:x500Name-equal </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:1.0:function:rfc822Name-equal </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:1.0:function:hexBinary-equal </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:1.0:function:base64Binary-equal </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:1.0:function:integer-add </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:1.0:function:double-add </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:1.0:function:integer-subtract </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:1.0:function:double-subtract </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:1.0:function:integer-multiply </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:1.0:function:double-multiply </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:1.0:function:integer-divide </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:1.0:function:double-divide </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:1.0:function:integer-mod </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:1.0:function:integer-abs </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:1.0:function:double-abs </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:1.0:function:round </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:1.0:function:floorurn:oasis:names:tc:xacml:1.0:function:string-normalize-space </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:1.0:function:string-normalize-to-lower-case </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:1.0:function:double-to-integer </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:1.0:function:integer-to-double </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:1.0:function:or </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:1.0:function:and </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:1.0:function:n-of </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:1.0:function:noturn:oasis:names:tc:xacml:1.0:function:integer-greater-than </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:1.0:function:integer-greater-than-or-equal </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:1.0:function:integer-less-than </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:1.0:function:integer-less-than-or-equal </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:1.0:function:double-greater-than </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:1.0:function:double-greater-than-or-equal </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:1.0:function:double-less-than </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:1.0:function:double-less-than-or-equal </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:3.0:function:dateTime-add-dayTimeDuration </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:3.0:function:dateTime-add-yearMonthDuration </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:3.0:function:dateTime-subtract-dayTimeDuration  </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:3.0:function:dateTime-subtractyearMonthDuration </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:3.0:function:date-add-yearMonthDuration </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:3.0:function:date-subtract-yearMonthDuration </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:1.0:function:string-greater-than </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:1.0:function:string-greater-than-or-equal </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:1.0:function:string-less-than </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:1.0:function:string-less-than-or-equal </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:1.0:function:time-greater-than </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:1.0:function:time-greater-than-or-equal </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:1.0:function:time-less-than </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:1.0:function:time-less-than-or-equal </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:2.0:function:time-in-range </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:1.0:function:dateTime-greater-than </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:1.0:function:dateTime-greater-than-or-equal </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:1.0:function:dateTime-less-than </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:1.0:function:dateTime-less-than-or-equal </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:1.0:function:date-greater-than </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:1.0:function:date-greater-than-or-equal </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:1.0:function:date-less-than </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:1.0:function:date-less-than-or-equal </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:1.0:function:string-one-and-only </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:1.0:function:string-bag-size </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:1.0:function:string-is-in </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:1.0:function:string-bag </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:1.0:function:boolean-one-and-only </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:1.0:function:boolean-bag-size </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:1.0:function:boolean-is-in </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:1.0:function:boolean-bag </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:1.0:function:integer-one-and-only </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:1.0:function:integer-bag-size</option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:1.0:function:integer-is-in </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:1.0:function:integer-bag </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:1.0:function:double-one-and-only </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:1.0:function:double-bag-size </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:1.0:function:double-is-in </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:1.0:function:double-bag </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:1.0:function:time-one-and-only </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:1.0:function:time-bag-size </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:1.0:function:time-is-in </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:1.0:function:time-bag </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:1.0:function:date-one-and-only </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:1.0:function:date-bag-size </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:1.0:function:date-is-in </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:1.0:function:date-bag </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:1.0:function:dateTime-one-and-only </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:1.0:function:dateTime-bag-size </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:1.0:function:dateTime-is-in </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:1.0:function:dateTime-bag </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:1.0:function:anyURI-one-and-only </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:1.0:function:anyURI-bag-size </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:1.0:function:anyURI-is-in </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:1.0:function:anyURI-bag </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:1.0:function:hexBinary-one-and-only </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:1.0:function:hexBinary-bag-size </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:1.0:function:hexBinary-is-in </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:1.0:function:hexBinary-bag </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:1.0:function:base64Binary-one-and-only </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:1.0:function:base64Binary-bag-size </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:1.0:function:base64Binary-is-in </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:1.0:function:base64Binary-bag </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-one-and-only </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-bag-size </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-is-in </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-bag </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-one-and-only </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-bag-size </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-is-in </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-bag </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:1.0:function:x500Name-one-and-only </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:1.0:function:x500Name-bag-size </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:1.0:function:x500Name-is-in </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:1.0:function:x500Name-bag </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:1.0:function:rfc822Name-one-and-only </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:1.0:function:rfc822Name-bag-size </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:1.0:function:rfc822Name-is-in </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:1.0:function:rfc822Name-bag </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:2.0:function:ipAddress-one-and-only </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:2.0:function:ipAddress-bag-size </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:2.0:function:ipAddress-bag </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:2.0:function:dnsName-one-and-only </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:2.0:function:dnsName-bag-size </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:2.0:function:dnsName-bag </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:2.0:function:string-concatenate </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:3.0:function:boolean-from-string </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:3.0:function:string-from-boolean </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:3.0:function:integer-from-string </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:3.0:function:string-from-integer </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:3.0:function:double-from-string</option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:3.0:function:string-from-double </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:3.0:function:time-from-string </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:3.0:function:string-from-time </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:3.0:function:date-from-string </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:3.0:function:string-from-date </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:3.0:function:dateTime-from-string </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:3.0:function:string-from-dateTime </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:3.0:function:anyURI-from-string </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:3.0:function:string-from-anyURI </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-from-string </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:3.0:function:string-from-dayTimeDuration </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-from-string </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:3.0:function:string-from-yearMonthDuration </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:3.0:function:x500Name-from-string </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:3.0:function:string-from-x500Name </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:3.0:function:rfc822Name-from-string </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:3.0:function:string-from-rfc822Name </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:3.0:function:ipAddress-from-string </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:3.0:function:string-from-ipAddress </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:3.0:function:dnsName-from-string </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:3.0:function:string-from-dnsName </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:3.0:function:string-starts-with </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:3.0:function:anyURI-starts-with </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:3.0:function:string-ends-with </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:3.0:function:anyURI-ends-with </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:3.0:function:string-contains </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:3.0:function:anyURI-contains </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:3.0:function:string-substring </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:3.0:function:anyURI-substring </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:3.0:function:any-of </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:3.0:function:all-of </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:3.0:function:any-of-any </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:1.0:function:all-of-any </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:1.0:function:any-of-all  </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:1.0:function:all-of-all  </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:3.0:function:map </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:1.0:function:x500Name-match  </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:1.0:function:rfc822Name-match  </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:1.0:function:string-regexp-match  </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:2.0:function:anyURI-regexp-match  </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:2.0:function:ipAddress-regexp-match  </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:2.0:function:dnsName-regexp-match  </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:2.0:function:rfc822Name-regexp-match  </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:2.0:function:x500Name-regexp-match  </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:3.0:function:xpath-node-count </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:3.0:function:xpath-node-equal </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:3.0:function:xpath-node-match </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:1.0:function:string-intersection </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:1.0:function:string-at-least-one-member-of </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:1.0:function:string-union </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:1.0:function:string-subset </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:1.0:function:string-set-equals </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:1.0:function:boolean-intersection </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:1.0:function:boolean-at-least-one-member-of </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:1.0:function:boolean-union </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:1.0:function:boolean-subset </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:1.0:function:boolean-set-equals </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:1.0:function:integer-intersection</option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:1.0:function:integer-at-least-one-member-of  </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:1.0:function:integer-union  </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:1.0:function:integer-subset  </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:1.0:function:integer-set-equals  </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:1.0:function:double-intersection  </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:1.0:function:double-at-least-one-member-of  </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:1.0:function:double-union  </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:1.0:function:double-subset  </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:1.0:function:double-set-equals  </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:1.0:function:time-intersection  </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:1.0:function:time-at-least-one-member-of  </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:1.0:function:time-union  </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:1.0:function:time-subset  </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:1.0:function:time-set-equals  </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:1.0:function:date-intersection  </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:1.0:function:date-at-least-one-member-of  </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:1.0:function:date-union  </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:1.0:function:date-subset  </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:1.0:function:date-set-equals  </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:1.0:function:dateTime-intersection  </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:1.0:function:dateTime-at-least-one-member-of  </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:1.0:function:dateTime-union  </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:1.0:function:dateTime-subset  </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:1.0:function:dateTime-set-equals  </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:1.0:function:anyURI-intersection  </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:1.0:function:anyURI-at-least-one-member-of  </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:1.0:function:anyURI-union  </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:1.0:function:anyURI-subset  </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:1.0:function:anyURI-set-equals  </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:1.0:function:hexBinary-intersection  </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:1.0:function:hexBinary-at-least-one-member-of  </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:1.0:function:hexBinary-union  </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:1.0:function:hexBinary-subset  </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:1.0:function:hexBinary-set-equals  </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:1.0:function:base64Binary-intersection  </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:1.0:function:base64Binary-at-least-one-memberof  </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:1.0:function:base64Binary-union  </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:1.0:function:base64Binary-subset  </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:1.0:function:base64Binary-set-equals  </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-intersection  </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-at-least-onemember-of  </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-union  </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-subset  </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-set-equals  </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-intersection  </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-at-least-onemember-of  </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-union  </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-subset  </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-set-equals  </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:1.0:function:x500Name-intersection  </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:1.0:function:x500Name-at-least-one-member-of  </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:1.0:function:x500Name-union  </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:1.0:function:x500Name-subset  </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:1.0:function:x500Name-set-equals  </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:1.0:function:rfc822Name-intersection </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:1.0:function:rfc822Name-at-least-one-member-of </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:1.0:function:rfc822Name-union </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:1.0:function:rfc822Name-subset </option>\n' +
            '                                 <option>urn:oasis:names:tc:xacml:1.0:function:rfc822Name-set-equals </option>\n' +
            '                             </select>\n' +
            '                        </div>\n' +
            '                    </div>\n' +
            '                    <div class="form-group">\n' +
            '                         <label class="col-sm-2 control-label">Data Type</label>\n' +
            '                         <div class="col-sm-10">\n' +
            '                             <select class="form-control">\n' +
            '                            <option>http://www.w3.org/2001/XMLSchema#string\n' +
            '                            </option>\n' +
            '                            <option>http://www.w3.org/2001/XMLSchema#boolean\n' +
            '                            </option>\n' +
            '                            <option>http://www.w3.org/2001/XMLSchema#integer\n' +
            '                            </option>\n' +
            '                            <option>ahttp://www.w3.org/2001/XMLSchema#double\n' +
            '                            </option>\n' +
            '                            <option>http://www.w3.org/2001/XMLSchema#time\n' +
            '                            </option>\n' +
            '                            <option>http://www.w3.org/2001/XMLSchema#date\n' +
            '                            </option>\n' +
            '                            <option>http://www.w3.org/2001/XMLSchema#dateTime\n' +
            '                            </option>\n' +
            '                            <option>http://www.w3.org/2001/XMLSchema#dayTimeDuration\n' +
            '                            </option>\n' +
            '                            <option>http://www.w3.org/2001/XMLSchema#yearMonthDuration\n' +
            '                            </option>\n' +
            '                            <option>http://www.w3.org/2001/XMLSchema#anyURI\n' +
            '                            </option>\n' +
            '                            <option>http://www.w3.org/2001/XMLSchema#hexBinary\n' +
            '                            </option>\n' +
            '                            <option>http://www.w3.org/2001/XMLSchema#base64Binary\n' +
            '                            </option>\n' +
            '                            <option>urn:oasis:names:tc:xacml:1.0:data-type:rfc822Name\n' +
            '                            </option>\n' +
            '                            <option>urn:oasis:names:tc:xacml:1.0:data-type:x500Name\n' +
            '                            </option>\n' +
            '                            <option>urn:oasis:names:tc:xacml:3.0:data-type:xpathExpression\n' +
            '                            </option>\n' +
            '                            <option>urn:oasis:names:tc:xacml:2.0:data-type:ipAddress\n' +
            '                            </option>\n' +
            '                            <option>urn:oasis:names:tc:xacml:2.0:data-type:dnsName\n' +
            '                            </option>\n' +
            '                             </select>\n' +
            '                        </div>\n' +
            '                    </div>\n' +
            '                    <div class="form-group">\n' +
            '                        <label for="value" class="col-sm-2 control-label">value</label>\n' +
            '                        <div class="col-sm-10">\n' +
            '                            <input type="text" class="form-control" id="value" placeholder="vresion">\n' +
            '                        </div>\n' +
            '                    </div>\n' +
            '<div class="form-group">\n' +
            '                                            <label for="attributeId" class="col-sm-2 control-label">attributeId</label>\n' +
            '                                            <div class="col-sm-10">\n' +
            '                                                <input type="text" class="form-control" id="attributeId"\n' +
            '                                                       placeholder="attributeId">\n' +
            '                                            </div>\n' +
            '                                        </div>\n' +
            '                                        <div class="form-group">\n' +
            '                                            <label  class="col-sm-2 control-label">mustbepresent</label>\n' +
            '                                            <div class="col-sm-10">\n' +
            '                                                <select class="form-control">\n' +
            '                                                    <option>true</option>\n' +
            '                                                    <option>false</option>\n' +
            '                                                </select>\n' +
            '                                            </div>\n' +
            '                                        </div>\n'+
            '                 </div>\n'+
            '                </div>\n'+
            '                    <div class="panel panel-default">\n' +
            '                        <div class="panel-heading">\n' +
            '                            <h3 class="panel-title">\n' +
            '                            Resource\n' +
            '                            </h3>\n' +
            '                        </div>\n' +
            '                        <div class="panel-body">\n' +
            '                            <div class="form-group">\n' +
            '                                 <label class="col-sm-2 control-label">MatchId</label>\n' +
            '                                <div class="col-sm-10">\n' +
            '                                    <select class="form-control">\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:string-equal</option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:boolean-equal </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:integer-equal </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:double-equal </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:date-equal</option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:time-equal </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:dateTime-equal </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-equal </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-equal </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:3.0:function:string-equal-ignore-case </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:anyURI-equal </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:x500Name-equal </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:rfc822Name-equal </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:hexBinary-equal </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:base64Binary-equal </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:integer-add </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:double-add </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:integer-subtract </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:double-subtract </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:integer-multiply </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:double-multiply </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:integer-divide </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:double-divide </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:integer-mod </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:integer-abs </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:double-abs </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:round </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:floorurn:oasis:names:tc:xacml:1.0:function:string-normalize-space </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:string-normalize-to-lower-case </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:double-to-integer </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:integer-to-double </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:or </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:and </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:n-of </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:noturn:oasis:names:tc:xacml:1.0:function:integer-greater-than </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:integer-greater-than-or-equal </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:integer-less-than </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:integer-less-than-or-equal </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:double-greater-than </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:double-greater-than-or-equal </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:double-less-than </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:double-less-than-or-equal </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:3.0:function:dateTime-add-dayTimeDuration </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:3.0:function:dateTime-add-yearMonthDuration </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:3.0:function:dateTime-subtract-dayTimeDuration  </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:3.0:function:dateTime-subtractyearMonthDuration </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:3.0:function:date-add-yearMonthDuration </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:3.0:function:date-subtract-yearMonthDuration </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:string-greater-than </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:string-greater-than-or-equal </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:string-less-than </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:string-less-than-or-equal </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:time-greater-than </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:time-greater-than-or-equal </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:time-less-than </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:time-less-than-or-equal </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:2.0:function:time-in-range </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:dateTime-greater-than </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:dateTime-greater-than-or-equal </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:dateTime-less-than </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:dateTime-less-than-or-equal </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:date-greater-than </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:date-greater-than-or-equal </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:date-less-than </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:date-less-than-or-equal </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:string-one-and-only </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:string-bag-size </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:string-is-in </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:string-bag </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:boolean-one-and-only </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:boolean-bag-size </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:boolean-is-in </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:boolean-bag </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:integer-one-and-only </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:integer-bag-size</option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:integer-is-in </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:integer-bag </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:double-one-and-only </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:double-bag-size </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:double-is-in </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:double-bag </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:time-one-and-only </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:time-bag-size </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:time-is-in </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:time-bag </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:date-one-and-only </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:date-bag-size </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:date-is-in </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:date-bag </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:dateTime-one-and-only </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:dateTime-bag-size </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:dateTime-is-in </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:dateTime-bag </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:anyURI-one-and-only </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:anyURI-bag-size </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:anyURI-is-in </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:anyURI-bag </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:hexBinary-one-and-only </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:hexBinary-bag-size </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:hexBinary-is-in </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:hexBinary-bag </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:base64Binary-one-and-only </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:base64Binary-bag-size </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:base64Binary-is-in </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:base64Binary-bag </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-one-and-only </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-bag-size </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-is-in </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-bag </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-one-and-only </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-bag-size </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-is-in </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-bag </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:x500Name-one-and-only </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:x500Name-bag-size </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:x500Name-is-in </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:x500Name-bag </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:rfc822Name-one-and-only </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:rfc822Name-bag-size </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:rfc822Name-is-in </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:rfc822Name-bag </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:2.0:function:ipAddress-one-and-only </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:2.0:function:ipAddress-bag-size </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:2.0:function:ipAddress-bag </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:2.0:function:dnsName-one-and-only </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:2.0:function:dnsName-bag-size </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:2.0:function:dnsName-bag </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:2.0:function:string-concatenate </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:3.0:function:boolean-from-string </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:3.0:function:string-from-boolean </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:3.0:function:integer-from-string </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:3.0:function:string-from-integer </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:3.0:function:double-from-string</option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:3.0:function:string-from-double </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:3.0:function:time-from-string </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:3.0:function:string-from-time </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:3.0:function:date-from-string </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:3.0:function:string-from-date </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:3.0:function:dateTime-from-string </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:3.0:function:string-from-dateTime </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:3.0:function:anyURI-from-string </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:3.0:function:string-from-anyURI </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-from-string </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:3.0:function:string-from-dayTimeDuration </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-from-string </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:3.0:function:string-from-yearMonthDuration </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:3.0:function:x500Name-from-string </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:3.0:function:string-from-x500Name </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:3.0:function:rfc822Name-from-string </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:3.0:function:string-from-rfc822Name </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:3.0:function:ipAddress-from-string </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:3.0:function:string-from-ipAddress </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:3.0:function:dnsName-from-string </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:3.0:function:string-from-dnsName </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:3.0:function:string-starts-with </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:3.0:function:anyURI-starts-with </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:3.0:function:string-ends-with </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:3.0:function:anyURI-ends-with </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:3.0:function:string-contains </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:3.0:function:anyURI-contains </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:3.0:function:string-substring </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:3.0:function:anyURI-substring </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:3.0:function:any-of </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:3.0:function:all-of </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:3.0:function:any-of-any </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:all-of-any </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:any-of-all  </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:all-of-all  </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:3.0:function:map </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:x500Name-match  </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:rfc822Name-match  </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:string-regexp-match  </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:2.0:function:anyURI-regexp-match  </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:2.0:function:ipAddress-regexp-match  </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:2.0:function:dnsName-regexp-match  </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:2.0:function:rfc822Name-regexp-match  </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:2.0:function:x500Name-regexp-match  </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:3.0:function:xpath-node-count </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:3.0:function:xpath-node-equal </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:3.0:function:xpath-node-match </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:string-intersection </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:string-at-least-one-member-of </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:string-union </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:string-subset </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:string-set-equals </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:boolean-intersection </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:boolean-at-least-one-member-of </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:boolean-union </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:boolean-subset </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:boolean-set-equals </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:integer-intersection</option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:integer-at-least-one-member-of  </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:integer-union  </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:integer-subset  </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:integer-set-equals  </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:double-intersection  </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:double-at-least-one-member-of  </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:double-union  </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:double-subset  </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:double-set-equals  </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:time-intersection  </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:time-at-least-one-member-of  </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:time-union  </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:time-subset  </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:time-set-equals  </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:date-intersection  </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:date-at-least-one-member-of  </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:date-union  </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:date-subset  </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:date-set-equals  </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:dateTime-intersection  </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:dateTime-at-least-one-member-of  </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:dateTime-union  </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:dateTime-subset  </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:dateTime-set-equals  </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:anyURI-intersection  </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:anyURI-at-least-one-member-of  </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:anyURI-union  </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:anyURI-subset  </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:anyURI-set-equals  </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:hexBinary-intersection  </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:hexBinary-at-least-one-member-of  </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:hexBinary-union  </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:hexBinary-subset  </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:hexBinary-set-equals  </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:base64Binary-intersection  </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:base64Binary-at-least-one-memberof  </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:base64Binary-union  </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:base64Binary-subset  </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:base64Binary-set-equals  </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-intersection  </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-at-least-onemember-of  </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-union  </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-subset  </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-set-equals  </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-intersection  </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-at-least-onemember-of  </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-union  </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-subset  </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-set-equals  </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:x500Name-intersection  </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:x500Name-at-least-one-member-of  </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:x500Name-union  </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:x500Name-subset  </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:x500Name-set-equals  </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:rfc822Name-intersection </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:rfc822Name-at-least-one-member-of </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:rfc822Name-union </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:rfc822Name-subset </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:rfc822Name-set-equals </option>\n' +
            '                                     </select>\n' +
            '                                </div>\n' +
            '                            </div>\n' +
            '                            <div class="form-group">\n' +
            '                                <label class="col-sm-2 control-label">Data\n' +
            '                                Type</label>\n' +
            '                                <div class="col-sm-10">\n' +
            '                                    <select class="form-control">\n' +
            '                                    <option>\n' +
            '                                        http://www.w3.org/2001/XMLSchema#string\n' +
            '                                    </option>\n' +
            '                                    <option>\n' +
            '                                        http://www.w3.org/2001/XMLSchema#boolean\n' +
            '                                    </option>\n' +
            '                                    <option>\n' +
            '                                        http://www.w3.org/2001/XMLSchema#integer\n' +
            '                                    </option>\n' +
            '                                    <option>\n' +
            '                                        ahttp://www.w3.org/2001/XMLSchema#double\n' +
            '                                    </option>\n' +
            '                                    <option>\n' +
            '                                        http://www.w3.org/2001/XMLSchema#time\n' +
            '                                    </option>\n' +
            '                                    <option>\n' +
            '                                        http://www.w3.org/2001/XMLSchema#date\n' +
            '                                    </option>\n' +
            '                                    <option>\n' +
            '                                        http://www.w3.og/2001/XMLSchema#dateTime\n' +
            '                                    </option>\n' +
            '                                    <option>\n' +
            '                                        http://www.w3.org/2001/XMLSchema#dayTimeDuration\n' +
            '                                    </option>\n' +
            '                                    <option>\n' +
            '                                        http://www.w3.org/2001/XMLSchema#yearMonthDuration\n' +
            '                                    </option>\n' +
            '                                    <option>\n' +
            '                                        http://www.w3.org/2001/XMLSchema#anyURI\n' +
            '                                    </option>\n' +
            '                                    <option>\n' +
            '                                        http://www.w3.org/2001/XMLSchema#hexBinary\n' +
            '                                    </option>\n' +
            '                                    <option>\n' +
            '                                        http://www.w3.org/2001/XMLSchema#base64Binary\n' +
            '                                    </option>\n' +
            '                                    <option>\n' +
            '                                        urn:oasis:names:tc:xacml:1.0:data-type:rfc822Name\n' +
            '                                    </option>\n' +
            '                                    <option>\n' +
            '                                        urn:oasis:names:tc:xacml:1.0:data-type:x500Name\n' +
            '                                    </option>\n' +
            '                                    <option>\n' +
            '                                        urn:oasis:names:tc:xacml:3.0:data-type:xpathExpression\n' +
            '                                    </option>\n' +
            '                                    <option>\n' +
            '                                        urn:oasis:names:tc:xacml:2.0:data-type:ipAddress\n' +
            '                                    </option>\n' +
            '                                    <option>\n' +
            '                                        urn:oasis:names:tc:xacml:2.0:data-type:dnsName\n' +
            '                                    </option>\n' +
            '                                     </select>\n' +
            '                                </div>\n' +
            '                            </div>\n' +
            '                            <div class="form-group">\n' +
            '                                 <label for="value" class="col-sm-2 control-label">value</label>\n' +
            '                                <div class="col-sm-10">\n' +
            '                                     <input type="text" class="form-control" id="value" placeholder="vresion">\n' +
            '                                </div>\n' +
            '                            </div>\n' +
            '<div class="form-group">\n' +
            '                                            <label for="attributeId" class="col-sm-2 control-label">attributeId</label>\n' +
            '                                            <div class="col-sm-10">\n' +
            '                                                <input type="text" class="form-control" id="attributeId"\n' +
            '                                                       placeholder="attributeId">\n' +
            '                                            </div>\n' +
            '                                        </div>\n' +
            '                                        <div class="form-group">\n' +
            '                                            <label  class="col-sm-2 control-label">mustbepresent</label>\n' +
            '                                            <div class="col-sm-10">\n' +
            '                                                <select class="form-control">\n' +
            '                                                    <option>true</option>\n' +
            '                                                    <option>false</option>\n' +
            '                                                </select>\n' +
            '                                            </div>\n' +
            '                                        </div>\n'+
            '                        </div>\n' +
            '                    </div>\n' +
            '                    <div class="panel panel-default">\n' +
            '                        <div class="panel-heading">\n' +
            '                            <h3 class="panel-title">\n' +
            '                            Action\n' +
            '                            </h3>\n' +
            '                        </div>\n' +
            '                        <div class="panel-body">\n' +
            '                            <div class="form-group">\n' +
            '                                <label class="col-sm-2 control-label">MatchId</label>\n' +
            '                                <div class="col-sm-10">\n' +
            '                                    <select class="form-control">\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:string-equal</option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:boolean-equal </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:integer-equal </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:double-equal </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:date-equal</option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:time-equal </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:dateTime-equal </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-equal </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-equal </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:3.0:function:string-equal-ignore-case </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:anyURI-equal </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:x500Name-equal </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:rfc822Name-equal </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:hexBinary-equal </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:base64Binary-equal </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:integer-add </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:double-add </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:integer-subtract </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:double-subtract </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:integer-multiply </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:double-multiply </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:integer-divide </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:double-divide </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:integer-mod </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:integer-abs </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:double-abs </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:round </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:floorurn:oasis:names:tc:xacml:1.0:function:string-normalize-space </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:string-normalize-to-lower-case </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:double-to-integer </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:integer-to-double </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:or </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:and </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:n-of </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:noturn:oasis:names:tc:xacml:1.0:function:integer-greater-than </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:integer-greater-than-or-equal </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:integer-less-than </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:integer-less-than-or-equal </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:double-greater-than </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:double-greater-than-or-equal </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:double-less-than </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:double-less-than-or-equal </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:3.0:function:dateTime-add-dayTimeDuration </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:3.0:function:dateTime-add-yearMonthDuration </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:3.0:function:dateTime-subtract-dayTimeDuration  </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:3.0:function:dateTime-subtractyearMonthDuration </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:3.0:function:date-add-yearMonthDuration </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:3.0:function:date-subtract-yearMonthDuration </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:string-greater-than </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:string-greater-than-or-equal </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:string-less-than </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:string-less-than-or-equal </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:time-greater-than </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:time-greater-than-or-equal </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:time-less-than </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:time-less-than-or-equal </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:2.0:function:time-in-range </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:dateTime-greater-than </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:dateTime-greater-than-or-equal </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:dateTime-less-than </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:dateTime-less-than-or-equal </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:date-greater-than </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:date-greater-than-or-equal </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:date-less-than </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:date-less-than-or-equal </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:string-one-and-only </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:string-bag-size </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:string-is-in </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:string-bag </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:boolean-one-and-only </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:boolean-bag-size </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:boolean-is-in </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:boolean-bag </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:integer-one-and-only </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:integer-bag-size</option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:integer-is-in </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:integer-bag </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:double-one-and-only </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:double-bag-size </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:double-is-in </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:double-bag </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:time-one-and-only </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:time-bag-size </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:time-is-in </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:time-bag </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:date-one-and-only </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:date-bag-size </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:date-is-in </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:date-bag </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:dateTime-one-and-only </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:dateTime-bag-size </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:dateTime-is-in </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:dateTime-bag </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:anyURI-one-and-only </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:anyURI-bag-size </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:anyURI-is-in </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:anyURI-bag </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:hexBinary-one-and-only </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:hexBinary-bag-size </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:hexBinary-is-in </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:hexBinary-bag </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:base64Binary-one-and-only </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:base64Binary-bag-size </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:base64Binary-is-in </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:base64Binary-bag </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-one-and-only </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-bag-size </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-is-in </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-bag </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-one-and-only </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-bag-size </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-is-in </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-bag </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:x500Name-one-and-only </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:x500Name-bag-size </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:x500Name-is-in </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:x500Name-bag </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:rfc822Name-one-and-only </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:rfc822Name-bag-size </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:rfc822Name-is-in </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:rfc822Name-bag </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:2.0:function:ipAddress-one-and-only </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:2.0:function:ipAddress-bag-size </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:2.0:function:ipAddress-bag </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:2.0:function:dnsName-one-and-only </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:2.0:function:dnsName-bag-size </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:2.0:function:dnsName-bag </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:2.0:function:string-concatenate </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:3.0:function:boolean-from-string </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:3.0:function:string-from-boolean </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:3.0:function:integer-from-string </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:3.0:function:string-from-integer </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:3.0:function:double-from-string</option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:3.0:function:string-from-double </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:3.0:function:time-from-string </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:3.0:function:string-from-time </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:3.0:function:date-from-string </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:3.0:function:string-from-date </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:3.0:function:dateTime-from-string </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:3.0:function:string-from-dateTime </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:3.0:function:anyURI-from-string </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:3.0:function:string-from-anyURI </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-from-string </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:3.0:function:string-from-dayTimeDuration </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-from-string </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:3.0:function:string-from-yearMonthDuration </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:3.0:function:x500Name-from-string </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:3.0:function:string-from-x500Name </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:3.0:function:rfc822Name-from-string </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:3.0:function:string-from-rfc822Name </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:3.0:function:ipAddress-from-string </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:3.0:function:string-from-ipAddress </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:3.0:function:dnsName-from-string </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:3.0:function:string-from-dnsName </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:3.0:function:string-starts-with </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:3.0:function:anyURI-starts-with </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:3.0:function:string-ends-with </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:3.0:function:anyURI-ends-with </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:3.0:function:string-contains </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:3.0:function:anyURI-contains </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:3.0:function:string-substring </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:3.0:function:anyURI-substring </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:3.0:function:any-of </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:3.0:function:all-of </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:3.0:function:any-of-any </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:all-of-any </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:any-of-all  </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:all-of-all  </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:3.0:function:map </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:x500Name-match  </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:rfc822Name-match  </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:string-regexp-match  </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:2.0:function:anyURI-regexp-match  </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:2.0:function:ipAddress-regexp-match  </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:2.0:function:dnsName-regexp-match  </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:2.0:function:rfc822Name-regexp-match  </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:2.0:function:x500Name-regexp-match  </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:3.0:function:xpath-node-count </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:3.0:function:xpath-node-equal </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:3.0:function:xpath-node-match </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:string-intersection </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:string-at-least-one-member-of </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:string-union </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:string-subset </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:string-set-equals </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:boolean-intersection </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:boolean-at-least-one-member-of </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:boolean-union </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:boolean-subset </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:boolean-set-equals </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:integer-intersection</option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:integer-at-least-one-member-of  </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:integer-union  </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:integer-subset  </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:integer-set-equals  </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:double-intersection  </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:double-at-least-one-member-of  </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:double-union  </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:double-subset  </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:double-set-equals  </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:time-intersection  </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:time-at-least-one-member-of  </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:time-union  </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:time-subset  </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:time-set-equals  </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:date-intersection  </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:date-at-least-one-member-of  </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:date-union  </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:date-subset  </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:date-set-equals  </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:dateTime-intersection  </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:dateTime-at-least-one-member-of  </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:dateTime-union  </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:dateTime-subset  </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:dateTime-set-equals  </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:anyURI-intersection  </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:anyURI-at-least-one-member-of  </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:anyURI-union  </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:anyURI-subset  </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:anyURI-set-equals  </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:hexBinary-intersection  </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:hexBinary-at-least-one-member-of  </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:hexBinary-union  </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:hexBinary-subset  </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:hexBinary-set-equals  </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:base64Binary-intersection  </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:base64Binary-at-least-one-memberof  </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:base64Binary-union  </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:base64Binary-subset  </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:base64Binary-set-equals  </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-intersection  </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-at-least-onemember-of  </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-union  </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-subset  </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-set-equals  </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-intersection  </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-at-least-onemember-of  </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-union  </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-subset  </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-set-equals  </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:x500Name-intersection  </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:x500Name-at-least-one-member-of  </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:x500Name-union  </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:x500Name-subset  </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:x500Name-set-equals  </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:rfc822Name-intersection </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:rfc822Name-at-least-one-member-of </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:rfc822Name-union </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:rfc822Name-subset </option>\n' +
            '                                        <option>urn:oasis:names:tc:xacml:1.0:function:rfc822Name-set-equals </option>\n' +
            '                                     </select>\n' +
            '                                </div>\n' +
            '                            </div>\n' +
            '                            <div class="form-group">\n' +
            '                                <label class="col-sm-2 control-label">Data\n' +
            '                                Type</label>\n' +
            '                                <div class="col-sm-10">\n' +
            '                                    <select class="form-control">\n' +
            '                                    <option>http://www.w3.org/2001/XMLSchema#string</option>\n' +
            '                                    <option>http://www.w3.org/2001/XMLSchema#boolean</option>\n' +
            '                                    <option>http://www.w3.org/2001/XMLSchema#integer</option>\n' +
            '                                    <option>ahttp://www.w3.org/2001/XMLSchema#double</option>\n' +
            '                                    <option>http://www.w3.org/2001/XMLSchema#time</option>\n' +
            '                                    <option>http://www.w3.org/2001/XMLSchema#date</option>\n' +
            '                                    <option>http://www.w3.org/2001/XMLSchema#dateTime</option>\n' +
            '                                    <option>http://www.w3.org/2001/XMLSchema#dayTimeDuration</option>\n' +
            '                                    <option>http://www.w3.org/2001/XMLSchema#yearMonthDuration</option>\n' +
            '                                    <option>http://www.w3.org/2001/XMLSchema#anyURI</option>\n' +
            '                                    <option>http://www.w3.org/2001/XMLSchema#hexBinary</option>\n' +
            '                                    <option>http://www.w3.org/2001/XMLSchema#base64Binary</option>\n' +
            '                                    <option>urn:oasis:names:tc:xacml:1.0:data-type:rfc822Name</option>\n' +
            '                                    <option>urn:oasis:names:tc:xacml:1.0:data-type:x500Name</option>\n' +
            '                                    <option>urn:oasis:names:tc:xacml:3.0:data-type:xpathExpression</option>\n' +
            '                                    <option>urn:oasis:names:tc:xacml:2.0:data-type:ipAddress</option>\n' +
            '                                    <option>urn:oasis:names:tc:xacml:2.0:data-type:dnsName</option>\n' +
            '                                    </select>\n' +
            '                                </div>\n' +
            '                            </div>\n' +
            '                            <div class="form-group">\n' +
            '                                <label for="value" class="col-sm-2 control-label">value</label>\n' +
            '                                <div class="col-sm-10">\n' +
            '                                    <input type="text" class="form-control" id="value" placeholder="value">\n' +
            '                                </div>\n' +
            '                            </div>\n' +
            '<div class="form-group">\n' +
            '                                            <label for="attributeId" class="col-sm-2 control-label">attributeId</label>\n' +
            '                                            <div class="col-sm-10">\n' +
            '                                                <input type="text" class="form-control" id="attributeId"\n' +
            '                                                       placeholder="attributeId">\n' +
            '                                            </div>\n' +
            '                                        </div>\n' +
            '                                        <div class="form-group">\n' +
            '                                            <label  class="col-sm-2 control-label">mustbepresent</label>\n' +
            '                                            <div class="col-sm-10">\n' +
            '                                                <select class="form-control">\n' +
            '                                                    <option>true</option>\n' +
            '                                                    <option>false</option>\n' +
            '                                                </select>\n' +
            '                                            </div>\n' +
            '                                        </div>\n'+

            '                        </div>\n' +
            '                    </div>';
        $("#formpart").after($(addform));
        //删除表单
        $("#removeform").click(function(){
            $("#addformbody").remove();
        });

    });
</script>
</html>