package com.parkit.parkingsystem.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.parkit.parkingsystem.config.DataBaseConfig;
import com.parkit.parkingsystem.constants.DBConstants;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;

public class TicketDAO {

    private static final Logger logger = LogManager.getLogger(TicketDAO.class);
    
    public DataBaseConfig dataBaseConfig = new DataBaseConfig();

	public boolean saveTicket(Ticket ticket){
    	
        Connection con = null;
        try {
            con = dataBaseConfig.getConnection();
            PreparedStatement ps = con.prepareStatement(DBConstants.SAVE_TICKET);
            //ID, PARKING_NUMBER, VEHICLE_REG_NUMBER, PRICE, IN_TIME, OUT_TIME)
            ps.setInt(1,ticket.getParkingSpot().getId());
            ps.setString(2, ticket.getVehicleRegNumber());
            ps.setDouble(3, ticket.getPrice());
            ps.setTimestamp(4, Timestamp.valueOf(ticket.getInTime()));
            ps.setTimestamp(5, (ticket.getOutTime() == null)?null: Timestamp.valueOf(ticket.getOutTime()));
            ps.setBoolean(6, ticket.getRecurrentUser());
            return ps.execute();
        }
        catch (Exception ex){
            logger.error("Error fetching next available slot",ex);
        }
		finally {
			dataBaseConfig.closeConnection(con);
		}
        return false;	
    }
    
	public Ticket getTicket(String vehicleRegNumber) {
    	
        Connection con = null;
        Ticket ticket = null;
        try {
            con = dataBaseConfig.getConnection();
            PreparedStatement ps = con.prepareStatement(DBConstants.GET_TICKET);
            //ID, PARKING_NUMBER, VEHICLE_REG_NUMBER, PRICE, IN_TIME, OUT_TIME)
            ps.setString(1,vehicleRegNumber);
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                ticket = new Ticket();
                ParkingSpot parkingSpot = new ParkingSpot(rs.getInt(1), ParkingType.valueOf(rs.getString(7)),false);
                ticket.setParkingSpot(parkingSpot);
                ticket.setId(rs.getInt(2));
                ticket.setVehicleRegNumber(vehicleRegNumber);
                ticket.setPrice(rs.getDouble(3));              
				ticket.setInTime(rs.getTimestamp(4).toLocalDateTime());
				ticket.setOutTime((rs.getTimestamp(5) == null) ? null : rs.getTimestamp(5).toLocalDateTime());
				ticket.setRecurrentUser(rs.getBoolean(6));
            }
            dataBaseConfig.closeResultSet(rs);
            dataBaseConfig.closePreparedStatement(ps);
        }
        catch (Exception ex){
            logger.error("Error fetching next available slot",ex);
        }
        finally {
            dataBaseConfig.closeConnection(con);
        }
        return ticket;
    }
    
    // Methode public boolean renvoi True si le VehicleRegNumber à un ticket en BDD
    // via BDConstants.CHECK_IF_VEHICLE_ALREADY_COME
	public boolean getIfRecurrentUser(String vehicleRegNumber) {
    	
    	Connection con = null;
        try {
            con = dataBaseConfig.getConnection();
            PreparedStatement ps = con.prepareStatement(DBConstants.CHECK_IF_VEHICLE_ALREADY_COME);
            ps.setString(1,vehicleRegNumber);
            ResultSet rs = ps.executeQuery();
			if(rs.isLast()){
            	return true;
            }
			dataBaseConfig.closeResultSet(rs);
            dataBaseConfig.closePreparedStatement(ps);
        }
        catch (Exception ex){
            logger.error("Error fetching to get Ticket",ex);
        }
        finally {
            dataBaseConfig.closeConnection(con);
        }
		return false;
    }
    
    public boolean updateTicket(Ticket ticket) {
    	
        Connection con = null;
        try {
            con = dataBaseConfig.getConnection();
            PreparedStatement ps = con.prepareStatement(DBConstants.UPDATE_TICKET);
            ps.setDouble(1, ticket.getPrice());
            ps.setTimestamp(2, Timestamp.valueOf(ticket.getOutTime()));
            ps.setInt(3,ticket.getId());
            ps.execute();
            return true;
        }
        catch (Exception ex){
            logger.error("Error saving ticket info",ex);
        }
        finally {
            dataBaseConfig.closeConnection(con);
        }
        return false;
    }
}