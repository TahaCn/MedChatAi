package com.taha.medchatai.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponse {

    private String response;
    private String sessionId;
    private boolean success;
    private boolean emergency;
    private String emergencyMessage;
}
