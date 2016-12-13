/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package FlightSchedulerRobertYan_rjy5060;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
//import java.util.Date;
import java.sql.Date;
import java.sql.PreparedStatement;

/**
 *
 * @author Admin
 */
public class Days {
    private final Date date;
    
    public Days(Date d){
        date=d;
    }
    
    public Date getDate(){
        return date;
    }
    
    public static ArrayList<String> getDates(){
            Connection connection;
            ResultSet resultSet;
            Statement statement;
            ArrayList<String> days = new ArrayList();
            
        try{
            connection=Database.getConnection();
            statement = connection.createStatement();
            resultSet=statement.executeQuery("SELECT dates FROM Days");    
          
            while(resultSet.next())
                    days.add(resultSet.getObject(1).toString());
            
        }catch(SQLException e){
            e.printStackTrace();
        }
        
        return days;
    }
    
    public static void addDay(Date d){
         try{
            Connection connection = Database.getConnection();
            PreparedStatement insertFlight = connection.prepareStatement("INSERT INTO days values ?"); 
            insertFlight.setDate(1, d);
            insertFlight.executeUpdate();
            
        }catch(SQLException e){ //check for invalid conversion
            e.printStackTrace();
        }
    }
}
