package org.doit.ik.user;

public interface FindIdService {
    void sendCode(String phone);
    String verifyCode(String phone, String code);
}