package com.example.bluecatsbeacons;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import com.bluecats.sdk.BCBeacon;
import com.bluecats.sdk.BCBeaconManager;
import com.bluecats.sdk.BCBeaconManagerCallback;
import com.bluecats.sdk.BCBeaconMode;
import com.bluecats.sdk.BlueCatsSDK;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends BaseActivity {

    private static final String TAG = "MainActivity";

    private static final String BLUE_CATS_APP_TOKEN = "YOUR-APP-TOKEN-HERE";

    private final List<BCBeacon> beacons = Collections.synchronizedList(new ArrayList<BCBeacon>());
    private final BeaconSnifferAdapter beaconSnifferAdapter = new BeaconSnifferAdapter(this.beacons);
    private final BCBeaconManager beaconManager = new BCBeaconManager();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RecyclerView recyclerView = this.findViewById(R.id.rcy_beacons_sniffer);
        recyclerView.setAdapter(this.beaconSnifferAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        recyclerView.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(@NonNull RecyclerView recyclerView, @NonNull MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    View childView = recyclerView.findChildViewUnder(motionEvent.getX(), motionEvent.getY());
                    BeaconSnifferAdapter.ViewHolder viewHolder = (BeaconSnifferAdapter.ViewHolder)recyclerView.getChildViewHolder(childView);
                    BCBeacon beacon = viewHolder.getBeacon();
                    MainActivity.alertUser(MainActivity.this, MainActivity.generateBeaconDisplayMessage(beacon), "Beacon Details");
                    return true;
                }

                return false;
            }

            @Override
            public void onTouchEvent(@NonNull RecyclerView recyclerView, @NonNull MotionEvent motionEvent) {
            }

            @Override
            public void onRequestDisallowInterceptTouchEvent(boolean b) {
            }
        });

        BlueCatsSDK.startPurringWithAppToken(this.getApplicationContext(), MainActivity.BLUE_CATS_APP_TOKEN);
    }

    @Override
    protected void onResume() {
        super.onResume();

        Log.d(MainActivity.TAG, "onResume");

        BlueCatsSDK.didEnterForeground();
        this.beaconManager.registerCallback(this.beaconManagerCallback);
    }

    @Override
    protected void onPause() {
        super.onPause();

        Log.d(MainActivity.TAG, "onPause");

        BlueCatsSDK.didEnterBackground();
        this.beaconManager.unregisterCallback(this.beaconManagerCallback);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        Log.d(MainActivity.TAG, "onDestroy");

        BlueCatsSDK.stopPurring();
        this.beaconManager.unregisterCallback(this.beaconManagerCallback);
    }

    public static boolean isTrackedBeacon(BCBeacon beacon) {
        boolean isTracked = beacon.isBlueCats() && beacon.getBeaconMode().getBeaconModeID() == BCBeaconMode.BC_BEACON_MODE_ID_SECURE;
        if (!isTracked) {
            Log.d(MainActivity.TAG, "Ignoring non-bluecats or non-secure beacon");
        }

        return isTracked;
    }

    public static String generateBeaconDisplayMessage(BCBeacon beacon) {
        StringBuilder builder = new StringBuilder();
        builder.append(String.format("Serial #: %s\n", beacon.getSerialNumber()));
        builder.append(String.format("Region: %s\n", beacon.getBeaconRegion().getName()));
        builder.append(String.format("Site: %s\n", beacon.getSiteName()));
        builder.append(String.format("Accuracy: %s\n", String.valueOf(Math.round(beacon.getAccuracy() * 100) / 100)));
        builder.append(String.format("Firmware: %s\n", beacon.getFirmwareVersion()));
        return builder.toString();
    }

    public static void alertUser(Context context, String message, String title) {
        AlertDialog.Builder alert = new AlertDialog.Builder(context);
        alert.setTitle(title);
        alert.setMessage(message);
        alert.setPositiveButton("OK", null);
        alert.show();
    }

    private final BCBeaconManagerCallback beaconManagerCallback = new BCBeaconManagerCallback()
    {
        @Override
        public void didEnterBeacons(final List<BCBeacon> beacons)
        {
            Log.d(MainActivity.TAG, "didEnterBeacons: " + beacons.size() + " beacons found");

            for (final BCBeacon beacon : beacons) {

                if (MainActivity.isTrackedBeacon(beacon)) {
                    if(!MainActivity.this.beacons.contains(beacon))
                    {
                        MainActivity.this.beacons.add(beacon);

                        runOnUiThread(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                MainActivity.this.beaconSnifferAdapter.notifyItemInserted(MainActivity.this.beacons.size() - 1);
                            }
                        });
                    }
                }
            }
        }

        @Override
        public void didRangeBeacons(final List<BCBeacon> beacons)
        {
            Log.d(MainActivity.TAG, "didRangeBeacons: " + beacons.size() + " beacons found");

            // A hack to add any beacons that have entered before this callback was created
            final List<BCBeacon> unfoundBeacons = new ArrayList<>();

            for (final BCBeacon beacon : beacons)
            {
                final int index = MainActivity.this.beacons.indexOf(beacon);
                if( index > -1 ) {
                    MainActivity.this.beacons.set(index, beacon);
                }
                else {
                    // Beacon doesn't exist, add it
                    unfoundBeacons.add(beacon);
                }
            }

            if (unfoundBeacons.size() > 0) {
                didEnterBeacons(unfoundBeacons);
            }

            // Beacons are updated on every range, i.e. new RSSI, so update data set every time
            runOnUiThread(new Runnable()
            {
                @Override
                public void run()
                {
                    MainActivity.this.beaconSnifferAdapter.notifyDataSetChanged();
                }
            });
        }

        @Override
        public void didExitBeacons(final List<BCBeacon> beacons)
        {
            Log.d(MainActivity.TAG, "didExitBeacons: " + beacons.size() + " beacons found");
            for (final BCBeacon beacon : beacons)
            {
                if (MainActivity.this.beacons.contains(beacon))
                {
                    final int index = MainActivity.this.beacons.indexOf(beacon);
                    MainActivity.this.beacons.remove(index);
                    runOnUiThread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            MainActivity.this.beaconSnifferAdapter.notifyItemRemoved(index);
                        }
                    });
                }
            }
        }
    };
}
