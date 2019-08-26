package com.example.safe.drivelert;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Geocoder;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.telephony.SmsManager;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.safe.drivelert.EmergencyMode.Data;
import com.example.safe.drivelert.Utility.Const;
import com.example.safe.drivelert.Utility.TinyDB;
import com.example.safe.drivelert.Utility.Utils;
import com.example.safe.drivelert.ui.camera.CameraSourcePreview;
import com.example.safe.drivelert.ui.camera.GraphicOverlay;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;
import com.google.android.material.snackbar.Snackbar;
import com.google.common.hash.Hashing;
import com.google.gson.Gson;

import net.gotev.speech.Speech;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

/**
 * Activity for the face tracker app.  This app detects faces with the rear facing camera, and draws
 * overlay graphics to indicate the position, size, and ID of each face.
 */
public final class FaceTrackerActivity extends AppCompatActivity implements LocationListener, GpsStatus.Listener, TextToSpeech.OnInitListener, TextToSpeech.OnUtteranceCompletedListener, RecognitionListener {
    private static final String TAG = "FaceTracker";

    private CameraSource mCameraSource = null;
    private Button end_button;
    private ToggleButton n_mode;
    private TextView tv, tv_1, tv_2;
    static int count = 0, count1 = 0;
    private LinearLayout layout;
    private MediaPlayer mp;
    private CameraSourcePreview mPreview;
    private GraphicOverlay mGraphicOverlay;
    private String start_2;
    private String key = "facetrackeractivity";
    private String key_2 = "safe's project";
    private String key_3 = "hello";
    private String key_4 = "senstivity";
    private int s_status, s_time;
    private static final int RC_HANDLE_GMS = 9001;
    // permission request codes need to be < 256
    private static final int RC_HANDLE_CAMERA_PERM = 2;
    public int flag = 0;


    TextView mSpeedLimitTextView;
    TextView mSpeedCurrentTextView;
    TextView mSpeedoMeterTextView;
    ImageView speedoMeter;
    ConstraintLayout mLayout;
    private TextToSpeech myTTS;

    TinyDB tinyDB;
    private LocationManager mLocationManager;
    boolean isPlayed, isWarningIssued, isForeground;

    private Timer listeningTimer, timer;
    private long TTStimer = 0;
    int currentSpeed;
    TextView mCityName;
    private boolean isValueChanged = false;
    private String matchedCityName = "";
    private boolean isAutomatic;
    private Button mChange;
    private AlertDialog alertDialog;

    private SharedPreferences sharedPreferences;
    //----------Emergency Mode------
    private NfcAdapter mNfcAdapter;
    private boolean isEmergency=true; //Boolean variable used to activate emergency mode on specific scenarios
    private boolean isDialogshowing=false; //Boolean variable used to dismiss the emergency mode timer in specific scenarios
    private boolean isDriving,hasDriven,isDrivingDialogShown,run=false;//Boolean variable used to detect  the driving mode
    private boolean tagDetach=false;//Boolean variable used to detect the tag detach scenarios
    private boolean isCorrect=false;//Boolean variable used to detect the correct NFC tag with the specific code in it
    private boolean isConnected=false;//Boolean variable to check Raspberry PI Connection
    private int nfcError=0,dialogCounter=0;
    private String longitude="0.0",latitude="0.0";
    Dialog dialog;
    AlertDialog errorDialog;
    //private ImageView nfcIcon,RSPIcon;
    //private ImageView gpsIcon;
    // TextView txtSearching,txtSpeed;
    /**URL For Raspberry PI /Sensor Should go here*/
    final URL APIURL=new URL(" https://dl.dropboxusercontent.com/s/pkp0q84g09tlrg2/api2.json");
    final URL SENSORURL=new URL(" https://demo7381782.mockable.io/");
    /**
     * For GPS
     */
    SpannableString s ;
    private static Data data;
    double speed;
    //TextView drivingMode;
    private Data.OnGpsServiceUpdate onGpsServiceUpdate;
    private boolean firstfix;
    /**For Speech recognition*/
    private SpeechRecognizer speech = null;
    private Intent recognizerIntent;
    boolean useNFC,showSpeed ;
    String start;
    public FaceTrackerActivity() throws MalformedURLException {
    }

    //==============================================================================================
    // Activity Methods
    //==============================================================================================

