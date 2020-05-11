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
import android.widget.TextView;
import android.widget.Toast;

import com.example.ecommerceshopping.Model.User;
import com.example.ecommerceshopping.Prevalent.Prevalent;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rey.material.widget.CheckBox;

import io.paperdb.Paper;

public class LoginActivity extends AppCompatActivity {

    private EditText inputPhoneNumber,inputPassword;
    private Button loginbtn;
    private ProgressDialog progressDialog;
    private CheckBox remembermechkb;

    private TextView AdminLink, NotAdminLink;

    private String parentDbName = "Users";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //bind views
        loginbtn = (Button) findViewById(R.id.login_btn);
        inputPhoneNumber = (EditText) findViewById(R.id.login_phone_number_input);
        inputPassword = (EditText) findViewById(R.id.login_password_input);
        remembermechkb = (CheckBox) findViewById(R.id.remember_me_chkb);
        AdminLink = (TextView) findViewById(R.id.admin_panel_link);
        NotAdminLink = (TextView) findViewById(R.id.not_admin_panel_link);


        progressDialog = new ProgressDialog(this);
        Paper.init(this);


        loginbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LoginUser();
            }
        });

        AdminLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginbtn.setText("Admin Login");
                AdminLink.setVisibility(View.INVISIBLE);
                NotAdminLink.setVisibility(View.VISIBLE);
                remembermechkb.setVisibility(View.INVISIBLE);
                parentDbName = "Admins";
            }
        });
        NotAdminLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginbtn.setText("Login");
                AdminLink.setVisibility(View.VISIBLE);
                NotAdminLink.setVisibility(View.INVISIBLE);
                remembermechkb.setVisibility(View.VISIBLE);
                parentDbName = "Users";
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
        if (remembermechkb.isChecked()){
            Paper.book().write(Prevalent.UserPhoneKey,phoneNumber);
            Paper.book().write(Prevalent.UserPasswordKey,password);
        }

        final DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.child(parentDbName).child(phoneNumber).exists()){
                    final User userData = dataSnapshot.child(parentDbName).child(phoneNumber).getValue(User.class);

                    if(userData.getPhoneNumber().equals(phoneNumber)){
                        if(userData.getPassword().equals(password)){
                            if (parentDbName.equals("Users")) {
                                Prevalent.currentOnlineUser = userData;
                                Toast.makeText(LoginActivity.this, "Successful Login", Toast.LENGTH_SHORT).show();
                                progressDialog.dismiss();

                                Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                            }else if(parentDbName.equals("Admins")){
                                Toast.makeText(LoginActivity.this, "Admin Login Successful", Toast.LENGTH_SHORT).show();
                                progressDialog.dismiss();
                                startActivity(new Intent(LoginActivity.this, AdminCategoryActivity.class));
                            }

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
