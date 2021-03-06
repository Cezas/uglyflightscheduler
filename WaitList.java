/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package FlightSchedulerRobertYan_rjy5060;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;

/**
 *
 * @author Admin
 */
public class WaitList extends Bookings{


    public WaitList(Customer c, Flights f, Days d, Timestamp t){ 
        super(c,f,d,t);
    }
    
    public static int insertWaiting(Bookings b){
            Connection connection;
            ResultSet resultSet;
            Statement statement;
            WaitList waitlist = new WaitList(b.getCustomer(), b.getFlight(), b.getDay(), b.getTime());
        try{
            //check to see if attempted book still has seats
            connection=Database.getConnection();

            PreparedStatement insertBooking= connection.prepareStatement("INSERT INTO WaitList (Customer, Flight, Day, timestamp) values (?,?,?,?)");
            insertBooking.setString(1, waitlist.getCustomer().getName());
            insertBooking.setString(2, waitlist.getFlight().getName());
            insertBooking.setString(3, waitlist.getDay().getDate().toString());
            insertBooking.setString(4, waitlist.getTime().toString());
            insertBooking.executeUpdate();    
            
        }catch(SQLIntegrityConstraintViolationException e){
            return 2;
        }
        catch(SQLException e){          
            e.printStackTrace();
        }   
              
        return 1;//1=returned good waitlist entry
    } 
    
    public static ArrayList<String> getWaitList(){
        Connection connection;
            ResultSet resultSet;
            Statement statement;
            ArrayList<String> waitlist = new ArrayList();
            String flightName="";
            String customerName="";
            String dayDate="empty";
            String tsName="";
            String temp="";

        try{
            connection=Database.getConnection();
            statement = connection.createStatement();
            resultSet=statement.executeQuery("SELECT customer, flight, day, timestamp FROM WaitList");    
            ResultSetMetaData metaData = resultSet.getMetaData();
            int cols = metaData.getColumnCount();
            
            while(resultSet.next()){
                for(int i = 1; i<=cols;i++){
                    if(metaData.getColumnName(i).equalsIgnoreCase("Customer"))
                        customerName=resultSet.getObject(i).toString();
                    
                    if(metaData.getColumnName(i).equalsIgnoreCase("Flight"))
                        flightName=resultSet.getObject(i).toString();
                    
                    if(metaData.getColumnName(i).equalsIgnoreCase("Day"))
                        dayDate=resultSet.getDate(i).toString();
                    
                    if(metaData.getColumnName(i).equalsIgnoreCase("Timestamp"))
                        tsName=resultSet.getTime(i).toString();
                }
                temp = String.format("%-40s %-40s %40s %40s",customerName, flightName, dayDate, tsName);
                waitlist.add(temp);
            }
        }catch(SQLException e){
            e.printStackTrace();
        }
        
        return waitlist;
    }
   
    public static ArrayList<String> getWaitListStatusbyDay(String dName){
        Connection connection;
            ResultSet resultSet;
            Statement statement;
            ArrayList<String> waitlist = new ArrayList();
            String customerName="";
            String tsName="";
            String flightName="";
            String temp="";
            
        try{
            connection=Database.getConnection();
            PreparedStatement getBooks = connection.prepareStatement("SELECT customer, flight, day, timestamp FROM WaitList WHERE day = ?");
            getBooks.setString(1, dName);
            resultSet=getBooks.executeQuery();    
            
            ResultSetMetaData metaData = resultSet.getMetaData();
            int cols = metaData.getColumnCount();
            
            while(resultSet.next()){
                for(int i = 1; i<=cols;i++){
                    if(metaData.getColumnName(i).equalsIgnoreCase("Flight"))
                        flightName=resultSet.getObject(i).toString();
                    
                    if(metaData.getColumnName(i).equalsIgnoreCase("Customer"))
                        customerName=resultSet.getObject(i).toString();
                    
                    if(metaData.getColumnName(i).equalsIgnoreCase("Timestamp"))
                        tsName=resultSet.getTime(i).toString();
                }
                
                temp = String.format("%-40s %-40s %40s %40s",customerName, flightName, dName, tsName);
                waitlist.add(temp);
                //bookings.add(new Bookings(new Customer(customerName), new Flights(flightName), new Days(dayDate)));               
            }
        }catch(SQLException e){
            e.printStackTrace();
        }
        
        return waitlist;
    }
    
}
