package com.parkit.parkingsystem.service;

import java.time.Duration;
import java.time.LocalDateTime;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.model.Ticket;

public class FareCalculatorService {

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
	                	ticket.setPrice(Fare.CAR_RATE_PER_HOUR);
	                }
	                else {
	                	ticket.setPrice(timeInHours* Fare.CAR_RATE_PER_HOUR);
	               }
	                break;
	            }
	            
	            case BIKE: {
	                if (timeInMinutes>=30 && timeInMinutes<=60 && timeInHours==0) {
	                	ticket.setPrice(Fare.BIKE_RATE_PER_HOUR);
	                }
	                else {
	                	ticket.setPrice(timeInHours * Fare.BIKE_RATE_PER_HOUR);
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
    
    //Cacul de l'utilisateur reccurent
    public boolean CheckIfVehiculeComeMoreThanOnce() {
    /*	if () {
    	ticket.setPrice(Fare.CAR_RATE_PER_HOUR);
    	}	*/
    	return true;
    }
}