package org.example;


import java.util.Map;

import lombok.Data;

@Data
public class HTTPRequest {
    Map<String, String> queryStringParameters;
}
