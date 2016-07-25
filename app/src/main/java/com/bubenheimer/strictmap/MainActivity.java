package com.bubenheimer.strictmap;

import android.os.Bundle;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;

public final class MainActivity extends AppCompatActivity implements OnMapReadyCallback {
    private static final String TAG = MainActivity.class.getSimpleName();

    private static boolean strictModeSet;

    private GoogleApiClient mGoogleApiClient;

    private GoogleApiClient.ConnectionCallbacks mConnectionCallbacks;
    private GoogleApiClient.OnConnectionFailedListener mConnectionFailedHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (BuildConfig.DEBUG) {
            if (!strictModeSet) {
                strictModeSet = true;

                final StrictMode.ThreadPolicy.Builder threadPolicyBuilder =
                        new StrictMode.ThreadPolicy.Builder(StrictMode.getThreadPolicy())
                                .detectDiskReads()
                                .detectDiskWrites()
                                .penaltyDeath()
                                .penaltyLog();
                StrictMode.setThreadPolicy(threadPolicyBuilder.build());
            }
        }

        mConnectionCallbacks = new ConnectionCallbacksHandler();
        mConnectionFailedHandler = new ConnectionFailedHandler();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, mConnectionFailedHandler)
                .addConnectionCallbacks(mConnectionCallbacks)
                .addApi(LocationServices.API)
                .build();

        setContentView(R.layout.activity_main);

        final SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.fragment_map);
        mapFragment.getMapAsync(this);
    }

    @Override
    protected void onDestroy() {
        mGoogleApiClient.unregisterConnectionCallbacks(mConnectionCallbacks);
        mGoogleApiClient = null;
        mConnectionCallbacks = null;
        mConnectionFailedHandler = null;

        super.onDestroy();
    }

    @Override
    protected void onStart() {
        super.onStart();

        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();

        super.onStop();
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
    }

    private final class ConnectionCallbacksHandler implements GoogleApiClient.ConnectionCallbacks {
        @Override
        public void onConnected(final Bundle bundle) {
        }

        @Override
        public void onConnectionSuspended(final int cause) {
            final String causeText;
            switch (cause) {
                case CAUSE_SERVICE_DISCONNECTED:
                    causeText = "Google API service has been killed";
                    break;
                case CAUSE_NETWORK_LOST:
                    causeText = "Network connection lost";
                    break;
                default:
                    causeText = "Unidentified cause";
                    break;
            }
            Log.i(TAG, "onConnectionSuspended(" + cause + "): " + causeText);
        }
    }

    private final class ConnectionFailedHandler
            implements GoogleApiClient.OnConnectionFailedListener {
        @Override
        public void onConnectionFailed(@NonNull final ConnectionResult connectionResult) {
            if (connectionResult.getErrorCode() == ConnectionResult.CANCELED) {
                final String errorMsg =
                        "Google APIs connection error resolution cancelled: " + connectionResult;
                Log.e(TAG, errorMsg, new AssertionError(errorMsg));
            } else {
                final String errorMsg =
                        "Unresolvable Google APIs connection error: " + connectionResult;
                Log.e(TAG, errorMsg, new AssertionError(errorMsg));
            }
            Toast.makeText(MainActivity.this, "Unresolvable Google APIs connection error",
                    Toast.LENGTH_LONG).show();
        }
    }
}
