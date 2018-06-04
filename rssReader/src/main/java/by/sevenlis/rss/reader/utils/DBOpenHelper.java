package by.sevenlis.rss.reader.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DBOpenHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "rssReader.s3db";
    private static final int DATABASE_VERSION = 2;
    
    DBOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
                "CREATE TABLE [" + DBLocal.TABLE_SOURCES + "] (" +
                        "[_id] INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," +
                        "[guid] TEXT," +
                        "[name] TEXT," +
                        "[sourceUrl] TEXT," +
                        "[enabled] INTEGER" +
                        ")"
        );
    
        db.execSQL(
                "CREATE TABLE [" + DBLocal.TABLE_ENTITIES + "] (" +
                        "[_id] INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," +
                        "[title] TEXT," +
                        "[description] TEXT," +
                        "[publish_timestamp] INTEGER," +
                        "[stringLink] TEXT," +
                        "[uri] TEXT," +
                        "[imageUrl] TEXT," +
                        "[sourceUrl] TEXT," +
                        "[sourceGuid] TEXT," +
                        "[read] INTEGER" +
                        ")"
        );
        
        db.execSQL("CREATE INDEX [IDX_ENTITIES_URI] ON [" + DBLocal.TABLE_ENTITIES + "] ([uri] ASC)");
        db.execSQL("CREATE INDEX [IDX_ENTITIES_S_URL] ON [" + DBLocal.TABLE_ENTITIES + "] ([sourceUrl] ASC)");
        db.execSQL("CREATE INDEX [IDX_ENTITIES_S_GUID] ON [" + DBLocal.TABLE_ENTITIES + "] ([sourceGuid] ASC)");
    
        db.execSQL("CREATE INDEX [IDX_SOURCES_URL] ON [" + DBLocal.TABLE_SOURCES + "] ([sourceUrl] ASC)");
        db.execSQL("CREATE INDEX [IDX_SOURCES_GUID] ON [" + DBLocal.TABLE_SOURCES + "] ([guid] ASC)");
        
    }
    
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        List<ContentValues> sourcesDataArray = new ArrayList<>();
        Cursor cursor = db.query(DBLocal.TABLE_SOURCES,null,null,null,null,null,null);
        while (cursor.moveToNext()) {
            ContentValues cv = new ContentValues();
            cv.put("guid",cursor.getString(cursor.getColumnIndex("guid")));
            cv.put("name",cursor.getString(cursor.getColumnIndex("name")));
            cv.put("sourceUrl",cursor.getString(cursor.getColumnIndex("sourceUrl")));
            cv.put("enabled",cursor.getInt(cursor.getColumnIndex("enabled")));
            
            sourcesDataArray.add(cv);
        }
        cursor.close();
    
        db.execSQL("DROP TABLE IF EXISTS " + DBLocal.TABLE_SOURCES);
        db.execSQL("DROP TABLE IF EXISTS " + DBLocal.TABLE_ENTITIES);
        onCreate(db);
    
        for (ContentValues cv : sourcesDataArray) {
            db.insert(DBLocal.TABLE_SOURCES, null, cv);
        }
    }
    
    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        super.onDowngrade(db, oldVersion, newVersion);
    }
}
