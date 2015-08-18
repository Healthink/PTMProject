package hello;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

/**
 * Created by lenovo on 2015/8/18.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PsmDetail {
    public String id;
    public String assayAccession;
    public int startPosition;
    public int endPosition;
    public List<ModifiedLocation> modifications;
    public String projectAccession;
    public String sequence;
    public String proteinAccession;
    public List<String> searchEngines;
    public List<SearchEngineScore> searchEngineScores;
    public float retentionTime;
    public int charge;
    public float calculatedMZ;
    public float experimentalMZ;
    public String preAA;
    public String postAA;
    public String spectrumID;
    public String reportedID;
}
