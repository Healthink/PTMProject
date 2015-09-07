package com.company;

import org.w3c.dom.*;
import sun.misc.BASE64Decoder;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] args) {
	// write your code here
        try {

            // standard for reading an XML file
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder;
            Document doc = null;
            XPathExpression expr = null;
            builder = factory.newDocumentBuilder();
            doc = builder.parse("C:\\Users\\Haomin\\Documents\\PTMProject\\1histone_F002262.dat-pride.xml");

            // create an XPathFactory
            XPathFactory xFactory = XPathFactory.newInstance();

            // create an XPath object
            XPath xpath = xFactory.newXPath();
            try {
                // compile the XPath expression of precursor
                XPathExpression precursorXpath = xpath.compile(String.format("//spectrum[@id='%s']/spectrumDesc/precursorList/precursor[1]/ionSelection/cvParam[@accession='PSI:1000040']/@value", "1"));
                // run the query and get a nodeset
                NodeList precursornodes = (NodeList) precursorXpath.evaluate(doc, XPathConstants.NODESET);
                Attr precursorAttr = (Attr) precursornodes.item(0);
                float PrecursorMz = Float.parseFloat(precursorAttr.getValue());
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            // compile the XPath expression
            expr = xpath.compile(String.format("//spectrum[@id='%s']/mzArrayBinary/data","703"));
            // run the query and get a nodeset
            NodeList mzArraynodes = (NodeList)expr.evaluate(doc, XPathConstants.NODESET);
            Element dataNode  = (Element) mzArraynodes.item(0);
          List<Double> r = GetBase64Values(dataNode.getTextContent(), dataNode.getAttribute("precision"), dataNode.getAttribute("endian"), Integer.parseInt(dataNode.getAttribute("length")));

            // compile the XPath expression of mzarray
            XPathExpression intArrayXpath = xpath.compile(String.format("//spectrum[@id='%s']/intenArrayBinary/data","703"));
            // run the query and get a nodeset
            NodeList intArraynodes = (NodeList)intArrayXpath.evaluate(doc, XPathConstants.NODESET);
            Element dataNode2  = (Element) intArraynodes.item(0);
            List<Double> r2  = GetBase64Values(dataNode2.getTextContent(), dataNode2.getAttribute("precision"), dataNode2.getAttribute("endian"), Integer.parseInt(dataNode2.getAttribute("length")));
            int l = r2.size();

        }catch (Exception e)
        {
            e.printStackTrace();
        }

    }
    private static List<Double> GetBase64Values(String EncodedValues, String precision, String endian, int length )
    {
        List<Double> result = new ArrayList<Double>();
        try {
            BASE64Decoder decoder = new BASE64Decoder();
            byte[] arr = decoder.decodeBuffer(EncodedValues);
            ByteBuffer buffer = ByteBuffer.wrap(arr);
            if (endian.equals("big"))
            {
                buffer.order(ByteOrder.BIG_ENDIAN);
            }
                else {
                buffer.order(ByteOrder.LITTLE_ENDIAN);
            }

            for(int i=0;i<length;i++) {
                if(precision.equals("64")){
                    result.add(buffer.getDouble(i*8));
                }
                else {
                    result.add((double) buffer.getFloat(i*4));
                }
            }


        }catch (Exception e)
        {

        }
        return result;
    }
}
