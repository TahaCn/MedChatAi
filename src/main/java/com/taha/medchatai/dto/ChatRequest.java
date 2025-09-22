package com.taha.medchatai.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatRequest {

    private String message;
    private String chatType; // PSYCHOLOGICAL_CHAT, DIAGNOSIS, MEDICINE_INTERACTION
    private String sessionId;
}
