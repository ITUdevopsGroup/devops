package com.devops.itu_minitwit.Json;

import java.util.ArrayList;

public class PublicDataContainer {
    ArrayList<PublicDataRecord> data = new ArrayList<PublicDataRecord>();

        private boolean followed;
    
    
    public PublicDataContainer() {
    }

    

    public PublicDataContainer(ArrayList<PublicDataRecord> data, boolean followed) {
        this.data = data;
        this.followed = followed;
    }

    public ArrayList<PublicDataRecord> getData() {
        return data;
    }

    public void setData(ArrayList<PublicDataRecord> data) {
        this.data = data;
    }



    public boolean isFollowed() {
        return followed;
    }



    public void setFollowed(boolean followed) {
        this.followed = followed;
    }
    
}