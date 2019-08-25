package com.example.safe.drivelert.Fragments;

import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.safe.drivelert.Adapters.CityAdapter;
import com.example.safe.drivelert.R;
import com.example.safe.drivelert.Utility.Const;
import com.example.safe.drivelert.Utility.TinyDB;
import com.example.safe.drivelert.Utility.Utils;
import com.google.common.hash.Hashing;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class AvailableCitiesFragment extends Fragment implements View.OnClickListener {


    EditText et_cityName;
    Button mAdd;
    Spinner mSpinner;
    TinyDB tinyDB;
    RecyclerView recyclerView;
    CityAdapter adapter;
    RecyclerView.LayoutManager layoutManager;

    ArrayList<String> spinnerArray = new ArrayList<String>();


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View root;
        root = inflater.inflate(R.layout.city_fragment, container, false);
        init(root);
        return root;
    }

    private void init(View root) {
        et_cityName = root.findViewById(R.id.editText_city_name);
        mAdd = root.findViewById(R.id.btn_add_city);
        mSpinner = root.findViewById(R.id.spinner);

        recyclerView = root.findViewById(R.id.recyclerView);
        layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        tinyDB = new TinyDB(getActivity());


        spinnerArray = tinyDB.getListString(Const.CITIES_LIST);
        adapter = new CityAdapter(spinnerArray , getActivity());
        recyclerView.setAdapter(adapter);
        recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL));


        setSpinner();

        mAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = et_cityName.getText().toString();
                if (name.equals("")) {
                    et_cityName.setError("This field can't be empty");
                } else {


                    String hash =  Utils.calculateHash(et_cityName.getText().toString().toLowerCase());
                    spinnerArray.add(et_cityName.getText().toString().toLowerCase());
                    tinyDB.putBoolean(hash , true);

//                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(
//                            getActivity(), android.R.layout.simple_spinner_item, spinnerArray);
//                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//                    mSpinner.setAdapter(adapter);

                    et_cityName.setText("");
                    tinyDB.putListString(Const.CITIES_LIST, spinnerArray);
                    adapter.notifyDataSetChanged();
                }
            }
        });
    }

    private void setSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                getActivity(), android.R.layout.simple_spinner_item, spinnerArray);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinner.setAdapter(adapter);
    }

    @Override
    public void onClick(View view) {

    }
}
