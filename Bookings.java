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
import java.util.ArrayList;

/**
 *
 * @author Admin
 */
public class Bookings {
    private final Flights flight;
    private final Customer customer;
    private final Days day;
    
    public Bookings(Customer c, Flights f, Days d){ 
        flight=f;
        customer=c;
        day=d;
    }
    
    public Flights getFlight(){
        return flight;
    }
    
    
    public Customer getCustomer(){
        return customer;
    }
    
    public Days getDay(){
        return day;
    }
    
    public static int insertBooking(Bookings b){
            Connection connection;
            ResultSet resultSet;
            Statement statement;
        try{
            //check to see if attempted book still has seats
            connection=Database.getConnection();
            PreparedStatement getFlightSeats = connection.prepareStatement("select count(flight) from bookings where flight = ? and day = ?"); 
            getFlightSeats.setString(1, b.getFlight().getName()); 
            getFlightSeats.setDate(2, b.getDay().getDate());            
            resultSet = getFlightSeats.executeQuery(); 
            resultSet.next(); 
            int seatsBooked = resultSet.getInt(1);  //how many currently booked for that flight on that day
            
            PreparedStatement getMaxOccupancy = connection.prepareStatement("select seats from flights where names = ?");
            getMaxOccupancy.setString(1, b.getFlight().getName());
            resultSet = getMaxOccupancy.executeQuery();
            
            resultSet.next();       
            int maxSeats = resultSet.getInt(1);

            
            if(seatsBooked<maxSeats){
                PreparedStatement insertBooking= connection.prepareStatement("INSERT INTO Bookings (Customer, Flight, Day) values (?,?,?)");
                insertBooking.setString(1, b.getCustomer().getName());
                insertBooking.setString(2, b.getFlight().getName());
                insertBooking.setString(3, b.getDay().getDate().toString());
                insertBooking.executeUpdate();    
            }else{//otherwise, send to waitlist
                return WaitList.insertWaiting(b);
            }
                 

        }catch(SQLIntegrityConstraintViolationException e){
            return 2;//2=return conflict key
        }
        catch(SQLException e){          
            e.printStackTrace();
        }   
        
        return 0;//0=returned good booking entry
    }
    
