package com.motey.tcfile.model;

public class AskDrawingFromPartResponse {

    private TcPart[] parts;

    private String message;

    private boolean isSuccess;

    public TcPart[] getParts() {
        return parts;
    }

    public void setParts(TcPart[] parts) {
        this.parts = parts;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isSuccess() {
        return isSuccess;
    }

    public void setSuccess(boolean success) {
        isSuccess = success;
    }
}
