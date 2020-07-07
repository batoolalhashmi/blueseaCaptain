package com.barmej.blueseacaptain.domain.entity;

import java.io.Serializable;

public class Captain implements Serializable {
    private String id;
    private String email;
    private String status;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Captain() {

    }

    public enum Status {
        FREE,
        ON_TRIP
    }
}
