package com.ruider.tests;

import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;

/*
测试解析xacml文档
 */
public class Test3 {
    public static void main(String[] args){

        DocumentBuilderFactory factory=DocumentBuilderFactory.newInstance();
        DocumentBuilder db=null;
        Document doc=null;
        try{
            db=factory.newDocumentBuilder();
            doc=db.parse(new File("E://PolicyId.xml"));

            Element policy=doc.getDocumentElement();
            String PolicyId=policy.getAttribute("PolicyId");
            PolicyId=PolicyId.substring(46);
            System.out.println("PolicyID>>   "+PolicyId);
            System.out.println("Version>>   "+policy.getAttribute("Version"));
            System.out.println("RuleCombiningAIgId>>   "+policy.getAttribute("RuleCombiningAlgId"));

            NodeList nodes=policy.getChildNodes();
            String description1=null;
            for(int i=0;i<nodes.getLength();i++){
                if(nodes.item(i).getNodeName().equalsIgnoreCase("Description")){
                    description1=nodes.item(i).getTextContent();
                    break;
                }
            }

            System.out.println("Description>>   "+description1);

            NodeList nodez=policy.getChildNodes();

            boolean hasTarget=false;
            boolean hasUser=false;
            boolean hasResource=false;
            boolean hasAction=false;
            for(int i=0;i<nodez.getLength();i++){
                if(nodez.item(i).getNodeName().equalsIgnoreCase("Target")){
                    Element target=(Element) policy.getElementsByTagName("Target").item(0);
                    hasTarget=true;
                    NodeList AttributeDesignators=target.getElementsByTagName("AttributeDesignator");
                    for(int j=0;j<AttributeDesignators.getLength();j++){
                        NamedNodeMap attrs=AttributeDesignators.item(j).getAttributes();
                        for(int k=0;k<attrs.getLength();k++){
                            if(attrs.item(k).getNodeName().equalsIgnoreCase("AttributeId")){
                                String AttributeId=attrs.item(k).getNodeValue();

                                if(AttributeId.endsWith("subject-id")){
                                    hasUser=true;
                                }
                                else if(AttributeId.endsWith("resource-id")){
                                    hasResource=true;
                                }
                                else if(AttributeId.endsWith("action-id")){
                                    hasAction=true;
                                }
                            }
                            else if(attrs.item(k).getNodeName().equalsIgnoreCase("MustBePresent")){
                                String MustBePresent=attrs.item(k).getNodeValue();
                                System.out.println("MustBePresent>>　　"+MustBePresent);
                            }

                        }
                    }
                }
            }
            if(hasTarget){
                Element target=(Element) policy.getElementsByTagName("Target").item(0);
                NodeList matches=target.getElementsByTagName("Match");
                if(hasUser){
                    Element userMatch=(Element) matches.item(0);
                    String MatchId1=userMatch.getAttribute("MatchId");
                    System.out.println("MatchId1>>   "+MatchId1);

                    NodeList attributeValues=userMatch.getChildNodes();
                    NamedNodeMap attrs=userMatch.getAttributes();
                    for(int y=0;y<attributeValues.getLength();y++){
                        if(attributeValues.item(y).getNodeName().equalsIgnoreCase("AttributeValue")){
                            attrs=attributeValues.item(y).getAttributes();
                            for(int x=0;x<attrs.getLength();x++){
                                if(attrs.item(x).getNodeName().equals("DataType")){                                 //DataType需要修改
                                    System.out.println("DataType1>>   "+attrs.item(x).getNodeValue());
                                }
                            }
                            System.out.println("value1>>   "+attributeValues.item(y).getTextContent());
                        }
                    }
                }
                else
                    System.out.println("User>>  null");

                if(hasResource){
                    Element resourceMatch=(Element) matches.item(1);
                    String MatchId2=resourceMatch.getAttribute("MatchId");
                    System.out.println("MatchId2>>   "+MatchId2);

                    NodeList attributeValues=resourceMatch.getChildNodes();
                    NamedNodeMap attrs=resourceMatch.getAttributes();
                    for(int y=0;y<attributeValues.getLength();y++){
                        if(attributeValues.item(y).getNodeName().equalsIgnoreCase("AttributeValue")){
                            attrs=attributeValues.item(y).getAttributes();
                            for(int x=0;x<attrs.getLength();x++){
                                if(attrs.item(x).getNodeName().equals("DataType")){                                 //DataType需要修改
                                    System.out.println("DataType2>>   "+attrs.item(x).getNodeValue());
                                }
                            }
                            System.out.println("value2>>   "+attributeValues.item(y).getTextContent());
                        }
                    }
                }else
                    System.out.println("Resource>>   null");
                if(hasAction){
                    Element actionMatch=(Element) matches.item(2);
                    String MatchId3=actionMatch.getAttribute("MatchId");
                    System.out.println("MatchId3>>   "+MatchId3);

                    NodeList attributeValues=actionMatch.getChildNodes();
                    NamedNodeMap attrs=actionMatch.getAttributes();
                    for(int y=0;y<attributeValues.getLength();y++){
                        if(attributeValues.item(y).getNodeName().equalsIgnoreCase("AttributeValue")){
                            attrs=attributeValues.item(y).getAttributes();
                            for(int x=0;x<attrs.getLength();x++){
                                if(attrs.item(x).getNodeName().equals("DataType")){                                 //DataType需要修改
                                    System.out.println("DataType3>>   "+attrs.item(x).getNodeValue());
                                }
                            }
                            System.out.println("value3>>   "+attributeValues.item(y).getTextContent());
                        }
                    }
                }else
                    System.out.println("Action>>   null");

            }

            else{
                System.out.println("Target>>   null");
            }


            //Rule(s)
            int a=0;
            if(hasTarget)
                a=1;

            //System.out.println("长度>>>"+nodez.getLength());
            int s=0;

            nodez=policy.getElementsByTagName("Rule");
            for(int l=0;l<nodez.getLength();l++,s++){
                //if(nodez.item(i).getNodeName().equalsIgnoreCase("Rule")){
                boolean hasRule=false;
                boolean hasRuleUser=false;
                boolean hasRuleResource=false;
                boolean hasRuleAction=false;
                //Element rule=(Element) policy.getElementsByTagName("Rule").item(s);
                Element rule=(Element)nodez.item(l);
                hasRule=true;
                NodeList AttributeDesignators=rule.getElementsByTagName("AttributeDesignator");
                for(int j=0;j<AttributeDesignators.getLength();j++){
                    NamedNodeMap attrs=AttributeDesignators.item(j).getAttributes();
                    for(int k=0;k<attrs.getLength();k++){
                        if(attrs.item(k).getNodeName().equalsIgnoreCase("AttributeId")){
                            String value=attrs.item(k).getNodeValue();
                            if(value.endsWith("subject-id")){
                                hasRuleUser=true;
                            }
                            else if(value.endsWith("resource-id")){
                                hasRuleResource=true;
                            }
                            else if(value.endsWith("action-id")){
                                hasRuleAction=true;
                            }
                        }
                    }
                }



            if(hasRule){
                String RuleId=rule.getAttribute("RuleId").substring(46);
                System.out.println("RuleId>>   "+RuleId);
                String Effect=rule.getAttribute("Effect");
                System.out.println("Effect>>   "+Effect);

                //NodeList nodes=policy.getChildNodes();
                String desc=null;
                for(int i=0;i<nodes.getLength();i++){
                    if(nodes.item(i).getNodeName().equalsIgnoreCase("Description")){
                        desc=nodes.item(i).getTextContent();
                        break;
                    }
                }
                System.out.println("description>>   "+desc);

                NodeList matches=rule.getElementsByTagName("Match");
                if(hasRuleUser){
                    Element userMatch=(Element) matches.item(0);
                    String MatchId4=userMatch.getAttribute("MatchId");
                    System.out.println("MatchId4>>   "+MatchId4);

                    NodeList attributeValues=userMatch.getChildNodes();
                    NamedNodeMap attrs=userMatch.getAttributes();
                    for(int y=0;y<attributeValues.getLength();y++){
                        if(attributeValues.item(y).getNodeName().equalsIgnoreCase("AttributeValue")){
                            attrs=attributeValues.item(y).getAttributes();
                            for(int x=0;x<attrs.getLength();x++){
                                if(attrs.item(x).getNodeName().equals("DataType")){                                 //DataType需要修改
                                    System.out.println("DataType4>>   "+attrs.item(x).getNodeValue());
                                }
                            }
                            System.out.println("value4>>   "+attributeValues.item(y).getTextContent());
                        }
                    }
                }
                else
                    System.out.println("RuleUser>>  null");

                int c=-1;

                if(hasRuleResource){

                    if(hasRuleUser)
                        c=1;
                    else
                        c=0;
                    Element resourceMatch=(Element) matches.item(c);
                    String MatchId5=resourceMatch.getAttribute("MatchId");
                    System.out.println("MatchId5>>   "+MatchId5);

                    NodeList attributeValues=resourceMatch.getChildNodes();
                    NamedNodeMap attrs=resourceMatch.getAttributes();
                    for(int y=0;y<attributeValues.getLength();y++){
                        if(attributeValues.item(y).getNodeName().equalsIgnoreCase("AttributeValue")){
                            attrs=attributeValues.item(y).getAttributes();
                            for(int x=0;x<attrs.getLength();x++){
                                if(attrs.item(x).getNodeName().equals("DataType")){                                 //DataType需要修改
                                    System.out.println("DataType5>>   "+attrs.item(x).getNodeValue());
                                }
                            }
                            System.out.println("value5>>   "+attributeValues.item(y).getTextContent());
                        }
                    }
                }else
                    System.out.println("RuleResource>>   null");

                int z=-1;

                if(hasRuleAction){

                    if(hasRuleUser&&hasRuleResource)
                        z=2;
                    else if((hasRuleUser&&!hasRuleResource)||(!hasRuleUser&&hasRuleResource))
                        z=1;
                    else if(!hasRuleUser&&!hasRuleResource)
                        z=0;

                    Element actionMatch=(Element) matches.item(z);
                    String MatchId6=actionMatch.getAttribute("MatchId");
                    System.out.println("MatchId6>>   "+MatchId6);

                    NodeList attributeValues=actionMatch.getChildNodes();
                    NamedNodeMap attrs=actionMatch.getAttributes();
                    for(int y=0;y<attributeValues.getLength();y++){
                        if(attributeValues.item(y).getNodeName().equalsIgnoreCase("AttributeValue")){
                            attrs=attributeValues.item(y).getAttributes();
                            for(int x=0;x<attrs.getLength();x++){
                                if(attrs.item(x).getNodeName().equals("DataType")){                                 //DataType需要修改
                                    System.out.println("DataType6>>   "+attrs.item(x).getNodeValue());
                                }
                            }
                            System.out.println("value6>>   "+attributeValues.item(y).getTextContent());
                        }
                    }
                }else
                    System.out.println("RuleAction>>   null");

            }

            else{
                System.out.println("Rule>>   null");
            }

            }



        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
}
