package com.example.planit;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

public class AccountActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        TextView textView = (TextView) this.findViewById(R.id.welcome);
        TextView textView2 = (TextView) this.findViewById(R.id.nameLabel);
        TextView textView3 = (TextView) this.findViewById(R.id.nameVal);
        TextView textView4 = (TextView) this.findViewById(R.id.emailLabel);
        TextView textView5 = (TextView) this.findViewById(R.id.emailVal);
        Button editBut = (Button) this.findViewById(R.id.editAccount);

    }
}
