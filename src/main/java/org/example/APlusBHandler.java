package org.example;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

public class APlusBHandler implements RequestHandler<HTTPRequest, String> {
    @Override
    public String handleRequest(HTTPRequest httpRequest, Context context) {
        int result = Integer.valueOf(httpRequest.getQueryStringParameters().get("a"))
                + Integer.valueOf(httpRequest.getQueryStringParameters().get("b"));
        return String.valueOf(result);
    }
}
