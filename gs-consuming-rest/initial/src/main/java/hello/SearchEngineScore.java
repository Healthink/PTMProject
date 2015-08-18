package hello;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

/**
 * Created by lenovo on 2015/8/18.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SearchEngineScore {
    public String searchEngine;
    public String score;
}
