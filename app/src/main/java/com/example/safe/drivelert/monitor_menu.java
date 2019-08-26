package com.example.safe.drivelert;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.example.safe.drivelert.Utility.Const;
import com.example.safe.drivelert.Utility.TinyDB;

import java.text.DateFormat;
import java.util.Date;

import br.com.bloder.magic.view.MagicButton;

public class monitor_menu extends Fragment {
    MagicButton b;
    SeekBar s;
    TextView ttv;
    Button mTranmission;
    private boolean isAutomatic;
    private static final String TAG = "monitor_menu";
    private SharedPreferences sharedPreferences;
    private String key_2 = "safe's project";
    private String key_4 = "senstivity";

    SeekBar mSpeedLimitSeekbar;
    TextView mSpeedLimitTextView;
    TinyDB tinyDB;

    @SuppressLint("ApplySharedPref")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View root;
        root = inflater.inflate(R.layout.activity_monitor_menu, container, false);
        init(root);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(key_4,""+s.getProgress()).commit();
        String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());
        editor.putString(key_2,""+currentDateTimeString).commit();
        // sharedPreferences.edit().putString(key_4,""+s.getProgress()).commit();
        //   sharedPreferences.edit().putString(key_2,""+DateFormat.getDateTimeInstance().format(new Date())).commit();
        Log.d(TAG, "s :: " + s.getProgress());
        Log.d(TAG, "s :: " + currentDateTimeString);






        return root;

    }

    private void init(final View root) {
        tinyDB = new TinyDB(getActivity());
        isAutomatic = tinyDB.getBoolean(Const.IS_AUTOMATIC);

        mTranmission = root.findViewById(R.id.btn_transmission);
        b = (MagicButton) root.findViewById(R.id.magic_button);
        s = (SeekBar) root.findViewById(R.id.seekBar2);
        ttv = (TextView) root.findViewById(R.id.textView21);
        mSpeedLimitTextView = (TextView) root.findViewById(R.id.tv_speedLimit);
        mSpeedLimitSeekbar = root.findViewById(R.id.seekBar);

        int saved_speed = tinyDB.getInt(Const.ORIGINAL_SPEED);
        if (saved_speed > 0) {
            mSpeedLimitTextView.setText(Integer.toString(saved_speed) + " km/h");
            mSpeedLimitSeekbar.setProgress(saved_speed);
        }

        if(isAutomatic)
        {
            mTranmission.setBackgroundColor(getResources().getColor(R.color.green));
            mTranmission.setText("Manual Mode");
        }


        if (s.getProgress() == 0) {
            ttv.setText("0.5 second");
        } else if (s.getProgress() == 1) {
            ttv.setText("0.75 second");
        } else if (s.getProgress() == 2) {
            ttv.setText("1 seconds");
        } else if (s.getProgress() == 3) {
            ttv.setText("1.25 seconds");
        } else if (s.getProgress() == 4) {
            ttv.setText("1.5 seconds");
        } else if (s.getProgress() == 5) {
            ttv.setText("1.75 seconds");
        } else if (s.getProgress() == 6) {
            ttv.setText("2 seconds");
        } else if (s.getProgress() == 7) {
            ttv.setText("2.25 seconds");
        } else {
            ttv.setText("2.5 seconds");
        }
        s.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @SuppressLint("ApplySharedPref")
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {
                if (s.getProgress() == 0) {
                    ttv.setText("0.5 second");
                } else if (s.getProgress() == 1) {
                    ttv.setText("0.75 second");
                } else if (s.getProgress() == 2) {
                    ttv.setText("1 seconds");
                } else if (s.getProgress() == 3) {
                    ttv.setText("1.25 seconds");
                } else if (s.getProgress() == 4) {
                    ttv.setText("1.5 seconds");
                } else if (s.getProgress() == 5) {
                    ttv.setText("1.75 seconds");
                } else if (s.getProgress() == 6) {
                    ttv.setText("2 seconds");
                } else if (s.getProgress() == 7) {
                    ttv.setText("2.25 seconds");
                } else {
                    ttv.setText("2.5 seconds");
                }
                sharedPreferences.edit().putString(key_4,""+s.getProgress()).commit();

            }
        });
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        boolean automatic = preferences.getBoolean("pref_automatic",true);
        if(!automatic) {
            b.setVisibility(View.VISIBLE);
            b.setMagicButtonClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(root.getContext(), FaceTrackerActivity.class);
                    i.putExtra(key_4, "" + s.getProgress());
                    i.putExtra(key_2, DateFormat.getDateTimeInstance().format(new Date()));
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(i);
                    getActivity().finish();

                }
            });
        }

        mTranmission.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkAutomatic();
            }
        });

        mSpeedLimitSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                int progress = seekBar.getProgress();
                mSpeedLimitTextView.setText(Integer.toString(progress) + " km/h");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                tinyDB.putInt(Const.SPEED_LIMIT, seekBar.getProgress());
                tinyDB.putInt(Const.ORIGINAL_SPEED, seekBar.getProgress());
            }
        });


    }

    private void checkAutomatic() {


        if (!isAutomatic) {
            mTranmission.setBackgroundColor(getResources().getColor(R.color.green));
            mTranmission.setText("Manual Mode");
            isAutomatic = !isAutomatic;
            tinyDB.putBoolean(Const.IS_AUTOMATIC, true);
        } else {
            mTranmission.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
            mTranmission.setText("Automatic Mode");
            tinyDB.putBoolean(Const.IS_AUTOMATIC, false);
            isAutomatic = !isAutomatic;
        }

    }
}
