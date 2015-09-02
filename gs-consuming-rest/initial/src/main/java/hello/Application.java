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
        ArrayList<ExtractProjectPTM> tasks = new ArrayList<ExtractProjectPTM>();
        RestTemplate restTemplate = new RestTemplate();
        String PTMFilter = "acetylation";
        String PTMProjectCount = String.format("https://www.ebi.ac.uk:443/pride/ws/archive/project/count?ptmsFilter=%s", PTMFilter);
        int ProjectCount = restTemplate.getForObject(PTMProjectCount,int.class);
        if (ProjectCount>0)
        {
            int PageNumber = ProjectCount/10;
            for(int i=0;i<=PageNumber;i++)
            {

                String ProjectURL = String.format("http://www.ebi.ac.uk:80/pride/ws/archive/project/list?show=10&page=%d&order=desc&ptmsFilter=%s",i,PTMFilter);
                ProjectSummaryList list = restTemplate.getForObject(ProjectURL,ProjectSummaryList.class);
                if (tasks.size()==0)
                {
                    for (ProjectSummary pj : list.list) {
                        ExtractProjectPTM extractTask = new ExtractProjectPTM(pj, PTMFilter);
                        extractTask.start();
                        tasks.add(extractTask);
                        System.out.println(String.format("Prject  %s started! at PageNumber %d", pj.accession,i));
                    }
                }
                else {
                    for (ProjectSummary pj : list.list) {
                        int waittime = 0;
                        while(true && waittime<60) {
                            boolean getRoom = false;
                            for (ExtractProjectPTM task : tasks) {
                                if (task.Stopped) {
                                    tasks.remove(task);
                                    ExtractProjectPTM extractTask = new ExtractProjectPTM(pj, PTMFilter);
                                    extractTask.start();
                                    tasks.add(extractTask);
                                    System.out.println(String.format("Prject  %s started! at PageNumber %d", pj.accession,i));
                                    getRoom =true;
                                    break;
                                }


                            }
                            if (getRoom)
                                break;
                            Thread.sleep(1000);
                            waittime+=1;
                        }
                        if( waittime==60){
                            ExtractProjectPTM extractTask = new ExtractProjectPTM(pj, PTMFilter);
                            extractTask.start();
                            tasks.add(extractTask);
                            System.out.println(String.format("Prject  %s started! at PageNumber %d", pj.accession,i));
                        }

                    }
                }
            }

        }

        System.out.println("End!!!!!!!!!");

    }
}