    /**
     * Initializes the UI and initiates the creation of a face detector.
     */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.main);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        prevInit();
        init();
        getLastLocationNewMethod();

        reqPermission();
        initNFC();
        /**
         * GPS
         */
        data = new Data(onGpsServiceUpdate);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        /**END
         * Begining of Speech recognition service
         * */
        // start speech recogniser
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO},1);
        }
        reqPermission();
        resetSpeechRecognizer();
        setRecogniserIntent();
        speech.startListening(recognizerIntent);
        useNFC= (preferences.getBoolean("pref_nfc",true));
        Speech.init(this, getPackageName());
        sharedPreferences.edit().putString("key_driveTime","0:0").commit();

    }

    private void getLastLocationNewMethod() {

        FusedLocationProviderClient mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {

                        Log.v("LOCATION DATA", "data");
                    }

                });
    }


    private void prevInit() {
        mPreview = (CameraSourcePreview) findViewById(R.id.preview);
        mGraphicOverlay = (GraphicOverlay) findViewById(R.id.faceOverlay);
        end_button = (Button) findViewById(R.id.button);
        layout = (LinearLayout) findViewById(R.id.topLayout);
        n_mode = (ToggleButton) findViewById(R.id.toggleButton);
        n_mode.setTextOn("N-Mode ON");
        n_mode.setText("N-Mode");
        n_mode.setTextOff("N-Mode OFF");
        n_mode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mPreview.setVisibility(View.INVISIBLE);
                    //   speedoMeter.setVisibility(View.INVISIBLE);
                    speedoMeter.setImageDrawable(getResources().getDrawable(R.drawable.box));
                    Toast.makeText(getApplicationContext(), "Increase Brightness to maximum for higher accuracy", Toast.LENGTH_LONG).show();

                } else {
                    mPreview.setVisibility(View.VISIBLE);
                    //  speedoMeter.setVisibility(View.VISIBLE);
                    speedoMeter.setImageDrawable(getResources().getDrawable(R.drawable.bluespeedometer));

                }
            }
        });
        tv = (TextView) findViewById(R.id.textView3);
        tv_1 = (TextView) findViewById(R.id.textView4);
        final AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int c = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        if (c == 0) {
            Toast.makeText(getApplicationContext(), "Volume is MUTE", Toast.LENGTH_LONG).show();
        }
        Intent intent_2 = getIntent();
        //final String start = intent_2.getStringExtra(key_2);
        start =sharedPreferences.getString(key_2, "0");
        start_2 = start;
        //String time_info = intent_2.getStringExtra(key_4);
        String time_info =sharedPreferences.getString(key_4, "5");
        s_status = Integer.parseInt(time_info);
        if (s_status == 0) {
            s_time = 500;
        } else if (s_status == 1) {
            s_time = 750;
        } else if (s_status == 2) {
            s_time = 1000;
        } else if (s_status == 3) {
            s_time = 1250;
        } else if (s_status == 4) {
            s_time = 1500;
        } else if (s_status == 5) {
            s_time = 1750;
        } else if (s_status == 6) {
            s_time = 2000;
        } else if (s_status == 7) {
            s_time = 2250;
        } else if (s_status == 8) {
            s_time = 2500;
        }


        View decorview = getWindow().getDecorView(); //hide navigation bar
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        decorview.setSystemUiVisibility(uiOptions);

        end_button.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                count = 0;
                count1 = 0;
                startEndActivity();
                return false;
            }
        });



        // Check for the camera permission before accessing the camera.  If the
        // permission is not granted yet, request permission.
        int rc = ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        if (rc == PackageManager.PERMISSION_GRANTED) {
            createCameraSource();
        } else {
            requestCameraPermission();
        }
    }
    private  void startEndActivity(){
        Intent next = new Intent(FaceTrackerActivity.this, end.class);
        next.putExtra(key_3, start);
        next.putExtra(key, tv_1.getText());
        next.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(next);
        FaceTrackerActivity.this.finish();
    }

    private void init() {

        mSpeedLimitTextView = findViewById(R.id.valueMaxSpeed);
        mSpeedCurrentTextView = findViewById(R.id.valueCurrentSpeed);
        mSpeedoMeterTextView = findViewById(R.id.textView_speedometerValue);
        speedoMeter = findViewById(R.id.imageView6);
        mLayout = findViewById(R.id.relativeLayout3);
        mCityName = findViewById(R.id.tv_cityNameValue);
        mChange = findViewById(R.id.change);

        Typeface face = Typeface.createFromAsset(getAssets(), "fonts/longhaul.ttf");
        Typeface face2 = Typeface.createFromAsset(getAssets(), "fonts/gothicb.ttf");

        mSpeedoMeterTextView.setTypeface(face);
        mSpeedLimitTextView.setTypeface(face2);
        mSpeedCurrentTextView.setTypeface(face2);
        mCityName.setTypeface(face2);


        tinyDB = new TinyDB(this);
        myTTS = new TextToSpeech(this, this);
        mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        isAutomatic = tinyDB.getBoolean(Const.IS_AUTOMATIC);
        int saved_speed = tinyDB.getInt(Const.ORIGINAL_SPEED);
        if (saved_speed > 0) {
            mSpeedLimitTextView.setText(saved_speed + " km/h");
        }

        mChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //manuallyChangeCity("kasur");
            }
        });
    }


    private void requestCameraPermission() {
        Log.w(TAG, "Camera permission is not granted. Requesting permission");

        final String[] permissions = new String[]{Manifest.permission.CAMERA};

        if (!ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.CAMERA)) {
            ActivityCompat.requestPermissions(this, permissions, RC_HANDLE_CAMERA_PERM);
            return;
        }

        final Activity thisActivity = this;

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ActivityCompat.requestPermissions(thisActivity, permissions,
                        RC_HANDLE_CAMERA_PERM);
            }
        };

        Snackbar.make(mGraphicOverlay, R.string.permission_camera_rationale,
                Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.ok, listener)
                .show();

    }

    private void createCameraSource() {

        Context context = getApplicationContext();
        FaceDetector detector = new FaceDetector.Builder(context)
                .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
                .build();

        detector.setProcessor(
                new MultiProcessor.Builder<>(new GraphicFaceTrackerFactory())
                        .build());

        if (!detector.isOperational()) {
            // Note: The first time that an app using face API is installed on a device, GMS will
            // download a native library to the device in order to do detection.  Usually this
            // completes before the app is run for the first time.  But if that download has not yet
            // completed, then the above call will not detect any faces.
            //
            // isOperational() can be used to check if the required native library is currently
            // available.  The detector will automatically become operational once the library
            // download completes on device.
            Toast.makeText(getApplicationContext(), "Dependencies are not yet available. ", Toast.LENGTH_LONG).show();
            Log.w(TAG, "Face detector dependencies are not yet available.");
        }

        mCameraSource = new CameraSource.Builder(context, detector)
                .setRequestedPreviewSize(640, 480)
                .setFacing(CameraSource.CAMERA_FACING_FRONT)
                .setRequestedFps(45.0f)
                .build();

    }


    @Override
    protected void onResume() {
        super.onResume();
        startCameraSource();
        isForeground = true;


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions

            Toast.makeText(FaceTrackerActivity.this, "Location Permission Required For Some Features", Toast.LENGTH_SHORT).show();

            return;
        }


        if (mLocationManager.getAllProviders().indexOf(LocationManager.GPS_PROVIDER) >= 0) {
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100, 1, this);
        } else {
            Log.w("MainActivity", "No GPS location provider found. GPS data display will not be available.");
        }

        if (!mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            showGpsDisabledDialog();
        }
        mLocationManager.addGpsStatusListener(this);

        //---------------Emergency Mode---------------------
        IntentFilter tagDetected = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
        IntentFilter ndefDetected = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
        IntentFilter techDetected = new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED);
        IntentFilter[] nfcIntentFilter = new IntentFilter[]{techDetected,tagDetected,ndefDetected};
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        if(mNfcAdapter!= null)
            mNfcAdapter.enableForegroundDispatch(this, pendingIntent, nfcIntentFilter, null);
        /**
         * GPS
         */
        firstfix = true;
        if (!data.isRunning()){
            Gson gson = new Gson();
            String json = sharedPreferences.getString("data", "");
            data = gson.fromJson(json, Data.class);
        }
        if (data == null){
            data = new Data(onGpsServiceUpdate);
        }else{
            data.setOnGpsServiceUpdate(onGpsServiceUpdate);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1);
        }
        else {
            if (mLocationManager.getAllProviders().indexOf(LocationManager.GPS_PROVIDER) >= 0) {
                mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 500, 0, this);
            } else {
                Log.w("MainActivity", "No GPS location provider found. GPS data display will not be available.");
            }

            if (!mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                showErrorDialog("gps");
            }
            try {
                mNfcAdapter.isEnabled();
                if (useNFC) {
                    if (!mNfcAdapter.isEnabled()) {
                        showErrorDialog("nfc");
                    }
                }
            } catch (NullPointerException e) {
                if (useNFC) {
                    showErrorDialog("nonfc");
                    nfcError++;
                }
            }
            mLocationManager.addGpsStatusListener(this);
        }
        /***/
        /**
         * Speech recognition , Starting the Speech service again onResume of the app
         */
        if (recognizerIntent!=null) {
            resetSpeechRecognizer();
            speech.startListening(recognizerIntent);
        }
    }


    public void showGpsDisabledDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("PERMISSION");
        builder.setPositiveButton("accept", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startActivity(new Intent("android.settings.LOCATION_SOURCE_SETTINGS"));
            }
        });
        builder.show();


    }


    @Override
    protected void onPause() {
        super.onPause();
        mPreview.stop();
        isForeground = false;

        //----------------Emergency Mode---------------
        mLocationManager.removeUpdates(this);
        mLocationManager.removeGpsStatusListener(this);
        SharedPreferences.Editor prefsEditor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(data);
        prefsEditor.putString("data", json);
        prefsEditor.commit();

        if (recognizerIntent!=null) {
            speech.stopListening();
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mCameraSource != null) {
            mCameraSource.release();
        }
        stop_playing();
        mLocationManager.removeUpdates(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode != RC_HANDLE_CAMERA_PERM) {
            Log.d(TAG, "Got unexpected permission result: " + requestCode);
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            return;
        }

        if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Camera permission granted - initialize the camera source");
            // we have permission, so create the camerasource
            createCameraSource();
            return;
        }

        Log.e(TAG, "Permission not granted: results len = " + grantResults.length +
                " Result code = " + (grantResults.length > 0 ? grantResults[0] : "(empty)"));

        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                finish();
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("ALERT")
                .setMessage(R.string.no_camera_permission)
                .setPositiveButton(R.string.ok, listener)
                .show();
    }

    private void startCameraSource() {

        // check that the device has play services available.
        int code = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(
                getApplicationContext());
        if (code != ConnectionResult.SUCCESS) {
            Dialog dlg =
                    GoogleApiAvailability.getInstance().getErrorDialog(this, code, RC_HANDLE_GMS);
            dlg.show();
        }

        if (mCameraSource != null) {
            try {
                mPreview.start(mCameraSource, mGraphicOverlay);
            } catch (IOException e) {
                Log.e(TAG, "Unable to start camera source.", e);
                mCameraSource.release();
                mCameraSource = null;
            }
        }
    }

    public static int incrementer() {
        count++;
        return (count);
    }

    public static int incrementer_1() {
        count1++;
        return (count1);
    }

    public static int get_incrementer() {
        return (count);
    }

    public void play_media(int id) {
        stop_playing();
        mp = MediaPlayer.create(this, id);
        mp.start();
    }

    public void stop_playing() {
        if (mp != null) {
            mp.stop();
            mp.release();
            mp = null;
        }
    }

    public void alert_box() {
        play_media(R.raw.alarm);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                play_media(R.raw.alarm);
                AlertDialog dig;
                dig = new AlertDialog.Builder(FaceTrackerActivity.this)
                        .setTitle("Drowsy Alert !!!")
                        .setMessage("Tracker suspects that the driver is experiencing Drowsiness, Touch OK to Stop the Alarm")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                stop_playing();
                                flag = 0;
                            }
                        }).setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
                dig.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        stop_playing();
                        flag = 0;
                    }
                });
            }
        });


    }

    @Override
    public void onGpsStatusChanged(int event) {

        switch (event) {
            case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
                break;

            case GpsStatus.GPS_EVENT_STOPPED:
                if (!mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    showGpsDisabledDialog();
                }
                break;
            case GpsStatus.GPS_EVENT_FIRST_FIX:
                break;
        }
    }

    @Override
    public void onLocationChanged(Location location) {

        if (location.hasSpeed()) {

            currentSpeed = (int) (long) (location.getSpeed() * 3.6);
            int speedLimit = tinyDB.getInt(Const.SPEED_LIMIT);
            mSpeedCurrentTextView.setText(currentSpeed + " km/h ");
            mSpeedoMeterTextView.setText(Integer.toString(currentSpeed));

            if (speedLimit > 0 && (currentSpeed < speedLimit / 2)) {
                mLayout.setBackgroundColor(getResources().getColor(R.color.green));
                isPlayed = false;
                stop_playing();
            } else if (speedLimit > 0 && (currentSpeed > speedLimit / 2) && currentSpeed < speedLimit) {
                mLayout.setBackgroundColor(getResources().getColor(R.color.yellow));
                isPlayed = false;
                stop_playing();

            } else if (speedLimit > 0 && (currentSpeed > speedLimit)) {

                mLayout.setBackgroundColor(getResources().getColor(R.color.red));
                final Handler handler = new Handler();
                if (!isPlayed && isForeground) {
                    play_media(R.raw.beep_merged);
                    isPlayed = true;
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {

                            if (!isWarningIssued) {
                                myTTS.speak("Your Current Speed exceeds from speed limit Slow Down", TextToSpeech.QUEUE_FLUSH, null);
                                isWarningIssued = true;
                                initializeTimer();

                            }
                        }
                    }, 3500);
                }


            }
            //-------------------Emergency Mode-------------------
            speed = location.getSpeed() * 3.6;
            longitude=Double.toString(location.getLongitude());
            latitude=Double.toString(location.getLatitude());
            String units="km/h";
            s= new SpannableString(String.format(Locale.ENGLISH, "%.0f %s", speed, units));
            s.setSpan(new RelativeSizeSpan(0.45f), s.length()-units.length()-1, s.length(), 0);
            updateUI();

        }

        try {
            getCityName(location.getLatitude(), location.getLongitude());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void getCityName(double MyLat, Double MyLong) throws IOException {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        List<Address> addresses = geocoder.getFromLocation(MyLat, MyLong, 1);

        if (addresses.get(0).getLocality() != null) {

            String cityName = addresses.get(0).getLocality().toLowerCase();
            if (!cityName.equals("") && !cityName.equals(matchedCityName)) {

                mCityName.setText(cityName);

                if (isAutomatic) {

                    String hash = Utils.calculateHash(cityName);

                    if (tinyDB.getBoolean(hash)) {

                        Utils.showCityChangeDialogue(this,
                                cityName,
                                "You entered into ",
                                "Alert speed has been adjusted to 50 km/h");
                        mSpeedLimitTextView.setText("50 Km/h");
                        tinyDB.putInt(Const.SPEED_LIMIT, 50);
                        isValueChanged = true;
                        matchedCityName = cityName;
                        return;

                    }


                    if (isValueChanged) {
                        tinyDB.putInt(Const.SPEED_LIMIT, tinyDB.getInt(Const.ORIGINAL_SPEED));
                        mSpeedLimitTextView.setText(Integer.toString(tinyDB.getInt(Const.ORIGINAL_SPEED)) + " km/h");
                        Utils.showCityChangeDialogue(
                                this,
                                matchedCityName,
                                "You just exit from ",
                                "Alert speed has been adjusted to original speed");

                        matchedCityName = cityName;
                        isValueChanged = false;
                    }
                }
            }
        }
    }

//    private void manuallyChangeCity(String cityName) {
//        if (!cityName.equals("") && !cityName.equals(matchedCityName)) {
//            mCityName.setText(cityName);
//            if (isAutomatic) {
//
//                String hash =  Utils.calculateHash(cityName);
//
//                if (tinyDB.getBoolean(hash)) {
//
//                        Utils.showCityChangeDialogue(this,
//                                cityName,
//                                "You entered into ",
//                                "Alert speed has been adjusted to 50 km/h");
//                        mSpeedLimitTextView.setText("50 Km/h");
//                        tinyDB.putInt(Const.SPEED_LIMIT, 50);
//                        isValueChanged = true;
//                        matchedCityName = cityName;
//                        return;
//                    }
//                }
//
//
//                if (isValueChanged) {
//                    tinyDB.putInt(Const.SPEED_LIMIT, tinyDB.getInt(Const.ORIGINAL_SPEED));
//                    mSpeedLimitTextView.setText(Integer.toString(tinyDB.getInt(Const.ORIGINAL_SPEED)));
//                    matchedCityName = cityName;
//                    isValueChanged = false;
//
//                    Utils.showCityChangeDialogue(
//                            this,
//                            matchedCityName,
//                            "You just exit from ",
//                            "Alert speed has been adjusted to original speed");
//                }
//            }
//        }



    void initializeTimer() {
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (isForeground && isPlayed) {
                    myTTS.speak("Your Current Speed is" + currentSpeed, TextToSpeech.QUEUE_FLUSH, null);
                }
            }
        }, 0, 6000); // called after 1 minute
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    @Override
    public void onInit(int initStatus) {

        if (initStatus == TextToSpeech.SUCCESS) {
            myTTS.setLanguage(Locale.US);
        } else if (initStatus == TextToSpeech.ERROR) {
            Toast.makeText(this, "Sorry! Text To Speech failed...", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onUtteranceCompleted(String s) {

    }

    // Graphic Face Tracker

    private class GraphicFaceTrackerFactory implements MultiProcessor.Factory<Face> {
        @Override
        public Tracker<Face> create(Face face) {
            return new GraphicFaceTracker(mGraphicOverlay);
        }
    }

    private class GraphicFaceTracker extends Tracker<Face> {
        private GraphicOverlay mOverlay;
        private FaceGraphic mFaceGraphic;

        GraphicFaceTracker(GraphicOverlay overlay) {
            mOverlay = overlay;
            mFaceGraphic = new FaceGraphic(overlay);
        }

        /**
         * Start tracking the detected face instance within the face overlay.
         */
        @Override
        public void onNewItem(int faceId, Face item) {
            mFaceGraphic.setId(faceId);
        }


        int state_i, state_f = -1;
        long start, end = System.currentTimeMillis();
        long begin, stop;
        int c;

        @Override
        public void onUpdate(FaceDetector.Detections<Face> detectionResults, Face face) {
            mOverlay.add(mFaceGraphic);
            mFaceGraphic.updateFace(face);
            if (flag == 0) {
                eye_tracking(face);
            }
        }

        @Override
        public void onMissing(FaceDetector.Detections<Face> detectionResults) {
            mOverlay.remove(mFaceGraphic);
            setText(tv_1, "Face Missing");

        }

        @Override
        public void onDone() {
            mOverlay.remove(mFaceGraphic);
        }

        private void setText(final TextView text, final String value) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    text.setText(value);
                }
            });
        }

        private void eye_tracking(Face face) {
            float l = face.getIsLeftEyeOpenProbability();
            float r = face.getIsRightEyeOpenProbability();
            if (l < 0.50 && r < 0.50) {
                state_i = 0;
            } else {
                state_i = 1;
            }
            if (state_i != state_f) {
                start = System.currentTimeMillis();
                if (state_f == 0) {
                    c = incrementer_1();

                }
                end = start;
                stop = System.currentTimeMillis();
            } else if (state_i == 0 && state_f == 0) {
                begin = System.currentTimeMillis();
                if (begin - stop > s_time) {
                    c = incrementer();
                    alert_box();
                    flag = 1;
                }
                begin = stop;
            }
            state_f = state_i;
            status();
        }

        public void status() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    int s = get_incrementer();
                    if (s < 5) {
                        setText(tv_1, "Active");
                        tv_1.setTextColor(Color.GREEN);
                        tv_1.setTypeface(Typeface.DEFAULT_BOLD);
                    }
                    if (s > 4) {
                        setText(tv_1, "Sleepy");
                        tv_1.setTextColor(Color.YELLOW);
                        tv_1.setTypeface(Typeface.DEFAULT_BOLD);
                    }
                    if (s > 8) {
                        setText(tv_1, "Drowsy");
                        tv_1.setTextColor(Color.RED);
                        tv_1.setTypeface(Typeface.DEFAULT_BOLD);
                    }


                }
            });

        }

    }

    @Override
    public void onBackPressed() {
        Intent next = new Intent(FaceTrackerActivity.this, end.class);
        count = 0;
        count1 = 0;
        next.putExtra(key_3, start_2);
        next.putExtra(key, tv_1.getText());
        next.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(next);
        FaceTrackerActivity.this.finish();
    }
    //------------------------Emergency Mode-----------------------------------
    /** Raspberry Pi connection / Sensor Connection
     */
    public void sendRequest() {
        final Handler handler = new Handler();
        Timer timer = new Timer();
        TimerTask doAsynchronousTask = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    public void run() {
                        AsyncTask.execute(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    HttpURLConnection apiConnection =
                                            (HttpURLConnection) APIURL.openConnection();
                                    apiConnection.setRequestProperty("Content-Type", "text/plain");
                                    /* if (myConnection.getResponseCode() == 200) {*/
                                    // Success
                                    InputStream responseBody = apiConnection.getInputStream();
                                    InputStreamReader responseBodyReader =
                                            new InputStreamReader(responseBody, "UTF-8");
                                    BufferedReader r = new BufferedReader(new InputStreamReader(responseBody));
                                    StringBuilder total = new StringBuilder();
                                    for (String line; (line = r.readLine()) != null; ) {
                                        total.append(line);
                                    }
                                    String result = total.toString();
                                    //String result = "true";
                                    Log.d(TAG, "JSON ResponseBody :" + result);

                                    if (result.equalsIgnoreCase("True"))
                                    {
                                        isConnected=true;
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                //uiTransitions(true);
                                            }
                                        });
                                    }
                                    apiConnection.disconnect();
                                    /***Sensor Connection  **/
                                    HttpURLConnection sensorConnection =
                                            (HttpURLConnection) SENSORURL.openConnection();
                                    sensorConnection.setRequestProperty("Content-Type", "text/plain");
                                    InputStream seonsorResponseBody = apiConnection.getInputStream();
                                    InputStreamReader sensorResponseBodyReader =
                                            new InputStreamReader(seonsorResponseBody, "UTF-8");
                                    BufferedReader r2 = new BufferedReader(new InputStreamReader(seonsorResponseBody));
                                    StringBuilder readVal = new StringBuilder();
                                    for (String line; (line = r2.readLine()) != null; ) {
                                        readVal.append(line);
                                    }
                                    String sensorResult = readVal.toString();
                                    //String result = "true";
                                    Log.d(TAG, "JSON ResponseBody :" + sensorResult);

                                    if (sensorResult.equalsIgnoreCase("True"))
                                    {

                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                activateEmergency();
                                            }
                                        });
                                    }


                                    sensorConnection.disconnect();
                                } catch (IOException  e) {
                                    e.printStackTrace();
                                    Log.d(TAG, "JSON Request Failed(exception)");
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            //uiTransitions(false);
                                        }
                                    });
                                }

                            }
                        });
                    }
                });
            }
        };
        timer.schedule(doAsynchronousTask, 0, 10000); //execute in every 10000 ms
    }

    /**
     * NFC Reader Initialization
     */
    private void initNFC(){
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
    }
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        Log.d(TAG, "onNewIntent: " + intent.getAction());
        if (tag != null) {
            Toast.makeText(this, getString(R.string.message_tag_detected), Toast.LENGTH_SHORT).show();
            final Ndef ndef = Ndef.get(tag);
            onNfcDetected(ndef);
        }
    }
    /**This Method envokes when a NFC tag is detected by the device*/
    public void onNfcDetected(Ndef ndef){
        reqPermission();
        new ProcessNFCTask().execute(ndef);
    }
    /** Async Task(Runs in background thread) to detect the correct NFC tag and read it in a indefinite loop */
    public class ProcessNFCTask extends AsyncTask<Ndef, NdefMessage, Void> {
        @Override
        protected void onPreExecute() {
            if (isDialogshowing) {
                dialog.dismiss();
                isDialogshowing=false;
                isEmergency = false;
            }
        }
        protected Void doInBackground(Ndef... tag) {
            Ndef ndef=tag[0];
            try
            {
                ndef.connect();
                NdefMessage ndefMessage = ndef.getNdefMessage();
                ndef.close();
                String message = new String(ndefMessage.getRecords()[0].getPayload());
                Log.d(TAG, "readFromNFC Before Pass: " + message);
                //Toast.makeText(this, "Text" + message, Toast.LENGTH_LONG).show();
                /*************Value to be checked in the NFC Tag**** write it using a NFC Read Write App as Plain Text*************/
                if (message.equals("in")) {
                    tagDetach=false;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //uiTransitions(true);
                        }
                    });
                    isCorrect=true;
                    //Toast.makeText(this.getApplicationContext(), R.string.message_nfc_holder_detected, Toast.LENGTH_LONG).show();
                    while (1 == 1) {
                        ndef.connect();
                        ndefMessage = ndef.getNdefMessage();
                        message = new String(ndefMessage.getRecords()[0].getPayload());
                        //Log.d(TAG, "readFromNFCPassed: " + message);
                        TimeUnit.SECONDS.sleep(1);
                        ndef.close();
                    }
                } else {
                    //Toast.makeText(this.getApplicationContext(), R.string.message_nfc_holder_error, Toast.LENGTH_LONG).show();
                    ndef.close();
                    isCorrect=false;
                }
                /**
                 * Raspberry PI
                 */

                /**end*/

            } catch (IOException | FormatException | InterruptedException  e ) {
                e.printStackTrace();
                tagDetach=true;
                isCorrect=false;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //uiTransitions(false);
                    }
                });
                //Toast.makeText(this.getApplicationContext(), R.string.message_nfc_holder_detached, Toast.LENGTH_LONG).show();
            }
            catch (NullPointerException e)
            {
                e.printStackTrace();
            }
            return null;
        }
        protected void onProgressUpdate(NdefMessage... progress) {
            updateUI();
            if (isCorrect) {
                //uiTransitions(true);
            }
            else
            {
                //uiTransitions(false);
            }
        }

        protected void onPostExecute(Void result) {
            if (tagDetach) {
                isEmergency=true;
                activateEmergency();
            } else {
                if (isDialogshowing) {
                    dialog.dismiss();
                    //Log.d(TAG, "dissmiss fro isdialogshowing true 2 " );
                    isDialogshowing = false;
                }
            }

        }
    }
    /**This Method is Updating the Speed value and driving mode values when needed */
    private void updateUI(){
        final AlertDialog.Builder dialogIsDriving = new AlertDialog.Builder(this);
        dialogIsDriving.setCancelable(true);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                   /* String status=pref.getString("driving", "stop");
                    Log.d(TAG, "Driving Status : " + status);
                    if(status.equalsIgnoreCase("stop"))
                    {
                        sharedPreferences.edit().putString("key_driveTime","0:0").commit();
                        sharedPreferences.edit().putString("driving","running").commit();
                        Log.d(TAG, "Driving Status : " + status);
                    }*/
                    String counter=pref.getString("key_driveTime", "0:0");
                    String split[]=counter.split(":");
                    int miniuts=Integer.parseInt(split[0]);
                    int seconds=Integer.parseInt(split[1]);

                    Integer count=(miniuts*60)+seconds;
                    //Double count=Double.parseDouble(counter);
                    while (speed>1)
                    {
                        Thread.sleep(1000);
                        count++;

                        miniuts=count/60;
                        seconds=count%60;
                        String finalVal=String.valueOf(miniuts)+":"+String.valueOf(seconds);
                        //Log.d(TAG, "Driving Time : " + finalVal);
                        sharedPreferences.edit().putString("key_driveTime",finalVal).commit();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
        //drivingMode.setText(R.string.msg_notDriving);
       /* if (showSpeed) {
            drivingMode.setTextColor(Color.parseColor("#a5d6a7"));
        }*/
        // Log.d(TAG, "activeNFC: "+correctTAG);

        String valSpeed=s.toString();
        String strSpeed="Speed: ";
        SpannableString speedValue=  new SpannableString(valSpeed);
        SpannableString speedText=  new SpannableString(strSpeed);
        speedText.setSpan(new RelativeSizeSpan(1.35f), 0,5, 0); // set size
        if (speed > 10) {
            isDriving = true;
            hasDriven=true;
        } else {
            speedValue.setSpan(new ForegroundColorSpan(Color.parseColor("#ef5350")),0,6,0);// set color
            isDriving=false;
            dialogIsDriving.setMessage("Are You Still Driving?");
            dialogIsDriving.setPositiveButton(
                    "Yes",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();

                        }
                    });
            dialogIsDriving.setNegativeButton(
                    "No",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                            boolean useNFC = (preferences.getBoolean("pref_nfc",true));
                            startEndActivity();
                            //uiTransitions(true);

                        }
                    });
            dialogIsDriving.setNeutralButton("Emergency", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    isEmergency=true;
                    activateEmergency();
                }
            });
            if (hasDriven & (!isDrivingDialogShown)) {
                errorDialog = dialogIsDriving.create();
                errorDialog.show();
                Speech.getInstance().say("Are You Still Driving?");
                isDrivingDialogShown=true;
                Timer timer = new Timer();
                timer.schedule(new TimerTask(){
                    @Override
                    public void run() {
                        //This is where we tell it what to do when it hits 60 seconds
                        hasDriven=false;
                        isDrivingDialogShown=false;
                        Log.d(TAG, "*****Delayed" );
                    }
                }, 60000);
            }

        }

    }
    /**
     * Generating the Emergency Mode Window and the logic to read the nfc if canceled etc
     */
    private void activateEmergency()
    {
        try {
            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
            final String dialNo = (pref.getString("key_dialNo", "123"));
            final String message = (pref.getString("key_smsMessage", "123"));
            final String finalMessage
                    =message+" My Current Location:https://www.google.com/maps/search/?api=1&query="+latitude+','+longitude;
            String dialName = pref.getString("key_name", "Kasun");
            Log.d(TAG, "Number " + dialNo);
            final SmsManager smsManager = SmsManager.getDefault();
            final Intent dialer = new Intent(Intent.ACTION_CALL);
            dialer.setData(Uri.parse("tel:" + dialNo));
            //startActivity(dialer);
            dialog = new Dialog(this);
            dialog.setContentView(R.layout.calling_dialog);
            final Button btnCancel = dialog.findViewById(R.id.btnCancel);
            final TextView counter = dialog.findViewById(R.id.counter);

            dialog.setCanceledOnTouchOutside(false);
            if ((!isDriving) & (isConnected)){
                dialog.show();
                isDialogshowing = true;
            }
            new Thread(new Runnable() {
                @Override
                public void run() {
                    for (int i = 10; i > 0; i--) {
                        try {
                            Thread.sleep(1000);

                            final int val = i;
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    counter.setText(String.valueOf(val));
                                    Log.d(TAG, "count " + val);
                                    btnCancel.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            dialog.dismiss();
                                            isEmergency = false;
                                            isDialogshowing = false;
                                        }
                                    });

                                }
                            });
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    if ((!isEmergency) & isDialogshowing)
                    {
                        //Toast.makeText(getApplicationContext(), "Call Not Completed Since Not Driving", Toast.LENGTH_LONG).show();
                        dialog.dismiss();
                        isDialogshowing=false;
                        Log.d(TAG, "dissmiss fro isemergeny false " );
                    }
                    if (isEmergency & isDialogshowing) {
                        dialog.dismiss();
                        isDialogshowing=false;
                        startActivity(dialer);
                        /**Sleeping the background thread for a while because call gets disconnected when sending the message at the same time in a call*/
                        try {
                            Thread.sleep(50000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        smsManager.sendTextMessage(dialNo, null, finalMessage, null, null);
                    }
                }
            }).start();
        } catch (Exception ex) {
            Toast.makeText(getApplicationContext(),ex.getMessage().toString(),
                    Toast.LENGTH_LONG).show();
            ex.printStackTrace();
        }
        speech.startListening(recognizerIntent);
    }

    /**Requesting the needed permissions from the user*/
    private void reqPermission()
    {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO},1);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CALL_PHONE},1);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.SEND_SMS},1);
        }
        else
        {
            return;
        }
    }
    /**Showing error messages when *GPS disabled *NFC disabled and When NFC is not supported by the Device*/
    public void showErrorDialog(String errorType){
        AlertDialog.Builder errorMessage = new AlertDialog.Builder(this);
        errorMessage.setCancelable(true);

        if (errorType.equalsIgnoreCase("gps"))
        {
            errorMessage.setMessage(R.string.errorMsg_GPSDisabled);
            errorMessage.setPositiveButton(
                    "Open Location Settings",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                        }
                    });
        }
        if (errorType.equalsIgnoreCase("nfc"))
        {
            // errorMessage.setMessage(R.string.errorMsg_NFCDisabled);
            /*errorMessage.setPositiveButton(
                    "Open NFC Settings",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            startActivity(new Intent(Settings.ACTION_NFC_SETTINGS));
                        }
                    });*/
        }
        if (errorType.equalsIgnoreCase("nonfc"))
        {
            if (nfcError<=2) {
                /*errorMessage.setMessage(R.string.errorMsg_NFCDNotFound);
                errorMessage.setPositiveButton(
                        "Use the App without NFC",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });*/
            }
        }
        errorMessage.setNegativeButton(
                "Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        AlertDialog errorDialog = errorMessage.create();
        //errorDialog.show();
    }
    public static Data getData() {
        return data;
    }

    /***/


    /**
     *Speech recognition Codes
     */
    private void resetSpeechRecognizer() {
        if (speech != null)
            speech.destroy();
        speech = SpeechRecognizer.createSpeechRecognizer(this);

        Log.i(TAG, "isRecognitionAvailable: " + SpeechRecognizer.isRecognitionAvailable(this));

        if (SpeechRecognizer.isRecognitionAvailable(this))
            speech.setRecognitionListener(this);
        else
            finish();

    }
    private void setRecogniserIntent() {

        recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE,
                "en");
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        //recognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3);
    }

    @Override
    public void onBufferReceived(byte[] buffer) {
        Log.i(TAG, "onBufferReceived: " + buffer);
    }

    @Override
    public void onEndOfSpeech() {
        Log.i(TAG, "onEndOfSpeech");
        speech.stopListening();
    }

    /**Recognized Speech results comes in Here*/
    @Override
    public void onResults(Bundle results) {
        Log.i(TAG, "onResults");
        ArrayList<String> matches = results
                .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        String text = "";
        for (String result : matches) {
            text += result + "\n";
            if (result.equalsIgnoreCase("Emergency")) {
                SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
                final Intent dialer = new Intent(Intent.ACTION_CALL);
                final String dialNo = (pref.getString("key_dialNo", "123"));
                dialer.setData(Uri.parse("tel:"+dialNo));
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED)
                {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.CALL_PHONE},1);
                }
                else {
                    startActivity(dialer);
                    SharedPreferences.Editor prefsEditor = sharedPreferences.edit();
                    prefsEditor.putString("driving", "stop").commit();
                }
            }
            else if (result.equalsIgnoreCase("Yes"))
            {
                if (errorDialog.isShowing())
                {
                    errorDialog.dismiss();
                }
            }
            else if (result.equalsIgnoreCase("No"))
            {
                if (errorDialog.isShowing()) {
                    errorDialog.dismiss();
                    startEndActivity();
                    //isEmergency = true;
                    //activateEmergency();
                }
            }
            else if (result.equalsIgnoreCase("Cancel"))
            {
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
            }
            //returnedText.setText(text);
            speech.startListening(recognizerIntent);
        }
    }

    @Override
    public void onError(int errorCode) {
        String errorMessage = getErrorText(errorCode);
        Log.i(TAG, "FAILED " + errorMessage);
        resetSpeechRecognizer();
        speech.startListening(recognizerIntent);

    }


    @Override
    public void onEvent(int arg0, Bundle arg1) {
        Log.i(TAG, "onEvent");
    }

    @Override
    public void onPartialResults(Bundle arg0) {
        Log.i(TAG, "onPartialResults");
    }

    @Override
    public void onReadyForSpeech(Bundle arg0) {
        //Log.i(TAG, "onReadyForSpeech");
    }
    @Override
    public void onBeginningOfSpeech() {
        Log.i(TAG, "onBeginningOfSpeech********");
        sendRequest();

    }

    @Override
    public void onRmsChanged(float rmsdB) {
        //Log.i(TAG, "onRmsChanged: " + rmsdB);
    }

    public String getErrorText(int errorCode) {
        String message;
        switch (errorCode) {
            case SpeechRecognizer.ERROR_AUDIO:
                message = "Audio recording error";
                break;
            case SpeechRecognizer.ERROR_CLIENT:
                message = "Client side error";
                break;
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                message = "Insufficient permissions";
                break;
            case SpeechRecognizer.ERROR_NETWORK:
                message = "Network error";
                break;
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                message = "Network timeout";
                break;
            case SpeechRecognizer.ERROR_NO_MATCH:
                message = "No match";
                break;
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                message = "RecognitionService busy";
                break;
            case SpeechRecognizer.ERROR_SERVER:
                message = "error from server";
                break;
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                message = "No speech input";
                break;
            default:
                message = "Didn't understand, please try again.";
                break;
        }
        return message;
    }


}



