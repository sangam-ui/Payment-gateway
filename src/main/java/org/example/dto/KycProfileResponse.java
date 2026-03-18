package org.example.dto;

import org.example.model.KycStatus;

public class KycProfileResponse {

    private final String phone;
    private final String fullName;
    private final String email;
    private final String panMasked;
    private final KycStatus status;

    public KycProfileResponse(String phone, String fullName, String email, String panMasked, KycStatus status) {
        this.phone = phone;
        this.fullName = fullName;
        this.email = email;
        this.panMasked = panMasked;
        this.status = status;
    }

    public String getPhone() {
        return phone;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getPanMasked() {
        return panMasked;
    }

    public KycStatus getStatus() {
        return status;
    }
}

