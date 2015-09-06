package hello;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

/**
 * Created by lenovo on 2015/9/2.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class FileDetail {
    public String fileName;
    public String downloadLink;
    public String projectAccession;
    public String assayAccession;
    public String fileType;
    public String fileSource;
    public String fileSize;
}
