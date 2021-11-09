package com.parkit.parkingsystem;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.text.DecimalFormat;
import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.FareCalculatorService;

@ExtendWith(MockitoExtension.class)
class FareCalculatorServiceTest {

    private FareCalculatorService fareCalculatorService;
    private Ticket ticket;
        
    @Mock
    private TicketDAO ticketDAO;
  
    @BeforeEach
    void setUpPerTest() {
        ticket = new Ticket();
        fareCalculatorService = new FareCalculatorService();
        fareCalculatorService.setTicketDAO(ticketDAO);
    }

    @Test
    void calculateFareCar(){
    	
        ticket.setInTime(LocalDateTime.now().minusMinutes(60));
        ticket.setOutTime(LocalDateTime.now());
    	ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR,false);
    	ticket.setParkingSpot(parkingSpot);
    	when(ticketDAO.getIfRecurrentUser(ticket.getVehicleRegNumber())).thenReturn(false);
        fareCalculatorService.calculateFare(ticket);
        
        assertEquals(Fare.CAR_RATE_PER_HOUR,ticket.getPrice());
    }

    @Test
    void calculateFareBike(){
    	
        ticket.setInTime(LocalDateTime.now().minusMinutes(60));
        ticket.setOutTime(LocalDateTime.now());
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE,false);
        ticket.setParkingSpot(parkingSpot);
        
        when(ticketDAO.getIfRecurrentUser(ticket.getVehicleRegNumber())).thenReturn(false);
        fareCalculatorService.calculateFare(ticket);
        
        assertEquals(Fare.BIKE_RATE_PER_HOUR,ticket.getPrice());
    }

    @Test
    void calculateFareUnkownType(){
    	
        ticket.setInTime(LocalDateTime.now());
        ticket.setOutTime(LocalDateTime.now().plusMinutes(45));
        ParkingSpot parkingSpot = new ParkingSpot(1, null,false);
        ticket.setParkingSpot(parkingSpot);
        
        assertThrows(NullPointerException.class, () -> fareCalculatorService.calculateFare(ticket));
    }
    
    @Test
    void calculateFareBikeWithFutureInTime(){
    	
    	ticket.setInTime(LocalDateTime.MAX);
    	ticket.setOutTime(LocalDateTime.MIN);
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE,false);
        ticket.setParkingSpot(parkingSpot);
        
        assertThrows(IllegalArgumentException.class, () -> fareCalculatorService.calculateFare(ticket));
    }

    @Test
    void calculateFareBikeWithLessThanHalfAnHourParkingTime(){

        ticket.setInTime(LocalDateTime.now().minusMinutes(29));
        ticket.setOutTime(LocalDateTime.now());
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE,false);
        ticket.setParkingSpot(parkingSpot);
        
        fareCalculatorService.calculateFare(ticket);
        
        assertEquals(0, ticket.getPrice());
    }

    @Test
    void calculateFareCarWithLessThanHalfAnHourParkingTime(){
    	
        ticket.setInTime(LocalDateTime.now().minusMinutes(29));
        ticket.setOutTime(LocalDateTime.now());
    	ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR,false);
        ticket.setParkingSpot(parkingSpot);
        
        fareCalculatorService.calculateFare(ticket);
        
        assertEquals(0, ticket.getPrice());
    }

    @Test
    void calculateFareCarWithMoreThanADayParkingTime() {

        ticket.setInTime(LocalDateTime.now());
        ticket.setOutTime(LocalDateTime.now().plusHours(48));
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR,false);
        ticket.setParkingSpot(parkingSpot);
        
        fareCalculatorService.calculateFare(ticket);
        
        assertEquals(48*Fare.CAR_RATE_PER_HOUR, ticket.getPrice());
    }
    
    @Test
    void calculateFareBikeWithMoreThanADayParkingTime() {

        ticket.setInTime(LocalDateTime.now());
        ticket.setOutTime(LocalDateTime.now().plusHours(48));
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE,false);
        ticket.setParkingSpot(parkingSpot);
        
        fareCalculatorService.calculateFare(ticket);
        
        assertEquals(48*Fare.BIKE_RATE_PER_HOUR, ticket.getPrice());
    }
    
    @Test
    void CalculateFareCarIfRecurrentUser(){
	    // ARRANGE
    	DecimalFormat roundDec = new DecimalFormat("#.##");
    	ticket.setInTime(LocalDateTime.now());
    	ticket.setOutTime(LocalDateTime.now().plusMinutes(60));
    	ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR,false);
    	ticket.setParkingSpot(parkingSpot);
	    // ACT
    	when(ticketDAO.getIfRecurrentUser(ticket.getVehicleRegNumber())).thenReturn(true);
    	fareCalculatorService.calculateFare(ticket);
	    // ASSERT
		assertEquals(Double.valueOf(roundDec.format(0.95*Fare.CAR_RATE_PER_HOUR)),ticket.getPrice());
    }
    
    @Test
    void CalculateFareBikeIfRecurrentUser(){
	    // ARRANGE
    	DecimalFormat roundDec = new DecimalFormat("#.##");
    	ticket.setInTime(LocalDateTime.now());
    	ticket.setOutTime(LocalDateTime.now().plusMinutes(60));
    	ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE,false);
    	ticket.setParkingSpot(parkingSpot);
	    // ACT
    	when(ticketDAO.getIfRecurrentUser(ticket.getVehicleRegNumber())).thenReturn(true);
    	fareCalculatorService.calculateFare(ticket);
	    // ASSERT
		assertEquals(Double.valueOf(roundDec.format(0.95*Fare.BIKE_RATE_PER_HOUR)),ticket.getPrice());
    }
    
    @Test
    void calculateFareForReccurentCarWithADayParkingTime() {

    	DecimalFormat roundDec = new DecimalFormat("#.##");
        ticket.setInTime(LocalDateTime.now());
        ticket.setOutTime(LocalDateTime.now().plusHours(24));
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR,false);
        ticket.setParkingSpot(parkingSpot);
        
        when(ticketDAO.getIfRecurrentUser(ticket.getVehicleRegNumber())).thenReturn(true);
        fareCalculatorService.calculateFare(ticket);
        
        assertEquals(Double.valueOf(roundDec.format(0.95*Fare.CAR_RATE_PER_HOUR*24)), ticket.getPrice());
    }
    
    @Test
    void calculateFareForReccurentBikeWithADayParkingTime() {
    	
    	DecimalFormat roundDec = new DecimalFormat("#.##");
        ticket.setInTime(LocalDateTime.now());
        ticket.setOutTime(LocalDateTime.now().plusHours(24));
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE,false);
        ticket.setParkingSpot(parkingSpot);
        
        when(ticketDAO.getIfRecurrentUser(ticket.getVehicleRegNumber())).thenReturn(true);
        fareCalculatorService.calculateFare(ticket);
        
        assertEquals(Double.valueOf(roundDec.format(0.95*Fare.BIKE_RATE_PER_HOUR*24)), ticket.getPrice());
    }

    @Test
    void checkIfRecurrentUserSendTrue(){
        // ARRANGE
        // ACT
        when(ticketDAO.getIfRecurrentUser(ticket.getVehicleRegNumber())).thenReturn(true);
        // ASSERT
        assertEquals(0.95, fareCalculatorService.calculateFareForReccurentUser(ticket));
    }
    
    @Test
    void checkIfRecurrentUserSendFalse(){
        // ARRANGE
        // ACT
        when(ticketDAO.getIfRecurrentUser(ticket.getVehicleRegNumber())).thenReturn(false);
        // ASSERT
        assertEquals(1.0, fareCalculatorService.calculateFareForReccurentUser(ticket));
    }
    
    @Disabled
    @Test 
    void checkIfIllegalExceptionIsSend(){
    	when(ticket.getParkingSpot().getParkingType()).thenThrow(Exception.class);
    	assertThrows(Exception.class, () -> fareCalculatorService.calculateFare(ticket));
    }
}
