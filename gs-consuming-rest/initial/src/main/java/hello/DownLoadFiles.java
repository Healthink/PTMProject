package hello;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.*;

/**
 * Created by lenovo on 2015/9/2.
 */
@SpringBootApplication
public class DownLoadFiles implements CommandLineRunner {
//   public static void main(String args[]) {
//        SpringApplication.run(DownLoadFiles.class);
//    }
    @Override
    public void run(String... strings) throws Exception {
        System.out.println("The COPa Library Search Engine is starting....");
        //todo: please using a configure file to save the database connection string.
        String  connectstring =    "jdbc:oracle:thin:@172.16.200.22:1521:ptm";
        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
        } catch (ClassNotFoundException e) {
            System.out.println("Where is your mySQL JDBC Driver?");
            e.printStackTrace();
            return;
        }
        Connection connection;
        connection = null;

        try {
            connection = DriverManager.getConnection(connectstring,"Sys as sysdba","ptm");
        } catch (SQLException e) {
            System.out.println("Connection Failed! Check output console");
            e.printStackTrace();
            return;
        }

        if (connection != null) {
            System.out.println("Connected success!");
        } else {
            System.out.println("Failed to make connection!");
        }
        String query = "select spectrum  from ptm_phosphorylation where rownum<2";
        ResultSet rs=null;
        Statement stmt = null;
        try {
            stmt = connection.createStatement();
            rs = stmt.executeQuery(query);
            while (rs.next()) {
                String Spectrum = rs.getString(1);
                String[] SplitResults = Spectrum.split(";");

                ProjectFileDownLoad pfdownload = new ProjectFileDownLoad(SplitResults[0],SplitResults[1],"2");

            }
        }catch(Exception e)
        {

        }




    }
}
