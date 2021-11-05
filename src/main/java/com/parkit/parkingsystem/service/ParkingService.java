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
                //ticketDAO.getIfRecurrentUser(vehicleRegNumber);
                ticketDAO.saveTicket(ticket);
                logger.warn("Generated Ticket and saved in DB");
                logger.info("Please park your vehicle in spot number: {}",parkingSpot.getId());
                logger.info("Recorded in-time for vehicle number: {} / In-time The: {} at {}",vehicleRegNumber,inTime.toLocalDate(),inTime.toLocalTime());
            }
        }
        catch(Exception e){
            logger.error("Unable to process incoming vehicle",e);
            throw e;
        }
    }

    private String getVehicleRegNumber() {
    	logger.info("Please type the vehicle registration number and press enter key");
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
        logger.info("Please select vehicle type from menu");
        logger.info("1 CAR");
        logger.info("2 BIKE");
        int input = inputReaderUtil.readSelection();
        switch(input){
            case 1: {
                return ParkingType.CAR;
            }
            case 2: {
                return ParkingType.BIKE;
            }
            default: {
            	logger.info("Incorrect input provided");
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
                if (ticketDAO.getIfRecurrentUser(vehicleRegNumber)) {
                    logger.info("Welcome back!" + "As usual user, you get benefit of 5% discount!");
                }
                logger.info("Please pay the parking fare: {}€", ticket.getPrice());
                logger.info("Recorded out-time for vehicle number: {} / Out-time The: {} at {}", ticket.getVehicleRegNumber(), outTime.toLocalDate(), outTime.toLocalTime());
            }
            else{
               logger.info("Unable to update ticket information. Error occurred");
            }
        }
        catch(Exception e){
            logger.error("Unable to process exiting vehicle",e);
        }
    }
}