package hello;

import java.awt.geom.CubicCurve2D;
import java.util.List;

/**
 * Created by lenovo on 2015/9/5.
 */
public class SpectrumInfo {

    public String id ;

    public String msLevel;

    public float PrecursorMz = 0;

    public int PrecursorCharge = 0;

    public List<Float> mzArray;

    public List<Float> intenArray;

    public String GetDTA(){
        if (mzArray == null)
            return "";
        String DTA = String.format("%f %d", PrecursorMz,PrecursorCharge);

        for (int i=0;i<mzArray.size();i++)
        {
            DTA += String.format("\r\n%f %f",mzArray.get(i),intenArray.get(i));
        }
        return DTA;
    }

}
