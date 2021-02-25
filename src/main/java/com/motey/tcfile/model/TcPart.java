package com.motey.tcfile.model;

public class TcPart extends TcComponent{

    private String itemRevisionId;

    private TcDrawing[] drawings;

    public TcDrawing[] getDrawings() {
        return drawings;
    }

    public void setDrawings(TcDrawing[] drawings) {
        this.drawings = drawings;
    }

    public String getItemRevisionId() {
        return itemRevisionId;
    }

    public void setItemRevisionId(String itemRevisionId) {
        this.itemRevisionId = itemRevisionId;
    }
}
