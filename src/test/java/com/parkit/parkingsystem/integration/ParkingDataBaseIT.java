package com.parkit.parkingsystem.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
import com.parkit.parkingsystem.integration.service.DataBasePrepareService;
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
    }

    @BeforeEach
    private void setUpPerTest() throws Exception {
    	dataBasePrepareService.clearDataBaseEntries();
        when(inputReaderUtil.readSelection()).thenReturn(1);
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
    }

	@AfterAll
    private static void tearDown(){

    }
	
	@Test
    void testParkingACar() throws Exception{
    	// GIVEN
		parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
		//ticket.setParkingSpot(new ParkingSpot(1, ParkingType.CAR, false));
		//ticket.setVehicleRegNumber("ABCEDF");
		//ticket.setInTime(LocalDateTime.now().minusMinutes(45));
		//ticket.setOutTime(LocalDateTime.now());
		//ticket.setPrice(1.5);
        // WHEN
		int nextAvailableSlot = parkingService.getNextParkingNumberIfAvailable().getId();
		parkingService.processIncomingVehicle();
		ticket = ticketDAO.getTicket("ABCDEF");
		// THEN
        assertEquals(true, ticket != null);
        assertEquals(nextAvailableSlot + 1, parkingService.getNextParkingNumberIfAvailable().getId());
        assertEquals(ticket.getVehicleRegNumber(), ticketDAO.getTicket(ticket.getVehicleRegNumber()).getVehicleRegNumber());
	
        // TODO: check that a ticket is actualy saved in DB and Parking table is updated with availability

}
		
    @Test	
    void testParkingLotExit() throws Exception{
    	
    	// ARRANGE
    	parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
    	parkingService.processIncomingVehicle();
        // ACT
    	parkingService.processExitingVehicle();
        // ASSERT
        //assertEquals(true, ticket.getPrice() >= 0);
        //assertThat(ticket.getParkingSpot().getId()).isEqualTo(1);
        //assertNotNull(ticket.getOutTime());
        
        // TODO: check that the fare generated and out time are populated correctly in the database
    }
}