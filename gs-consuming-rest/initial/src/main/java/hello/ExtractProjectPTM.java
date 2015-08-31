package hello;


import org.springframework.web.client.RestTemplate;

import java.io.FileWriter;
import java.io.PrintWriter;


/**
 * Created by lenovo on 2015/8/31.
 */
public class ExtractProjectPTM  implements Runnable {
    private ProjectSummary project;
    private String modificiation;
    private String modname;
    private Thread t;
    public Boolean Stopped;
    public ExtractProjectPTM(ProjectSummary pj, String mod)
    {
        project = pj;
        modname = mod;
        switch(mod){
            case "phosphorylation":
                modificiation = "MOD:00696";
                break;
            default:
                modificiation="";

        }
        Stopped  = false;
        //modificiation = mod;

    }

   public void run()  {
       try {
           RestTemplate restTemplate = new RestTemplate();
           PrintWriter out = new PrintWriter(new FileWriter(String.format("C:\\Users\\lenovo\\Documents\\Work\\UCLA\\ptmfrompride_%s_%s.txt", project.accession, modname), true));
           String PeptideCountURL = String.format("http://www.ebi.ac.uk:80/pride/ws/archive/peptide/count/project/%s", project.accession);
           int PeptideCount = restTemplate.getForObject(PeptideCountURL, int.class);
           if (PeptideCount > 0) {
               int PageNumber = PeptideCount / 1000;
               for (int i = 0; i <= PageNumber; i++) {
                   System.out.println(String.format("%s : %d/%d", project.accession, i, PageNumber));
                   String PsmURL = String.format("http://www.ebi.ac.uk:80/pride/ws/archive/peptide/list/project/%s?show=1000&page=%d", project.accession, i);
                   PsmDetailList pdList = restTemplate.getForObject(PsmURL, PsmDetailList.class);
                   for (PsmDetail pd : pdList.list) {
                       if (pd.modifications.size() > 0) {
                           for (ModifiedLocation mod : pd.modifications) {
                               if (mod.modification.equals(modificiation)) {
                                   out.println(String.format("%s|%s|%s", pd.sequence, pd.modifications.toString(), pd.spectrumID));
                                   break;
                               }

                           }

                       }
                   }
               }
           }
       }catch (Exception e)
       {
           System.out.println(e.getStackTrace().toString());
       }
       Stopped = true;
    }
    public void start()
    {
        if (t==null)
        {
            t = new Thread(this);
            t.start();
        }

    }
}
