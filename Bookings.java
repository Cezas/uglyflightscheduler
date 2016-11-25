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
}
