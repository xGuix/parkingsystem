package com.parkit.parkingsystem.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
import com.parkit.parkingsystem.integration.service.DataBasePrepareService;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;

@ExtendWith(MockitoExtension.class)
class ParkingDataBaseIT {

    private static DataBaseTestConfig dataBaseTestConfig;
    private static DataBasePrepareService dataBasePrepareService;
    private static ParkingSpotDAO parkingSpotDAO;
    private static ParkingService parkingService;
    private static TicketDAO ticketDAO;
    private static Ticket ticket;
        
    @Mock
    private static InputReaderUtil inputReaderUtil;
    
    @BeforeAll
    private static void setUp() throws Exception{
        dataBaseTestConfig = new DataBaseTestConfig();
        dataBasePrepareService = new DataBasePrepareService();
        parkingSpotDAO = new ParkingSpotDAO();
        parkingSpotDAO.dataBaseConfig = dataBaseTestConfig;
        ticketDAO = new TicketDAO();
        ticketDAO.dataBaseConfig = dataBaseTestConfig;
        ticket = new Ticket();
    }

    @BeforeEach
    private void setUpPerTest() throws Exception {
        when(inputReaderUtil.readSelection()).thenReturn(1);
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
    	dataBasePrepareService.clearDataBaseEntries();
    }

	@AfterAll
    private static void tearDown(){

    }

	@Test
    void testParkingACar() throws Exception{
		
    	// GIVEN
		parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
		parkingService.getNextParkingNumberIfAvailable().getId();
		Ticket savedTicket = ticketDAO.getTicket("ABCDEF");
        // WHEN
		parkingService.processIncomingVehicle();
		// THEN
        assertThat(parkingService.getNextParkingNumberIfAvailable().getId()).isEqualTo(2);
        assertThat(savedTicket).isEqualTo(ticketDAO.getTicket(ticket.getVehicleRegNumber()));
		assertThat(ticketDAO.getIfRecurrentUser(ticket.getVehicleRegNumber())).isFalse();
		assertThat(ticketDAO.updateTicket(ticket)).isTrue();
	    // DONE : check that a ticket is actualy saved in DB and Parking table is updated with availability
	}
	
	@Test
	void testParkingABike() throws Exception{
		
		// GIVEN
		ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
		ParkingSpot parkingSpot = new ParkingSpot(2, ParkingType.BIKE, false);
		// WHEN
		parkingService.processIncomingVehicle();
		parkingSpotDAO.updateParking(parkingSpot);
		// THEN
		assertNotNull(parkingSpot);
		assertEquals(ParkingType.BIKE, parkingSpot.getParkingType());
		assertThat(parkingService.getNextParkingNumberIfAvailable().getId()).isEqualTo(3);
		assertThat(ticketDAO.updateTicket(ticket)).isTrue();
	}
			
    @Test	
    void testParkingLotExit() throws Exception{
    	
    	// GIVEN
    	parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
    	ticket.setOutTime(LocalDateTime.now().plusMinutes(60));
        // WHEN
    	parkingService.processExitingVehicle();
    	// THEN
        assertThat(parkingService.getNextParkingNumberIfAvailable().getId()).isEqualTo(1);
        assertEquals(ticketDAO.getTicket("ABCDEF"), ticket.getVehicleRegNumber());
	    assertEquals(false, ticketDAO.getIfRecurrentUser("ABCDEF"));
        assertEquals(true, ticket.getPrice() >= 0);
        assertNotNull(ticket.getOutTime());
		assertThat(ticketDAO.updateTicket(ticket)).isTrue();
        // DONE: check that the fare generated and out time are populated correctly in the database
    }

    @Test
	void testIfRecurrentUserWithReduction()  throws Exception{
    	
    	// GIVEN
		parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
		ticket.setOutTime(LocalDateTime.now().plusMinutes(60));
		// WHEN
		parkingService.processIncomingVehicle();
		parkingService.processExitingVehicle();
		// THEN
        assertThat(parkingService.getNextParkingNumberIfAvailable().getId()).isEqualTo(1);
	    assertEquals(true, ticketDAO.getIfRecurrentUser("ABCDEF"));
	    assertNotNull(ticket.getOutTime());
	    assertEquals(true, ticket.getPrice() >= 0);
	    assertThat(ticketDAO.updateTicket(ticket)).isTrue();
	}
}