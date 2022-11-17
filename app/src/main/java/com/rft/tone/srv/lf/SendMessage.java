package com.rft.tone.srv.lf;

import com.rft.tone.srv.Request;
import com.rft.tone.srv.interfaces.SendMessagesInt;

public class SendMessage implements SendMessagesInt {

    private final SenderHandler senderHandler;

    public SendMessage(SenderHandler senderHandler) {
        this.senderHandler = senderHandler;
    }

    @Override
    public void sendMessage(Request request) {
        this.senderHandler.sendToAll(request);
    }
}
