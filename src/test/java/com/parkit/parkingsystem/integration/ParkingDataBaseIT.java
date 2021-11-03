package com.parkit.parkingsystem.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
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

    private static DataBaseTestConfig dataBaseTestConfig = new DataBaseTestConfig();
    private static ParkingSpotDAO parkingSpotDAO;
    private static TicketDAO ticketDAO;
    private static DataBasePrepareService dataBasePrepareService;
    
    LocalDateTime inTime;
    LocalDateTime outTime;
    
    @Mock
    private static InputReaderUtil inputReaderUtil;
    private ParkingSpot parkingSpot;
    private static Ticket ticket; 
    
    @BeforeAll
    private static void setUp() throws Exception{
        parkingSpotDAO = new ParkingSpotDAO();
        parkingSpotDAO.dataBaseConfig = dataBaseTestConfig;
        ticketDAO = new TicketDAO();
        ticketDAO.dataBaseConfig = dataBaseTestConfig;
        dataBasePrepareService = new DataBasePrepareService();
	    ticket = new Ticket();
    }

    @BeforeEach
    private void setUpPerTest() throws Exception {
    	
		inTime = LocalDateTime.now();
		outTime = LocalDateTime.now().plusHours(3);
        
        when(inputReaderUtil.readSelection()).thenReturn(1);
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
        		
		ticket.setInTime(inTime);
		ticket.setOutTime(outTime);
		
		dataBasePrepareService.clearDataBaseEntries();
    }

	@AfterAll
    private static void tearDown(){

    }
	@Disabled
	@DisplayName("NextAvailableSlotCheck") //Check parking availability in database via parkingNumber and parkingType
	@Test
	void testIfNextSlotIsAvailable() {

		// ARRANGE
    	// ACT
    	parkingSpot = new ParkingSpot(1,null,false);
    	parkingSpot.setId(parkingSpot.getId());
    	parkingSpot.setAvailable(parkingSpot.isAvailable());
    	parkingSpot.setParkingType(ParkingType.CAR);
    	//ASSERT
		assertEquals(1,parkingSpot.getId());
		assertEquals(false,parkingSpot.isAvailable());
		assertEquals(ParkingType.CAR,parkingSpot.getParkingType());
	}

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