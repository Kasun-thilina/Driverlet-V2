package com.example.safe.drivelert;

import android.Manifest;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.speech.SpeechRecognizer;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentActivity;
import androidx.multidex.MultiDex;

import com.example.safe.drivelert.Authentication.LoginActivity;
import com.example.safe.drivelert.EmergencyMode.Data;
import com.example.safe.drivelert.Fragments.AvailableCitiesFragment;
import com.example.safe.drivelert.Utility.Const;
import com.example.safe.drivelert.Utility.TinyDB;
import com.google.android.material.navigation.NavigationView;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;

import br.com.bloder.magic.view.MagicButton;

public class MainActivity extends FragmentActivity
        implements NavigationView.OnNavigationItemSelectedListener, LocationListener, GpsStatus.Listener {
    FrameLayout frame;
    Button agree,disagree;
    TextView mHeader;

    TinyDB tinyDB;
    public int PERMISSION_CODE = 23;
    String[] PERMISSION = {Manifest.permission.ACCESS_FINE_LOCATION};
    //----------Emergency Mode------
    private NfcAdapter mNfcAdapter;
    private boolean isEmergency=true; //Boolean variable used to activate emergency mode on specific scenarios
    private boolean isDialogshowing=false; //Boolean variable used to dismiss the emergency mode timer in specific scenarios
    private boolean isDriving,hasDriven,isDrivingDialogShown,run=false;//Boolean variable used to detect  the driving mode
    private boolean tagDetach=false;//Boolean variable used to detect the tag detach scenarios
    private boolean isCorrect=false;//Boolean variable used to detect the correct NFC tag with the specific code in it
    private boolean isConnected=false;//Boolean variable to check Raspberry PI Connection
    Boolean proceed=false;
    private int nfcError=0,dialogCounter=0;
    private String longitude="0.0",latitude="0.0";
    Dialog dialog;
    android.app.AlertDialog errorDialog;
    private ImageView nfcIcon,RSPIcon;
    private ImageView gpsIcon;
    TextView txtSearching,txtSpeed;
    private static final String TAG = "MainActivity";
    /**
     * For GPS
     */
    SpannableString s ;
    private SharedPreferences sharedPreferences;
    private static Data data;
    double speed;
    private TextView currentSpeed;
    TextView drivingMode;
    private Data.OnGpsServiceUpdate onGpsServiceUpdate;
    private boolean firstfix;
    /**For Speech recognition*/
    private SpeechRecognizer speech = null;
    private Intent recognizerIntent;
    boolean useNFC,showSpeed ;
    private LocationManager mLocationManager;
    MagicButton button;
    AtomicBoolean done;

    /**URL For Raspberry PI Should go here*/
    final URL APIURL=new URL("https://dl.dropboxusercontent.com/s/pkp0q84g09tlrg2/api2.json");


    public MainActivity() throws MalformedURLException {
    }
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        mHeader = findViewById(R.id.tv_header);
        frame = (FrameLayout)findViewById(R.id.frame);
        getSupportFragmentManager().beginTransaction().replace(R.id.frame,new monitor_menu()).commit();
        Toast.makeText(getApplicationContext(),"Swipe left for menu",Toast.LENGTH_SHORT).show();
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView =  findViewById(R.id.nav_view);
        View headerView = navigationView.getHeaderView(0);
        TextView navUsername =  headerView.findViewById(R.id.tv_header);


        navigationView.setNavigationItemSelectedListener(this);
        boolean isFirstTime = MyPreferences.isFirst(MainActivity.this);
        tinyDB = new TinyDB(getApplicationContext());
        if(!tinyDB.getBoolean(Const.IS_SIGNED_IN))
        {
            Intent login = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(login);
        }
        else
        {
            navUsername.setText(tinyDB.getString(Const.USER_NAME));
            // navUsername.setText("Kamal");

        }

        if(isFirstTime == true)
        {
            Intent help = new Intent(MainActivity.this, help.class);
            startActivity(help);
        }

        ActivityCompat.requestPermissions(this, PERMISSION, PERMISSION_CODE);
        reqPermission();
        initNFC();
        /**
         * GPS
         */
        data = new Data(onGpsServiceUpdate);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        reqPermission();
        sharedPreferences.edit().putString("key_driveTime","0:0").commit();
        useNFC= (preferences.getBoolean("pref_nfc",true));
        sendRequest();
        button=findViewById(R.id.magic_button);
        done = new AtomicBoolean();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            Toast.makeText(MainActivity.this , "Permission Granted" , Toast.LENGTH_SHORT).show();

        }
    }
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(newBase);
        MultiDex.install(this);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            new AlertDialog.Builder(this)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle("Closing Application")
                    .setMessage("Are you sure you want to close this application?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }

                    })
                    .setNegativeButton("No", null)
                    .show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            getSupportFragmentManager().beginTransaction().replace(R.id.frame,new monitor_menu()).commit();
        }
        else if(id == R.id.help_page)
        {
            Intent hp = new Intent(MainActivity.this,help.class);
            startActivity(hp);
        }
        else if (id == R.id.nav_send) {

            getSupportFragmentManager().beginTransaction().replace(R.id.frame,new contactus()).commit();
        }

        else if(id == R.id.nav_setting)
        {
            getSupportFragmentManager().beginTransaction().replace(R.id.frame,new AvailableCitiesFragment()).commit();
        }
        else if(id == R.id.nav_safedrive_setting)
        {
            getSupportFragmentManager().beginTransaction().replace(R.id.frame,new SettingsActivity.SettingsFragment()).commit();
        }
        if (id == R.id.nav_logout) {

            logout();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void logout() {

        tinyDB.putBoolean(Const.IS_SIGNED_IN , false);
        Intent intent = new Intent(MainActivity.this , LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);

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
                                                uiTransitions(true);
                                            }
                                        });
                                    }
                                    apiConnection.disconnect();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                    Log.d(TAG, "JSON Request Failed(exception)");
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            uiTransitions(false);
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

    @Override
    public void onGpsStatusChanged(int event) {

    }

    /**This Method envoke on device GPS location change*/
    @Override
    public void onLocationChanged(Location location) {
        if (location.hasSpeed()) {
            speed = location.getSpeed() * 3.6;
            longitude=Double.toString(location.getLongitude());
            latitude=Double.toString(location.getLatitude());
            String units="km/h";
            s= new SpannableString(String.format(Locale.ENGLISH, "%.0f %s", speed, units));
            s.setSpan(new RelativeSizeSpan(0.45f), s.length()-units.length()-1, s.length(), 0);
            Log.d(TAG, "Speed: "+speed);
            if (speed>10 &isConnected) {

                if (done.compareAndSet(false, true)) {
                    updateUI();
                }
            }
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

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
                            uiTransitions(true);
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
                        uiTransitions(false);
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
            //updateUI();
            if (isCorrect) {
                uiTransitions(true);
            }
            else
            {
                uiTransitions(false);
            }
        }

        protected void onPostExecute(Void result) {
            if (tagDetach) {
                isEmergency=true;
                //activateEmergency();
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
        final android.app.AlertDialog.Builder dialogIsDriving = new android.app.AlertDialog.Builder(this);
        dialogIsDriving.setCancelable(true);
        // txtSpeed=findViewById(R.id.txtSpeed);
        drivingMode=findViewById(R.id.txtSpeed);
        String valSpeed=s.toString();
        String strSpeed="Speed: ";
        Log.d(TAG, "########Speed2: "+speed);
        gpsIcon.setVisibility(View.VISIBLE);
        isDriving = true;
        hasDriven = true;
        /**
         * Starting new activity if speed is greater than 10 and Raspberry PI Connected
         */
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        boolean automatic = (preferences.getBoolean("pref_automatic",true));
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
                                if (drivingMode != null) {
                                    drivingMode.setText(R.string.msg_driving);
                                    drivingMode.setTextColor(Color.parseColor("#4caf50"));
                                    drivingMode.setText("Starting Facetracker in "+String.valueOf(val));
                                }
                                Log.d(TAG, "Intent counter " + val);
                            }
                        });
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Intent faceTracker=new Intent(MainActivity.this,FaceTrackerActivity.class);
                        faceTracker.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(faceTracker);
                        finish();
                    }
                });
            }
        }).start();
    }


    /**This Method set the GPS Scanner animation and NFC detected , Navigation Mode icons*/
    private void uiTransitions(boolean isfound){
        try {
            txtSearching = findViewById(R.id.txtDriving);
            Animation anim = new AlphaAnimation(0.0f, 1.0f);
            anim.setDuration(50); //You can manage the time of the blink with this parameter
            anim.setStartOffset(800);
            anim.setRepeatMode(Animation.REVERSE);
            anim.setRepeatCount(Animation.INFINITE);
            txtSearching.startAnimation(anim);
            //gifScanning = findViewById(R.id.gifScanner);
            nfcIcon = findViewById(R.id.imgNFC);
            gpsIcon = findViewById(R.id.imgNavigation);
            RSPIcon = findViewById(R.id.imgRSPConnected);
            if (isfound) {
                //gifScanning.setVisibility(View.INVISIBLE);
                if (useNFC & isCorrect & isConnected) {
                    nfcIcon.setVisibility(View.VISIBLE);
                    txtSearching.setText(R.string.txt_NFC_and_Vehicle_Connected);
                    txtSearching.clearAnimation();
                }
                else if(useNFC & isCorrect){
                    nfcIcon.setVisibility(View.VISIBLE);
                    txtSearching.setText(R.string.txt_NFC_Connected);
                }
                else {
                    txtSearching.setText(R.string.txt_ConnectedToVehicle);
                    if (isConnected) {
                        RSPIcon.setVisibility(View.VISIBLE);
                        txtSearching.clearAnimation();
                    } else {
                        RSPIcon.setVisibility(View.INVISIBLE);
                    }
                }

                if (isDriving) {
                    gpsIcon.setVisibility(View.VISIBLE);
                } else {
                    gpsIcon.setVisibility(View.INVISIBLE);
                }
            } else {
                if (useNFC) {
                    txtSearching.setText(R.string.txt_SearchingAll);
                } else {
                    txtSearching.setText(R.string.txt_scanningMessgage);
                    RSPIcon.setVisibility(View.INVISIBLE);
                }

                //gifScanning.setVisibility(View.VISIBLE);
                nfcIcon.setVisibility(View.INVISIBLE);
                gpsIcon.setVisibility(View.INVISIBLE);
            }
        }
        catch (NullPointerException e)
        {
            e.printStackTrace();
        }

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
        android.app.AlertDialog.Builder errorMessage = new android.app.AlertDialog.Builder(this);
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

        android.app.AlertDialog errorDialog = errorMessage.create();
        errorDialog.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        reqPermission();
        uiTransitions(false);
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

    }
    @Override
    protected void onPause() {
        super.onPause();
        reqPermission();
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
        //----------------Emergency Mode--------------
        SharedPreferences.Editor prefsEditor = sharedPreferences.edit();
        prefsEditor.putString("driving", "stop").commit();
        // prevent memory leaks when activity is destroyed
    }


}
