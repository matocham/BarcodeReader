package pl.edu.pb.wi.projekt.barcodereader.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pl.edu.pb.wi.projekt.barcodereader.asyncTasks.CopyDatabaseAsyncTask;

/**
 * Created by Mateusz on 26.07.2016.
 * Helper class used to manage database instances. Store, open, and close database instances
 */
public class BcDatabaseHelper extends SQLiteOpenHelper {
    private static Map<String,BcDatabaseHelper> databases = new HashMap<>();
    private static final String DEFAULT_DB_NAME = "database.db";
    private static final int DB_VERSION = 1;

    private BcDatabaseHelper(Context context, String path) {
        super(context, path, null, DB_VERSION);
    }

    public static BcDatabaseHelper getInstance(Context context){
        String fullPath = getDatabaseAbsolutePath(context,DEFAULT_DB_NAME);
        return createDatabase(context,fullPath);
    }

    public static BcDatabaseHelper getInstance(Context context, String name){
        String fullPath = getDatabaseAbsolutePath(context,name);
        return createDatabase(context,fullPath);
    }

    public static BcDatabaseHelper getInstance(Context context, String path, String name){
        String fullPath;
        if(path.endsWith(File.pathSeparator)){
            fullPath = path+name;
        } else{
            fullPath = path+File.pathSeparator+name;
        }
        return createDatabase(context,fullPath);
    }

    public static void close(String path){
        if(databases.containsKey(path)){
            databases.get(path).close();
            databases.remove(path);
        }
    }

    public static void closeAll(){
        for(String key : databases.keySet()){
            databases.get(key).close();
            Log.e("Helper","closing "+key);
        }
        databases.clear();
    }

    private static BcDatabaseHelper createDatabase(Context context, String fullPath) {
        BcDatabaseHelper instance;
        if(databases.containsKey(fullPath)){
            instance = databases.get(fullPath);
        } else{
            instance = new BcDatabaseHelper(context,fullPath);
            databases.put(fullPath,instance);
            Log.e("Helper","Created database "+fullPath);
        }
        return instance;
    }

    @NonNull
    private static String getDatabaseAbsolutePath(Context context, String dbName) {
        return new File(context.getDir(CopyDatabaseAsyncTask.LOCATION, Context.MODE_PRIVATE),dbName).getAbsolutePath();
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(BcDatabaseHelper.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");
        onCreate(db);
    }

    public static List<String> getTableNames(SQLiteDatabase database){
        ArrayList<String> names = new ArrayList<>();
        database.beginTransaction();
        Cursor c = database.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);

        if (c.moveToFirst()) {
            while ( !c.isAfterLast() ) {
                names.add(c.getString(0));
                c.moveToNext();
            }
        }
        c.close();
        return names;
    }
 }
