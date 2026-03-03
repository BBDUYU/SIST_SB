package org.doit.ik.user;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class PhoneAuth {

    @Id @GeneratedValue
    private Long id;

    private String phone;
    private String code;
    private LocalDateTime createdAt;

    public PhoneAuth(String phone, String code) {
        this.phone = phone;
        this.code = code;
        this.createdAt = LocalDateTime.now();
    }
}