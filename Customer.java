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
}
