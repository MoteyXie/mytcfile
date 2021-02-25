package com.motey.tcfile.model;

public class TcDrawing extends TcComponent {

    private String itemRevisionId;

    private TcDataset[] datasets;

    public TcDataset[] getDatasets() {
        return datasets;
    }

    public void setDatasets(TcDataset[] datasets) {
        this.datasets = datasets;
    }

    public String getItemRevisionId() {
        return itemRevisionId;
    }

    public void setItemRevisionId(String itemRevisionId) {
        this.itemRevisionId = itemRevisionId;
    }
}
