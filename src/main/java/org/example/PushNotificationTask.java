package org.example;

import lombok.Data;

@Data
public class PushNotificationTask {
    private Integer userIdToNotify;
    private String content;
}
