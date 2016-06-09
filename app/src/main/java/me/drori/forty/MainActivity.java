package me.drori.forty;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Pair;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public final static String CALENDAR_PREFERENCE_LIST = "list_calendar";
    private final static int FORTY_PERMISSIONS_REQUEST_CODE = 42;
    private final static String PREFERENCE_FRAGMENT_TAG = "preference_fragment_tag";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(null);
        setContentView(R.layout.activity_main);
        if (!hasPermission()) {
            askPermissions();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (hasPermission()) {
            setScreen(true);
        } else {
            setScreen(false);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        removeFragment();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions,
                                           int[] grantResults) {
        switch (requestCode) {
            case FORTY_PERMISSIONS_REQUEST_CODE:
                for (int result : grantResults) {
                    if (result != PackageManager.PERMISSION_GRANTED) {
                        askPermissions();
                    }
                }

                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
        if (!hasNotificationAccess()) {
            startActivity(new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"));
        }
    }

    private void removeFragment() {
        Fragment preferenceFragment = getSupportFragmentManager().findFragmentByTag(PREFERENCE_FRAGMENT_TAG);
        if (preferenceFragment != null) {
            getSupportFragmentManager().beginTransaction().remove(preferenceFragment).commit();
        }
    }

    private boolean hasNotificationAccess() {
        ContentResolver contentResolver = this.getContentResolver();
        String enabledNotificationListeners = Settings.Secure.getString(contentResolver, "enabled_notification_listeners");
        String packageName = this.getPackageName();

        // check to see if the enabledNotificationListeners String contains our package name
        return !(enabledNotificationListeners == null || !enabledNotificationListeners.contains(packageName));
    }

    private void askPermissions() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.READ_CALENDAR,
                        Manifest.permission.WRITE_CALENDAR,
                        Manifest.permission.GET_ACCOUNTS},
                FORTY_PERMISSIONS_REQUEST_CODE);
    }

    private boolean hasPermission() {
        int permissionCheckCalendarWrite = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_CALENDAR);
        int permissionCheckCalendarRead = ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_CALENDAR);
        int permissionCheckGetAccount = ContextCompat.checkSelfPermission(this,
                Manifest.permission.GET_ACCOUNTS);
        if (permissionCheckCalendarRead != PackageManager.PERMISSION_GRANTED ||
                permissionCheckCalendarWrite != PackageManager.PERMISSION_GRANTED ||
                permissionCheckGetAccount != PackageManager.PERMISSION_GRANTED ||
                !hasNotificationAccess()) {
            return false ;
        } else {
            return true;
        }
    }

    private void setScreen(boolean granted) {
        if (granted) {
            getFragmentManager().beginTransaction()
                    .add(R.id.main, new SettingsFragment(), PREFERENCE_FRAGMENT_TAG)
                    .commit();
        } else {
            removeFragment();
        }
    }

    public static class SettingsFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(null);
            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.preferences);

            final ListPreference listPreference = (ListPreference) findPreference(CALENDAR_PREFERENCE_LIST);
            Calendar calendar = new Calendar();
            List<Pair<String, String>> calendars = calendar.getCalendars();
            List<String> calendar_names = new ArrayList<>();
            List<String> calendar_ids = new ArrayList<>();
            for (Pair<String, String> tup : calendars) {
                calendar_names.add(tup.first);
                calendar_ids.add(tup.second);
            }
            final CharSequence[] entries = calendar_names.toArray(new CharSequence[calendar_names.size()]);
            final CharSequence[] values = calendar_ids.toArray(new CharSequence[calendar_ids.size()]);
            listPreference.setEntries(entries);
            listPreference.setEntryValues(values);
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
            String calendar_id = sharedPref.getString(CALENDAR_PREFERENCE_LIST, "");
            if (calendar_ids.contains(calendar_id)) {
                listPreference.setDefaultValue(calendar_id);
            } else {
                listPreference.setDefaultValue(calendar_ids.get(0));
                listPreference.setSummary(R.string.calendar_settings_summary_none);
            }
            listPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object o) {
                    listPreference.setSummary(R.string.calendar_settings_summary);
                    return true;
                }
            });
        }
    }
}
