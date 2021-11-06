package com.parkit.parkingsystem.service;

import java.time.LocalDateTime;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.util.InputReaderUtil;

/**
 * Service for park vehicles
 */
public class ParkingService {

    private static final Logger logger = LogManager.getLogger("ParkingService");

    private FareCalculatorService fareCalculatorService = new FareCalculatorService();

    private InputReaderUtil inputReaderUtil;
    private ParkingSpotDAO parkingSpotDAO;
    private TicketDAO ticketDAO;
    

    public ParkingService(InputReaderUtil inputReaderUtil, ParkingSpotDAO parkingSpotDAO, TicketDAO ticketDAO){
        this.inputReaderUtil = inputReaderUtil;
        this.parkingSpotDAO = parkingSpotDAO;
        this.ticketDAO = ticketDAO;
    }
    
    public void processIncomingVehicle() {
        try{
            ParkingSpot parkingSpot = getNextParkingNumberIfAvailable();
            
            if(parkingSpot !=null && parkingSpot.getId() > 0 ){
                String vehicleRegNumber = getVehicleRegNumber();
                parkingSpot.setAvailable(false);
                parkingSpotDAO.updateParking(parkingSpot);//allot this parking space and mark it's availability as false
                LocalDateTime inTime = LocalDateTime.now();
                Ticket ticket = new Ticket();
                ticket.setParkingSpot(parkingSpot);
                ticket.setVehicleRegNumber(vehicleRegNumber);
                ticket.setPrice(0);
                ticket.setInTime(inTime);
                ticket.setOutTime(null);
                ticketDAO.saveTicket(ticket);
                System.out.println("============================================================================================");
                System.out.println("Generated Ticket and saved in DB");
                System.out.println("Please park your vehicle in spot number: "+ parkingSpot.getId());
                System.out.println("Recorded in-time for vehicle number: "+vehicleRegNumber+" / In-time The: "+inTime.toLocalDate()+" at "+inTime.toLocalTime());
                System.out.println("============================================================================================");
                if (ticketDAO.getIfRecurrentUser(ticket.getVehicleRegNumber())) {
                	System.out.println("Welcome back! As usual user, you get benefit of 5% discount!");
                	System.out.println("============================================================================================");
                }
            }
        }
        catch(Exception e){
            logger.error("Unable to process incoming vehicle",e);
            throw e;
        }
    }

    private String getVehicleRegNumber() {
    	System.out.println("Please type the vehicle registration number and press enter key");
        return inputReaderUtil.readVehicleRegistrationNumber();
    }

    public ParkingSpot getNextParkingNumberIfAvailable(){
        int parkingNumber=0;
        ParkingSpot parkingSpot = null;
        try{
            ParkingType parkingType = getVehichleType();
            parkingNumber = parkingSpotDAO.getNextAvailableSlot(parkingType);
            if(parkingNumber > 0){
                parkingSpot = new ParkingSpot(parkingNumber,parkingType, true);
            }
            else{
                throw new Exception("Error fetching parking number from DB. Parking slots might be full");
            }
        }
        catch(IllegalArgumentException ie){
            logger.error("Error parsing user input for type of vehicle", ie);
        }
        catch(Exception e){
            logger.error("Error fetching next available parking slot", e);
        }
        return parkingSpot;
    }

    private ParkingType getVehichleType(){
    	System.out.println("============================================================================================");
    	System.out.println("Please select vehicle type from menu");
    	System.out.println("1 CAR");
    	System.out.println("2 BIKE");
    	System.out.println("============================================================================================");
        int input = inputReaderUtil.readSelection();
        switch(input){
            case 1: {
                return ParkingType.CAR;
            }
            case 2: {
                return ParkingType.BIKE;
            }
            default: {
            	System.out.println("Incorrect input provided");
                throw new IllegalArgumentException("Entered input is invalid");
            }
        }
    }
    
    public void processExitingVehicle() {
        try{
            String vehicleRegNumber = getVehicleRegNumber();
            Ticket ticket = ticketDAO.getTicket(vehicleRegNumber);

            Thread.sleep(600);
            LocalDateTime outTime = LocalDateTime.now();
            ticket.setOutTime(outTime);

            fareCalculatorService.calculateFare(ticket);
            
            if(ticketDAO.updateTicket(ticket)) {
            	ParkingSpot parkingSpot = ticket.getParkingSpot();
                parkingSpot.setAvailable(true);
                parkingSpotDAO.updateParking(parkingSpot);
                System.out.println("============================================================================================");
                System.out.println("Please pay the parking fare: "+ticket.getPrice()+"€");
                System.out.println("Recorded out-time for vehicle number: "+ticket.getVehicleRegNumber()+" / Out-time The: "+outTime.toLocalDate()+" at "+outTime.toLocalTime());
                System.out.println("============================================================================================");
            }
            else{
            	System.out.println("Unable to update ticket information. Error occurred");
            }
        }
        catch(Exception e){
            logger.error("Unable to process exiting vehicle",e);
        }
    }
}