package src.test.java.com.ruider.tests;
//编辑修改代码
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.*;
import javax.xml.xpath.*;

public class Test2 {
    public static void main(String[] args) throws Exception{
        DocumentBuilderFactory factory=DocumentBuilderFactory.newInstance();
        //Element theBook=null,theElem=null,root=null;
        try {
            factory.setIgnoringElementContentWhitespace(true);

            DocumentBuilder db = factory.newDocumentBuilder();
            Document xmldoc = db.parse(new File("E://Test1.xml"));
            //Element root = xmldoc.getDocumentElement();
            NodeList nodes=xmldoc.getElementsByTagName("name");
            Node node=nodes.item(0);
            System.out.println(node.getNodeName()+",,,,"+node.getTextContent());
            node.setTextContent("hhhhhhh");
            System.out.println(node.getNodeName()+",,,,"+node.getTextContent());


            //保存
            TransformerFactory transFactory=TransformerFactory.newInstance();

            Transformer transformer = transFactory.newTransformer();
            transformer.setOutputProperty("indent", "yes");

            DOMSource source=new DOMSource();
            source.setNode(xmldoc);
            StreamResult result=new StreamResult();
            result.setOutputStream(new FileOutputStream("E://Test1.xml"));

            transformer.transform(source, result);

        }
        catch (Exception e){
            throw new Exception(e);
        }
    }

}

