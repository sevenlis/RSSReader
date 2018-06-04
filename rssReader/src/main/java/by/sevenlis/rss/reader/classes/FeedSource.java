package by.sevenlis.rss.reader.classes;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.UUID;

public class FeedSource implements Parcelable {
    private String guid;
    private String name;
    private String sourceUrl;
    private boolean enabled;
    
    public FeedSource(String guid, String name, String sourceUrl, boolean enabled) {
        this.guid = guid;
        this.name = name;
        this.sourceUrl = sourceUrl;
        this.enabled = enabled;
    }
    
    public FeedSource() {
        this(UUID.randomUUID().toString(), "rss","https://rss",false);
    }
    
    private FeedSource(Parcel in) {
        guid = in.readString();
        name = in.readString();
        sourceUrl = in.readString();
        enabled = in.readByte() != 0;
    }
    
    public static final Creator<FeedSource> CREATOR = new Creator<FeedSource>() {
        @Override
        public FeedSource createFromParcel(Parcel in) {
            return new FeedSource(in);
        }
        
        @Override
        public FeedSource[] newArray(int size) {
            return new FeedSource[size];
        }
    };
    
    public String getGuid() {
        return guid;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getSourceUrl() {
        return sourceUrl;
    }
    
    public void setSourceUrl(String sourceUrl) {
        this.sourceUrl = sourceUrl;
    }
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    @Override
    public int describeContents() {
        return 0;
    }
    
    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(guid);
        parcel.writeString(name);
        parcel.writeString(sourceUrl);
        parcel.writeByte((byte) (enabled ? 1 : 0));
    }
}
