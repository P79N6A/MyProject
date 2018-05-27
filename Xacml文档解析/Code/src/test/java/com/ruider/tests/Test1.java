package src.test.java.com.ruider.tests;

import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.swing.text.AbstractDocument;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * 完成新建xml文件
 */
public class Test1{

    public static void main(String[] args){
        DocumentBuilderFactory factory=DocumentBuilderFactory.newInstance();

        String fileName="policyID1";                 //名称为policyID
        try{
            DocumentBuilder documentBuilder=factory.newDocumentBuilder();
            Document doc=documentBuilder.newDocument();

            //创建结点
            Element policy=doc.createElement("policy");
            //添加属性
            policy.setAttribute("xmlns","urn:oasis:names:tc:xacml:3.0:core:schema:wd-17");    //固定
            policy.setAttribute("xmlns:xsi","http://www.w3.org/2001/XMLSchema-instance");    //固定
            policy.setAttribute("PolicyID","urn:oasis:names:tc:xacml:2.0:conformance-test:"+"1");                     //只改1
            policy.setAttribute("RulecombiningAigId","4");                //只改4
            policy.setAttribute("version","3");                            //只改3
            policy.setAttribute("xsi:schemaLocation","urn:oasis:names:tc:xacml:3.0:" +
                    "policy:schema:os access_control-xacml-2.0-policy-schema-os.xsd");                         //固定

            //创建子节点Description
            Element description=doc.createElement("Description");
            description.setTextContent("5");     //内容值只改5

            //绑定父子结点关系
            policy.appendChild(description);


            //policy子节点Target
            Element target=doc.createElement("Target");

            //Target子节点User
            Element anyof=doc.createElement("Anyof");
            Element allof=doc.createElement("Allof");

            //创建Allof子节点Match
            Element match1 =doc.createElement("Match");
            match1.setAttribute("MatchId","MatchID");

            Element attributeValue1=doc.createElement("AttributeValue");
            attributeValue1.setAttribute("DataType","1");            //只改1
            attributeValue1.setTextContent("2");

            Element attributeDesignator1=doc.createElement("AttributeDesignator");
            attributeDesignator1.setAttribute("AttributeId","urn:oasis:names:tc:xacml:3.0:subject:subject-“+”id");      //只改id
            attributeDesignator1.setAttribute("Catagory","urn:oasis:names:tc:xacml:3.0:subject-catagory:access-subject");  //固定
            attributeDesignator1.setAttribute("DataType","1");                //和上面dataType相同
            attributeDesignator1.setAttribute("MustBePresent","false");

            match1.appendChild(attributeValue1);
            match1.appendChild(attributeDesignator1);

            allof.appendChild(match1);
            anyof.appendChild(allof);
            target.appendChild(anyof);



            //Target子节点Resource
            anyof=doc.createElement("Anyof");
            allof=doc.createElement("Allof");
            //创建User子节点Match
            Element match2 =doc.createElement("Match");
            match2.setAttribute("MatchId","MatchID");

            Element attributeValue2=doc.createElement("AttributeValue");
            attributeValue2.setAttribute("DataType","DataType");
            attributeValue2.setTextContent("value");

            Element attributeDesignator2=doc.createElement("AttributeDesignator");
            attributeDesignator2.setAttribute("AttributeId","urn:oasis:names:tc:xacml:3.0:resource:"+"resource-id");
            attributeDesignator2.setAttribute("Catagory","urn:oasis:names:tc:xacml:3.0:attribute-catagory:resource");
            attributeDesignator2.setAttribute("DataType","1");
            attributeDesignator2.setAttribute("MustBePresent","false");

            match2.appendChild(attributeValue2);
            match2.appendChild(attributeDesignator2);

            allof.appendChild(match2);
            anyof.appendChild(allof);
            target.appendChild(anyof);

            //Target子节点Action
            Element anyof4=doc.createElement("Anyof");
            Element allof4=doc.createElement("Allof");
            //创建User子节点Match
            Element match3 =doc.createElement("Match");
            match3.setAttribute("MatchId","MatchID");

            Element attributeValue3=doc.createElement("AttributeValue");
            attributeValue3=doc.createElement("AttributeValue");
            attributeValue3.setAttribute("DataType","DataType");
            attributeValue3.setTextContent("value");

            Element attributeDesignator3=doc.createElement("AttributeDesignator");
            attributeDesignator3.setAttribute("AttributeId","urn:oasis:names:tc:xacml:3.0:action:"+"action-id");
            attributeDesignator3.setAttribute("Catagory","urn:oasis:names:tc:xacml:3.0:attribute-catagory:action");
            attributeDesignator3.setAttribute("DataType","1");
            attributeDesignator3.setAttribute("MustBePresent","false");

            match3.appendChild(attributeValue3);
            match3.appendChild(attributeDesignator3);

            allof4.appendChild(match3);
            anyof4.appendChild(allof4);
            target.appendChild(anyof4);

            policy.appendChild(target);


            //数组接受新增rule,循环添加即可

            //policy子节点Rule
            Element rule=doc.createElement("Rule");
            rule.setAttribute("RuleID","urn:oasis:names:tc:xacml:2.0:conformance-test:"+"RuleID");   //只改RuleID
            rule.setAttribute("Effect","Effect");          //只改Effect

            //Rule子节点description
            description=doc.createElement("description");
            description.setTextContent("description");
            rule.appendChild(description);

            //Rule子节点Target
            Element target1=doc.createElement("Target");


            //Rule的子节点User
            Element anyof1=doc.createElement("Anyof");
            Element allof1=doc.createElement("Allof");

            Element match4 =doc.createElement("Match");
            match4.setAttribute("MatchId","MatchID");


            Element attributeValue4=doc.createElement("AttributeValue");
            attributeValue4.setAttribute("DataType","DataType");                //只改DataType
            attributeValue4.setTextContent("value");                          //只改value

            Element attributeDesignator4=doc.createElement("AttributeDesignator");
            attributeDesignator4.setAttribute("AttributeId","urn:oasis:names:tc:xacml:3.0:subject:subject-"+"id");    //只改
            attributeDesignator4.setAttribute("Catagory","urn:oasis:names:tc:xacml:3.0:attribute-catagory:access-subject");
            attributeDesignator4.setAttribute("DataType","1");                     //和上面一样
            attributeDesignator4.setAttribute("MustBePresent","false");

            match4.appendChild(attributeValue4);
            match4.appendChild(attributeDesignator4);

            allof1.appendChild(match4);
            anyof1.appendChild(allof1);
            target1.appendChild(anyof1);


            //Rule的子节点Resource
            Element anyof2=doc.createElement("Anyof");
            Element allof2=doc.createElement("Allof");
            //创建User子节点Match
            Element match5 =doc.createElement("Match");
            match5.setAttribute("MatchId","MatchID");

            Element attributeValue5=doc.createElement("AttributeValue");
            attributeValue5.setAttribute("DataType","DataType");
            attributeValue5.setTextContent("value");

            Element attributeDesignator5=doc.createElement("AttributeDesignator");
            attributeDesignator5.setAttribute("AttributeId","urn:oasis:names:tc:xacml:3.0:resource:"+"resource-id");
            attributeDesignator5.setAttribute("Catagory","urn:oasis:names:tc:xacml:3.0:attribute-catagory:resource");
            attributeDesignator5.setAttribute("DataType","1");
            attributeDesignator5.setAttribute("MustBePresent","false");

            match5.appendChild(attributeValue5);
            match5.appendChild(attributeDesignator5);

            allof2.appendChild(match5);
            anyof2.appendChild(allof2);
            target1.appendChild(anyof2);

            //Target子节点Action
            Element anyof3=doc.createElement("Anyof");
            Element allof3=doc.createElement("Allof");
            //创建User子节点Match
            Element match6 =doc.createElement("Match");
            match6.setAttribute("MatchId","MatchID");

            Element attributeValue6=doc.createElement("AttributeValue");
            attributeValue6=doc.createElement("AttributeValue");
            attributeValue6.setAttribute("DataType","DataType");
            attributeValue6.setTextContent("value");

            Element attributeDesignator6=doc.createElement("AttributeDesignator");
            attributeDesignator6.setAttribute("AttributeId","urn:oasis:names:tc:xacml:3.0:action:"+"action-id");
            attributeDesignator6.setAttribute("Catagory","urn:oasis:names:tc:xacml:3.0:attribute-catagory:action");
            attributeDesignator6.setAttribute("DataType","1");
            attributeDesignator6.setAttribute("MustBePresent","false");

            match6.appendChild(attributeValue6);
            match6.appendChild(attributeDesignator6);

            allof3.appendChild(match6);
            anyof3.appendChild(allof3);
            target1.appendChild(anyof3);

            rule.appendChild(target1);

            policy.appendChild(rule);


            //根节点
            doc.appendChild(policy);


            //存储文件
            TransformerFactory transFactory=TransformerFactory.newInstance();

            Transformer transformer = transFactory.newTransformer();
            transformer.setOutputProperty("indent", "yes");

            DOMSource source=new DOMSource();
            source.setNode(doc);
            StreamResult result=new StreamResult();
            result.setOutputStream(new FileOutputStream("E://"+fileName+".xml"));

            transformer.transform(source, result);
            System.out.println("successful");
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }
}
