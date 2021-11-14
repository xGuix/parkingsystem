package com.parkit.parkingsystem.constants;

import groovy.transform.Generated;

@Generated
public class DBConstants {

    public static final String GET_NEXT_PARKING_SPOT = "select min(PARKING_NUMBER) from parking where AVAILABLE = true and TYPE = ?";
    public static final String UPDATE_PARKING_SPOT = "update parking set available = ? where PARKING_NUMBER = ?";

    public static final String SAVE_TICKET = "insert into ticket(PARKING_NUMBER, VEHICLE_REG_NUMBER, PRICE, IN_TIME, OUT_TIME, RECURRENT_USER) values(?,?,?,?,?,?)";
    public static final String UPDATE_TICKET = "update ticket set PRICE=?, OUT_TIME=? where ID=?";
    public static final String GET_TICKET = "select t.PARKING_NUMBER, t.ID, t.PRICE, t.IN_TIME, t.OUT_TIME, t.RECURRENT_USER, p.TYPE from ticket t,parking p where p.parking_number = t.parking_number and t.VEHICLE_REG_NUMBER=? and OUT_TIME is null order by t.IN_TIME limit 1";
    
    public static final String CHECK_IF_VEHICLE_ALREADY_COME = "SELECT * from TICKET where VEHICLE_REG_NUMBER = ? and OUT_TIME IS NOT NULL";
    public static final String CHECK_IF_VEHICLE_ALREADY_IN  = "SELECT * from TICKET where VEHICLE_REG_NUMBER = ? and OUT_TIME IS NULL limit 1";
}
