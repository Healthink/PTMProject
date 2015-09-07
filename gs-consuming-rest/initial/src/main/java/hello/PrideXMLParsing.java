package hello;

import org.xml.sax.SAXException;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Created by Haomin on 2015/9/7.
 */
public class PrideXMLParsing {
    private String SpectrumIndex;
    private String PrideXML;
    public PrideXMLParsing(String xmlFile, String spectrumId ){
        PrideXML = xmlFile;
        SpectrumIndex = spectrumId;
    }

    public SpectrumInfo Parsing(){
        SpectrumInfo spectrum = new SpectrumInfo();
        FileInputStream instream = null;
        InputSource is = null;
        PrideXMLContentHandler pxmlCont = new PrideXMLContentHandler(SpectrumIndex);
        try{
            SAXParserFactory spf = SAXParserFactory.newInstance();
            spf.setNamespaceAware(true);
            XMLReader xmlReader = spf.newSAXParser().getXMLReader();
            xmlReader.setContentHandler(pxmlCont);
            instream = new FileInputStream(PrideXML);
            is = new InputSource(instream);
            xmlReader.parse(is);
        } catch (IOException e)
        {
            e.printStackTrace();
            return spectrum;
        }
        catch(MySAXTerminatorException e)
        {
            spectrum= pxmlCont.GetSpectrum();
            return spectrum;
        }
        catch(SAXException e){
            e.printStackTrace();
            return spectrum;
        }catch(ParserConfigurationException e){
            e.printStackTrace();
            return spectrum;
        }
        return spectrum;
    }
}
