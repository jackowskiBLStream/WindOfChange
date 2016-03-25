package com.blstream.windofchange;

import android.os.Parcel;
import android.os.Parcelable;

/**
 *
 */
public class RSSInfo implements Parcelable {
    public static final Creator<RSSInfo> CREATOR = new Creator<RSSInfo>() {
        @Override
        public RSSInfo createFromParcel(Parcel in) {
            return new RSSInfo(in);
        }

        @Override
        public RSSInfo[] newArray(int size) {
            return new RSSInfo[size];
        }
    };
    protected String image;
    protected String title;
    protected String description;
    protected String pubDate;
    protected String link;

    protected RSSInfo(Parcel in) {
        image = in.readString();
        title = in.readString();
        description = in.readString();
        pubDate = in.readString();
        link = in.readString();
    }

    public RSSInfo() {
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }


    public void setImage(String image) {
        this.image = image;
    }


    public void setTitle(String title) {
        this.title = title;
    }


    public void setDescription(String description) {
        this.description = description;
    }


    public void setPubDate(String pubDate) {
        this.pubDate = pubDate;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(image);
        dest.writeString(title);
        dest.writeString(description);
        dest.writeString(pubDate);
        dest.writeString(link);
    }
}
