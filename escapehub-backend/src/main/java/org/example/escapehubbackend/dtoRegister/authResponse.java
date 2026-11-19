package org.example.escapehubbackend.dtoRegister;

public class authResponse {
    private String token;
    private String messege;

    public authResponse(String messege, String token) {
        this.messege = messege;
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getMessege() {
        return messege;
    }

    public void setMessege(String messege) {
        this.messege = messege;
    }
}
