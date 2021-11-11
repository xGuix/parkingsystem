package com.parkit.parkingsystem.service;

import java.text.DecimalFormat;
import java.time.Duration;
import java.time.LocalDateTime;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.model.Ticket;


public class FareCalculatorService {

	private TicketDAO ticketDAO = new TicketDAO();
	
	public void setTicketDAO(TicketDAO ticketDAO) {
        this.ticketDAO = ticketDAO;
    }
	
    public void calculateFare(Ticket ticket){
        if( (ticket.getOutTime()== null) || (ticket.getOutTime().isBefore(ticket.getInTime())) ){
            throw new IllegalArgumentException("Out time provided is incorrect:"+ticket.getOutTime().toString());
        }
        //Récupération de la durée in vs out
        Duration parkTime = getParkDurationInHour(ticket);
        double timeInMinutes = parkTime.toMinutes();
        
        DecimalFormat roundDec = new DecimalFormat("#.##"); 

		if (parkTime.toMinutes()<30) {
        	ticket.setPrice(0);
        }
        else{
	        switch (ticket.getParkingSpot().getParkingType()){

	        	case CAR: {
	        		ticket.setPrice(Double.valueOf(roundDec.format(timeInMinutes/60 * (Fare.CAR_RATE_PER_HOUR * calculateFareForReccurentUser(ticket)))));
	               }
	                break;
	            
	            case BIKE: {
	        		ticket.setPrice(Double.valueOf(roundDec.format(timeInMinutes/60 * (Fare.BIKE_RATE_PER_HOUR * calculateFareForReccurentUser(ticket)))));
				    }
	                break;
	            
	            default: throw new IllegalArgumentException("Unkown Parking Type");
	        }
        }
    }
   
    public Duration getParkDurationInHour(Ticket ticket) {
	    LocalDateTime inHour = ticket.getInTime();
	    LocalDateTime outHour = ticket.getOutTime();
	    return Duration.between(inHour,outHour);
    }
    
    // Cacul de l'utilisateur reccurent renvoi 1 ou 0.95
    public Double calculateFareForReccurentUser(Ticket ticket) {

    	Double result = 0.0;
    	try {
			if(ticketDAO.getIfRecurrentUser(ticket.getVehicleRegNumber())) {
				result = 0.95;
			}
			else {
				result = 1.0;
			}
		} 
    	catch (Exception e) {
			e.printStackTrace();
		}
		return result;
    }
}