    public static ArrayList<String> getBookingsByDay(){
        Connection connection;
            ResultSet resultSet;
            Statement statement;
            ArrayList<String> bookings = new ArrayList();
            String flightName="";
            String customerName="";
            String dayDate="empty";
            String temp="";
            
        try{
            connection=Database.getConnection();
            statement = connection.createStatement();
            resultSet=statement.executeQuery("SELECT customer, flight, day FROM Bookings");    
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
                }
                
                temp = String.format("%-70s %-70s %1s",customerName, flightName, dayDate);
                bookings.add(temp);
                //bookings.add(new Bookings(new Customer(customerName), new Flights(flightName), new Days(dayDate)));               
            }
        }catch(SQLException e){
            e.printStackTrace();
        }
        
        return bookings;
    }
    
    public static ArrayList<String> getBookingsByDay(String fName, String dName){
        Connection connection;
            ResultSet resultSet;
            Statement statement;
            ArrayList<String> bookings = new ArrayList();
            String customerName="";
            String temp="";
            
        try{
            connection=Database.getConnection();
            PreparedStatement getBooks = connection.prepareStatement("SELECT customer, flight, day FROM Bookings WHERE flight = ? and day = ?");
            getBooks.setString(1, fName);
            getBooks.setString(2, dName);
            resultSet=getBooks.executeQuery();    
            
            ResultSetMetaData metaData = resultSet.getMetaData();
            int cols = metaData.getColumnCount();
            
            while(resultSet.next()){
                for(int i = 1; i<=cols;i++){
                    if(metaData.getColumnName(i).equalsIgnoreCase("Customer"))
                        customerName=resultSet.getObject(i).toString();
                }
                
                temp = String.format("%-70s %-70s %1s",customerName, fName, dName);
                bookings.add(temp);
                //bookings.add(new Bookings(new Customer(customerName), new Flights(flightName), new Days(dayDate)));               
            }
        }catch(SQLException e){
            e.printStackTrace();
        }
        
        return bookings;
    }
    
    public static String dropFlight(String cName, String fName, String dName){
         Connection connection;  
         ResultSet resultSet;
         boolean insideWait=false, insideBook=false;
        try{
           connection=Database.getConnection();
           PreparedStatement checkWaitList = connection.prepareStatement("SELECT customer, flight, day FROM WaitList where customer = ? and flight = ? and day = ?");
           checkWaitList.setString(1, cName);
           checkWaitList.setString(2, fName);
           checkWaitList.setDate(3, java.sql.Date.valueOf(dName));           
           resultSet = checkWaitList.executeQuery();
           
           if(resultSet.next()){
               insideWait=true;
           }
           
           if(insideWait){
            PreparedStatement removeFlight = connection.prepareStatement("DELETE FROM Waitlist where customer = ? and flight = ? and day = ?");
            removeFlight.setString(1, cName);
            removeFlight.setString(2, fName);
            removeFlight.setDate(3, java.sql.Date.valueOf(dName));
            
            removeFlight.executeUpdate();
            
            return "waitlist";
           }
           /**************************************/
           PreparedStatement checkBookings = connection.prepareStatement("SELECT customer, flight, day FROM Bookings where customer = ? and flight = ? and day = ?");
           checkBookings.setString(1, cName);
           checkBookings.setString(2, fName);
           checkBookings.setDate(3, java.sql.Date.valueOf(dName));
           
           resultSet = checkBookings.executeQuery();
           
           if(resultSet.next()){
               insideBook=true;
           }
           
           if(insideBook){
            PreparedStatement removeFlight = connection.prepareStatement("DELETE FROM Bookings where customer = ? and flight = ? and day = ?");
            removeFlight.setString(1, cName);
            removeFlight.setString(2, fName);
            removeFlight.setDate(3, Date.valueOf(dName));        
            
            removeFlight.executeUpdate();
            
            //now move frrom waitlist to bookings
            
            Statement statement = connection.createStatement();
            resultSet=statement.executeQuery("SELECT customer, flight, day FROM waitlist order by timestamp asc");
            ResultSetMetaData metaData = resultSet.getMetaData();
            int cols = metaData.getColumnCount();
            String customerName="ERROR", flightName="ERROR";
            Date dayDate = Date.valueOf(dName);
            
            
            if(resultSet.next()){
                for(int i = 1; i<=cols;i++){
                    if(metaData.getColumnName(i).equalsIgnoreCase("Customer"))
                        customerName=resultSet.getObject(i).toString();
                    
                    if(metaData.getColumnName(i).equalsIgnoreCase("Flight"))
                        flightName=resultSet.getObject(i).toString();
                    
                    if(metaData.getColumnName(i).equalsIgnoreCase("Day"))
                        dayDate=Date.valueOf(resultSet.getDate(i).toString());
                }
                
                PreparedStatement removeWait = connection.prepareStatement("DELETE FROM Waitlist where customer = ? and flight = ? and day = ?");
                removeWait.setString(1, customerName);
                removeWait.setString(2, flightName);
                removeWait.setDate(3, java.sql.Date.valueOf(dayDate.toString()));          
                removeWait.executeUpdate();
                
                PreparedStatement insertBooking= connection.prepareStatement("INSERT INTO Bookings (Customer, Flight, Day) values (?,?,?)");
                insertBooking.setString(1, customerName);
                insertBooking.setString(2, flightName);
                insertBooking.setString(3, dayDate.toString());
                insertBooking.executeUpdate();   
                
                
            }    
             
            Bookings.getBookingsByDay();
            WaitList.getWaitList();
            return "bookings";
           }
           
           
        }catch(SQLException e){
            e.printStackTrace();
        }
        
        return "";
    }
}

