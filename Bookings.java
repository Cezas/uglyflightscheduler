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
public class Bookings {
    private final Flights flight;
    private final Customer customer;
    private final Days day;
    private final Timestamp timestamp;
    
    public Bookings(Customer c, Flights f, Days d,  Timestamp t){ 
        flight=f;
        customer=c;
        day=d;
        timestamp=new Timestamp(System.currentTimeMillis());
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
    
    public Timestamp getTime(){
        return timestamp;
    } 
    
    public static int insertBooking(Bookings b, int case1){
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
                PreparedStatement insertBooking= connection.prepareStatement("INSERT INTO Bookings (Customer, Flight, Day, timestamp) values (?,?,?,?)");
                insertBooking.setString(1, b.getCustomer().getName());
                insertBooking.setString(2, b.getFlight().getName());
                insertBooking.setString(3, b.getDay().getDate().toString());
                insertBooking.setString(4, b.getTime().toString());
                insertBooking.executeUpdate();    
            }else{//otherwise, send to waitlist
                if(case1==0)
                    return WaitList.insertWaiting(b);
            }
           
        }catch(SQLIntegrityConstraintViolationException e){
            return 2;//2=return conflict key
        }
        
        catch(SQLException e){          
            e.printStackTrace();
        }   
        
        if(case1==1)    
            return 3; 
        
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
    
    public static String cancelFlight(String cName,String dName){
         Connection connection;  
         ResultSet resultSet;
         boolean insideWait=false, insideBook=false;
        try{
           connection=Database.getConnection();
           PreparedStatement checkWaitList = connection.prepareStatement("SELECT customer, flight, day FROM WaitList where customer = ? and day = ?");
           checkWaitList.setString(1, cName);
           checkWaitList.setDate(2, java.sql.Date.valueOf(dName));           
           resultSet = checkWaitList.executeQuery();
           
           if(resultSet.next()){
               insideWait=true;
           }
           
           if(insideWait){
            PreparedStatement removeFlight = connection.prepareStatement("DELETE FROM Waitlist where customer = ? and day = ?");
            removeFlight.setString(1, cName);
            removeFlight.setDate(2, java.sql.Date.valueOf(dName));
            
            removeFlight.executeUpdate();
            
            return "waitlist";
           }
           /**************************************/
           PreparedStatement checkBookings = connection.prepareStatement("SELECT customer, day FROM Bookings where customer = ? and day = ?");
           checkBookings.setString(1, cName);
           checkBookings.setDate(2, java.sql.Date.valueOf(dName));
           
           resultSet = checkBookings.executeQuery();
           
           if(resultSet.next()){
               insideBook=true;
           }
           
           //not true?
           if(insideBook){
            PreparedStatement removeFlight = connection.prepareStatement("DELETE FROM Bookings where customer = ? and day = ?");
            removeFlight.setString(1, cName);
            removeFlight.setDate(2, Date.valueOf(dName));        
            
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
                
                PreparedStatement insertBooking= connection.prepareStatement("INSERT INTO Bookings (Customer, Flight, Day, Timestamp) values (?,?,?,?)");
                insertBooking.setString(1, customerName);
                insertBooking.setString(2, flightName);
                insertBooking.setDate(3, dayDate);
                insertBooking.setTimestamp(4, new Timestamp(System.currentTimeMillis()));
                insertBooking.executeUpdate();   
         
            }    
             
            Bookings.getBookingsByDay();
            WaitList.getWaitList();
            return "bookings";
           }
        }catch(SQLIntegrityConstraintViolationException e){
                     
        }catch(SQLException e){
            e.printStackTrace();
        }
        
