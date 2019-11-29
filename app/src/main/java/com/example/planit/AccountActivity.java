package com.example.planit;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

public class AccountActivity extends AppCompatActivity {
    TextView name, email;
    Button edit, signOut;
    GoogleSignInClient mGoogleSignInClient;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);
        String serverClientId = getString(R.string.server_client_id);
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
//                .requestIdToken(getString(R.string.server_client_id))
                .requestServerAuthCode(serverClientId)
                .requestEmail()
                .build();
        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(this);
        String personName = "", personEmail = "", personId;
        Uri personPhoto;
        if (acct != null) {
            personName = acct.getDisplayName();
            personEmail = acct.getEmail();
            personId = acct.getId();
            personPhoto = acct.getPhotoUrl();
        }
        TextView textView = (TextView) this.findViewById(R.id.welcome);
        TextView textView2 = (TextView) this.findViewById(R.id.nameLabel);
//        TextView textView3 = (TextView) this.findViewById(R.id.nameVal);
        name = (TextView) this.findViewById(R.id.nameVal);
        name.setText(personName);
        TextView textView4 = (TextView) this.findViewById(R.id.emailLabel);
        email = (TextView) this.findViewById(R.id.emailVal);
        email.setText(personEmail);
        edit = (Button) this.findViewById(R.id.editAccount);
        signOut = (Button) this.findViewById(R.id.button_sign_out);
        signOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (view.getId()) {
                    case R.id.button_sign_out:
                        signOut();
                        break;
                }
            }
        });
    }

    private void signOut() {
        mGoogleSignInClient.signOut()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(AccountActivity.this, "Signed out successfully!", Toast.LENGTH_LONG).show();
                        finish();
                    }
                });
    }
}
