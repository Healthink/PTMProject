package hello;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

/**
 * Created by lenovo on 2015/9/2.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class FileDetailList {
    public List<FileDetail> list;
}
