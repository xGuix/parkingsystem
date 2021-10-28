package com.parkit.parkingsystem;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.FareCalculatorService;

class FareCalculatorServiceTest {

    private static FareCalculatorService fareCalculatorService;
    private Ticket ticket;

    @BeforeAll
    private static void setUp() {
        fareCalculatorService = new FareCalculatorService();
    }

    @BeforeEach
    private void setUpPerTest() {
        ticket = new Ticket();
    }

    @Test
    void calculateFareCar(){
    	
        ticket.setInTime(LocalDateTime.now().minusMinutes(60));
        ticket.setOutTime(LocalDateTime.now());
    	ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR,false);

        ticket.setParkingSpot(parkingSpot);
        fareCalculatorService.calculateFare(ticket);
        assertEquals(Fare.CAR_RATE_PER_HOUR,ticket.getPrice());
    }

    @Test
    void calculateFareBike(){
    	
        ticket.setInTime(LocalDateTime.now().minusMinutes(60));
        ticket.setOutTime(LocalDateTime.now());
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE,false);

        ticket.setParkingSpot(parkingSpot);
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
    void calculateFareCarWithMoreThanADayParkingTime() throws InterruptedException{

        ticket.setInTime(LocalDateTime.now().minusHours(24));
        ticket.setOutTime(LocalDateTime.now());
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR,false);
        
        ticket.setParkingSpot(parkingSpot);
        fareCalculatorService.calculateFare(ticket);
        assertEquals(24*Fare.CAR_RATE_PER_HOUR, ticket.getPrice());
    }

}
