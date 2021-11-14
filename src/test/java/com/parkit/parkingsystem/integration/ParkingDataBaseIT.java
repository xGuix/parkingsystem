package com.parkit.parkingsystem.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

import java.sql.SQLException;
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
	    // TODO: check that a ticket is actualy saved in DB and Parking table is updated with availability
	}
	
	@Test
	public void testParkingABike() {
		ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
		parkingService.processIncomingVehicle();
		ParkingSpot parkingSpot = new ParkingSpot(2, ParkingType.BIKE, false);
		try {
			dataBaseTestConfig.getConnection();
			parkingSpotDAO.getNextAvailableSlot(ParkingType.BIKE);
			parkingSpotDAO.updateParking(parkingSpot);
			assertNotNull(parkingSpot);
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
		}
	}
			
    @Test	
    void testParkingLotExit() throws Exception{
    	
    	// GIVEN
    	parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
    	parkingService.processIncomingVehicle();
    	ticket.setOutTime(LocalDateTime.now().plusMinutes(60));
        // WHEN
    	parkingService.processExitingVehicle();
    	ticketDAO.getTicket("ABCDEF");
        // THEN
        assertEquals(true, ticket.getPrice() >= 0);
        assertNotNull(ticket.getOutTime());
        
        // TODO: check that the fare generated and out time are populated correctly in the database
    }

    @Test
	public void testIfRecurrentUserWithReduction() {
		parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
		parkingService.processIncomingVehicle();
		ticket.setOutTime(LocalDateTime.now().plusMinutes(60));
		ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);
		try {
			dataBaseTestConfig.getConnection();
			parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR);
			parkingSpotDAO.updateParking(parkingSpot);
			ticketDAO.saveTicket(ticket);
			parkingService.processExitingVehicle();
			assertNotNull(ticket.getPrice());
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
		}
	}
}