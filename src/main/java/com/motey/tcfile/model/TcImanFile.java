package com.motey.tcfile.model;

public class TcImanFile extends TcComponent{

    private String fileSize = null;
    private String mimeType = null;

    public String getFileSize() {
        return fileSize;
    }

    public void setFileSize(String fileSize) {
        this.fileSize = fileSize;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }
}
