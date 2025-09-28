package org.example;


import java.util.Map;

import lombok.Data;

@Data
public class WebSocketConnectResponse {
    int statusCode = 200;
    Map<String, String> headers = Map.of();
    String body = "";
    boolean isBase64Encoded = false;
}

