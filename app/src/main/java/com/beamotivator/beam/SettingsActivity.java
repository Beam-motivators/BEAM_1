package com.beamotivator.beam;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;

public class SettingsActivity extends AppCompatActivity {

    //views
    SwitchCompat postSwitch;
    ActionBar actionBar;

    //use shared preferences to save the state of switch
    SharedPreferences sp;
    SharedPreferences.Editor editor; //to edit the value of shared preference

    //constant for topic
    private static final String TOPIC_POST_NOTIFICATION = "POST"; //assign any value but use same kind of notifications



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = this.getWindow();
            Drawable background = this.getResources().getDrawable(R.drawable.main_gradient);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(this.getResources().getColor(android.R.color.transparent));
            window.setNavigationBarColor(this.getResources().getColor(android.R.color.transparent));
            window.setBackgroundDrawable(background);

        }
        setContentView(R.layout.activity_settings);

        actionBar = getSupportActionBar();
        actionBar.setTitle("Settings");

        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
        //init views
        postSwitch = findViewById(R.id.postSwitch);

        sp = getSharedPreferences("Notification_SP",MODE_PRIVATE);
        boolean isPostEnabled = sp.getBoolean(""+TOPIC_POST_NOTIFICATION,false);
        //if enabled check switch otherwise uncheck switch - by default unchecked/false
        if(isPostEnabled)
        {
            postSwitch.setChecked(true);
        }
        else
        {
            postSwitch.setChecked(false);
        }


        //implement on checked listener
        postSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                //edit switch state
                editor = sp.edit();
                editor.putBoolean(""+TOPIC_POST_NOTIFICATION,isChecked);
                editor.apply();

                if(isChecked)
                {
                    subscribePostNotification(); //call to subscribe
                }
                else
                {
                    unsubscribePostNotification(); //call to unsubscribe
                }
            }
        });
    }

    private void unsubscribePostNotification() {
        //unsubscribe to a topic (POST) to disable it's notification
        FirebaseMessaging.getInstance().unsubscribeFromTopic(""+TOPIC_POST_NOTIFICATION)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        String msg = "You will not receive Post Notifications";
                        if(!task.isSuccessful())
                        {
                            msg = "Unsubscription failed";
                        }
                        Toast.makeText(SettingsActivity.this,msg, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void subscribePostNotification() {
        //subscribe to a topic (POST) to enable it's notification
        FirebaseMessaging.getInstance().subscribeToTopic(""+TOPIC_POST_NOTIFICATION)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        String msg = "You will receive Post Notifications";
                        if(!task.isSuccessful())
                        {
                            msg = "Subscription failed";
                        }
                        Toast.makeText(SettingsActivity.this,msg, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }
}