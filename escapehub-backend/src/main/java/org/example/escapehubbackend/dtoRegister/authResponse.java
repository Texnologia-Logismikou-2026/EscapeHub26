package org.example.escapehubbackend.dtoRegister;

public class authResponse {
    private String token;
    private String messege;
    private Long userId;
    private String username;
    private String email;

    public authResponse(String messege, String token) {
        this.messege = messege;
        this.token = token;
    }

    public authResponse(String messege, String token, Long userId, String username, String email) {
        this.messege = messege;
        this.token = token;
        this.userId = userId;
        this.username = username;
        this.email = email;
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getMessege() { return messege; }
    public void setMessege(String messege) { this.messege = messege; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}