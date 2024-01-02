package com.zigsaadvertisement.AdvertisementModel;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class AdvertisementModel {

    @SerializedName("announcement")
    @Expose
    private Object announcement;
    @SerializedName("slides")
    @Expose
    private List<Slide> slides;

    public Object getAnnouncement() {
        return announcement;
    }

    public void setAnnouncement(Object announcement) {
        this.announcement = announcement;
    }

    public List<Slide> getSlides() {
        return slides;
    }

    public void setSlides(List<Slide> slides) {
        this.slides = slides;
    }

}
