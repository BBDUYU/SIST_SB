package org.doit.ik.user;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PhoneVerificationRequest {
    private String phone;
    private String code;
}