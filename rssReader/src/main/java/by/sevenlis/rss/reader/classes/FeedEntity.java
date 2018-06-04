package by.sevenlis.rss.reader.classes;

import android.os.Parcel;
import android.os.Parcelable;

import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;

public class FeedEntity implements Comparator<FeedEntity>, Parcelable {
    private String title;
    private String description;
    private String stringLink;
    private String uri;
    private String imageUrl;
    private Date datePublished;
    private FeedSource feedSource;
    private boolean read;
    
    public FeedEntity(String title, String description, String stringLink, Date datePublished, String uri, String imageUrl, FeedSource feedSource, boolean read) {
        this.title = title;
        this.description = description;
        this.stringLink = stringLink;
        this.datePublished = datePublished;
        this.uri = uri;
        this.imageUrl = imageUrl;
        this.feedSource = feedSource;
        this.read = read;
    }
    
    protected FeedEntity(Parcel in) {
        title = in.readString();
        description = in.readString();
        stringLink = in.readString();
        uri = in.readString();
        imageUrl = in.readString();
        feedSource = in.readParcelable(FeedSource.class.getClassLoader());
        read = in.readByte() != 0;
    }
    
    public static final Creator<FeedEntity> CREATOR = new Creator<FeedEntity>() {
        @Override
        public FeedEntity createFromParcel(Parcel in) {
            return new FeedEntity(in);
        }
        
        @Override
        public FeedEntity[] newArray(int size) {
            return new FeedEntity[size];
        }
    };
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getDescription() {
        return description;
    }
    
    public String getStringLink() {
        return stringLink;
    }
    
    public Date getDatePublished() {
        return datePublished;
    }
    
    public FeedSource getFeedSource() {
        return feedSource;
    }
    
    public String getUri() {
        return uri;
    }
    
    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
    
    public String getDatePublishedFormatted() {
        return new SimpleDateFormat("dd.MM.yyyy hh:mm", Locale.getDefault()).format(getDatePublished());
    }
    
    public boolean isRead() {
        return read;
    }
    
    public void setRead(boolean read) {
        this.read = read;
    }
    
    @Override
    public int compare(FeedEntity feedEntity1, FeedEntity feedEntity2) {
        if (feedEntity1.getDatePublished().getTime() > feedEntity2.getDatePublished().getTime()) {
            return 1;
        }
        return 0;
    }
    
    public String getHtmlRepresentation() {
        String s = "<table border=\"0\">\n" +
                "    <tr bgcolor=\"#C1C1C1\">\n" +
                "      <td>{0}</td>\n" +
                "      <td>{1}</td>\n" +
                "    </tr>\n" +
                "    <tr>\n" +
                "    \t<td colspan=\"2\"><h2>{2}</h2></td>\n" +
                "    </tr>\n" +
                "    <tr>\n" +
                "    \t<td colspan=\"2\">{3}</td>\n" +
                "    </tr>\n" +
                "</table>";
        return MessageFormat.format(s, getDatePublishedFormatted(), getFeedSource().getName(), getTitle(), getDescription());
    }
    
    @Override
    public int describeContents() {
        return 0;
    }
    
    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(title);
        parcel.writeString(description);
        parcel.writeString(stringLink);
        parcel.writeString(uri);
        parcel.writeString(imageUrl);
        parcel.writeParcelable(feedSource, i);
        parcel.writeByte((byte) (read ? 1 : 0));
    }
}
