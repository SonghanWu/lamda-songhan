package org.example;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.apigatewaymanagementapi.ApiGatewayManagementApiClient;
import software.amazon.awssdk.services.apigatewaymanagementapi.model.GoneException;
import software.amazon.awssdk.services.apigatewaymanagementapi.model.PostToConnectionRequest;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.ComparisonOperator;
import software.amazon.awssdk.services.dynamodb.model.Condition;
import software.amazon.awssdk.services.dynamodb.model.DeleteItemRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryResponse;

public class PushNotificationTaskConsumer implements RequestHandler<SQSEvent, Void> {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final DynamoDbClient dynamoDbClient = DynamoDbClient.builder().build();

   private static final ApiGatewayManagementApiClient apiGatewayManagementApiClient =
        ApiGatewayManagementApiClient.builder()
                .region(Region.of("us-east-1"))
                .endpointOverride(URI.create("https://r9ka6r29e3.execute-api.us-east-1.amazonaws.com/production/"))
                .build();

    @Override
    public Void handleRequest(SQSEvent sqsEvent, Context context) {
        try {
            for (SQSEvent.SQSMessage sqsMessage : sqsEvent.getRecords()) {
                PushNotificationTask pushNotificationTask = OBJECT_MAPPER.readValue(sqsMessage.getBody(),
                                                                                    PushNotificationTask.class);
                this.pushNotification(pushNotificationTask.getUserIdToNotify(),
                                      pushNotificationTask.getContent());

            }
            return null;
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }

    public void pushNotification(Integer userIdToNotify,
                                 String content) {

            QueryRequest queryRequest = QueryRequest.builder()
                    .tableName("user-connections-songhan")
                    .keyConditions(Map.of(
                            "UserId", Condition.builder()
                                    .comparisonOperator(ComparisonOperator.EQ)
                                    .attributeValueList(AttributeValue.builder()
                                                                .s(String.valueOf(userIdToNotify))
                                                                .build())
                                    .build()
                    ))
                    .build();

            QueryResponse queryResponse = dynamoDbClient.query(queryRequest);
            for (var item : queryResponse.items()) {
                String connectionId = item.get("ConnectionId").s();
                String userId = item.get("UserId").s();

                try {
                    PostToConnectionRequest postToConnectionRequest = PostToConnectionRequest.builder()
                            .connectionId(connectionId)
                            .data(SdkBytes.fromUtf8String(content))
                            .build();
                    apiGatewayManagementApiClient.postToConnection(postToConnectionRequest);
                } catch (GoneException goneException) {
                    System.out.println("Connection " + connectionId + " is gone. Deleting it from DynamoDB.");
                    DeleteItemRequest deleteItemRequest = DeleteItemRequest.builder()
                            .tableName("user-connections-songhan")
                            .key(Map.of(
                                    "ConnectionId", AttributeValue.builder()
                                            .s(connectionId)
                                            .build(),
                                    "UserId", AttributeValue.builder()
                                            .s(userId)
                                            .build()
                            ))
                            .build();
                    dynamoDbClient.deleteItem(deleteItemRequest);
                }
            }

    }
}
