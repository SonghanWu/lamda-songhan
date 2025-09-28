package org.example;


import java.util.Map;

import lombok.Data;

@Data
public class WebsocketConnectRequest {
    Map<String, Object> requestContext;
    Map<String, Object> headers;
}
