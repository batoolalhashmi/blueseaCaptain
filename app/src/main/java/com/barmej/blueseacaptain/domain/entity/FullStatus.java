package com.barmej.blueseacaptain.domain.entity;

import java.io.Serializable;

public class FullStatus implements Serializable {
    private Captain captain;
    private Trip trip;


    public Captain getCaptain() {
        return captain;
    }

    public void setCaptain(Captain captain) {
        this.captain = captain;
    }

    public Trip getTrip() {
        return trip;
    }

    public void setTrip(Trip trip) {
        this.trip = trip;
    }
}
