package com.nathit.signinwithgoogle.model;

public class ModelUser {

    String email;
    String name;
    String status;

    public ModelUser() {
    }

    public ModelUser(String email, String name, String status) {    //กดปุ่มบนคีย์บอร์ด Alt+Ins -> Constructor
        this.email = email;
        this.name = name;
        this.status = status;
    }

    //กดปุ่มบนคีย์บอร์ด Alt+Ins -> Getter and Setter
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
