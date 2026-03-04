// ✅ (권장) AuthVerification 엔티티가 있다면 PK는 이렇게 (DB도 id PK AUTO_INCREMENT로 맞춰야 함)
package org.doit.ik.user;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "auth_verification")
public class AuthVerification {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "verification_id") // ✅ 실제 DB 컬럼명인 verification_id로 매핑
	private Long id;
	
    @Column(nullable = true)
    private Long uid;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VerifyType verifyType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VerifyChannel channel;

    @Column(nullable = false)
    private String targetValue;

    @Column(nullable = false)
    private String code;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    private String usedYn = "N";

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}