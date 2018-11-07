package com.meituan.service.mobile.thrift.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: gaosheng
 * Date: 15-1-20
 * Time: 下午7:07
 * To change this template use File | Settings | File Templates.
 */
public class IdlFile {

    /**
     * idl文件内容
     */
    private String content;

    public String getContent() {
        this.genContent();
        return content;
    }

    public void genContent(){
        StringBuffer sb = new StringBuffer();
        if(namespaceList != null){
            for(int i = 0; i < namespaceList.size(); i++)
                sb.append("namespace " + namespaceList.get(i).getLanguage() + " " + namespaceList.get(i).getNamespace() + "\n");
            sb.append("\n");
        }
        if(constantList != null){
            for(int i = 0; i < constantList.size(); i++){
                sb.append("const " + constantList.get(i).getType() + " " + constantList.get(i).getName()
                        + " " + constantList.get(i).getValue() + ";\n");
            }
            sb.append("\n");
        }
        if(structList != null){
            for(int i = 0; i < structList.size(); i++){
                sb.append("struct " + structList.get(i).getStructName() + " {\n");
                List<IdlVariable> idlVariable_list = structList.get(i).getVariableList();
                for(int j = 0; j < idlVariable_list.size(); j++){
                    sb.append("    " + (j+1) + ": " + idlVariable_list.get(j).getType() + " " + idlVariable_list.get(j).getName());
                    if(!idlVariable_list.get(j).getValue().trim().equals(""))
                        sb.append(" = " + idlVariable_list.get(j).getValue().trim());

                    sb.append(";\n");

                }
                sb.append("}\n\n");
            }

        }

        if(serviceList != null){
            for(int i = 0; i < serviceList.size(); i++){
                sb.append("service " + serviceList.get(i).getServiceName() + " {\n");
                List<IdlMethod> idlMethod_list = serviceList.get(i).getMethodList();
                for(int j = 0; j < idlMethod_list.size(); j++){
                    sb.append("    " + idlMethod_list.get(j).getType() + " " + idlMethod_list.get(j).getName() + "(");
                    List<IdlParameter> idlParameter_list = idlMethod_list.get(j).getParameterList();
                    for(int k = 0; k < idlParameter_list.size(); k++){
                        sb.append((k+1) + ":" + idlParameter_list.get(k).getType() + " " + idlParameter_list.get(k).getName());
                        if(k+1 < idlParameter_list.size())
                            sb.append(", ");
                    }
                    sb.append(")\n");
                }
                sb.append("}\n\n");
            }

        }
        this.content = sb.toString();

    }


    //命名空间
    private List<IdlNamespace> namespaceList;

    //常量
    private List<IdlConstant> constantList;

    //结构体定义
    private List<IdlStruct> structList;

    //服务定义
    private List<IdlService> serviceList;


    public List<IdlNamespace> getNamespaceList() {
        return namespaceList;
    }

    public void setNamespaceList(List<IdlNamespace> namespaceList) {
        this.namespaceList = namespaceList;
    }

    public List<IdlConstant> getConstantList() {
        return constantList;
    }

    public void setConstantList(List<IdlConstant> constantList) {
        this.constantList = constantList;
    }

    public List<IdlStruct> getStructList() {
        return structList;
    }

    public void setStructList(List<IdlStruct> structList) {
        this.structList = structList;
    }

    public List<IdlService> getServiceList() {
        return serviceList;
    }

    public void setServiceList(List<IdlService> serviceList) {
        this.serviceList = serviceList;
    }

    public IdlFile(){
        namespaceList = new ArrayList<IdlNamespace>();
        constantList = new ArrayList<IdlConstant>();
        structList = new ArrayList<IdlStruct>();
        serviceList = new ArrayList<IdlService>();
    }
}
