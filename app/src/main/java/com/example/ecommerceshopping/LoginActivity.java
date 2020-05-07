package com.example.ecommerceshopping;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.ecommerceshopping.Model.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {

    private EditText inputPhoneNumber,inputPassword;
    private Button loginbtn;
    private ProgressDialog progressDialog;

    private String parentDbName = "Users";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //bind views
        loginbtn = (Button) findViewById(R.id.login_btn);
        inputPhoneNumber = (EditText) findViewById(R.id.login_phone_number_input);
        inputPassword = (EditText) findViewById(R.id.login_password_input);
        progressDialog = new ProgressDialog(this);
        loginbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LoginUser();
            }
        });
    }
    private void LoginUser(){
        String phoneNumber = inputPhoneNumber.getText().toString();
        String password = inputPassword.getText().toString();

        if(TextUtils.isEmpty(phoneNumber)){
            Toast.makeText(this, "Please enter phone number", Toast.LENGTH_SHORT).show();
        }else if(TextUtils.isEmpty(password)){
            Toast.makeText(this, "Please enter the password", Toast.LENGTH_SHORT).show();
        }else{
            progressDialog.setTitle("Login");
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.setMessage("Please wait...");
            progressDialog.show();

            AllowAccess(phoneNumber,password);
        }
    }

    private void AllowAccess(final String phoneNumber, final String password) {
        final DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.child(parentDbName).child(phoneNumber).exists()){
                    User userData =dataSnapshot.child(parentDbName).child(phoneNumber).getValue(User.class);

                    if(userData.getPhoneNumber().equals(phoneNumber)){
                        if(userData.getPassword().equals(password)){
                            Toast.makeText(LoginActivity.this, "Successful Login", Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                            startActivity(new Intent(LoginActivity.this,HomeActivity.class));

                        }else{
                            Toast.makeText(LoginActivity.this, "Incorrect password!", Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                            inputPassword.setText("");
                        }
                    }

                }else{
                    Toast.makeText(LoginActivity.this, "Account not found.", Toast.LENGTH_LONG).show();
                    progressDialog.dismiss();
                    Toast.makeText(LoginActivity.this, "Please Register", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(LoginActivity.this,RegisterActivity.class));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
