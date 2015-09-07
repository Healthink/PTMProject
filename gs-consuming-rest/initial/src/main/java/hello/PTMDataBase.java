package hello;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Created by Haomin on 2015/9/6.
 */
public  class PTMDataBase {

    public static String ConnectionString;
    public static Connection connection;

    public PTMDataBase()
    {
       // ConnectionString = Connect;
        ConnectDB();
    }

    private static void ConnectDB(){
        //todo: please using a configure file to save the database connection string.
        ConnectionString =    "jdbc:oracle:thin:@172.16.200.22:1521:ptm";
        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
        } catch (ClassNotFoundException e) {
            System.out.println("Where is your mySQL JDBC Driver?");
            e.printStackTrace();
            return;
        }
       // Connection connection;
        connection = null;

        try {
            connection = DriverManager.getConnection(ConnectionString, "Sys as sysdba", "ptm");
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
    }
   public static boolean InsertSpectrum(String Peptide,String Modification, String Spectrum, String DTA)
   {
       Statement stmt = null;
       boolean result = false;
       String query = String.format("insert into ptm_phosphorylation (peptide,modifiedlocation,spectrum,dta) values ('%s','%s','%s','%s') ",Peptide,Modification,Spectrum,DTA);
       try{
           stmt = connection.createStatement();
           result = stmt.execute(query);
           stmt.close();
       }catch (SQLException e)
       {
           System.out.println(String.format(" %s Insert spectrum dta failed",Spectrum));
           return false;
       }
       return result;

   }
    public static int UpdateDTA(String Spectrum, String DTA)
    {
        if (connection == null)
            ConnectDB();
        Statement stmt = null;
        int result = 0;
        String query = String.format("update ptm_phosphorylation set DTA='%s' where spectrum='%s'",DTA,Spectrum);
        try{
            stmt = connection.createStatement();
            result = stmt.executeUpdate(query);
            stmt.close();
        }catch (SQLException e)
        {
            System.out.println(String.format(" %s Update dta failed",Spectrum));
            return -2;
        }
        return result;
    }

}
