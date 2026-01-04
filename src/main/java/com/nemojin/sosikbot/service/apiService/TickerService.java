package com.nemojin.sosikbot.service.apiService;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.nemojin.sosikbot.exception.BotException;
import com.nemojin.sosikbot.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@RequiredArgsConstructor
@Service
public class TickerService {
    /// [API] Get Bithumb Each Ticker by API
    public Map<String, Object> getBithumbTickers(String coin) {
        String apiUrl = "https://api.bithumb.com/v1/ticker?markets=KRW-"+coin;
        try {
            URL url = new URL(apiUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            // Receive API response data
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String inputLine;
            StringBuilder content = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            in.close();
            conn.disconnect();

            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());

            String data = content.substring(1, content.length()-1);
            return mapper.readValue(data, new TypeReference<Map<String, Object>>() {});

        } catch (Exception e) {
            throw new BusinessException(BotException.BITHUMB_TICKER_CONNECTION_FAIL);
        }
    }

    /// [API] Get All Bithumb Tickers by API
    public Map<String, Double> getAllBithumbTickers() {
        String apiUrl = "https://api.bithumb.com/public/ticker/ALL_KRW";
        try {
            URL url = new URL(apiUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            // Receive API response data
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String inputLine;
            StringBuilder content = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            in.close();
            conn.disconnect();

            // Parsing JSON for data processing
            return parseAndPrintTickers(content.toString());

        } catch (Exception e) {
            throw new BusinessException(BotException.BITHUMB_TICKER_CONNECTION_FAIL);
        }
    }

    /// [Util] Parsing API Data in Map
    private Map<String, Double> parseAndPrintTickers(String jsonResponse) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(jsonResponse);

            // Extract nodes under the 'data' key
            JsonNode dataNode = rootNode.get("data");
            Map<String, Double> resultMap = new HashMap<>();

            // Iterate through all coin data
            for (Iterator<String> it = dataNode.fieldNames(); it.hasNext();) {
                String coinName = it.next();
                JsonNode coinNode = dataNode.get(coinName);

                // Print only when 'closing_price' exists
                if (coinNode.has("closing_price")) {
                    String closingPrice = coinNode.get("closing_price").asText();
                    resultMap.put(coinName, Double.parseDouble(closingPrice));
                }
            }
            return resultMap;
        } catch (Exception e) {
            throw new BusinessException(BotException.BITHUMB_TICKER_PARSE_FAIL);
        }
    }
}
