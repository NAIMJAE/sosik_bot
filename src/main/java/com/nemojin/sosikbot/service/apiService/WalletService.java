package com.nemojin.sosikbot.service.apiService;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.nemojin.sosikbot.exception.BotException;
import com.nemojin.sosikbot.exception.BusinessException;
import com.nemojin.sosikbot.model.Wallet;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class WalletService {
    @Value("${BITHUMB_ACCESS_KEY}")
    private String accessKey;
    @Value("${BITHUMB_SECRET_KEY}")
    private String secretKey;

    @Value("${BITHUMB_ACCESS_KEY2}")
    private String accessKey2;
    @Value("${BITHUMB_SECRET_KEY2}")
    private String secretKey2;

    private final String API_URL = "https://api.bithumb.com";
    private static final ObjectMapper objectMapper = new ObjectMapper();

    /// [API] Get Bithumb Wallet Deposit History
    public Wallet getBithumbWallet(String coinName) throws Exception {

        // Set API parameters
        List<NameValuePair> queryParams = new ArrayList<>();
        queryParams.add(new BasicNameValuePair("currency", coinName));
        queryParams.add(new BasicNameValuePair("limit", "1"));
        queryParams.add(new BasicNameValuePair("page", "1"));
        queryParams.add(new BasicNameValuePair("order_by", "desc"));
        List<String> uuids = new ArrayList<>();
        uuids.add("202603087");
        uuids.add("202601491");
        String uuidQuery = uuids.stream().map(uuid -> "uuids[]=" + uuid).collect(Collectors.joining("&"));

        // Generate access token
        String query = URLEncodedUtils.format(queryParams, StandardCharsets.UTF_8);
        if (uuidQuery.isEmpty()) {
            query = query + "&" + uuidQuery;
        }
        MessageDigest md = MessageDigest.getInstance("SHA-512");
        md.update(query.getBytes(StandardCharsets.UTF_8));
        String queryHash = String.format("%0128x", new BigInteger(1, md.digest()));
        Algorithm algorithm = Algorithm.HMAC256(secretKey);
        String jwtToken = JWT.create()
                .withClaim("access_key", accessKey)
                .withClaim("nonce", UUID.randomUUID().toString())
                .withClaim("timestamp", System.currentTimeMillis())
                .withClaim("query_hash", queryHash)
                .withClaim("query_hash_alg", "SHA512")
                .sign(algorithm);
        String authenticationToken = "Bearer " + jwtToken;

        // Call API
        final HttpGet httpRequest = new HttpGet(API_URL + "/v1/deposits?" + query);
        httpRequest.addHeader("Authorization", authenticationToken);

        try (CloseableHttpClient client = HttpClients.createDefault();
             CloseableHttpResponse response = client.execute(httpRequest)) {
            // handle to response
            int httpStatus = response.getStatusLine().getStatusCode();
            String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
            if (responseBody.trim().equals("[]")) {return new Wallet();}

            // parse JSON response and return Wallet object
            if (httpStatus == 200) {
                String data = responseBody.substring(1, responseBody.length()-1);
                return parseJsonToWallet(data);
            } else {
                throw new BusinessException(BotException.BITHUMB_API_REQUEST_FAIL);
            }
        }
    }

    /// [Util] Parse Json to Wallet
    private Wallet parseJsonToWallet(String data) {
        objectMapper.registerModule(new JavaTimeModule());

        try {
            return objectMapper.readValue(data, new TypeReference<>() {});
        } catch (Exception e) {
            throw new BusinessException(BotException.JSON_PARSING_FAIL);
        }
    }

    /// [API] Get Transaction History
    public boolean getTransactionHistory(String coin) throws Exception {
        // Set API parameters
        List<NameValuePair> queryParams = new ArrayList<>();
        queryParams.add(new BasicNameValuePair("market", coin));
        queryParams.add(new BasicNameValuePair("limit", "1"));
        queryParams.add(new BasicNameValuePair("page", "1"));
        queryParams.add(new BasicNameValuePair("state", "done"));
        queryParams.add(new BasicNameValuePair("order_by", "desc"));

        // Generate access token
        String query = URLEncodedUtils.format(queryParams, StandardCharsets.UTF_8);
        MessageDigest md = MessageDigest.getInstance("SHA-512");
        md.update(query.getBytes(StandardCharsets.UTF_8));
        String queryHash = String.format("%0128x", new BigInteger(1, md.digest()));
        Algorithm algorithm = Algorithm.HMAC256(secretKey2);
        String jwtToken = JWT.create()
                .withClaim("access_key", accessKey2)
                .withClaim("nonce", UUID.randomUUID().toString())
                .withClaim("timestamp", System.currentTimeMillis())
                .withClaim("query_hash", queryHash)
                .withClaim("query_hash_alg", "SHA512")
                .sign(algorithm);
        String authenticationToken = "Bearer " + jwtToken;

        // Call API
        final HttpGet httpRequest = new HttpGet(API_URL + "/v1/orders?" + query);
        httpRequest.addHeader("Authorization", authenticationToken);

        try (CloseableHttpClient client = HttpClients.createDefault();
             CloseableHttpResponse response = client.execute(httpRequest)) {
            // handle to response
            int httpStatus = response.getStatusLine().getStatusCode();
            String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);

            if (responseBody.trim().equals("[]")) {return true;}

            if (httpStatus == 200) {
                List<Map<String, Object>> resultList = objectMapper.readValue(responseBody, new TypeReference<>() {});

                Map<String, Object> item = resultList.get(0);
                String createdAtStr = (String) item.get("created_at");
                OffsetDateTime offsetDateTime = OffsetDateTime.parse(createdAtStr);
                LocalDate createdDate = offsetDateTime.atZoneSameInstant(ZoneId.of("Asia/Seoul")).toLocalDate();

                LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
                return !createdDate.equals(today);

            } else {
                throw new BusinessException(BotException.BITHUMB_API_REQUEST_FAIL);
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /// [API] Get Transaction History at UUID
    public BigDecimal getTransactionAtUuid(String uuid) throws Exception {
        // Set API parameters
        List<NameValuePair> queryParams = new ArrayList<>();
        queryParams.add(new BasicNameValuePair("uuid", uuid));

        // Generate access token
        String query = URLEncodedUtils.format(queryParams, StandardCharsets.UTF_8);
        MessageDigest md = MessageDigest.getInstance("SHA-512");
        md.update(query.getBytes(StandardCharsets.UTF_8));
        String queryHash = String.format("%0128x", new BigInteger(1, md.digest()));
        Algorithm algorithm = Algorithm.HMAC256(secretKey2);
        String jwtToken = JWT.create()
                .withClaim("access_key", accessKey2)
                .withClaim("nonce", UUID.randomUUID().toString())
                .withClaim("timestamp", System.currentTimeMillis())
                .withClaim("query_hash", queryHash)
                .withClaim("query_hash_alg", "SHA512")
                .sign(algorithm);
        String authenticationToken = "Bearer " + jwtToken;

        // Call API
        final HttpGet httpRequest = new HttpGet(API_URL + "/v1/order?" + query);
        httpRequest.addHeader("Authorization", authenticationToken);

        try (CloseableHttpClient client = HttpClients.createDefault();
             CloseableHttpResponse response = client.execute(httpRequest)) {
            // handle to response
            int httpStatus = response.getStatusLine().getStatusCode();
            String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);

            if (httpStatus == 200) {
                Map<String, Object> result = objectMapper.readValue(responseBody, new TypeReference<>() {});
                String volumeStr = (String) result.get("executed_volume");

                return new BigDecimal(volumeStr);

            }else {
                throw new BusinessException(BotException.TRADE_SUCCESS_FAIL);
            }

        } catch (Exception e) {
            throw new BusinessException(BotException.AUTO_TRADING_ERROR);
        }
    }

    /// [API] Bithumb Submit Order
    public String submitOrder(String coin, String side, BigDecimal volume, double price, String type) throws Exception {
        // Set API parameters
        Map<String, Object> requestBody = new LinkedHashMap<>();
        requestBody.put("market", coin);
        requestBody.put("side", side);
        requestBody.put("ord_type", type);

        if ("price".equals(type)) {
            // 시장가 매수일 경우, volume 제거
            requestBody.put("price", price);
        } else {
            // 시장가 매도일 경우, price 제거
            requestBody.put("volume", volume);
        }

        // Generate access token
        List<BasicNameValuePair> queryParams = requestBody.entrySet().stream()
                .map(entry -> new BasicNameValuePair(entry.getKey(), String.valueOf(entry.getValue())))
                .toList();
        String query = URLEncodedUtils.format(queryParams, StandardCharsets.UTF_8);
        MessageDigest md = MessageDigest.getInstance("SHA-512");
        md.update(query.getBytes(StandardCharsets.UTF_8));
        String queryHash = String.format("%0128x", new BigInteger(1, md.digest()));
        Algorithm algorithm = Algorithm.HMAC256(secretKey2);
        String jwtToken = JWT.create()
                .withClaim("access_key", accessKey2)
                .withClaim("nonce", UUID.randomUUID().toString())
                .withClaim("timestamp", System.currentTimeMillis())
                .withClaim("query_hash", queryHash)
                .withClaim("query_hash_alg", "SHA512")
                .sign(algorithm);
        String authenticationToken = "Bearer " + jwtToken;

        // Call API
        final HttpPost httpRequest = new HttpPost(API_URL + "/v1/orders");
        httpRequest.addHeader("Authorization", authenticationToken);
        httpRequest.addHeader("Content-type", "application/json");
        httpRequest.setEntity(new StringEntity(new ObjectMapper().writeValueAsString(requestBody), StandardCharsets.UTF_8));

        try (CloseableHttpClient client = HttpClients.createDefault();
             CloseableHttpResponse response = client.execute(httpRequest)) {
            // handle to response
            int httpStatus = response.getStatusLine().getStatusCode();
            String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);

            if (httpStatus == 201) {
                Map<String, Object> result = objectMapper.readValue(responseBody, new TypeReference<>() {});
                return (String) result.get("uuid");

            }else {
                throw new BusinessException(BotException.TRADE_SUCCESS_FAIL);
            }

        } catch (Exception e) {
            throw new BusinessException(BotException.AUTO_TRADING_ERROR);
        }
    }
}
