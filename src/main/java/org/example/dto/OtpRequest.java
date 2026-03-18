package org.example.dto;

import javax.validation.constraints.NotBlank;

public class OtpRequest {

    @NotBlank(message = "phone is required")
    private String phone;

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}

