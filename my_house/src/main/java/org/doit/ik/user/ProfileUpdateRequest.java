package org.doit.ik.user;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProfileUpdateRequest {

	private String nickname;
    private String phone;

    private String currentPassword;
    private String newPassword;
    private String confirmPassword;
}
