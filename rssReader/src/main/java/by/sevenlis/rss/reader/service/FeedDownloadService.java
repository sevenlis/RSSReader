package by.sevenlis.rss.reader.service;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.support.annotation.Nullable;

import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.text.MessageFormat;
import java.util.List;
import java.util.Objects;

import by.sevenlis.rss.reader.R;
import by.sevenlis.rss.reader.activities.MainActivity;
import by.sevenlis.rss.reader.classes.FeedEntity;
import by.sevenlis.rss.reader.classes.FeedSource;
import by.sevenlis.rss.reader.intents.FeedUpdateServiceIntents;
import by.sevenlis.rss.reader.utils.DBLocal;

public class FeedDownloadService extends IntentService {
    public static String FEED_UPDATE_ACTION_START = "FEED_UPDATE_ACTION_START";
    public static String FEED_UPDATE_ACTION_STOP = "FEED_UPDATE_ACTION_STOP";
    
    public static int SERVICE_STARTED = 1;
    public static int SERVICE_STOPPED = 0;
    public static int SERVICE_PROGRESS = 2;
    
    private static boolean SERVICE_CANCELLED = true;
    
    private ResultReceiver receiver;
    private DBLocal dbLocal;
    private NotificationManager notificationManager;
    private Notification.Builder notificationBuilder;
    private static final int FEED_UPDATE_NOTIF_ID = 1001;
    private String loadingSourceString;
    
    public FeedDownloadService() {
        super(FeedDownloadService.class.getSimpleName());
    }
    
    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        Context context = getApplicationContext();
        String feedUpdatesString = context.getString(R.string.feeds_updates);
        dbLocal = new DBLocal(context);
        loadingSourceString = context.getString(R.string.loading_source);
    
        String CHANNEL_ID        = "RSS_READER_FEED_DOWNLOAD_CHANNEL";
        CharSequence channelName = "RSS Reader Feed download channel";
        
        notificationManager = NotificationManager.class.cast(context.getSystemService(Context.NOTIFICATION_SERVICE));
        
