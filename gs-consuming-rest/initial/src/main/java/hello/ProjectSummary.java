package hello;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

/**
 * Created by lenovo on 2015/8/18.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProjectSummary {
    public List<String> ptmNames;
    public String accession;
    public List<String> species;
    public List<String> instrumentNames;
    public List<String> tissues;
    public String title;
    public String projectDescription;
    public  int numAssays;
    public String submissionType;
    public String publicationDate;
    public List<String> projectTags;

}
