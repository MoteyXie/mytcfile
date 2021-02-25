package com.motey.tcfile.model;

public class JLAskDrawingFromPartResponse {

    private String drawingNo;
    private String drawingName;
    private String drawingRevNo;
    private String url;
    private String state;
    private String errorMsg;
    private String partNo;

    public String getDrawingNo() {
        return drawingNo;
    }

    public void setDrawingNo(String drawingNo) {
        this.drawingNo = drawingNo;
    }

    public String getDrawingName() {
        return drawingName;
    }

    public void setDrawingName(String drawingName) {
        this.drawingName = drawingName;
    }

    public String getDrawingRevNo() {
        return drawingRevNo;
    }

    public void setDrawingRevNo(String drawingRevNo) {
        this.drawingRevNo = drawingRevNo;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public String getPartNo() {
        return partNo;
    }

    public void setPartNo(String partNo) {
        this.partNo = partNo;
    }
}
