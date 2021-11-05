package com.parkit.parkingsystem.integration;

import static org.mockito.Mockito.when;

import java.time.LocalDateTime;

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

    private static DataBaseTestConfig dataBaseTestConfig = new DataBaseTestConfig();
    private static ParkingSpotDAO parkingSpotDAO;
    private static TicketDAO ticketDAO;
    private static DataBasePrepareService dataBasePrepareService;
    
    //LocalDateTime inTime;
    //LocalDateTime outTime;
    
    @Mock
    private static InputReaderUtil inputReaderUtil;
    /* @Mock
    private ParkingSpot parkingSpot;
    @Mock
    private static Ticket ticket; */
    
    @BeforeAll
    private static void setUp() throws Exception{
        parkingSpotDAO = new ParkingSpotDAO();
        parkingSpotDAO.dataBaseConfig = dataBaseTestConfig;
        ticketDAO = new TicketDAO();
        ticketDAO.dataBaseConfig = dataBaseTestConfig;
        dataBasePrepareService = new DataBasePrepareService();
	   // Ticket ticket = new Ticket();
    }

    @BeforeEach
    private void setUpPerTest() throws Exception {
    	
    	dataBasePrepareService.clearDataBaseEntries();
    	
		//inTime = LocalDateTime.now();
		//outTime = LocalDateTime.now().plusHours(3);
        
        when(inputReaderUtil.readSelection()).thenReturn(1);
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
        		
		Ticket ticket = new Ticket();
		ticket.setInTime(LocalDateTime.now());
		//ticket.setOutTime(outTime);
		
    }

	@AfterAll
    private static void tearDown(){

    }
	/*
	@Disabled
	@DisplayName("NextAvailableSlotCheck") //Check parking availability in database via parkingNumber and parkingType
	@Test
	void testIfNextSlotIsAvailable() {
		// ARRANGE
    	// ACT
    	parkingSpot = new ParkingSpot(1,ParkingType.CAR,false);
    	parkingSpot.setId(parkingSpot.getId());
    	parkingSpot.setParkingType(ParkingType.CAR);
    	parkingSpot.setAvailable(parkingSpot.isAvailable());
    	//ASSERT
		assertEquals(false,parkingSpot.isAvailable());
	}*/

	@Test
    void testParkingACar() throws Exception{
    	// ARRANGE
        // ACT
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        parkingService.processIncomingVehicle();
        // ASSERT
    	// assertEquals(parkingSpot.getId(), equals(false));
		// assertEquals(parkingSpot.getParkingType(), equals(parkingSpotDAO));

        // TODO: check that a ticket is actualy saved in DB and Parking table is updated with availability       
    }

    @Test	
    void testParkingLotExit() throws Exception{
    	
        //testParkingACar();
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        parkingService.processIncomingVehicle();
        parkingService.processExitingVehicle();
        //TODO: check that the fare generated and out time are populated correctly in the database
    }
}