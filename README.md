# Lambda Songhan

A serverless real-time push notification system built with AWS Lambda, WebSocket, and DynamoDB.

## Overview

This project implements a real-time communication system using AWS serverless architecture. The system handles WebSocket connections, user authentication, and push notifications through AWS Lambda functions, DynamoDB for connection storage, and SQS for message queuing.

## Features

### WebSocket Connection Management
- User authentication based on login tokens
- Automatic storage of user connection information to DynamoDB
- Support for multiple device connections per user

### Real-time Message Push
- Targeted push notifications by user ID
- Push messages to all active connections of a user
- Automatic cleanup of disconnected connections

### HTTP API Service
- Simple calculation API example (A + B)
- RESTful interface design

## Technology Stack

- Java 17
- Maven
- AWS Lambda
- AWS DynamoDB
- AWS API Gateway
- AWS SQS
- Lombok

## Components

### Lambda Functions

#### ConnectHandler
Handles WebSocket connection establishment. Triggers when a WebSocket connection is requested and processes user authentication and connection information storage.

```java
private int authenticate(String loginToken) {
    if ("123".equals(loginToken)) {
        return 1;
    } else if ("456".equals(loginToken)) {
        return 2;
    }
    throw new RuntimeException();
}
```

#### APlusBHandler
Handles HTTP API requests. Triggers on HTTP GET requests and performs simple mathematical calculations (A + B).

```java
int result = Integer.valueOf(httpRequest.getQueryStringParameters().get("a"))
        + Integer.valueOf(httpRequest.getQueryStringParameters().get("b"));
```

#### PushNotificationTaskConsumer
Handles push notification tasks. Triggers from SQS message queue and processes user connection queries, message pushing, and connection cleanup.

### Data Models

#### HTTPRequest
```java
@Data
public class HTTPRequest {
    Map<String, String> queryStringParameters;
}
```

#### WebsocketConnectRequest
```java
@Data
public class WebsocketConnectRequest {
    Map<String, Object> requestContext;
    Map<String, Object> headers;
}
```

#### WebSocketConnectResponse
```java
@Data
public class WebSocketConnectResponse {
    int statusCode = 200;
    Map<String, String> headers = Map.of();
    String body = "";
    boolean isBase64Encoded = false;
}
```

#### PushNotificationTask
```java
@Data
public class PushNotificationTask {
    private Integer userIdToNotify;
    private String content;
}
```

## Deployment

### Prerequisites
- Java 17+
- Maven 3.6+
- AWS CLI configured

### AWS Resources

#### DynamoDB Table
```
Table Name: user-connections-songhan
Primary Key: UserId (String)
Sort Key: ConnectionId (String)
```

#### API Gateway
- WebSocket API endpoint
- HTTP API endpoint

#### SQS Queue
- Push notification task queue

### Build and Deploy

```bash
# Build the project
mvn clean package

# Package Lambda functions
mvn assembly:assembly

# Upload to AWS Lambda
aws lambda update-function-code \
  --function-name your-function-name \
  --zip-file fileb://target/lambda-songhan-1.0-SNAPSHOT-jar-with-dependencies.jar
```

## Usage Examples

### WebSocket Connection

```javascript
// Client connection example
const ws = new WebSocket('wss://your-api-gateway-url/websocket');
ws.onopen = function(event) {
    // Connection established
};
```

### HTTP API Call

```bash
# Calculation API example
curl "https://your-api-gateway-url/api/add?a=5&b=3"
# Returns: 8
```

### Send Push Notification

```json
// SQS message format
{
  "userIdToNotify": 1,
  "content": "Hello, this is a push notification!"
}
```

## Authentication

Currently supported login tokens:
- `"123"` → User ID: 1
- `"456"` → User ID: 2

## Workflow

### User Connection Flow
1. Client establishes WebSocket connection
2. Send Login-Token for authentication
3. ConnectHandler validates token
4. Store user connection information to DynamoDB
5. Return successful connection response

### Message Push Flow
1. System sends push task to SQS
2. PushNotificationTaskConsumer is triggered
3. Query all active connections for target user
4. Push message to all connections via API Gateway
5. Automatically clean up disconnected connections

## Notes

1. **Authentication**: Currently uses hardcoded authentication logic. Production environments should use more secure authentication mechanisms.
2. **Error Handling**: Consider adding more comprehensive error handling and logging.
3. **Monitoring**: Recommend configuring CloudWatch monitoring and alerts.
4. **Security**: Production environments require appropriate IAM permissions and VPC settings.

## Contributing

Issues and Pull Requests are welcome.

## License

This project is licensed under the MIT License.