        return "";
    }
    
    public static ArrayList<String> dropFlight(String fName){ 
        Connection connection = Database.getConnection();
        ResultSet resultSet;
        //ArrayList<String> unbooked =  new ArrayList();
        ArrayList<String> unbooked = new ArrayList();
        try{
            
        //see if valid flight
            PreparedStatement checkValid = connection.prepareStatement("Select flight from Bookings where flight = ?");
            checkValid.setString(1, fName);
            resultSet = checkValid.executeQuery();
            
            if(!resultSet.next())
                return null;
           
            //remove the flight itself from the flights table
            PreparedStatement clearFlights = connection.prepareStatement("DELETE From flights where names = ?");
            clearFlights.setString(1, fName);
            clearFlights.executeUpdate();
            
           //cancel those in waitlist
           PreparedStatement emptyWaitList = connection.prepareStatement("DELETE FROM WaitList where flight=?");
           emptyWaitList.setString(1, fName);      
           emptyWaitList.executeUpdate();
           
           //get all passengers in booking
           PreparedStatement getPassengers = connection.prepareStatement("SELECT customer, day, timestamp FROM Bookings where flight = ? order by timestamp asc, day asc");
           getPassengers.setString(1, fName);           
           resultSet = getPassengers.executeQuery();
           
           ResultSetMetaData metaData = resultSet.getMetaData();
           int cols = metaData.getColumnCount();
            
           
            String customerName="";
            Date dayDate = Date.valueOf("1111-11-11");
            Timestamp ts = new Timestamp(System.currentTimeMillis());
                        
            ArrayList<String> customers = new ArrayList();
            ArrayList<Date> days = new ArrayList();
            ArrayList<ArrayList<String>> customersPerDay = new ArrayList<ArrayList<String>>(); //should be parallel with days
            
            //ArrayList<Bookings> books= new ArrayList();
            
            while(resultSet.next()){
            //collect values into lists for priority              
                for(int i = 1; i <=cols; i++){
                    if(metaData.getColumnName(i).equalsIgnoreCase("Customer")){
                        customerName=resultSet.getObject(i).toString();
                    }
                    if(metaData.getColumnName(i).equalsIgnoreCase("Day"))
                        dayDate=resultSet.getDate(i);           
                    }
                
                days.add(dayDate);
                customers.add(customerName);
            }
            
            PreparedStatement clearBookings = connection.prepareStatement("DELETE From Bookings where flight = ?");
            clearBookings.setString(1, fName);
            clearBookings.executeUpdate();
            
            //getting all the flights for attempted booking
            Statement statement = connection.createStatement();
            resultSet=statement.executeQuery("SELECT names FROM flights");  
            metaData=resultSet.getMetaData();
            cols= metaData.getColumnCount();
            String flightName="";
            ArrayList<String> flightNames = new ArrayList();
              while(resultSet.next()){ 
            //collect values into lists for priority              
                for(int i = 1; i <=cols; i++){
                    if(metaData.getColumnName(i).equalsIgnoreCase("names"))
                        flightName=resultSet.getObject(i).toString();                        
                }
                flightNames.add(flightName);
              }
              
              
         //int skip=0;     
         flightName="";
         customerName="";
         dayDate = Date.valueOf("1111-11-11");
         
         PreparedStatement insertBookings = connection.prepareStatement("INSERT INTO Bookings (Customer, Flight, Day, Timestamp) values (?,?,?,?)");;
         for(int i=0; i<customers.size();i++){
             customerName=customers.get(i);
             insertBookings.setString(1, customerName);
             int unbookcount=0;
             
             System.out.println(customers);
             for(int j=0; j<flightNames.size();j++){
                 flightName=flightNames.get(j);
                 insertBookings.setString(2, flightName);
                 System.out.println(flightNames);
                 //check occupancy
                PreparedStatement getFlightSeats = connection.prepareStatement("select count(flight) from bookings where flight = ? and day = ?"); 
                getFlightSeats.setString(1, flightName); 
                getFlightSeats.setDate(2, days.get(i));            
                resultSet = getFlightSeats.executeQuery(); 
                resultSet.next(); 
                int seatsBooked = resultSet.getInt(1);  //how many currently booked for that flight on that day
            
                PreparedStatement getMaxOccupancy = connection.prepareStatement("select seats from flights where names = ?");
                getMaxOccupancy.setString(1, flightName);
                resultSet = getMaxOccupancy.executeQuery();
            
                resultSet.next();       
                int maxSeats = resultSet.getInt(1);
            
                if(seatsBooked<maxSeats){
                    insertBookings.setDate(3, days.get(i));
                    insertBookings.setTimestamp(4, new Timestamp(System.currentTimeMillis()));
                    insertBookings.executeUpdate();
                }
                else{//otherwise, add to unbooked
                    unbookcount++;
                }
               
             } 
              if(unbookcount==flightNames.size())
                  unbooked.add(customerName);
         }
              
          
         
         Bookings.getBookingsByDay();
         WaitList.getWaitList();  
        }catch(SQLIntegrityConstraintViolationException e){
            
        }catch(SQLException e){
            e.printStackTrace();
        }
        
        return unbooked;
    }
}

