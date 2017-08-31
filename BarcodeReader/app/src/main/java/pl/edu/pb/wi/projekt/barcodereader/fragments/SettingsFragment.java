package pl.edu.pb.wi.projekt.barcodereader.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;

import org.apache.commons.lang3.text.WordUtils;

import java.util.ArrayList;
import java.util.List;

import pl.edu.pb.wi.projekt.barcodereader.R;
import pl.edu.pb.wi.projekt.barcodereader.ScreenUtils;
import pl.edu.pb.wi.projekt.barcodereader.ServerConst;
import pl.edu.pb.wi.projekt.barcodereader.Utils;
import pl.edu.pb.wi.projekt.barcodereader.asyncTasks.CopyDatabaseAsyncTask;
import pl.edu.pb.wi.projekt.barcodereader.database.ColumnNotFoundException;
import pl.edu.pb.wi.projekt.barcodereader.database.TableNotFoundException;

/**
 * Created by Mateusz on 27.09.2016.
 */
public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    public static final int GET_DATABASE_CODE = 11;
    public static final int COPY_ERROR = 12;
    public static final int DB_TABLE_ERROR = 1;
    public static final int DB_COLUMN_ERROR = 4;
    public static final int COPY_OK = 9;
    public static final String TAG = "AppSettings";

    public boolean hasCamera;
    public boolean hasFlash;
    public boolean hasAutofocus;
    CharSequence[] piscutreSizes;
    CharSequence[] flashlightModes;
    CharSequence[] focusModes;

    String[] acceptableFocus = {Camera.Parameters.FOCUS_MODE_AUTO, Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE};
    String[] acceptableFlash = {Camera.Parameters.FLASH_MODE_AUTO, Camera.Parameters.FLASH_MODE_OFF, Camera.Parameters.FLASH_MODE_TORCH};

    Preference databasePreference;
    ListPreference cameraSizesPref;
    ListPreference autofocusPref;
    ListPreference flashlightPref;
    Context context;

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case ServerConst.CONNECTION_TIMEOUT:
                    Utils.showInfoDialog(context, context.getString(R.string.connection_error), context.getString(R.string.connection_timeout));
                    break;
                case ServerConst.NO_INTERNET:
                    Utils.showInfoDialog(context, context.getString(R.string.connection_error), context.getString(R.string.no_internet));
                    break;
                case ServerConst.AUTH_ERROR:
                    Utils.showInfoDialog(context, getString(R.string.auth_error_title), getString(R.string.auth_error_message));
                    break;
                case COPY_ERROR:
                    Utils.showInfoDialog(context, context.getString(R.string.copy_error), context.getString(R.string.load_database_error));
                    break;
                case DB_COLUMN_ERROR:
                    ColumnNotFoundException exColumn = (ColumnNotFoundException) msg.obj;
                    Utils.showInfoDialog(context, context.getString(R.string.validate_error), context.getString(R.string.column_not_found, exColumn.getColumnName(), exColumn.getTableName()));
                    break;
                case DB_TABLE_ERROR:
                    TableNotFoundException exTable = (TableNotFoundException) msg.obj;
                    Utils.showInfoDialog(context, context.getString(R.string.validate_error), context.getString(R.string.table_not_found, exTable.getTableName()));
                    break;
                case ServerConst.IO_ERROR:
                    Utils.showInfoDialog(context, context.getString(R.string.file_send_error), context.getString(R.string.server_connection_error));
                    break;
                case ServerConst.DOWNLOAD_OK:
                    String summary = context.getString(R.string.downloaded_online);
                    databasePreference.setSummary(summary);
                    databasePreference.getEditor().putString(getResources().getString(R.string.prefs_storage_source_key), summary).commit();
                    break;
                case COPY_OK:
                    String name = (String) msg.obj;
                    String fullSummary = context.getString(R.string.database_summary_start)
                            + name + context.getString(R.string.database_summary_second)
                            + Utils.getFormattedTime();
                    databasePreference.setSummary(fullSummary);
                    databasePreference.getEditor().putString(getResources().getString(R.string.prefs_storage_source_key), fullSummary).commit();
                default:
                    super.handleMessage(msg);
            }
        }
    };

    public SettingsFragment() {

    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        context = getActivity();
        hasCamera = Utils.checkCameraHardware(context);

        addPreferencesFromResource(R.xml.preferences);

        cameraSizesPref = (ListPreference) findPreference(getResources().getString(R.string.prefs_camera_picture_size_key));
        autofocusPref = (ListPreference) findPreference(getResources().getString(R.string.prefs_camera_autofocus_key));
        flashlightPref = (ListPreference) findPreference(getResources().getString(R.string.prefs_camera_torch_key));

        databasePreference = findPreference(getResources().getString(R.string.prefs_storage_source_key));
        databasePreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*"); // nie ma mimetype dla bazy sqlite więc dane są filtrowane po wybraniu pliku
                startActivityForResult(intent, GET_DATABASE_CODE);
                return true;
            }
        });

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        String database = sharedPref.getString(getResources().getString(R.string.prefs_storage_source_key), "");
        if (database.length() > 0) {
            databasePreference.setSummary(database);
        }
        if (hasCamera) {
            Camera cam = Utils.getCameraOrFinish(getActivity());

            hasFlash = Utils.checkFlashlightHardware(context);
            hasAutofocus = Utils.checkAutofocus(context);
            if (hasFlash) {
                loadFlashlightModes(cam);
            } else {
                flashlightPref.setEnabled(false);
            }

            if (hasAutofocus) {
                loadFocusModes(cam);
            } else {
                autofocusPref.setEnabled(false);
            }

            loadCameraSizes(cam);
            cam.release();
        } else {
            cameraSizesPref.setEnabled(false);
            autofocusPref.setEnabled(false);
            flashlightPref.setEnabled(false);
        }
    }

    private void loadCameraSizes(Camera cam) {
        piscutreSizes = getCameraSizes(cam).toArray(new CharSequence[0]);
        cameraSizesPref.setEntries(piscutreSizes);
        cameraSizesPref.setEntryValues(piscutreSizes);

        if (piscutreSizes.length > 0 && cameraSizesPref.getValue() == null) {
            cameraSizesPref.setValue(piscutreSizes[0].toString());
        }
        cameraSizesPref.setSummary(cameraSizesPref.getValue());
    }

    private void loadFlashlightModes(Camera cam) {
        List<String> modes = cam.getParameters().getSupportedFlashModes();
        List<String> acceptableModes = new ArrayList<>();
        List<String> acceptableModesNames = new ArrayList<>();
        for (String acceptMode : acceptableFlash) {
            if (modes.contains(acceptMode)) {
                acceptableModes.add(acceptMode);
                acceptableModesNames.add(WordUtils.capitalize(acceptMode.replace("-", " ")));
            }
        }

        flashlightModes = acceptableModes.toArray(new CharSequence[0]);
        flashlightPref.setEntryValues(flashlightModes);
        flashlightPref.setEntries(acceptableModesNames.toArray(new CharSequence[0]));

        if (flashlightModes.length > 0 && flashlightPref.getValue() == null) {
            flashlightPref.setValue(flashlightModes[0].toString());
        }
        flashlightPref.setSummary(WordUtils.capitalize(flashlightPref.getValue().replace("-", " ")));
    }

    private void loadFocusModes(Camera cam) {
        List<String> modes = cam.getParameters().getSupportedFocusModes();
        List<String> acceptableModes = new ArrayList<>();
        List<String> acceptableModesNames = new ArrayList<>();

        for (String acceptMode : acceptableFocus) {
            if (modes.contains(acceptMode)) {
                acceptableModes.add(acceptMode);
                acceptableModesNames.add(WordUtils.capitalize(acceptMode.replace("-", " ")));
            }
        }

        focusModes = acceptableModes.toArray(new CharSequence[0]);

        autofocusPref.setEntries(acceptableModesNames.toArray(new CharSequence[0]));
        autofocusPref.setEntryValues(focusModes);

        if (focusModes.length > 0 && autofocusPref.getValue() == null) {
            autofocusPref.setValue(focusModes[0].toString());
        }
        autofocusPref.setSummary(WordUtils.capitalize(autofocusPref.getValue().replace("-", " ")));
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Log.e(TAG, "key: " + key);

        if (key.equals(getResources().getString(R.string.prefs_account_reset_def))) {
            return;
        }

        Preference pref = findPreference(key);
        if (pref == null) {
            return;
        }
        if (key.equals(getResources().getString(R.string.prefs_camera_autofocus_key)) ||
                key.equals(getResources().getString(R.string.prefs_camera_torch_key))) {
            pref.setSummary(WordUtils.capitalize(sharedPreferences.getString(key, "").replace("-", " ")));
        } else {
            pref.setSummary(sharedPreferences.getString(key, ""));
        }
    }

    private List<CharSequence> getCameraSizes(Camera cam) {

        List<CharSequence> sizes = new ArrayList<>();

        if (cam != null) {
            List<Camera.Size> sizeList = cam.getParameters().getSupportedPreviewSizes();
            int checkValue = ScreenUtils.getXScreenSize(context);

            Log.e(TAG, checkValue + "");
            int width, height;

            for (int i = 1; i < sizeList.size(); i++) {
                width = sizeList.get(i).width;
                height = sizeList.get(i).height;
                Log.e(TAG, width + "");
                if (width > checkValue) continue;
                sizes.add(width + "x" + height);
            }
            cam.release();
        }
        return sizes;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == GET_DATABASE_CODE && resultCode == Activity.RESULT_OK) {
            String dataString = data.getDataString();
            String extension = "";
            if (dataString.lastIndexOf(".") > 0 && dataString.lastIndexOf(".") < dataString.length() - 1) {
                extension = dataString.substring(dataString.lastIndexOf(".") + 1);
            }
            if (extension.equals("db") || extension.equals("sqlite") || extension.equals("sqlite3") || extension.equals("db3")) {
                new CopyDatabaseAsyncTask(context, handler).execute(data.getData());
            } else {
                Utils.showInfoDialog(context, context.getString(R.string.extension_error_title), context.getString(R.string.extension_error_message));
            }

            Log.e(TAG, data.getDataString() + " " + extension);
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}