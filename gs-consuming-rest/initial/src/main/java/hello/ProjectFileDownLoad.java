package hello;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.springframework.web.client.RestTemplate;
import org.w3c.dom.*;
import sun.misc.BASE64Decoder;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by lenovo on 2015/9/2.
 */
public class ProjectFileDownLoad {
    private String Project;
    private String FileName;
    private String SpectrumID;
    private  String Folder = "C:\\Users\\haomin\\Documents\\PTMProject";
    private  boolean UseDom = false;
    public ProjectFileDownLoad(String Proj, String File,String spectrumId){
        Project = Proj;
        FileName = File;
        SpectrumID = spectrumId;

    }
   public SpectrumInfo GetSpectrum(){

       SpectrumInfo spectrumInfo = new SpectrumInfo();

       if (!UseDom)
       {
           PrideXMLParsing pxmlparser = new PrideXMLParsing(String.format("%s\\%s",Folder,FileName),SpectrumID);

           spectrumInfo = pxmlparser.Parsing();
           return spectrumInfo;
       }
       try {

           // standard for reading an XML file
           DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
           factory.setNamespaceAware(true);
           DocumentBuilder builder;
           Document doc = null;
           XPathExpression expr = null;
           builder = factory.newDocumentBuilder();
           doc = builder.parse(String.format("%s\\%s",Folder,FileName));

           // create an XPathFactory
           XPathFactory xFactory = XPathFactory.newInstance();

           // create an XPath object
           XPath xpath = xFactory.newXPath();
            try {
                // compile the XPath expression of msLevel
                XPathExpression msLevelXpath = xpath.compile(String.format("//spectrum[@id='%s']/spectrumDesc/spectrumSettings/spectrumInstrument/@msLevel", SpectrumID));
                // run the query and get a nodeset
                NodeList nodes = (NodeList) msLevelXpath.evaluate(doc, XPathConstants.NODESET);
                Attr msLevelAttr = (Attr) nodes.item(0);
                spectrumInfo.msLevel = msLevelAttr.getValue();
            }catch (Exception e)
            {
                e.printStackTrace();
            }

            try {
                // compile the XPath expression of precursor
                XPathExpression precursorXpath = xpath.compile(String.format("//spectrum[@id='%s']/spectrumDesc/precursorList/precursor[1]/ionSelection/cvParam[@accession='PSI:1000040']/@value", SpectrumID));
                // run the query and get a nodeset
                NodeList precursornodes = (NodeList) precursorXpath.evaluate(doc, XPathConstants.NODESET);

                Attr precursorAttr = (Attr) precursornodes.item(0);
                spectrumInfo.PrecursorMz = Float.parseFloat(precursorAttr.getValue());
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

           try {
               // compile the XPath expression of precursor
               XPathExpression chargeXpath = xpath.compile(String.format("//spectrum[@id='%s']/spectrumDesc/precursorList/precursor[1]/ionSelection/cvParam[@accession='PSI:1000041']/@value", SpectrumID));
               // run the query and get a nodeset
               NodeList chargenodes = (NodeList) chargeXpath.evaluate(doc, XPathConstants.NODESET);
               Attr chargeAttr = (Attr) chargenodes.item(0);
               spectrumInfo.PrecursorCharge = Integer.parseInt(chargeAttr.getValue());
           }catch (Exception e)
           {
               e.printStackTrace();
           }
            try {
                // compile the XPath expression of mzarray
                XPathExpression mzArrayXpath = xpath.compile(String.format("//spectrum[@id='%s']/mzArrayBinary/data", SpectrumID));
                // run the query and get a nodeset
                NodeList mzArraynodes = (NodeList) mzArrayXpath.evaluate(doc, XPathConstants.NODESET);
                Element dataNode = (Element) mzArraynodes.item(0);
                spectrumInfo.mzArray = GetBase64Values(dataNode.getTextContent(), dataNode.getAttribute("precision"), dataNode.getAttribute("endian"), Integer.parseInt(dataNode.getAttribute("length")));
            }catch (Exception e)
            {
                e.printStackTrace();
            }
           try {

               // compile the XPath expression of mzarray
               XPathExpression intArrayXpath = xpath.compile(String.format("//spectrum[@id='%s']/intenArrayBinary/data", SpectrumID));
               // run the query and get a nodeset
               NodeList intArraynodes = (NodeList) intArrayXpath.evaluate(doc, XPathConstants.NODESET);
               Element dataNode2 = (Element) intArraynodes.item(0);
               spectrumInfo.intenArray = GetBase64Values(dataNode2.getTextContent(), dataNode2.getAttribute("precision"), dataNode2.getAttribute("endian"), Integer.parseInt(dataNode2.getAttribute("length")));
           }
           catch (Exception e)
           {
               e.printStackTrace();
           }
       }catch (Exception e)
       {
        e.printStackTrace();
       }
       return spectrumInfo;
   }

    private List<Float> GetBase64Values(String EncodedValues, String precision, String endian, int length )
    {
        List<Float> result = new ArrayList<Float>();
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

            if (precision.equals("64"))
            {
                for(int i=0;i<length;i++) {
                    result.add((float)buffer.getDouble(i*8));
                }
            }
            else
            {
                for(int i=0;i<length;i++) {
                    result.add(buffer.getFloat(i*4));
                }
            }




        }catch (Exception e)
        {

        }
        return result;
    }
    public boolean Download(){
       // String Folder = "C:\\Users\\lenovo\\Documents\\GitHub\\PTMProject";
        RestTemplate restTemplate = new RestTemplate();
        String FileListURL = String.format("http://www.ebi.ac.uk:80/pride/ws/archive/file/list/project/%s",Project) ;
        FileDetailList fileList = restTemplate.getForObject(FileListURL, FileDetailList.class);
        for(FileDetail fileDetail:fileList.list){
            if (fileDetail.fileName.startsWith(FileName))
            {
                String URL = fileDetail.downloadLink;
                String server = "ftp.pride.ebi.ac.uk";
                int port = 21;
                File downloadFile1 = new File(String.format("%s\\%s",Folder,fileDetail.fileName));
                if (downloadFile1.exists())
                {
                    System.out.println(String.format("File %s is already there.",fileDetail.downloadLink));
                    break;
                }
                FTPClient ftpClient = new FTPClient();
                try {

                    ftpClient.connect(server, port);
                    ftpClient.login("anonymous","");
                    ftpClient.enterLocalPassiveMode();
                    ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

                    String remoteFile1 = URL.substring(URL.indexOf("/pride"));

                    OutputStream outputStream1 = new BufferedOutputStream(new FileOutputStream(downloadFile1));
                    boolean success = ftpClient.retrieveFile(remoteFile1, outputStream1);
                    outputStream1.close();
                    int code = ftpClient.getReplyCode();

                    if (success) {
                        System.out.println(String.format("File %s has been downloaded successfully.",fileDetail.downloadLink));
                        UncompressFileGZIP uncompressFileGZIP = new UncompressFileGZIP();
                        if (uncompressFileGZIP.getExtension(fileDetail.fileName).equalsIgnoreCase("gz"))
                        {
                            uncompressFileGZIP.doUncompressFile(String.format("%s\\%s",Folder,fileDetail.fileName));
                        }
                    }
                    break;

                }catch (Exception e)
                {
                    e.printStackTrace();
                    return  false;
                }

            }
        }
        return true;
    }
}
