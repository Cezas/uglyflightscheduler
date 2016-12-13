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
public class Customer {
    private final String name;
    
    public Customer(String name){
        this.name=name;
    }
    
    public String getName(){
        return name;
    }
    
    public static ArrayList<String> getCustomers(){
            Connection connection;
            ResultSet resultSet;
            Statement statement;
            ArrayList<String> customers = new ArrayList();
            
        try{
            connection=Database.getConnection();
            statement = connection.createStatement();
            resultSet=statement.executeQuery("SELECT names FROM Customers");    
          
            while(resultSet.next())
                    customers.add(resultSet.getObject(1).toString());
            
        }catch(SQLException e){
            e.printStackTrace();
        }
        
        return customers;
        
        
    }
    
    public static ArrayList<String> getStatus(String cName){
            Connection connection;
            ResultSet resultSet;
            ResultSetMetaData metaData;
            String fName="", dName="", temp = "";
            ArrayList<String> stats = new ArrayList();
        try{
            connection = Database.getConnection();
            PreparedStatement getStatus = connection.prepareStatement("SELECT flight, day FROM Bookings where customer = ? UNION Select flight, day FROM Waitlist where customer = ?"); 
            getStatus.setString(1, cName);
            getStatus.setString(2, cName);
            resultSet = getStatus.executeQuery();
            metaData = resultSet.getMetaData();
            int cols = metaData.getColumnCount();
            
            
            while(resultSet.next()){
                for(int i = 1; i<=cols;i++){
                    if(metaData.getColumnName(i).equalsIgnoreCase("Flight"))
                        fName=resultSet.getObject(i).toString();
                    
                    if(metaData.getColumnName(i).equalsIgnoreCase("Day"))
                        dName=resultSet.getDate(i).toString();               
                }
                
                temp = String.format("%-70s %-70s ",fName, dName);
                stats.add(temp);
            }
            
        }catch(SQLException e){
            e.printStackTrace();
        }
        return stats;
    }
}
