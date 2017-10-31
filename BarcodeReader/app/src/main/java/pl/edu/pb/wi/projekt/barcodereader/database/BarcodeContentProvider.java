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
    private static final int PERSON_LIST = 6;
    private static final int SECTION_LIST = 7;
    private static final UriMatcher URI_MATCHER;

    // prepare the UriMatcher - used to resolve uri to appropriate query to database
    static {
        URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
        URI_MATCHER.addURI(Contract.AUTHORITY,
                Contract.Devices.TABLE_NAME + "/" + Contract.Devices.Search.LAST_PATH_SEGMENT,
                BARCODE_LIST);
        URI_MATCHER.addURI(Contract.AUTHORITY,
                Contract.Devices.TABLE_NAME + "/*",
                SEARCH_LIST);
        URI_MATCHER.addURI(Contract.AUTHORITY,
                Contract.SummaryData.LAST_PATH_SEGMENT + "/#",
                BARCODE_ID);
        URI_MATCHER.addURI(Contract.AUTHORITY,
                Contract.SummaryData.LAST_PATH_SEGMENT,
                BARCODE_CODE);
        URI_MATCHER.addURI(Contract.AUTHORITY,
                Contract.Persons.LAST_PATH_SEGMENT,
                PERSON_LIST);
        URI_MATCHER.addURI(Contract.AUTHORITY,
                Contract.Sections.LAST_PATH_SEGMENT,
                SECTION_LIST);
    }

    @Override
    public boolean onCreate() {
        Log.e(TAG, "new ContentProvider");
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
                Log.e(TAG, "search query");
                result = database.getReadableDatabase().query(Contract.Devices.TABLE_NAME,
                        buildProjection(), selection, selectionArgs, null, null,
                        sortOrder, uri.getQueryParameter("limit"));
                break;
            case BARCODE_ID:
                Log.e(TAG, "barcode id");
                result = getSingleItem(uri, sortOrder);
                break;
            case BARCODE_CODE:
                Log.e(TAG, "barcode code");
                result = database.getReadableDatabase().query(Contract.SummaryData.TABLE_NAME,
                        projection, selection, selectionArgs, null, null,
                        sortOrder);
                break;
            case BARCODE_LIST:
                Log.e(TAG, "search list disp");
                result = database.getReadableDatabase().query(Contract.Devices.TABLE_NAME,
                        projection, selection, selectionArgs, null, null,
                        sortOrder);
                break;
            case PERSON_LIST:
                result = database.getReadableDatabase().query(Contract.Persons.TABLE_NAME,
                        projection, selection, selectionArgs, null, null,
                        sortOrder);
                break;
            case SECTION_LIST:
                result = database.getReadableDatabase().query(Contract.Sections.TABLE_NAME,
                        projection, selection, selectionArgs, null, null,
                        sortOrder);
                break;
            default:
                Log.e(TAG, "no match");
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
            case PERSON_LIST:
                return Contract.Persons.CONTENT_TYPE;
            case SECTION_LIST:
                return Contract.Sections.CONTENT_TYPE;
            default:
                return null;
        }
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        long id = -1;
        Uri baseUri = Uri.EMPTY;

        switch (URI_MATCHER.match(uri)) {
            case SEARCH_LIST:
            case BARCODE_ID:
            case BARCODE_CODE:
                break;
            case BARCODE_LIST:
                id = database.getWritableDatabase().insert(Contract.Devices.TABLE_NAME, null, values);
                baseUri = Contract.Devices.CONTENT_URI;
                break;
            case PERSON_LIST:
                id = database.getWritableDatabase().insert(Contract.Persons.TABLE_NAME, null, values);
                baseUri = Contract.Persons.CONTENT_URI;
                break;
            case SECTION_LIST:
                id = database.getWritableDatabase().insert(Contract.Sections.TABLE_NAME, null, values);
                baseUri = Contract.Sections.CONTENT_URI;
                break;
            default:
                Log.e(TAG, "no match");
        }
        if (id != -1) {
            return baseUri.buildUpon().appendPath(String.valueOf(id)).build();
        }
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
                Contract.Devices.COLUMN_SERIAL_NR + AS_PART + SearchManager.SUGGEST_COLUMN_TEXT_2,
                Contract.Devices.COLUMN_ID + AS_PART + SearchManager.SUGGEST_COLUMN_INTENT_DATA};
    }
}
