// Copyright 2018 Espressif Systems (Shanghai) PTE LTD
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package com.espressif.ui.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.espressif.AppConstants;
import com.espressif.provision.Provision;
import com.espressif.provision.R;
import com.espressif.provision.security.Security;
import com.espressif.provision.security.Security0;
import com.espressif.provision.security.Security1;
import com.espressif.provision.session.Session;
import com.espressif.provision.transport.SoftAPTransport;
import com.espressif.provision.transport.Transport;

import espressif.Constants;
import espressif.Obd;
import espressif.WifiConstants;

public class ProvisionActivity extends AppCompatActivity  {

    private static final String TAG = "Espressif::" + ProvisionActivity.class.getSimpleName();

    private Spinner uuidList;
    private EditText ssidInput;
    private EditText passwordInput;
    private Button btnProvision;
    private ProgressBar progressBar;
    private RadioButton radio1;
    private RadioButton radio2;
    private RadioButton radio3;
    private RadioButton radio4;
    private RadioGroup radioGroup;

    private byte uuid_id;
    private int wifiSecurityType;
    private String ssidValue, passphraseValue = "";
    private String pop, baseUrl, transportVersion, securityVersion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_provision);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.title_activity_provision);
        setSupportActionBar(toolbar);

        Intent intent = getIntent();
        final String wifiSSID = intent.getStringExtra(Provision.PROVISIONING_WIFI_SSID);
        wifiSecurityType = intent.getIntExtra(AppConstants.KEY_WIFI_SECURITY_TYPE, AppConstants.WIFI_OPEN);
        pop = intent.getStringExtra(AppConstants.KEY_PROOF_OF_POSSESSION);
        baseUrl = intent.getStringExtra(Provision.CONFIG_BASE_URL_KEY);
        transportVersion = intent.getStringExtra(Provision.CONFIG_TRANSPORT_KEY);
        securityVersion = intent.getStringExtra(Provision.CONFIG_SECURITY_KEY);

        radio1 = findViewById(R.id.radioButton);
        radio1.setVisibility(View.GONE);
        radio2 = findViewById(R.id.radioButton2);
        radio2.setVisibility(View.GONE);
        radio3 = findViewById(R.id.radioButton3);
        radio3.setVisibility(View.GONE);
        radio4 = findViewById(R.id.radioButton4);
        radio4.setVisibility(View.GONE);
        //radio1.setText("Test");

        //radioGroup.findViewById(R.id.radigroup);

        String[] items = new String[]{"CmdGetPIDValue", "CmdSetInit", "CmdGetAlgoirthmInfo"};
        uuidList = findViewById(R.id.uuid_list);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, items);
        //ArrayAdapter<String> myAdapter = new ArrayAdapter<String>(ProvisionActivity.this,android.R.layout.simple_list_item_1,getResources().getStringArray(R.array.uuids));
        //mAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        uuidList.setAdapter(adapter);
        uuidList.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                switch (i) {
                    case 0:
                        uuid_id=0;
                        radio1.setVisibility(view.GONE);
                        radio2.setVisibility(view.GONE);
                        radio3.setVisibility(view.GONE);
                        radio4.setVisibility(view.GONE);
                        ssidInput.setVisibility(view.VISIBLE);
                        ssidInput.setHint(R.string.mode);
                        passwordInput.setVisibility(view.VISIBLE);
                        passwordInput.setHint(R.string.pid);
                        Log.e(TAG, "onItemSelected: CmdGetPIDValue" );
                        break;
                    case 1:
                        ssidInput.setVisibility(view.GONE);
                        passwordInput.setVisibility(view.GONE);
                        radio1.setVisibility(view.VISIBLE);
                        radio2.setVisibility(view.VISIBLE);
                        radio3.setVisibility(view.VISIBLE);
                        radio4.setVisibility(view.VISIBLE);
                        ssidInput.setHint(R.string.init_speed);
                        passwordInput.setVisibility(view.GONE);
                        passwordInput.setHint(R.string.init_size);
                        uuid_id=1;
                        Log.e(TAG, "onItemSelected: CmdSetInit" );
                        break;
                    case 2:
                        ssidInput.setVisibility(view.GONE);
                        passwordInput.setVisibility(view.GONE);
                        radio1.setVisibility(view.GONE);
                        radio2.setVisibility(view.GONE);
                        radio3.setVisibility(view.GONE);
                        radio4.setVisibility(view.GONE);
                        uuid_id=2;
                        Log.e(TAG, "onItemSelected: CmdGetAlgoirthmInfo" );
                        break;

                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });


        ssidInput = findViewById(R.id.ssid_input);
        passwordInput = findViewById(R.id.second_input);
        btnProvision = findViewById(R.id.btn_provision);
        progressBar = findViewById(R.id.progress_indicator);

        ssidValue = wifiSSID;
        Log.d(TAG, "Selected AP -" + ssidValue);
        Log.e(TAG, "POP : " + pop);

        if (TextUtils.isEmpty(wifiSSID)) {

            //ssid.setVisibility(View.GONE);
            ssidInput.setVisibility(View.VISIBLE);

        } else {

            ssidInput.setVisibility(View.GONE);
           // ssid.setVisibility(View.VISIBLE);
           // ssid.setText(wifiSSID);

            if (wifiSecurityType == AppConstants.WIFI_OPEN) {

                passwordInput.setVisibility(View.GONE);
                findViewById(R.id.password_input_layout).setVisibility(View.GONE);
                btnProvision.setEnabled(false);
                btnProvision.setAlpha(0.5f);
                btnProvision.setTextColor(Color.WHITE);
                doProvisioning();
            }
        }

        btnProvision.setEnabled(true);
        //btnProvision.setAlpha(0.5f);
        btnProvision.setTextColor(Color.WHITE);

        ssidInput.addTextChangedListener(new TextWatcher() {



            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                ssidValue = editable.toString().trim();
                enableProvisionBtn();
            }
        });

        passwordInput.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                passphraseValue = editable.toString().trim();
                enableProvisionBtn();
            }
        });

        btnProvision.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Vibrator vib = (Vibrator) getSystemService(VIBRATOR_SERVICE);
                vib.vibrate(HapticFeedbackConstants.VIRTUAL_KEY);
                doProvisioning();
            }
        });
    }



    @Override
    public void onBackPressed() {
        BLEProvisionLanding.isBleWorkDone = true;
        super.onBackPressed();
    }

    private void doProvisioning() {

        btnProvision.setEnabled(false);
        btnProvision.setAlpha(0.5f);
        btnProvision.setTextColor(Color.WHITE);
        ssidInput.setEnabled(false);
        passwordInput.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);

        final Security security;
        final Transport transport;

        if (securityVersion.equals(Provision.CONFIG_SECURITY_SECURITY1)) {
            security = new Security1(pop);
        } else {
            security = new Security0();
        }

        if (transportVersion.equals(Provision.CONFIG_TRANSPORT_WIFI)) {

            transport = new SoftAPTransport(baseUrl);
            provision(transport, security);

        } else if (transportVersion.equals(Provision.CONFIG_TRANSPORT_BLE)) {

            if (BLEProvisionLanding.bleTransport == null) {

                Log.e(TAG, "BLE Transport is Null. It should not be null.");
                BLEProvisionLanding.isBleWorkDone = true;
                finish();

            } else {
                provision(BLEProvisionLanding.bleTransport, security);
            }
        }
    }

    private void provision(Transport transport, Security security) {

        Log.d(TAG, "================== PROVISION +++++++++++++++++++++++++++++");
        Session session = WiFiScanActivity.session;

        if (session == null) {

            session = new Session(transport, security);
            final Session finalSession = session;
            session.sessionListener = new Session.SessionListener() {

                @Override
                public void OnSessionEstablished() {

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(ProvisionActivity.this,
                                    "Session Established",
                                    Toast.LENGTH_SHORT)
                                    .show();
                        }
                    });
                    Log.e(TAG, "finalSession");
                    applyConfig(finalSession);
                }

                @Override
                public void OnSessionEstablishFailed(Exception e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(ProvisionActivity.this,
                                    "Cannot establish session",
                                    Toast.LENGTH_LONG)
                                    .show();
                            toggleFormState(true);
                        }
                    });
                    String statusText = getResources().getString(R.string.error_pop_incorrect);
                    finish();
                    Intent goToSuccessPage = new Intent(getApplicationContext(), ProvisionSuccessActivity.class);
                    goToSuccessPage.putExtra(AppConstants.KEY_STATUS_MSG, statusText);
                    goToSuccessPage.putExtras(getIntent());
                    startActivity(goToSuccessPage);
                }
            };
            session.init(null);
        } else {
            Log.e(TAG, " ELSE applyConfig(session)");
            applyConfig(session);
        }
    }

    private void applyConfig(Session session) {

        Log.e(TAG, "Session established123 : " + session.isEstablished());
        final Provision provision = new Provision(session);

        provision.provisioningListener = new Provision.ProvisioningListener() {
            @Override
            public void OnApplyConfigurationsSucceeded() {
                Log.e(TAG,"Configurations successfully applied");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        Toast.makeText(ProvisionActivity.this,
                                "Configurations successfully applied",
                                Toast.LENGTH_LONG)
                                .show();

                    }
                });

            }

            @Override
            public void OnApplyConfigurationsFailed() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(ProvisionActivity.this,
                                "Configurations cannot be applied",
                                Toast.LENGTH_LONG)
                                .show();
                    }
                });
            }

            @Override
            public void OnWifiConnectionStatusUpdated(final WifiConstants.WifiStationState newStatus,
                                                      final WifiConstants.WifiConnectFailedReason failedReason,
                                                      final Exception e) {

                Log.d(TAG, "OnWifiConnectionStatusUpdated");
                String statusText = "";
                if (e != null) {
                    statusText = e.getMessage();
                } else if (newStatus == WifiConstants.WifiStationState.Connected) {
                    statusText = getResources().getString(R.string.success_text);
                } else if (newStatus == WifiConstants.WifiStationState.Disconnected) {
                    statusText = getResources().getString(R.string.wifi_disconnected_text);
                } else {

                    if (failedReason == WifiConstants.WifiConnectFailedReason.AuthError) {
                        statusText = getResources().getString(R.string.error_authentication_failed);
                    } else if (failedReason == WifiConstants.WifiConnectFailedReason.NetworkNotFound) {
                        statusText = getResources().getString(R.string.error_network_not_found);
                    } else {
                        statusText = getResources().getString(R.string.error_unknown);
                    }
                }

                final String finalStatusText = statusText;

                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {

                        goToSuccessPage(finalStatusText);
                    }
                });
            }

            @Override
            public void OnProvisioningFailed(Exception e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(ProvisionActivity.this,
                                "Provisioning Failed",
                                Toast.LENGTH_LONG)
                                .show();
                        toggleFormState(true);
                    }
                });
                setResult(RESULT_CANCELED);
                finish();
            }
        };

        switch (uuid_id) {
            case 0:
                provision.cmdGetPIDValue(ssidValue, passphraseValue, new Provision.ProvisionActionListener() {

                    @Override
                    public void onComplete(Constants.Status status, Exception e) {
                        Log.e(TAG, "configureWifi::onComplete347");
                        goToSuccessPage("");
                        // toggleFormState(false);
                        // provision.applyConfigurations(null);
                    }
                });
                break;

            case 1:
                Obd.OBDInitSpeed speed;
                Obd.OBDInitBitsize bitsize;
                if( radio1.isChecked()) {
                    speed = Obd.OBDInitSpeed.speed250kbs;
                }else{
                   speed= Obd.OBDInitSpeed.speed500kbs;
                }


                if( radio3.isChecked()) {
                    bitsize = Obd.OBDInitBitsize.bitsize11;
                }else{
                    bitsize = Obd.OBDInitBitsize.bitsize29;
                }




        provision.cmdSetInit(speed,bitsize, new Provision.ProvisionActionListener() {

            @Override
            public void onComplete(Constants.Status status, Exception e) {
                Log.e(TAG, "configureWifi::onComplete347");
                goToSuccessPage("");
                // toggleFormState(false);
                // provision.applyConfigurations(null);
            }
        });
        break;
            case 2:
                provision.cmdGetAlgoirthmInfo( new Provision.ProvisionActionListener() {

                    @Override
                    public void onComplete(Constants.Status status, Exception e) {
                        Log.e(TAG, "configureWifi::onComplete347");
                        goToSuccessPage("");
                        // toggleFormState(false);
                        // provision.applyConfigurations(null);
                    }
                });
                break;
    }
    }

    private void enableProvisionBtn() {
        btnProvision.setEnabled(true);
        btnProvision.setAlpha(1f);
        /*if (!TextUtils.isEmpty(ssidValue)) {
            btnProvision.setEnabled(true);
            btnProvision.setAlpha(1f);
        } else {
            btnProvision.setEnabled(false);
            btnProvision.setAlpha(0.5f);
            btnProvision.setTextColor(Color.WHITE);
        }*/
    }

    private void toggleFormState(boolean isEnabled) {

       /* if (isEnabled) {
            Log.e(TAG,"goToSuccessPage 1");
            //progressBar.setVisibility(View.GONE);
            Log.e(TAG,"goToSuccessPage 1");
            btnProvision.setEnabled(true);
            Log.e(TAG,"goToSuccessPage 1");
            btnProvision.setAlpha(1f);
            Log.e(TAG,"goToSuccessPage 1");
            ssidInput.setEnabled(true);
            Log.e(TAG,"goToSuccessPage 1");
            passwordInput.setEnabled(true);
            Log.e(TAG,"goToSuccessPage 1");

        } else {

            progressBar.setVisibility(View.VISIBLE);
            btnProvision.setEnabled(false);
            btnProvision.setAlpha(0.5f);
            btnProvision.setTextColor(Color.WHITE);
            ssidInput.setEnabled(false);
            passwordInput.setEnabled(false);
        }*/
    }

    private void goToSuccessPage(String statusText) {
        Log.e(TAG,"goToSuccessPage 1");
        toggleFormState(true);
        Log.e(TAG,"goToSuccessPage 2");
        finish();
        Log.e(TAG,"goToSuccessPage 3");
        Intent goToSuccessPage = new Intent(getApplicationContext(), ProvisionSuccessActivity.class);
        Log.e(TAG,"goToSuccessPage 4");
        goToSuccessPage.putExtra(AppConstants.KEY_STATUS_MSG, statusText);
        Log.e(TAG,"goToSuccessPage 5");
        goToSuccessPage.putExtras(getIntent());
        Log.e(TAG,"goToSuccessPage 6");
        startActivity(goToSuccessPage);
        Log.e(TAG,"goToSuccessPage 7");
    }
}
