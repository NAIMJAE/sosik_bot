package com.sosikbot.service.apiService;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class BithumbApiService {
    
    public Map<String, Double> getAllTickers() {
        String apiUrl = "https://api.bithumb.com/public/ticker/ALL_KRW";
        try {
            URL url = new URL(apiUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            // API 응답 데이터 수신
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String inputLine;
            StringBuilder content = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            in.close();
            conn.disconnect();

            // JSON 파싱 및 데이터 처리
            Map<String, Double> resultMap = parseAndPrintTickers(content.toString());
            return resultMap;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private Map<String, Double> parseAndPrintTickers(String jsonResponse) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(jsonResponse);

            // "data" 키에 해당하는 노드 추출
            JsonNode dataNode = rootNode.get("data");

            Map<String, Double> resultMap = new HashMap<>(); 

            // 모든 코인 데이터 순회
            for (Iterator<String> it = dataNode.fieldNames(); it.hasNext();) {
                String coinName = it.next();
                JsonNode coinNode = dataNode.get(coinName);
            
                if (coinNode.has("closing_price")) { // "closing_price"가 존재하는 경우만 출력
                    String closingPrice = coinNode.get("closing_price").asText();
                    resultMap.put(coinName, Double.parseDouble(closingPrice));
                }
            }
            return resultMap;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
