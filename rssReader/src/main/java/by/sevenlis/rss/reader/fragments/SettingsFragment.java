package by.sevenlis.rss.reader.fragments;

import android.content.Context;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;

import by.sevenlis.rss.reader.R;
import by.sevenlis.rss.reader.intents.FeedUpdateServiceIntents;

public class SettingsFragment extends PreferenceFragment {
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings_fragment);

        Preference preference = findPreference(Settings.PREF_FEED_UPDATE_INTERVAL_KEY);
        preference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if (Integer.valueOf(newValue.toString()) == 0) {
                    FeedUpdateServiceIntents.stopExchangeDataServiceAlarm(getActivity());
                }
                return true;
            }
        });
    }
    
    public static class Settings {
        static String PREF_ENTITIES_AMOUNT_TO_DISPLAY_KEY = "default_entities_amount";
        static String PREF_FEED_UPDATE_INTERVAL_KEY = "feed_update_interval";
        
        public static int getDefaultEntitiesAmountToDisplay(Context context) {
            return Integer.valueOf(PreferenceManager.getDefaultSharedPreferences(context).getString(PREF_ENTITIES_AMOUNT_TO_DISPLAY_KEY,"50"));
        }
        
        public static int getFeedUpdateInterval(Context context) {
            return Integer.valueOf(PreferenceManager.getDefaultSharedPreferences(context).getString(PREF_FEED_UPDATE_INTERVAL_KEY,"15"));
        }
    }
}

