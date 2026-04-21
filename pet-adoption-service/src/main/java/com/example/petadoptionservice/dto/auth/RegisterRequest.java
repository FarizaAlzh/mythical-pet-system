package com.example.petadoptionservice.dto.auth;

import com.example.petadoptionservice.entity.HomeType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@JsonIgnoreProperties(ignoreUnknown = false)
public class RegisterRequest {

    @NotBlank
    private String username;

    @Email
    @NotBlank
    private String email;

    @NotBlank
    private String password;

    @NotNull
    @Min(value = 1, message = "magicTolerance must be between 1 and 10")
    @Max(value = 10, message = "magicTolerance must be between 1 and 10")
    private Integer magicTolerance;

    @NotNull
    private HomeType homeType;

    public RegisterRequest() {
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Integer getMagicTolerance() {
        return magicTolerance;
    }

    public void setMagicTolerance(Integer magicTolerance) {
        this.magicTolerance = magicTolerance;
    }

    public HomeType getHomeType() {
        return homeType;
    }

    public void setHomeType(HomeType homeType) {
        this.homeType = homeType;
    }

}
