package org.example.escapehubbackend.dtoRegister;

public class registerRequest {
    private String email;
    private String pass;
    private String name;

    public registerRequest() {}

    public registerRequest(String email, String pass, String name) {
        this.email = email;
        this.pass = pass;
        this.name = name;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPass() { return pass; }
    public void setPass(String pass) { this.pass = pass; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}