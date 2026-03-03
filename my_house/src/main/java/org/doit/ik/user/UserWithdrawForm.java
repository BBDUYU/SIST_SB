package org.doit.ik.user;

import jakarta.validation.constraints.AssertTrue;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserWithdrawForm {

    @AssertTrue(message = "탈퇴 안내사항 확인은 필수입니다.")
    private boolean confirmWithdraw;
}