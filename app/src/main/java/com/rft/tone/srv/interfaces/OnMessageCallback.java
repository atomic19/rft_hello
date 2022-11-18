package com.rft.tone.srv.interfaces;

import com.rft.tone.srv.Host;
import com.rft.tone.srv.Request;

public interface OnMessageCallback {
    void onMessage(Request request, Host host);
}
