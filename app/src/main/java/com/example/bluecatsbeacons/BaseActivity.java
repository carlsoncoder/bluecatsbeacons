package com.example.bluecatsbeacons;

import android.content.*;
import android.os.*;
import android.support.v7.app.*;

public class BaseActivity extends AppCompatActivity {
    protected ApplicationPermissions applicationPermissions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.applicationPermissions = new ApplicationPermissions(BaseActivity.this);
        this.applicationPermissions.verifyPermissions();
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (this.applicationPermissions != null) {
                this.applicationPermissions.verifyPermissions();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (this.applicationPermissions != null) {
            this.applicationPermissions.onRequestPermissionResult(requestCode, permissions, grantResults);
        }
    }
}