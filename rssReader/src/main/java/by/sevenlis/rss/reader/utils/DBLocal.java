package by.sevenlis.rss.reader.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import by.sevenlis.rss.reader.fragments.SettingsFragment;
import by.sevenlis.rss.reader.classes.FeedEntity;
import by.sevenlis.rss.reader.classes.FeedSource;

public class DBLocal {
    static final String TABLE_SOURCES = "sources";
    static final String TABLE_ENTITIES = "entities";
    private Context context;
    
    public DBLocal(Context context) {
        this.context = context;
    }
    
    private String getEntitiesSelectSQL() {
        return "SELECT E.title AS e_title, E.description AS e_description, E.stringLink AS e_stringLink, E.uri AS e_uri, " +
                "E.imageUrl AS e_imageUrl, E.publish_timestamp AS e_publish_timestamp, E.read AS e_read, " +
                "S.guid AS s_guid, S.name AS s_name, S.sourceUrl AS s_sourceUrl, S.enabled AS s_enabled " +
                "FROM " + TABLE_ENTITIES + " AS E " +
                "LEFT JOIN " + TABLE_SOURCES + " AS S " +
                "ON S.sourceUrl = E.sourceUrl ";
    }
    
    public List<FeedSource> getAllFeedSources() {
        List<FeedSource> sources = new ArrayList<>();
        
        SQLiteDatabase database = new DBOpenHelper(context).getReadableDatabase();
        Cursor cursor = database.query(TABLE_SOURCES, null, null, null, null, null, null);
        while (cursor.moveToNext()) {
            FeedSource feedSource = new FeedSource(
                    cursor.getString(cursor.getColumnIndex("guid")),
                    cursor.getString(cursor.getColumnIndex("name")),
                    cursor.getString(cursor.getColumnIndex("sourceUrl")).trim(),
                    cursor.getInt(cursor.getColumnIndex("enabled")) == 1
            );
            sources.add(feedSource);
        }
        cursor.close();
        database.close();
        
        return sources;
    }
    
    public List<FeedSource> getFeedSourcesEnabled() {
        List<FeedSource> sources = new ArrayList<>();
    
        SQLiteDatabase database = new DBOpenHelper(context).getReadableDatabase();
        Cursor cursor = database.query(TABLE_SOURCES, null, "enabled = ?", new String[]{"1"}, null, null, null);
        while (cursor.moveToNext()) {
            FeedSource feedSource = new FeedSource(
                    cursor.getString(cursor.getColumnIndex("guid")),
                    cursor.getString(cursor.getColumnIndex("name")),
                    cursor.getString(cursor.getColumnIndex("sourceUrl")).trim(),
                    cursor.getInt(cursor.getColumnIndex("enabled")) == 1
            );
            sources.add(feedSource);
        }
        cursor.close();
        database.close();
        
        return sources;
    }
    
    public FeedSource getFeedSource(String guid) {
        FeedSource feedSource = null;
    
        SQLiteDatabase database = new DBOpenHelper(context).getReadableDatabase();
        Cursor cursor = database.query(TABLE_SOURCES, null, "guid = ?", new String[]{guid}, null, null, null, "1");
        if (cursor.moveToFirst()) {
            feedSource = new FeedSource(
                    cursor.getString(cursor.getColumnIndex("guid")),
                    cursor.getString(cursor.getColumnIndex("name")),
                    cursor.getString(cursor.getColumnIndex("sourceUrl")).trim(),
                    cursor.getInt(cursor.getColumnIndex("enabled")) == 1
            );
        }
        cursor.close();
        database.close();
        
        return feedSource;
    }
    
    public FeedSource addFeedSource(FeedSource feedSource) {
        FeedSource source = getFeedSource(feedSource.getGuid());
        if (source != null) {
            feedSource.setName(source.getName());
            feedSource.setSourceUrl(source.getSourceUrl());
            feedSource.setEnabled(source.isEnabled());
            updateFeedSource(feedSource);
            return feedSource;
        }
    
        ContentValues cv = new ContentValues();
        cv.put("guid",feedSource.getGuid());
        cv.put("name", feedSource.getName());
        cv.put("sourceUrl", feedSource.getSourceUrl());
        cv.put("enabled", feedSource.isEnabled() ? 1 : 0);
    
        SQLiteDatabase database = new DBOpenHelper(context).getWritableDatabase();
        database.insert(TABLE_SOURCES,null,cv);
        database.close();
        
        return feedSource;
    }
    
    public void setFeedSourceEnabled(FeedSource feedSource, boolean enabled) {
        ContentValues cv = new ContentValues();
        cv.put("enabled", enabled ? 1 : 0);
        SQLiteDatabase database = new DBOpenHelper(context).getReadableDatabase();
        database.update(TABLE_SOURCES,cv,"guid = ?",new String[]{feedSource.getGuid()});
        database.close();
    }
    
