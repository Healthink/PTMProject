package hello;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.client.RestTemplate;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;

@SpringBootApplication
public class Application implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(Application.class);

    public static void main(String args[]) {
        SpringApplication.run(Application.class);
    }

    @Override
    public void run(String... strings) throws Exception {

        RestTemplate restTemplate = new RestTemplate();
        Quote result = restTemplate.getForObject("http://gturnquist-quoters.cfapps.io/api/random",  Quote.class );
        log.info(result.toString());
        ProjectSummaryList list = restTemplate.getForObject("http://www.ebi.ac.uk:80/pride/ws/archive/project/list?show=500&page=0&order=desc&ptmsFilter=phosphorylation",ProjectSummaryList.class);
        PrintWriter out = new PrintWriter(new FileWriter("C:\\Users\\lenovo\\Documents\\Work\\UCLA\\ptmfrompride.txt", true));
    try {
        for (ProjectSummary pj : list.list) {
            String PeptideCountURL = String.format("http://www.ebi.ac.uk:80/pride/ws/archive/peptide/count/project/%s", pj.accession);
            int PeptideCount = restTemplate.getForObject(PeptideCountURL, int.class);
            if (PeptideCount > 0) {
                int PageNumber = PeptideCount / 100;
                for (int i = 0; i <= PageNumber; i++) {
                    System.out.println(String.format("%d/%d", i, PageNumber));
                    String PsmURL = String.format("http://www.ebi.ac.uk:80/pride/ws/archive/peptide/list/project/%s?show=100&page=%d", pj.accession, i);
                    PsmDetailList pdList = restTemplate.getForObject(PsmURL, PsmDetailList.class);
                    for (PsmDetail pd : pdList.list) {
                        if (pd.modifications.size() > 0) {
                            out.println(String.format("%s|%s|%s", pd.sequence, pd.modifications.toString(), pd.spectrumID));
                        }
                    }
                }
            }
        }
    }catch (Exception e)
    {
        System.out.println(e.getStackTrace().toString());}
      finally {
        out.close();
    }
        log.info(list.toString());
    }
}