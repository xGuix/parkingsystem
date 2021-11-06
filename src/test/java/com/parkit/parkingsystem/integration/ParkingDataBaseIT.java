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
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;

@ExtendWith(MockitoExtension.class)
class ParkingDataBaseIT {

    private static DataBaseTestConfig dataBaseTestConfig;
    private static DataBasePrepareService dataBasePrepareService;
    private static ParkingSpotDAO parkingSpotDAO;
    private static TicketDAO ticketDAO;
    private static ParkingService parkingService;
    
    LocalDateTime inTime= LocalDateTime.now();;
    LocalDateTime outTime = LocalDateTime.now().plusHours(3);
    
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
	/*
	Check parking availability in database via parkingNumber and parkingType
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
		//parkingService.processIncomingVehicle();
        // ASSERT

		
        // TODO: check that a ticket is actualy saved in DB and Parking table is updated with availability
		 parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
	     parkingService.processIncomingVehicle();
    }

    @Test	
    void testParkingLotExit() throws Exception{
    	
    	// ARRANGE
		
        // ACT
		
        // ASSERT

    	
        // TODO: check that the fare generated and out time are populated correctly in the database
         parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
         parkingService.processIncomingVehicle();
         parkingService.processExitingVehicle();
    	
    }
}