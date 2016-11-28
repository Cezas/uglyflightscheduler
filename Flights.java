/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package FlightSchedulerRobertYan_rjy5060;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

/**
 *
 * @author Admin
 */
public class Flights {
    private final String name;
    private int seats;
    
    public Flights(String n){
         name=n;
         seats=0;
    }
    
    public Flights(String n, int s){
         name=n;
         seats=s;
    }
    
    public String getName(){
        return name;
    }
    
    public int getSeats(){
        return seats;
    }
    
    public void setSeats(int val){
        seats=val;
    }
    
    public static ArrayList<String> getFlights(){  //loop thru the table and shove strings into arraylist, then moves into combobox
            Connection connection;
            ResultSet resultSet;
            Statement statement;
            ArrayList<String> names = new ArrayList();
            
        try{
            connection=Database.getConnection();
            statement = connection.createStatement();
            resultSet=statement.executeQuery("SELECT names FROM Flights");   
            
            while(resultSet.next())
                    names.add(resultSet.getObject(1).toString());
        
        }catch(SQLException e){
            e.printStackTrace();
        }
        
        return names;
    }
    
    public static void addFlight(String name, int seats){
        //todo later
    }
    
    public static int getFlightSeats(String flight){
        /*if(!connected)
            throw new IllegalStateException("Not connected");*/
        
        int seats = 0;
        try{
           Connection connection = Database.getConnection();
           PreparedStatement getSeats = connection.prepareStatement("SELECT seats FROM Flights WHERE names=?");   
           getSeats.setString(1, flight);
           ResultSet resultSet=getSeats.executeQuery();
           
           
           if(resultSet.next())
                seats = resultSet.getInt(1);
           
        }catch(SQLException e){
            e.printStackTrace();
        }
        return seats;
    }
}
