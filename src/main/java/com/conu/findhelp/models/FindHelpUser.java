package com.conu.findhelp.models;


import com.conu.findhelp.enums.STATUS;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.List;

@Document("manager")
@Data
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FindHelpUser {

    @Id
    private String id;
    private String email;
    private String password;
    private String role;
    private String name;
    private String address;
    private String dob;
    private String phone;
    private String registrationNo;
    private STATUS status;

    private boolean assessmentTaken;

    private List<String> assessmentOptionsSelected;

    private String OTP;

    private Integer verificationAttempts;

    private Date otpExpiryDate;

    private List<String> patientQueue;

    private String counsellorAssigned;

    private String doctorAssigned;

    private boolean counsellingDone;

    private String counsellingComment;

    private boolean doctoringDone;

    private String doctorComment;

    private Date creationDate;

    public FindHelpUser(String email, String password, String name, String dob, String phone, String address, String role) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.dob = dob;
        this.phone = phone;
        this.address = address;
        this.status = STATUS.VERIFIED;
        this.role = role;
        this.creationDate = new Date();
    }

    public FindHelpUser(String email, String password, String name, String dob, String phone, String address, String registrationNo, String role) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.dob = dob;
        this.phone = phone;
        this.address = address;
        this.registrationNo = registrationNo;
        this.status = STATUS.PENDING;
        this.role = role;
        this.creationDate = new Date();
    }

    @Override
    public String toString() {
        return
                id + ","
                        + email +
                        "," + role +
                        ", " + name +
                        ", " + address +
                        ", " + dob +
                        ", " + phone +
                        ", " + status +
                        ", " + assessmentTaken +
                        ", " + counsellingDone +
                        ", " + doctoringDone +
                        ", " + counsellorAssigned +
                        ", " + doctorAssigned +
                        ", " + counsellingComment +
                        ", " + doctorComment +
                        ", " + creationDate;
    }
}
