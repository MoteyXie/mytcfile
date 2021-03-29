package com.motey.tcfile.model;

public class ImanFileContext extends ComponentContext {

    public String getOriginalFileName() {
        return originalFileName;
    }

    public void setOriginalFileName(String originalFileName) {
        this.originalFileName = originalFileName;
    }
    private String originalFileName;
}
