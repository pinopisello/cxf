package demo.ws_rm;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;

public class CheckDBMetatdata {

    public static void main(String[] args) throws Exception{
        Class.forName("org.postgresql.Driver");
        Connection con = DriverManager.getConnection("jdbc:postgresql://127.0.0.1:5432/CXF", "postgres", "postgres");
        DatabaseMetaData metadata = con.getMetaData();
        ResultSet rs = metadata.getColumns(null, null, "cxf_rm_src_sequences", "%");
        while (rs.next()) {
            System.out.println(rs.getString(4));
        }
        
        
    }

}
