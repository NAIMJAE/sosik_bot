package com.sosikbot.bot;

import java.io.IOException;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Component
public class BotUtil {
    public void KillChromeProcess() {
        try {
            // 크롬 메인 프로세스 종료
            Runtime.getRuntime().exec("pkill -f chrome");
            // 크롬 드라이버 종료
            Runtime.getRuntime().exec("pkill -f chromedriver");
        } catch (IOException e) {
            System.err.println("프로세스 종료 중 오류 발생: " + e.getMessage());
        }
    }
}