    public void updateFeedSource(FeedSource feedSource) {
        ContentValues cv = new ContentValues();
        cv.put("name",feedSource.getName());
        cv.put("sourceUrl",feedSource.getSourceUrl());
        cv.put("enabled", feedSource.isEnabled() ? 1 : 0);
        SQLiteDatabase database = new DBOpenHelper(context).getReadableDatabase();
        database.update(TABLE_SOURCES,cv,"guid = ?",new String[]{feedSource.getGuid()});
        database.close();
    }
    
    public void deleteFeedSource(FeedSource feedSource) {
        SQLiteDatabase database = new DBOpenHelper(context).getWritableDatabase();
        database.delete(TABLE_SOURCES,"guid = ?",new String[]{feedSource.getGuid()});
        database.delete(TABLE_ENTITIES,"sourceGuid = ?",new String[]{feedSource.getGuid()});
        database.close();
    }
    
    public void deleteFeedEntity(FeedEntity feedEntity) {
        SQLiteDatabase database = new DBOpenHelper(context).getWritableDatabase();
        database.delete(TABLE_ENTITIES,"uri = ?",new String[]{feedEntity.getUri()});
        database.close();
    }
    
    public List<FeedEntity> getEntities(boolean state) {
        List<FeedEntity> feedEntities = new ArrayList<>();
        
        String sql = getEntitiesSelectSQL() +
                "WHERE E.read = ? AND S.enabled = ? " +
                "ORDER BY E.publish_timestamp DESC " +
                "LIMIT " + SettingsFragment.Settings.getDefaultEntitiesAmountToDisplay(context);
        
        SQLiteDatabase database = new DBOpenHelper(context).getReadableDatabase();
        Cursor cursor = database.rawQuery(sql,new String[]{state ? "1" : "0", "1"});
        while (cursor.moveToNext()) {
            FeedSource feedSource = new FeedSource(
                    cursor.getString(cursor.getColumnIndex("s_guid")),
                    cursor.getString(cursor.getColumnIndex("s_name")),
                    cursor.getString(cursor.getColumnIndex("s_sourceUrl")).trim(),
                    cursor.getInt(cursor.getColumnIndex("s_enabled")) == 1
            );
            
            FeedEntity feedEntity = new FeedEntity(
                    cursor.getString(cursor.getColumnIndex("e_title")),
                    cursor.getString(cursor.getColumnIndex("e_description")),
                    cursor.getString(cursor.getColumnIndex("e_stringLink")),
                    new Date(cursor.getLong(cursor.getColumnIndex("e_publish_timestamp"))),
                    cursor.getString(cursor.getColumnIndex("e_uri")),
                    cursor.getString(cursor.getColumnIndex("e_imageUrl")),
                    feedSource,
                    cursor.getInt(cursor.getColumnIndex("e_read")) == 1
            );
            
            feedEntities.add(feedEntity);
        }
        cursor.close();
        database.close();
        
        return feedEntities;
    }
    
    public List<FeedEntity> getAllEntities() {
        List<FeedEntity> feedEntities = new ArrayList<>();
        
        String sql = getEntitiesSelectSQL() +
                "ORDER BY E.publish_timestamp DESC " +
                "LIMIT " + SettingsFragment.Settings.getDefaultEntitiesAmountToDisplay(context);
        
        SQLiteDatabase database = new DBOpenHelper(context).getReadableDatabase();
        Cursor cursor = database.rawQuery(sql,null);
        while (cursor.moveToNext()) {
            FeedSource feedSource = new FeedSource(
                    cursor.getString(cursor.getColumnIndex("s_guid")),
                    cursor.getString(cursor.getColumnIndex("s_name")),
                    cursor.getString(cursor.getColumnIndex("s_sourceUrl")).trim(),
                    cursor.getInt(cursor.getColumnIndex("s_enabled")) == 1
            );
            
            FeedEntity feedEntity = new FeedEntity(
                    cursor.getString(cursor.getColumnIndex("e_title")),
                    cursor.getString(cursor.getColumnIndex("e_description")),
                    cursor.getString(cursor.getColumnIndex("e_stringLink")),
                    new Date(cursor.getLong(cursor.getColumnIndex("e_publish_timestamp"))),
                    cursor.getString(cursor.getColumnIndex("e_uri")),
                    cursor.getString(cursor.getColumnIndex("e_imageUrl")),
                    feedSource,
                    cursor.getInt(cursor.getColumnIndex("e_read")) == 1
            );
            
            feedEntities.add(feedEntity);
        }
        cursor.close();
        database.close();
        
        return feedEntities;
    }
    
