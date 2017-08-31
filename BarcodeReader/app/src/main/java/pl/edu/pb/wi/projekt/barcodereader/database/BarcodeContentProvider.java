package pl.edu.pb.wi.projekt.barcodereader.database;

import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Created by Mateusz on 28.09.2016.
 */
public class BarcodeContentProvider extends ContentProvider {

    public static final String TAG = "BarcodeContentProv";
    private BcDatabaseHelper database;
    private static final String ID_ALIAS = "_id";
    private static final String AS_PART = " as ";

    // helper constants for use with the UriMatcher
    private static final int BARCODE_LIST = 1;
    private static final int BARCODE_ID = 2;
    private static final int BARCODE_CODE = 3;
    private static final int SEARCH_LIST = 5;
    private static final UriMatcher URI_MATCHER;

    // prepare the UriMatcher - used to resolve uri to appropriate query to database
    static {
        URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
        URI_MATCHER.addURI(Contract.AUTHORITY,
                Contract.Devices.TABLE_NAME +"/"+ Contract.Devices.Search.LAST_PATH_SEGMENT,
                BARCODE_LIST);
        URI_MATCHER.addURI(Contract.AUTHORITY,
                Contract.Devices.TABLE_NAME + "/*",
                SEARCH_LIST);
        URI_MATCHER.addURI(Contract.AUTHORITY,
                Contract.SummaryData.LAST_PATH_SEGMENT+"/#",
                BARCODE_ID);
        URI_MATCHER.addURI(Contract.AUTHORITY,
                Contract.SummaryData.LAST_PATH_SEGMENT,
                BARCODE_CODE);
    }

    @Override
    public boolean onCreate() {
        Log.e(TAG,"new ContentProvider");
        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        database = BcDatabaseHelper.getInstance(getContext());
        Cursor result;
        Log.e(Contract.Devices.TABLE_NAME, uri.toString());
        switch (URI_MATCHER.match(uri)) {
            case SEARCH_LIST:
                Log.e(TAG,"search query");
                result = database.getReadableDatabase().query(Contract.Devices.TABLE_NAME,
                        buildProjection(), selection, selectionArgs, null, null,
                        sortOrder, uri.getQueryParameter("limit"));
                break;
            case BARCODE_ID:
                Log.e(TAG,"barcode id");
                result = getSingleItem(uri, sortOrder);
                break;
            case BARCODE_CODE:
                Log.e(TAG,"barcode code");
                result = database.getReadableDatabase().query(Contract.SummaryData.TABLE_NAME,
                        projection, selection, selectionArgs, null, null,
                        sortOrder);
                break;
            case BARCODE_LIST:
                Log.e(TAG,"search list disp");
                result = database.getReadableDatabase().query(Contract.Devices.TABLE_NAME,
                        projection, selection, selectionArgs, null, null,
                        sortOrder);
                break;
            default:
                Log.e(TAG,"no match");
                result = null;
        }
        return result;
    }

    private Cursor getSingleItem(Uri uri, String sortOrder) {
        return database.getReadableDatabase().query(Contract.SummaryData.TABLE_NAME,
                Contract.SummaryData.getAliasedProjection(),
                Contract.SummaryData.COLUMN_ID + " = ?",
                new String[]{uri.getLastPathSegment()}, null, null,
                sortOrder);
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        switch (URI_MATCHER.match(uri)) {
            case SEARCH_LIST:
                return Contract.Devices.CONTENT_TYPE;
            case BARCODE_ID:
                return Contract.SummaryData.CONTENT_ITEM_TYPE;
            case BARCODE_LIST:
                return Contract.SummaryData.CONTENT_TYPE;
            default:
                return null;
        }
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }

    private String[] buildProjection() {
        return new String[]{Contract.Devices.COLUMN_ID + AS_PART + ID_ALIAS,
                Contract.Devices.COLUMN_PART_NAME + AS_PART + SearchManager.SUGGEST_COLUMN_TEXT_1,
                Contract.Devices.COLUMN_INVENTARY_NR + AS_PART + SearchManager.SUGGEST_COLUMN_TEXT_2,
                Contract.Devices.COLUMN_ID + AS_PART + SearchManager.SUGGEST_COLUMN_INTENT_DATA};
    }
}
