package com.parkit.parkingsystem;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
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

    private static FareCalculatorService fareCalculatorService;
    private Ticket ticket;
        
    @Mock
    private TicketDAO ticketDAO;

    @BeforeAll
    private static void setUp() {
        fareCalculatorService = new FareCalculatorService();
    }

    @BeforeEach
    void setUpPerTest() {
        ticket = new Ticket();
        fareCalculatorService.setTicketDAO(ticketDAO);
    }

    @Test
    void calculateFareCar(){
    	
        ticket.setInTime(LocalDateTime.now().minusMinutes(59));
        ticket.setOutTime(LocalDateTime.now());
    	ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR,false);
    	ticket.setParkingSpot(parkingSpot);
    	
    	when(ticketDAO.getIfRecurrentUser(ticket.getVehicleRegNumber())).thenReturn(false);
        fareCalculatorService.calculateFare(ticket);
        
        assertEquals(Fare.CAR_RATE_PER_HOUR,ticket.getPrice());
    }

    @Test
    void calculateFareBike(){
    	
        ticket.setInTime(LocalDateTime.now().minusMinutes(59));
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
    	ticket.setInTime(LocalDateTime.now());
    	ticket.setOutTime(LocalDateTime.now().plusMinutes(45));
    	ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR,false);
    	ticket.setParkingSpot(parkingSpot);
	    // ACT
    	when(ticketDAO.getIfRecurrentUser(ticket.getVehicleRegNumber())).thenReturn(true);
    	fareCalculatorService.calculateFare(ticket);
	    // ASSERT
		assertEquals(0.95*Fare.CAR_RATE_PER_HOUR,ticket.getPrice());
    }
    
    @Test
    void CalculateFareBikeIfRecurrentUser(){
	    // ARRANGE
    	ticket.setInTime(LocalDateTime.now());
    	ticket.setOutTime(LocalDateTime.now().plusMinutes(45));
    	ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE,false);
    	ticket.setParkingSpot(parkingSpot);
	    // ACT
    	when(ticketDAO.getIfRecurrentUser(ticket.getVehicleRegNumber())).thenReturn(true);
    	fareCalculatorService.calculateFare(ticket);
	    // ASSERT
		assertEquals(0.95*Fare.BIKE_RATE_PER_HOUR,ticket.getPrice());
    }
    
    @Test
    void calculateFareForReccurentCarWithMoreThanADayParkingTime() {

        ticket.setInTime(LocalDateTime.now());
        ticket.setOutTime(LocalDateTime.now().plusHours(48));
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR,false);
        ticket.setParkingSpot(parkingSpot);
        
        when(ticketDAO.getIfRecurrentUser(ticket.getVehicleRegNumber())).thenReturn(true);
        fareCalculatorService.calculateFare(ticket);
        
        assertEquals(0.95 * (48*Fare.CAR_RATE_PER_HOUR), ticket.getPrice());
    }
    
    @Test
    void calculateFareForReccurentBikeWithMoreThanADayParkingTime() {

        ticket.setInTime(LocalDateTime.now());
        ticket.setOutTime(LocalDateTime.now().plusHours(48));
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE,false);
        ticket.setParkingSpot(parkingSpot);
        
        when(ticketDAO.getIfRecurrentUser(ticket.getVehicleRegNumber())).thenReturn(true);
        fareCalculatorService.calculateFare(ticket);
        
        assertEquals(0.95 * (48*Fare.BIKE_RATE_PER_HOUR), ticket.getPrice());
    }
}
