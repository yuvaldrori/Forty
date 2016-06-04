package me.drori.forty;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    private final static int FORTY_PERMISSIONS_REQUEST_CODE = 42;
    private Button permissionButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        permissionButton = (Button) findViewById(R.id.buttonPermission);
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkPermission();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions,
                                           int[] grantResults) {
        switch (requestCode) {
            case FORTY_PERMISSIONS_REQUEST_CODE:
                for (int result : grantResults) {
                    if (result != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                }

                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
        setPermissionButton(false);
        if (!hasNotificationAccess()) {
            startActivity(new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"));
        }
    }

    private boolean hasNotificationAccess() {
        ContentResolver contentResolver = this.getContentResolver();
        String enabledNotificationListeners = Settings.Secure.getString(contentResolver, "enabled_notification_listeners");
        String packageName = this.getPackageName();

        // check to see if the enabledNotificationListeners String contains our package name
        return !(enabledNotificationListeners == null || !enabledNotificationListeners.contains(packageName));
    }

    public void permissionButtonOnClick(View view) {
        askPermissions();
    }

    private void askPermissions() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.READ_CALENDAR,
                        Manifest.permission.WRITE_CALENDAR,
                        Manifest.permission.GET_ACCOUNTS},
                FORTY_PERMISSIONS_REQUEST_CODE);
    }

    private void checkPermission() {
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
            setPermissionButton(false);
        } else {
            setPermissionButton(true);
        }
    }

    private void setPermissionButton(boolean granted) {
        if (granted) {
            permissionButton.setVisibility(View.GONE);
        } else {
            permissionButton.setVisibility(View.VISIBLE);
            permissionButton.setBackgroundColor(Color.RED);
            String fix_permissions = getString(R.string.fix_permissions);
            permissionButton.setText(fix_permissions);
        }
    }
}
