package com.parkit.parkingsystem;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ParkingServiceTest {

    private static ParkingService parkingService;
    private static Ticket ticket;

    @Mock
    private static InputReaderUtil inputReaderUtil;
    @Mock
    private static ParkingSpotDAO parkingSpotDAO;
    @Mock
    private static TicketDAO ticketDAO;

    @BeforeEach
    private void setUpPerTest() {
        try {
            when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");

            ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR,false);
            ticket = new Ticket();
            ticket.setInTime(LocalDateTime.now().minusMinutes(60));
            ticket.setParkingSpot(parkingSpot);
            ticket.setVehicleRegNumber("ABCDEF");

            when(parkingSpotDAO.updateParking(any(ParkingSpot.class))).thenReturn(true);

            parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        } catch (Exception e) {
            e.printStackTrace();
            throw  new RuntimeException("Failed to set up test mock objects");
        }
    }
       
	//Check parking availability in database for Car & Bike via parkingNumber and parkingType
    //Check ifRecurrentUser
	@Test
	void processIncomingVehicleTest() {
		// GIVEN
		when(inputReaderUtil.readSelection()).thenReturn(1);
		when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
    	when(ticketDAO.getIfRecurrentUser(ticket.getVehicleRegNumber())).thenReturn(true);
		when(parkingSpotDAO.getNextAvailableSlot(any(ParkingType.class))).thenReturn(1);
		// WHEN
        parkingService.processIncomingVehicle();
    	// THEN
		verify(inputReaderUtil, Mockito.times(1)).readSelection();
		verify(parkingSpotDAO, Mockito.times(1)).updateParking(any(ParkingSpot.class));
		verify(parkingSpotDAO, Mockito.atLeast(1)).getNextAvailableSlot(ParkingType.CAR);
		verify(parkingSpotDAO, Mockito.atMost(2)).getNextAvailableSlot(ParkingType.BIKE);
		verify(parkingSpotDAO,Mockito.times(1)).checkIfUserAlreadyIn(anyString());
		verify(ticketDAO, Mockito.times(1)).saveTicket(any(Ticket.class));
	}

	@Test 
	void testErrorIfAVehiculeIsAlreadyInPark(){
		// GIVEN
		when(inputReaderUtil.readSelection()).thenReturn(1);
		when(parkingSpotDAO.getNextAvailableSlot(any(ParkingType.class))).thenReturn(1);
		when(parkingSpotDAO.checkIfUserAlreadyIn(anyString())).thenReturn(true);
		// WHEN
		parkingService.processIncomingVehicle();

		// THEN
		verify(inputReaderUtil, Mockito.times(1)).readSelection();
		verify(parkingSpotDAO, Mockito.times(1)).getNextAvailableSlot(ParkingType.CAR);
		verify(parkingSpotDAO,Mockito.times(1)).checkIfUserAlreadyIn("ABCDEF");
	}
	
	@Test 
	void testErrorIfParkingIsFull(){
		// GIVEN
		when(inputReaderUtil.readSelection()).thenReturn(1);
		when(parkingSpotDAO.getNextAvailableSlot(any(ParkingType.class))).thenReturn(0);
		// WHEN
		parkingService.processIncomingVehicle();
		// THEN
		verify(inputReaderUtil, Mockito.times(1)).readSelection();
		verify(parkingSpotDAO, Mockito.times(1)).getNextAvailableSlot(ParkingType.CAR);
	}
	
	@Test 
	void testErrorIfParkingTypeIsInvalide(){
		// GIVEN
		when(inputReaderUtil.readSelection()).thenReturn(2);
		when(parkingSpotDAO.getNextAvailableSlot(any(ParkingType.class))).thenReturn(3);
		// WHEN
		parkingService.processIncomingVehicle();
		// THEN
		verify(inputReaderUtil, Mockito.times(1)).readSelection();
		verify(parkingSpotDAO, Mockito.times(1)).getNextAvailableSlot(ParkingType.BIKE);
	}
   
    @Test
    void processExitingVehicleTest() throws InterruptedException {
    	// GIVEN
    	when(ticketDAO.getTicket(anyString())).thenReturn(ticket);
        when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(true);
    	// WHEN
		parkingService.processExitingVehicle();
		// THEN
		verify(parkingSpotDAO, Mockito.times(1)).updateParking(any(ParkingSpot.class));
        verify(ticketDAO, Mockito.times(1)).getTicket(anyString());
        verify(ticketDAO, Mockito.times(1)).updateTicket(any(Ticket.class));
    }

    @Test
    void processExitingVehicleAsRecurrentUserTest() {
    	// GIVEN
    	when(ticketDAO.getTicket(anyString())).thenReturn(ticket);
        when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(true);
    	// WHEN
        ticket.setRecurrentUser(true);
		parkingService.processExitingVehicle();

		// THEN
        verify(ticketDAO, Mockito.times(1)).updateTicket(any(Ticket.class));
	    assertEquals(true, ticket.getRecurrentUser());
    }
    
    @Test
    void processExitingVehicleUpdateTicketFailed() {
    	// GIVEN
    	when(ticketDAO.getTicket(anyString())).thenReturn(ticket);
        when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(false);
    	// WHEN
		parkingService.processExitingVehicle();
		// THEN
        verify(ticketDAO, Mockito.times(1)).updateTicket(any(Ticket.class));
    }
}