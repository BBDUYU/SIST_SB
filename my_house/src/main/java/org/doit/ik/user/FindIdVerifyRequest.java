package org.doit.ik.user;

import lombok.Getter;

@Getter
public class FindIdVerifyRequest {
    private String phone;
    private String code;
}