package hello;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lenovo on 2015/8/18.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProjectSummaryList {
    public List<ProjectSummary> list;
}
