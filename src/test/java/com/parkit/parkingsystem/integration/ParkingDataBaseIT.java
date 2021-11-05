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

    private static DataBaseTestConfig dataBaseTestConfig = new DataBaseTestConfig();
    private static ParkingSpotDAO parkingSpotDAO;
    private static TicketDAO ticketDAO;
    private static DataBasePrepareService dataBasePrepareService;
    
    LocalDateTime inTime= LocalDateTime.now();;
    LocalDateTime outTime = LocalDateTime.now().plusHours(3);
    
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
    }

    @BeforeEach
    private void setUpPerTest() throws Exception {
    	   	        
        when(inputReaderUtil.readSelection()).thenReturn(1);
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
        		
		//Ticket ticket = new Ticket();
		//ticket.setInTime(inTime);
		//ticket.setOutTime(outTime);
		
		dataBasePrepareService.clearDataBaseEntries();
		
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
    	
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        parkingService.processIncomingVehicle();
        parkingService.processExitingVehicle();
        //TODO: check that the fare generated and out time are populated correctly in the database
    }
}