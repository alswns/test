package com.example.test_api;

import com.google.gson.JsonObject;

public class art {

    String id,lang,sessionId,timestamp;
    JsonObject result,status;
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public JsonObject getResult() {
        return result;
    }

    public void setResult(JsonObject result) {
        this.result = result;
    }

    public JsonObject getStatus() {
        return status;
    }

    public void setStatus(JsonObject status) {
        this.status = status;
    }
}
