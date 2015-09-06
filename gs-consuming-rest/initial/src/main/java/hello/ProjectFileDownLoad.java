package hello;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.springframework.web.client.RestTemplate;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
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
    private  String Folder = "C:\\Users\\lenovo\\Documents\\GitHub\\PTMProject";
    public ProjectFileDownLoad(String Proj, String File,String spectrumId){
        Project = Proj;
        FileName = File;
        SpectrumID = spectrumId;

    }
   public SpectrumInfo GetSpectrum(){

       SpectrumInfo spectrumInfo = new SpectrumInfo();
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

           // compile the XPath expression
           expr = xpath.compile(String.format("//spectrum[@id='%s']",SpectrumID));
           // run the query and get a nodeset
           Object result = expr.evaluate(doc, XPathConstants.NODESET);

           // cast the result to a DOM NodeList
           NodeList nodes = (NodeList) result;

          Element SpectrumNode = (Element) nodes.item(0);

           Node spectrumDescNode = SpectrumNode.getFirstChild();

           Node spectrumSeetingsNode = spectrumDescNode.getFirstChild();

           Element spectrumInstrumentNode = (Element) spectrumSeetingsNode.getFirstChild();

           spectrumInfo.msLevel =  spectrumInstrumentNode.getAttribute("msLevel");

           Node precursorList = spectrumDescNode.getChildNodes().item(1);

           Node precursorNode = precursorList.getFirstChild();

           Node ionSelection = precursorNode.getFirstChild();

           NodeList cvParams = ionSelection.getChildNodes();

           for(int i=0;i<cvParams.getLength();i++)
           {
               if (((Element)cvParams.item(i)).getAttribute("accession").equals("MS:1000744"))
               {
                   spectrumInfo.PrecursorMz = Float.parseFloat(((Element)cvParams.item(i)).getAttribute("value"));
               }else if (((Element)cvParams.item(i)).getAttribute("accession").equals("MS:1000041"))
               {
                   spectrumInfo.PrecursorCharge = Integer.parseInt(((Element) cvParams.item(i)).getAttribute("value"));
               }
           }

          NodeList lists =  SpectrumNode.getChildNodes();
           for(int i=0;i<lists.getLength();i++)
           {
               if (lists.item(i).getNodeName().equals("mzArrayBinary"))
               {
                   Element dataNode = (Element) lists.item(i).getFirstChild();
                   spectrumInfo.mzArray = GetBase64Values(dataNode.getTextContent(),dataNode.getAttribute("precision"),dataNode.getAttribute("endian"),Integer.parseInt(dataNode.getAttribute("length")));


               }
               else if (lists.item(i).getNodeName().equals("intenArrayBinary"))
               {
                   Element dataNode = (Element) lists.item(i).getFirstChild();
                   spectrumInfo.intenArray = GetBase64Values(dataNode.getTextContent(),dataNode.getAttribute("precision"),dataNode.getAttribute("endian"),Integer.parseInt(dataNode.getAttribute("length")));

               }
           }



       }catch (Exception e)
       {

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
            buffer.order(ByteOrder.LITTLE_ENDIAN);


            for(int i=0;i<length;i++) {
                result.add( buffer.getFloat(i));
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
                    return  false;
                }

            }
        }
        return true;
    }
}
