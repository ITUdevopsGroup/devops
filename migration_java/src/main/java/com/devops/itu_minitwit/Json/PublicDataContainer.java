package com.devops.itu_minitwit.Json;

import java.util.ArrayList;

public class PublicDataContainer {
    ArrayList<PublicDataRecord> data = new ArrayList<PublicDataRecord>();

    
    public PublicDataContainer() {
    }

    public PublicDataContainer(ArrayList<PublicDataRecord> data) {
        this.data = data;
    }

    public ArrayList<PublicDataRecord> getData() {
        return data;
    }

    public void setData(ArrayList<PublicDataRecord> data) {
        this.data = data;
    }
    
}