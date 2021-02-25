package com.motey.tcfile.model;

public class TcDataset extends TcComponent{

    private TcImanFile[] imanFiles;

    public TcImanFile[] getImanFiles() {
        return imanFiles;
    }

    public void setImanFiles(TcImanFile[] imanFiles) {
        this.imanFiles = imanFiles;
    }
}
