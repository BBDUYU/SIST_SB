package org.doit.ik.user;

import org.springframework.beans.factory.annotation.Value; // @Value를 위해 필요
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import net.nurigo.sdk.NurigoApp;
import net.nurigo.sdk.message.model.Message; // Message 모델 임포트
import net.nurigo.sdk.message.request.SingleMessageSendingRequest; // 단일 메시지 발송 요청 시 필요
import net.nurigo.sdk.message.response.SingleMessageSentResponse; // 응답 확인 시 필요
import net.nurigo.sdk.message.service.DefaultMessageService;

@Service
@RequiredArgsConstructor
public class SmsServiceImpl implements SmsService {

    @Value("${coolsms.api-key}")
    private String apiKey;

    @Value("${coolsms.api-secret}")
    private String apiSecret;

    @Value("${coolsms.from-number}")
    private String fromNumber;

    @Override
    public void sendSms(String to, String text) {
        // 초기화 시 API 주소는 생략 가능합니다 (기본값 설정됨)
        DefaultMessageService messageService = 
                NurigoApp.INSTANCE.initialize(apiKey, apiSecret, "https://api.coolsms.co.kr");

        Message message = new Message();
        message.setFrom(fromNumber);
        message.setTo(to);
        message.setText(text);

        // SDK 4.x 버전에서는 SingleMessageSendingRequest로 감싸서 보내는 것이 일반적입니다.
        SingleMessageSentResponse response = messageService.sendOne(new SingleMessageSendingRequest(message));
        
        // 콘솔에서 성공 여부 확인용 (포트폴리오 기록용)
        System.out.println("SMS 발송 결과: " + response);
    }
}