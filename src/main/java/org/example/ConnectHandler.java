package org.example;

import java.util.Map;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;

public class ConnectHandler implements RequestHandler<WebsocketConnectRequest, WebSocketConnectResponse> {
    private static DynamoDbClient DYNAMO_DB_CLIENT = DynamoDbClient.builder().build();

    @Override
    public WebSocketConnectResponse handleRequest(WebsocketConnectRequest websocketConnectRequest, Context context) {

        String loginToken = websocketConnectRequest.getHeaders().get("Login-Token").toString();
        int userId = this.authenticate(loginToken);
        String connectionId = websocketConnectRequest.getRequestContext().get("connectionId").toString();
        System.out.println("connectionId: " + connectionId);
        PutItemRequest putItemRequest = PutItemRequest.builder()
                .tableName("user-connections-songhan")
                .item(Map.of(
                        "UserId", AttributeValue.builder()
                                .s(String.valueOf(userId))
                                .build(),
                        "ConnectionId", AttributeValue.builder()
                                .s(connectionId)
                                .build()))
                .build();
        DYNAMO_DB_CLIENT.putItem(putItemRequest);
        return new WebSocketConnectResponse();
    }

    private int authenticate(String loginToken) {
        if ("123".equals(loginToken)) {
            return 1;
        } else if ("456".equals(loginToken)) {
            return 2;
        }
        throw new RuntimeException();
    }
}
