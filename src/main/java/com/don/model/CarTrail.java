package com.don.model;

import com.don.telemetry.CarTelemetry;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class CarTrail {
    private String plate;
    private Double startLat;
    private Double startLong;
    private Double endLat;
    private Double endLong;
    private Double avgSpeed;


    public CarTrail(CarTelemetry first, CarTelemetry last, Double avgSpeed) {
        this.startLat = first.getLatitude();
        this.startLong = first.getLongitude();
        this.endLat = last.getLatitude();
        this.endLong = last.getLongitude();
        this.plate = first.getPlate();
        this.avgSpeed = avgSpeed;
    }


    public void getStatment(PreparedStatement ps) throws SQLException {
        ps.setString(1, this.plate);
        ps.setDouble(2, this.startLat);
        ps.setDouble(3, this.startLong);
        ps.setDouble(4, this.endLat);
        ps.setDouble(5, this.endLong);
        ps.setDouble(6, avgSpeed);
    }


    public CarTrail() {
    }

    public String getPlate() {
        return plate;
    }

    public Double getAvgSpeed() {
        return avgSpeed;
    }

    public void setAvgSpeed(Double avgSpeed) {
        this.avgSpeed = avgSpeed;
    }

    public void setPlate(String plate) {
        this.plate = plate;
    }

    public Double getStartLat() {
        return startLat;
    }

    public void setStartLat(Double startLat) {
        this.startLat = startLat;
    }

    public Double getStartLong() {
        return startLong;
    }

    public void setStartLong(Double startLong) {
        this.startLong = startLong;
    }

    public Double getEndLat() {
        return endLat;
    }

    public void setEndLat(Double endLat) {
        this.endLat = endLat;
    }

    public Double getEndLong() {
        return endLong;
    }

    public void setEndLong(Double endLong) {
        this.endLong = endLong;
    }
}
