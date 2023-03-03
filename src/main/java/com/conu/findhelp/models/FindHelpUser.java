package com.conu.findhelp.models;


import com.conu.findhelp.enums.STATUS;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

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

    public FindHelpUser(String email, String password, String name, String dob, String phone, String address, String role) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.dob = dob;
        this.phone = phone;
        this.address = address;
        this.status = STATUS.VERIFIED;
        this.role = role;
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
    }
}
