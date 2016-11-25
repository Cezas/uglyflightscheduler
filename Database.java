package FlightSchedulerRobertYan_rjy5060;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;



/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Admin
 */
public class Database {
    
    public static Connection getConnection(){
        try{
            String host = "jdbc:derby://localhost:1527/FlightSchedulerRobertYan_rjy5060";
            String username="java";
            String password="java";
                
            Connection con=DriverManager.getConnection(host, username, password);
            return con;
        }
        catch(SQLException e){
            System.out.println(e.getMessage());
        }
        
        return null;
    }   
    
}
