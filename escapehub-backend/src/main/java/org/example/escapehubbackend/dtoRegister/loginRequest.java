package org.example.escapehubbackend.dtoRegister;

public class loginRequest {


    private String email;
    private String pass;


    public loginRequest(){}
    public loginRequest(String email, String pass){
        this.email=email;
        this.pass=email;
    }

    public String getPass() {
        return pass;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPass(String pass) {
        this.pass = pass;
    }

    public String getEmail() {
        return email;
    }



}
