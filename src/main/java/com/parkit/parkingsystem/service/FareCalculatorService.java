package com.parkit.parkingsystem.service;

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
	                	ticket.setPrice(timeInHours * (Fare.CAR_RATE_PER_HOUR * calculateFareForReccurentUser())+1.5);
	               }
	                break;
	            }
	            
	            case BIKE: {
					if (timeInMinutes>=30 && timeInMinutes<=60 && timeInHours==0) {
				    	ticket.setPrice(Fare.BIKE_RATE_PER_HOUR * calculateFareForReccurentUser());
				    }
					else {
						ticket.setPrice(timeInHours * (Fare.BIKE_RATE_PER_HOUR * calculateFareForReccurentUser())+1.0);
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
    
    //Cacul de l'utilisateur reccurent renvoi 1 ou 0.95
    public Double calculateFareForReccurentUser() {
    	Ticket ticket = new Ticket();
    	String vehicleRegNumber = ticket.getVehicleRegNumber();
    	Double result = (double) 0;
    	try {
			if(ticketDAO.getIfRecurrentUser(vehicleRegNumber)) {
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