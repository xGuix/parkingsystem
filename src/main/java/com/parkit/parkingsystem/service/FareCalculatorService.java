package com.parkit.parkingsystem.service;

import java.time.Duration;
import java.time.LocalDateTime;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.model.Ticket;

public class FareCalculatorService {

    public long getParkDurationInMinute (Ticket ticket) {
	    LocalDateTime inHour = ticket.getInTime();
	    LocalDateTime outHour = ticket.getOutTime();
	    Duration duration = Duration.between(inHour, outHour);
    return duration.toMinutes();
    }
	
    public void calculateFare(Ticket ticket){
        if( (ticket.getOutTime()== null) || (ticket.getOutTime().isBefore(ticket.getInTime())) ){
            throw new IllegalArgumentException("Out time provided is incorrect:"+ticket.getOutTime().toString());
        }
        
        switch (ticket.getParkingSpot().getParkingType()){
            case CAR: {
                ticket.setPrice(getParkDurationInMinute(ticket) * Fare.CAR_RATE_PER_HOUR);
                break;
            }
            case BIKE: {
                ticket.setPrice(getParkDurationInMinute(ticket) * Fare.BIKE_RATE_PER_HOUR);
                break;
            }
            default: throw new IllegalArgumentException("Unkown Parking Type");
        }
    } 
}