        notificationBuilder = getNotificationBuilder(context,CHANNEL_ID)
                .setTicker(feedUpdatesString)
                .setContentTitle(feedUpdatesString)
                .setContentText(feedUpdatesString)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher))
                .setSmallIcon(android.R.drawable.stat_sys_download)
                .setContentIntent(PendingIntent.getActivity(context, 0, new Intent(context, MainActivity.class), 0))
                .setDeleteIntent(createOnDismissIntent(context))
                
                .setAutoCancel(false)
                .setOngoing(false)
                .setProgress(0, 0, true)
                .setUsesChronometer(true);
    
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,channelName,importance);
            channel.setVibrationPattern(new long[]{0});
            channel.enableVibration(false);
            notificationManager.createNotificationChannel(channel);
            notificationBuilder.setChannelId(CHANNEL_ID);
        }
    
        return super.onStartCommand(intent, flags, startId);
    }
    
    private Notification.Builder getNotificationBuilder(Context ctx, String channelId) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            return new Notification.Builder(ctx,channelId);
        } else {
            return new Notification.Builder(ctx);
        }
    }
    
    private PendingIntent createOnDismissIntent(Context context) {
        Intent dismissIntent = new Intent(context,NotificationOnDismissReceiver.class);
        dismissIntent.putExtra("FeedDownloadService.FEED_UPDATE_NOTIF_ID",FEED_UPDATE_NOTIF_ID);
        return PendingIntent.getBroadcast(context,FEED_UPDATE_NOTIF_ID,dismissIntent,0);
    }
    
    protected void sendProgress(FeedSource feedSource, int fsCount, int fsAmount, FeedEntity feedEntity, int feCount, int feAmount) {
        if (SERVICE_CANCELLED) return;
        
        String contentText = MessageFormat.format(loadingSourceString,feedSource.getName(),fsCount + 1,fsAmount);
        notificationBuilder.setContentText(contentText);
        if (feedEntity == null) {
            notificationBuilder.setProgress(0,0,true);
        } else {
            notificationBuilder.setProgress(feAmount,feCount,false);
        }
        Notification notification = notificationBuilder.build();
        notification.flags = notification.flags | Notification.FLAG_ONLY_ALERT_ONCE;
        notificationManager.notify(FEED_UPDATE_NOTIF_ID, notification);
        
        Bundle data = new Bundle();
        data.putParcelable("feedSource",feedSource);
        data.putInt("fsCount",fsCount);
        data.putInt("fsAmount",fsAmount);
        data.putParcelable("feedEntity",feedEntity);
        data.putInt("feCount",feCount);
        data.putInt("feAmount",feAmount);
        sendResult(SERVICE_PROGRESS,data);
    }
    
    private String getImageUrl(SyndEntry syndEntry) {
        String imageUrl = "";
        if (syndEntry.getEnclosures().size() > 0) {
            imageUrl = syndEntry.getEnclosures().get(0).getUrl();
        } else if (syndEntry.getForeignMarkup().size() > 0) {
            if (syndEntry.getForeignMarkup().get(0).getAttributes().size() > 0) {
                imageUrl = syndEntry.getForeignMarkup().get(0).getAttributes().get(0).getValue();
            }
        }
        return imageUrl;
    }
    
    private String removeImageUrl(String descr) {
        String newString = descr.replaceAll("<img.+?>","").replaceFirst("^[\\t\\n\\r]+","");
        if (newString.length() > 1000) {
            return newString.substring(0,995) + " ...";
        }
        return newString;
    }
    
    private void downloadFeeds() {
        List<FeedSource> feedSources = dbLocal.getFeedSourcesEnabled();
        for (int i = 0; i < feedSources.size() && !SERVICE_CANCELLED; i++) {
            FeedSource feedSource = feedSources.get(i);
        
            sendProgress(feedSource,i,feedSources.size(),null,0,0);
            try {
                URLConnection urlConnection = new URL(feedSource.getSourceUrl()).openConnection();
                urlConnection.setConnectTimeout(4000);
                
                SyndFeedInput syndFeedInput = new SyndFeedInput();
                XmlReader xmlReader = new XmlReader(urlConnection);
                SyndFeed syndFeed = syndFeedInput.build(xmlReader);
                List<SyndEntry> syndEntries = syndFeed.getEntries();
                for (int j = 0; j < syndEntries.size() && !SERVICE_CANCELLED; j++) {
                    SyndEntry syndEntry = syndEntries.get(j);
                
                    String imageUrl = getImageUrl(syndEntry);
                
                    FeedEntity feedEntity = new FeedEntity(
                            syndEntry.getTitle(),
                            removeImageUrl(syndEntry.getDescription().getValue()),
                            syndEntry.getLink(),
                            syndEntry.getPublishedDate(),
                            syndEntry.getUri(),
                            imageUrl,
                            feedSource,
                            false
                    );
                
                    dbLocal.insertFeedEntity(feedEntity);
                
                    if (j % 5 == 0)
                        sendProgress(feedSource,i,feedSources.size(),feedEntity,j,syndEntries.size());
                }
            
            } catch (FeedException | IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    @Override
    protected void onHandleIntent(Intent intent) {
        if (!SERVICE_CANCELLED) return;
    
        SERVICE_CANCELLED = false;
        
        notificationManager.cancel(FEED_UPDATE_NOTIF_ID);
        if (intent != null) {
            receiver = intent.getParcelableExtra("receiver");
            
            String action = intent.getAction();
            if (Objects.equals(action, FEED_UPDATE_ACTION_START)) {
                sendResult(SERVICE_STARTED,null);
                if (MainActivity.isConnected(getApplicationContext()))
                    downloadFeeds();
            } else if (Objects.equals(action, FEED_UPDATE_ACTION_STOP)) {
                sendResult(SERVICE_STOPPED,null);
                stopSelf();
            }
        }
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        sendResult(SERVICE_STOPPED,null);
        notificationManager.cancel(FEED_UPDATE_NOTIF_ID);
        SERVICE_CANCELLED = true;
    }
    
    private void sendResult(int resultCode, Bundle resultData) {
        if (receiver != null) {
            receiver.send(resultCode,resultData);
        }
    }
    
    public static class NotificationOnDismissReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null && intent.getExtras() != null) {
                int notificationId = intent.getExtras().getInt("FeedDownloadService.FEED_UPDATE_NOTIF_ID");
                if (notificationId == FEED_UPDATE_NOTIF_ID) {
                    context.stopService(FeedUpdateServiceIntents.getIntent(context));
                }
            }
        }
    }
}