    public List<FeedEntity> searchEntities(String search) {
        List<FeedEntity> feedEntities = new ArrayList<>();
        
        String sql = getEntitiesSelectSQL() +
                "WHERE LOWER(E.title) like ? OR LOWER(E.description) like ? " +
                "ORDER BY E.publish_timestamp DESC ";
        
        SQLiteDatabase database = new DBOpenHelper(context).getReadableDatabase();
        String searchString = String.format("%%%s%%", search);
        Cursor cursor = database.rawQuery(sql,new String[]{searchString, searchString});
        while (cursor.moveToNext()) {
            FeedSource feedSource = new FeedSource(
                    cursor.getString(cursor.getColumnIndex("s_guid")),
                    cursor.getString(cursor.getColumnIndex("s_name")),
                    cursor.getString(cursor.getColumnIndex("s_sourceUrl")).trim(),
                    cursor.getInt(cursor.getColumnIndex("s_enabled")) == 1
            );
            
            FeedEntity feedEntity = new FeedEntity(
                    cursor.getString(cursor.getColumnIndex("e_title")),
                    cursor.getString(cursor.getColumnIndex("e_description")),
                    cursor.getString(cursor.getColumnIndex("e_stringLink")),
                    new Date(cursor.getLong(cursor.getColumnIndex("e_publish_timestamp"))),
                    cursor.getString(cursor.getColumnIndex("e_uri")),
                    cursor.getString(cursor.getColumnIndex("e_imageUrl")),
                    feedSource,
                    cursor.getInt(cursor.getColumnIndex("e_read")) == 1
            );
            
            feedEntities.add(feedEntity);
        }
        cursor.close();
        database.close();
        
        return feedEntities;
    }
    
    public void setFeedEntityRead(FeedEntity feedEntity, boolean read) {
        feedEntity.setRead(read);
        
        ContentValues cv = new ContentValues();
        cv.put("read", feedEntity.isRead() ? 1 : 0);
        SQLiteDatabase database = new DBOpenHelper(context).getWritableDatabase();
        database.update(TABLE_ENTITIES,cv,"uri = ?",new String[]{feedEntity.getUri()});
        database.close();
    }
    
    private FeedEntity getFeedEntity(String uri) {
        FeedEntity feedEntity = null;
        
        String sql = getEntitiesSelectSQL() +
                "WHERE E.uri = ? ";
    
        SQLiteDatabase database = new DBOpenHelper(context).getReadableDatabase();
        Cursor cursor = database.rawQuery(sql,new String[]{uri.trim()});
        if (cursor.moveToFirst()) {
            FeedSource feedSource = new FeedSource(
                    cursor.getString(cursor.getColumnIndex("s_guid")),
                    cursor.getString(cursor.getColumnIndex("s_name")),
                    cursor.getString(cursor.getColumnIndex("s_sourceUrl")).trim(),
                    cursor.getInt(cursor.getColumnIndex("s_enabled")) == 1
            );
        
            feedEntity = new FeedEntity(
                    cursor.getString(cursor.getColumnIndex("e_title")),
                    cursor.getString(cursor.getColumnIndex("e_description")),
                    cursor.getString(cursor.getColumnIndex("e_stringLink")),
                    new Date(cursor.getLong(cursor.getColumnIndex("e_publish_timestamp"))),
                    cursor.getString(cursor.getColumnIndex("e_uri")),
                    cursor.getString(cursor.getColumnIndex("e_imageUrl")),
                    feedSource,
                    cursor.getInt(cursor.getColumnIndex("e_read")) == 1
            );
        }
        cursor.close();
        database.close();
        
        return feedEntity;
    }
    
    public void insertFeedEntity(FeedEntity feedEntity) {
        if (getFeedEntity(feedEntity.getUri()) != null) return;
    
        ContentValues cv = new ContentValues();
        cv.put("title", feedEntity.getTitle());
        cv.put("description", feedEntity.getDescription());
        cv.put("stringLink", feedEntity.getStringLink());
        cv.put("uri", feedEntity.getUri());
        cv.put("imageUrl",feedEntity.getImageUrl());
        cv.put("publish_timestamp", feedEntity.getDatePublished().getTime());
        cv.put("sourceUrl", feedEntity.getFeedSource().getSourceUrl());
        cv.put("sourceGuid", feedEntity.getFeedSource().getGuid());
        cv.put("read", feedEntity.isRead() ? "1" : "0");
    
        SQLiteDatabase database = new DBOpenHelper(context).getWritableDatabase();
        database.insert(TABLE_ENTITIES,null,cv);
        database.close();
    }
    
    
}
