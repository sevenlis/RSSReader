package by.sevenlis.rss.reader.activities;

import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.FrameLayout;

import by.sevenlis.rss.reader.R;
import by.sevenlis.rss.reader.fragments.FeedEntitiesFragment;
import by.sevenlis.rss.reader.fragments.FeedSourcesFragment;
import by.sevenlis.rss.reader.fragments.SettingsFragment;
import by.sevenlis.rss.reader.intents.FeedUpdateServiceIntents;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private final String NAV_ITEM_SELECTED_ID_KEY = "NAV_ITEM_SELECTED_ID_KEY";
    private int navItemSelectedId;
    
    public static final String FEED_SOURCES_FRAGMENT_TAG = "FeedSourcesFragment.TAG";
    public static final String FEED_ENTITIES_FRAGMENT_TAG = "FeedEntitiesFragment.TAG";
    
    private static final int DEFAULT_TOOLBAR_HEIGHT = 56;
    private static int toolBarHeight = -1;
    
    private FragmentManager fragmentManager;
    private FeedEntitiesFragment feedEntitiesFragment;
    private FeedSourcesFragment feedSourcesFragment;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        
        fragmentManager = getFragmentManager();
        
        Toolbar toolbar = Toolbar.class.cast(findViewById(R.id.toolbar));
        setSupportActionBar(toolbar);
        
        DrawerLayout drawer = DrawerLayout.class.cast(findViewById(R.id.drawer_layout));
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        
        NavigationView navigationView = NavigationView.class.cast(findViewById(R.id.nav_view));
        navigationView.setNavigationItemSelectedListener(this);
    
        if (SettingsFragment.Settings.getFeedUpdateInterval(this) != 0) {
            FeedUpdateServiceIntents.startExchangeDataServiceAlarm(this);
        }
    
        navItemSelectedId = R.id.nav_unread;
        if (savedInstanceState != null) {
            navItemSelectedId = savedInstanceState.getInt(NAV_ITEM_SELECTED_ID_KEY);
        }
        feedEntitiesFragment = new FeedEntitiesFragment();
        feedSourcesFragment = new FeedSourcesFragment();
        
        Bundle arguments = new Bundle();
        if (navItemSelectedId == R.id.nav_unread || navItemSelectedId == R.id.nav_read || navItemSelectedId == R.id.nav_all) {
            if (navItemSelectedId == R.id.nav_unread) {
                arguments.putInt(FeedEntitiesFragment.ENTITY_STATE_KEY, FeedEntitiesFragment.ENTITY_STATE_UNREAD);
            } else if (navItemSelectedId == R.id.nav_read) {
                arguments.putInt(FeedEntitiesFragment.ENTITY_STATE_KEY, FeedEntitiesFragment.ENTITY_STATE_READ);
            } else {
                arguments.putInt(FeedEntitiesFragment.ENTITY_STATE_KEY, FeedEntitiesFragment.ENTITY_STATE_ALL);
            }
            feedEntitiesFragment.setArguments(arguments);
            fragmentManager.beginTransaction().replace(R.id.frame_content, feedEntitiesFragment, FEED_ENTITIES_FRAGMENT_TAG).commit();
        } else  if (navItemSelectedId == R.id.nav_manage_feeds) {
            fragmentManager.beginTransaction().replace(R.id.frame_content, feedSourcesFragment, FEED_SOURCES_FRAGMENT_TAG).commit();
        }
        
        navigationView.getMenu().findItem(navItemSelectedId).setChecked(true);
    
        FrameLayout frameContent = findViewById(R.id.frame_content);
        frameContent.setPadding(0, getToolBarHeight(this),0,0);
    }
    
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(NAV_ITEM_SELECTED_ID_KEY,navItemSelectedId);
    }
    
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }
    
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = DrawerLayout.class.cast(findViewById(R.id.drawer_layout));
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
    
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        navItemSelectedId = item.getItemId();
        
        if (navItemSelectedId == R.id.nav_unread || navItemSelectedId == R.id.nav_read || navItemSelectedId == R.id.nav_all) {
            feedEntitiesFragment = new FeedEntitiesFragment();
            Bundle arguments = new Bundle();
            if (navItemSelectedId == R.id.nav_unread) {
                arguments.putInt(FeedEntitiesFragment.ENTITY_STATE_KEY, FeedEntitiesFragment.ENTITY_STATE_UNREAD);
            } else if (navItemSelectedId == R.id.nav_read) {
                arguments.putInt(FeedEntitiesFragment.ENTITY_STATE_KEY, FeedEntitiesFragment.ENTITY_STATE_READ);
            } else {
                arguments.putInt(FeedEntitiesFragment.ENTITY_STATE_KEY, FeedEntitiesFragment.ENTITY_STATE_ALL);
            }
            feedEntitiesFragment.setArguments(arguments);
            fragmentManager.beginTransaction().replace(R.id.frame_content, feedEntitiesFragment,FEED_ENTITIES_FRAGMENT_TAG).commit();
        } else  if (navItemSelectedId == R.id.nav_manage_feeds) {
            feedSourcesFragment = new FeedSourcesFragment();
            fragmentManager.beginTransaction().replace(R.id.frame_content, feedSourcesFragment,FEED_SOURCES_FRAGMENT_TAG).commit();
        } else if (navItemSelectedId == R.id.nav_exit) {
            exitApplication();
        }
        
        DrawerLayout drawer = DrawerLayout.class.cast(findViewById(R.id.drawer_layout));
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
    
    private void exitApplication() {
        FeedUpdateServiceIntents.stopExchangeDataServiceAlarm(this);
    
        Intent intent = FeedUpdateServiceIntents.getIntent(getBaseContext());
        stopService(intent);
        
        onBackPressed();
        finish();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
    
    public static int getToolBarHeight(@Nullable Context context) {
        if (toolBarHeight > 0) {
            return toolBarHeight;
        }
        if (context == null) {
            return DEFAULT_TOOLBAR_HEIGHT;
        }
        final Resources resources = context.getResources();
        final int resourceId = resources.getIdentifier("action_bar_size", "dimen", "android");
        toolBarHeight = resourceId > 0 ? resources.getDimensionPixelSize(resourceId) : (int) convertDpToPixel(context, DEFAULT_TOOLBAR_HEIGHT);
        return toolBarHeight;
    }
    
    public static float convertDpToPixel(Context context, float dp) {
        float scale = context.getResources().getDisplayMetrics().density;
        return dp * scale + 0.5f;
    }
    
    public static boolean isConnected(Context ctx) {
        ConnectivityManager cm = (ConnectivityManager) ctx.getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm != null ? cm.getActiveNetworkInfo() : null;
        return ni != null && ni.isConnected();
    }
}
