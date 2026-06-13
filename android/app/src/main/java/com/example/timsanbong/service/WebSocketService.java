package com.example.timsanbong.service;

import android.content.Context;
import android.util.Log;

import com.example.timsanbong.data.api.ApiClient;
import com.example.timsanbong.utils.Constants;
import com.example.timsanbong.utils.SessionManager;

import org.json.JSONObject;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

public class WebSocketService {
    private static final String TAG = "WebSocketService";
    private static WebSocketService instance;
    private WebSocket webSocket;
    private SessionManager sessionManager;
    private WebSocketCallback callback;
    private boolean isConnected = false;

    public interface WebSocketCallback {
        void onMessageReceived(String destination, String body);
    }

    private WebSocketService(Context context) {
        sessionManager = new SessionManager(context);
    }

    public static synchronized WebSocketService getInstance(Context context) {
        if (instance == null) {
            instance = new WebSocketService(context.getApplicationContext());
        }
        return instance;
    }

    public void setCallback(WebSocketCallback callback) {
        this.callback = callback;
    }

    public void connect(Context context) {
        if (isConnected || !sessionManager.isLoggedIn()) return;

        OkHttpClient client = ApiClient.getRawClient(context);
        Request request = new Request.Builder()
                .url(Constants.WS_URL)
                .build();

        webSocket = client.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, Response response) {
                Log.d(TAG, "WebSocket Opened");
                // Send STOMP Connect
                String token = sessionManager.getToken();
                String connectFrame = "CONNECT\n" +
                        "accept-version:1.1,1.0\n" +
                        "heart-beat:10000,10000\n" +
                        "Authorization:Bearer " + token + "\n\n\u0000";
                webSocket.send(connectFrame);
            }

            @Override
            public void onMessage(WebSocket webSocket, String text) {
                Log.d(TAG, "Received: " + text);
                if (text.startsWith("CONNECTED")) {
                    isConnected = true;
                    subscribe("/user/queue/messages", "sub-0");
                    subscribe("/user/queue/notifications", "sub-1");
                } else if (text.startsWith("MESSAGE")) {
                    parseStompMessage(text);
                }
            }

            @Override
            public void onMessage(WebSocket webSocket, ByteString bytes) {
                onMessage(webSocket, bytes.utf8());
            }

            @Override
            public void onClosing(WebSocket webSocket, int code, String reason) {
                Log.d(TAG, "Closing: " + reason);
                webSocket.close(1000, null);
                isConnected = false;
            }

            @Override
            public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                Log.e(TAG, "Error: " + t.getMessage(), t);
                isConnected = false;
            }
        });
    }

    private void subscribe(String destination, String id) {
        if (webSocket != null) {
            String subscribeFrame = "SUBSCRIBE\n" +
                    "id:" + id + "\n" +
                    "destination:" + destination + "\n\n\u0000";
            webSocket.send(subscribeFrame);
        }
    }

    public void sendMessage(String destination, String jsonBody) {
        if (webSocket != null && isConnected) {
            String sendFrame = "SEND\n" +
                    "destination:" + destination + "\n" +
                    "content-type:application/json\n\n" +
                    jsonBody + "\u0000";
            webSocket.send(sendFrame);
        }
    }

    private void parseStompMessage(String text) {
        try {
            // Find headers
            String[] parts = text.split("\n\n", 2);
            if (parts.length < 2) return;
            
            String headersStr = parts[0];
            String body = parts[1].replace("\u0000", "");
            
            String destination = "";
            String[] headers = headersStr.split("\n");
            for (String header : headers) {
                if (header.startsWith("destination:")) {
                    destination = header.substring("destination:".length());
                }
            }
            
            if (callback != null && !destination.isEmpty()) {
                callback.onMessageReceived(destination, body);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error parsing STOMP message", e);
        }
    }

    public void disconnect() {
        if (webSocket != null) {
            String disconnectFrame = "DISCONNECT\n\n\u0000";
            webSocket.send(disconnectFrame);
            webSocket.close(1000, "User disconnected");
            isConnected = false;
        }
    }
}
