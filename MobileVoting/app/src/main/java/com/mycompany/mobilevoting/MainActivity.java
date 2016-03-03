package com.mycompany.mobilevoting;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import java.util.*;


public class MainActivity extends ActionBarActivity {

    Button btnSendSMS;
    EditText txtPhoneNo;
    EditText txtMessage;
    HashMap<String, Integer> tallyTable = new HashMap<String, Integer>();
    ArrayList<String> voterTable = new ArrayList<String>();
    String [] candList = {"15", "3", "4", "10"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        for (int i=0; i<candList.length; i++){
            tallyTable.put(candList[i], 0);
        }
        btnSendSMS = (Button)findViewById(R.id.btnSendSMS);
        txtPhoneNo = (EditText)findViewById(R.id.txtPhoneNo);
        txtMessage = (EditText)findViewById(R.id.txtMessage);

        btnSendSMS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phoneNo = txtPhoneNo.getText().toString();
                String message = txtMessage.getText().toString();
                if (phoneNo.length() > 0 && message.length()>0)
                    sendSMS(phoneNo, message);
                else
                    Toast.makeText(getBaseContext(), "Please enter both phone number and message.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    //----sends an SMS message to another device---
    private void sendSMS(String phoneNumber, String message) {

        String SENT = "SMS_SENT";
        String DELIVERED = "SMS_DELIVERED";

        PendingIntent sentPI = PendingIntent.getBroadcast(this, 0, new Intent(SENT), 0);
        PendingIntent deliveredPI = PendingIntent.getBroadcast(this, 0, new Intent(DELIVERED), 0);

        //---when the SMS has been sent---
        registerReceiver(new BroadcastReceiver(){
            @Override
            public void onReceive(Context arg0, Intent arg1) {
                boolean voted = false;
                switch (getResultCode()) {
                    case Activity.RESULT_OK:
                        Toast.makeText(getBaseContext(), "SMS sent",
                                Toast.LENGTH_SHORT).show();
                        if (!voterTable.contains(txtPhoneNo.getText().toString())){
                            for (String s : candList){
                                if (s.equals(txtMessage.getText().toString())){
                                    voterTable.add(txtPhoneNo.getText().toString());
                                    int count = tallyTable.get(txtMessage.getText().toString());
                                    tallyTable.put(txtMessage.getText().toString(), count++);
                                    Toast.makeText(getBaseContext(), "Vote was valid", Toast.LENGTH_SHORT).show();
                                    voted = true;
                                }
                            }
                            if (!voted)
                                Toast.makeText(getBaseContext(), "Vote was not valid", Toast.LENGTH_SHORT).show();
                            break;
                        }
                        else{
                            Toast.makeText(getBaseContext(), "Vote was duplicate", Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                        Toast.makeText(getBaseContext(), "Generic failure",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NO_SERVICE:
                        Toast.makeText(getBaseContext(), "No service",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NULL_PDU:
                        Toast.makeText(getBaseContext(), "Null PDU",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_RADIO_OFF:
                        Toast.makeText(getBaseContext(), "Radio off",
                                Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }, new IntentFilter(SENT));

        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context arg0, Intent arg1) {
                switch(getResultCode()){
                    case Activity.RESULT_OK:
                        Toast.makeText(getBaseContext(), "SMS DELIVERED", Toast.LENGTH_SHORT).show();
                        break;
                    case Activity.RESULT_CANCELED:
                        Toast.makeText(getBaseContext(), "SMS not delivered", Toast.LENGTH_SHORT).show();
                        break;

                }
            }
        }, new IntentFilter(DELIVERED));

        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(phoneNumber, null, message, sentPI, deliveredPI);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
}
