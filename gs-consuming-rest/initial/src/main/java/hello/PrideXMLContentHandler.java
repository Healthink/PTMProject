package hello;

import jdk.internal.org.xml.sax.SAXException;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.XMLFilterImpl;
import sun.misc.BASE64Decoder;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Haomin on 2015/9/7.
 */
public class PrideXMLContentHandler extends XMLFilterImpl {
    private String SpectrumIndex;
    private SpectrumInfo spectrumInfo;
    private boolean bRightSpectrum = false;
    private boolean bMzArray = false;
    private boolean bIntArray = false;
    private String BaseArrayData = "";
    private String endian = "";
    private String precision = "";
    private int length =0;
    public PrideXMLContentHandler(String SpectrumId){
        SpectrumIndex = SpectrumId;
        spectrumInfo = new SpectrumInfo();
    }

    public SpectrumInfo GetSpectrum()
    {
        return spectrumInfo;
    }
    public void startElement(String namespaceURI, String localName, String qName,Attributes attributes){
        if(localName.equals("spectrum"))
        {
            String id = attributes.getValue("id");
            if (id.equals(SpectrumIndex))
            {
                bRightSpectrum = true;
            }
        }
        else if(bRightSpectrum&localName.equals("cvParam"))
        {
            String accession = attributes.getValue("accession");
            switch (accession) {
                case "MS:1000744":
                case "PSI:1000040":
                    spectrumInfo.PrecursorMz = Float.parseFloat(attributes.getValue("value"));
                    break;
                case "MS:1000041":
                case "PSI:1000041":
                    spectrumInfo.PrecursorCharge = Integer.parseInt(attributes.getValue("value"));
                    break;

            }
        }
        else if(bRightSpectrum&localName.equals("mzArrayBinary"))
        {
            bMzArray =true;
        }
        else if(bRightSpectrum&localName.equals("intenArrayBinary"))
        {
            bIntArray =true;

        }else if(bRightSpectrum&localName.equals("data"))
        {
            if (bMzArray )
            {
                endian = attributes.getValue("endian");
                precision = attributes.getValue("precision");
                length = Integer.parseInt(attributes.getValue("length"));

            }else
            {
                endian = attributes.getValue("endian");
                precision = attributes.getValue("precision");
                length = Integer.parseInt(attributes.getValue("length"));

            }
        }
    }

    public void characters(char[] ch, int start, int length){
        String gotString = new String(ch,start,length);
        if(bRightSpectrum&(bMzArray|bIntArray))
        {
            if (!gotString.trim().equals("")) {
                BaseArrayData += gotString;
            }
        }
    }
    public void endElement(String namespaceURI, String localName, String qName) throws org.xml.sax.SAXException {
        if(localName.equals("spectrum"))
        {
            if (bRightSpectrum) {
                bRightSpectrum = false;
                throw new MySAXTerminatorException();
            }
        }else if (bMzArray&localName.equals("mzArrayBinary"))
        {
            spectrumInfo.mzArray = GetBase64Values(BaseArrayData,precision,endian,length);
            bMzArray = false;
            BaseArrayData="";
        }
        else if(bIntArray&localName.equals("intenArrayBinary"))
        {
            spectrumInfo.intenArray = GetBase64Values(BaseArrayData,precision,endian,length);
            bIntArray =false;
            BaseArrayData = "";
        }
    }

    private List<Float> GetBase64Values(String EncodedValues, String precision, String endian, int length )
    {
        List<Float> result = new ArrayList<Float>();
        try {
            BASE64Decoder decoder = new BASE64Decoder();
            byte[] arr = decoder.decodeBuffer(EncodedValues);
            int ByteLength = arr.length;
            ByteBuffer buffer = ByteBuffer.wrap(arr);
            if (endian.equals("big"))
            {
                buffer.order(ByteOrder.BIG_ENDIAN);
            }
            else {
                buffer.order(ByteOrder.LITTLE_ENDIAN);
            }

//            int OneRead = ByteLength/length;
//            switch(OneRead)
//            {
//                case 1:
//                    for(int i=0;i<length;i++) {
//                        result.add((float) buffer.get(i));
//                    }
//                    break;
//                case 2:
//                    for(int i=0;i<length;i++) {
//                        result.add((float) buffer.getShort(i*OneRead));
//                    }
//                    break;
//                case 4:
//                    for(int i=0;i<length;i++) {
//                        result.add((float) buffer.getFloat(i * OneRead));
//                    }
//                    break;
//                case 8:
//                    for(int i=0;i<length;i++) {
//                        result.add((float) buffer.getDouble(i*OneRead));
//                    }
//                    break;
//            }

            if (precision.equals("64"))
            {
                int ReadLength = ByteLength/8;
                for(int i=0;i<ReadLength;i++) {
                    result.add((float)buffer.getDouble(i*8));
                }
            }
            else
            {
                int ReadLength = ByteLength/4;
                for(int i=0;i<ReadLength;i++) {
                    result.add(buffer.getFloat(i*4));
                }
            }




        }catch (Exception e)
        {

        }
        return result;
    }
}
