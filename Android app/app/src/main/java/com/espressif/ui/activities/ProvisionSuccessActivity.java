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
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.espressif.AppConstants;
import com.espressif.EspApplication;
import com.espressif.provision.Provision;
import com.espressif.provision.R;

public class ProvisionSuccessActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_provision_success);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.title_activity_provision);
        setSupportActionBar(toolbar);
        TextView textView = findViewById(R.id.textView);

        String info = getIntent().getStringExtra(AppConstants.KEY_STATUS_MSG);
        getIntent().removeExtra(AppConstants.KEY_STATUS_MSG);
        Log.e("Succes", "onCreate: "+ info );
        //TextView successTextView = findViewById(R.id.success_textview);
        textView.setText(info);
        ((EspApplication) getApplicationContext()).disableOnlyWifiNetwork();

        Button doneButton = findViewById(R.id.done_button);
        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Vibrator vib = (Vibrator) getSystemService(VIBRATOR_SERVICE);
                vib.vibrate(HapticFeedbackConstants.VIRTUAL_KEY);
                finish();
                BLEProvisionLanding.isBleWorkDone = true;
                Intent intent = new Intent(getApplicationContext(), EspMainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });
        Button againButton = findViewById(R.id.done_button2);
        againButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //Vibrator vib = (Vibrator) getSystemService(VIBRATOR_SERVICE);
                //vib.vibrate(HapticFeedbackConstants.VIRTUAL_KEY);
               // finish();
                //BLEProvisionLanding.isBleWorkDone = true;
                //Intent intent = new Intent(getApplicationContext(), ProvisionActivity.class);
               // intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
               // startActivity(intent);
                Intent launchProvisionInstructions = new Intent(getApplicationContext(), ProvisionActivity.class);
                launchProvisionInstructions.putExtras(getIntent());
                launchProvisionInstructions.putExtra(AppConstants.KEY_PROOF_OF_POSSESSION, "");
                startActivityForResult(launchProvisionInstructions, Provision.REQUEST_PROVISIONING_CODE);
            }
        });
    }

    @Override
    public void onBackPressed() {
        BLEProvisionLanding.isBleWorkDone = true;
        Intent intent = new Intent(getApplicationContext(), EspMainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        super.onBackPressed();
    }
}
