package by.sevenlis.rss.reader.intents;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;

import by.sevenlis.rss.reader.fragments.SettingsFragment;
import by.sevenlis.rss.reader.service.FeedDownloadService;

public class FeedUpdateServiceIntents {
    public static boolean ALARM_IS_ON = false;
    private static Intent intent = null;
    private static PendingIntent pendingIntent = null;
    private static AlarmManager alarmManager = null;
    
    public static Intent getIntent(Context ctx) {
        if (intent == null) {
            intent = new Intent(ctx, FeedDownloadService.class);
        }
        return intent;
    }
    
    private static PendingIntent getPendingIntent(Context ctx) {
        if (pendingIntent == null) {
            pendingIntent = PendingIntent.getService(ctx, 0, getIntent(ctx), PendingIntent.FLAG_UPDATE_CURRENT);
        }
        return pendingIntent;
    }
    
    private static AlarmManager getAlarmManager(Context ctx) {
        if (alarmManager == null) {
            alarmManager = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
        }
        return alarmManager;
    }
    
    public static void startExchangeDataServiceAlarm(Context ctx) {
        if (ALARM_IS_ON) return;
        getAlarmManager(ctx).cancel(getPendingIntent(ctx));
        long updateFreq = SettingsFragment.Settings.getFeedUpdateInterval(ctx) * 60 * 1000;
        long timeToRefresh = SystemClock.elapsedRealtime() + updateFreq;
        getAlarmManager(ctx).setInexactRepeating(AlarmManager.ELAPSED_REALTIME, timeToRefresh, updateFreq, getPendingIntent(ctx));
        ALARM_IS_ON = true;
    }
    
    public static void stopExchangeDataServiceAlarm(Context ctx) {
        getAlarmManager(ctx).cancel(getPendingIntent(ctx));
        ALARM_IS_ON = false;
    }
}
