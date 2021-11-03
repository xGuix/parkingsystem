package com.parkit.parkingsystem.service;

import java.time.Duration;
import java.time.LocalDateTime;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.model.Ticket;


public class FareCalculatorService {

	private static TicketDAO ticketDAO = new TicketDAO();
	
	
    public void calculateFare(Ticket ticket){
        if( (ticket.getOutTime()== null) || (ticket.getOutTime().isBefore(ticket.getInTime())) ){
            throw new IllegalArgumentException("Out time provided is incorrect:"+ticket.getOutTime().toString());
        }
        //Récupération de la durée in vs out
        Duration parkTime = getParkDurationInHour(ticket);
        double timeInMinutes = parkTime.toMinutes();
        double timeInHours = parkTime.toHours();
        
        if (parkTime.toMinutes()<30) {
        	ticket.setPrice(0);
        }
        else{
        
	        switch (ticket.getParkingSpot().getParkingType()){
	            
	            case CAR: {
	            	if (timeInMinutes>=30 && timeInMinutes<=60 && timeInHours==0) {
	                	ticket.setPrice(Fare.CAR_RATE_PER_HOUR * calculateFareForReccurentUser());
	                }
	                else {
	                	
						ticket.setPrice((timeInHours * (Fare.CAR_RATE_PER_HOUR * calculateFareForReccurentUser()) + 1.5));
	               }
	                break;
	            }
	            
	            case BIKE: {
					if (timeInMinutes>=30 && timeInMinutes<=60 && timeInHours==0) {
				    	ticket.setPrice(Fare.BIKE_RATE_PER_HOUR * calculateFareForReccurentUser());
				    }
					else {
						ticket.setPrice((timeInHours * (Fare.BIKE_RATE_PER_HOUR * calculateFareForReccurentUser()) + 1));
					}
	                break;
	            }
	            default: throw new IllegalArgumentException("Unkown Parking Type");
	        }
        }
    }
   

    public Duration getParkDurationInHour(Ticket ticket) {
	    LocalDateTime inHour = ticket.getInTime();
	    LocalDateTime outHour = ticket.getOutTime();
	    return Duration.between(inHour,outHour);
    }
    
    //Cacul de l'utilisateur reccurent doit repondre si oui ou non 
    public Double calculateFareForReccurentUser() {
    	Double result = null ;
    	try {
			if(ticketDAO.getIfRecurrentUser()) {
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