package com.taha.medchatai.dto;

import com.taha.medchatai.entity.Gender;
import lombok.Data;

@Data
public class UserRegistrationDto {
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private Integer age;
    private Gender gender;
    private String address;
    private String chronicDiseases;

    public UserRegistrationDto() {
    }


}
