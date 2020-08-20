
package com.beamotivator.beam;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


public class Attendance extends AppCompatActivity {

    private EditText present,total,percentage;
    private Button attendancecheck;
    private TextView attendanceresult;
    float presentc,totalc,percentagec,currentpercentage,perc,newperc;
    int resultc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance);

        present=findViewById(R.id.checkpresent);
        total=findViewById(R.id.checktotal);
        percentage=findViewById(R.id.checkgoal);
        attendancecheck=findViewById(R.id.acheck);
        attendanceresult=findViewById(R.id.aresult);


    }

    public void checkAttendance(View v){

        presentc = Float.valueOf(present.getText().toString());
        totalc = Float.valueOf(total.getText().toString());
        percentagec = Float.valueOf(percentage.getText().toString());
        perc=percentagec/100;
        currentpercentage=(presentc/totalc)*100;
        if (presentc!=0.0f && totalc!=0.0f && percentagec!=0.0f) {
            if(currentpercentage>percentagec){
                resultc = (int)Math.ceil((presentc-( perc* totalc))/(perc));
                resultc=resultc-1;
                newperc=(presentc/(totalc+resultc))*100;
                attendanceresult.setText("Your current percentage is "+currentpercentage+"%.You can bunk"+resultc+" more classes while keeping your attendance at "+newperc+"% !");

            }
            else if(currentpercentage<percentagec){
                resultc = (int)Math.ceil((( perc* totalc)-presentc)/(1-perc));
                resultc=resultc-1;
                newperc=((presentc+resultc)/(totalc+resultc))*100;

                attendanceresult.setText("Your current percentage is "+currentpercentage+"%. Attend"+resultc+"more classes to keeping your attendance at "+newperc+"% !");

            }
            else if(currentpercentage==percentagec){


                attendanceresult.setText("Current Percentage is "+currentpercentage+"%. Don't bunk right now!");

            }
        }
        else{
            Toast.makeText(Attendance.this, "Fields Empty!", Toast.LENGTH_SHORT).show();
        }
    }
}
