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
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    private Button createAccount;
    private EditText inputName, inputPhoneNumber, inputPassword;
    private ProgressDialog loadingBar;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //bind views
        createAccount = (Button) findViewById(R.id.register_btn);
        inputName = (EditText) findViewById(R.id.register_username_input);
        inputPassword = (EditText) findViewById(R.id.register_password_input);
        inputPhoneNumber = (EditText) findViewById(R.id.register_phone_number_input);
        loadingBar = new ProgressDialog(this);

        createAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createAccount();
            }
        });
    }

    private void createAccount(){
        String name = inputName.getText().toString();
        String phoneNumber = inputPhoneNumber.getText().toString();
        String password = inputPassword.getText().toString();

        if(TextUtils.isEmpty(name)){
            Toast.makeText(this, "Please enter your name", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(phoneNumber)){
            Toast.makeText(this, "Please enter your phone number", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(password)){
            Toast.makeText(this, "Please enter your password", Toast.LENGTH_SHORT).show();
        }else{
            loadingBar.setTitle("Create Account");
            loadingBar.setMessage("Please wait....");
            loadingBar.setCanceledOnTouchOutside(false);
            loadingBar.show();

            Validate(name,phoneNumber,password);

        }
    }

    private void Validate(final String name, final String phoneNumber, final String password) {
        final DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(!dataSnapshot.child("Users").child(phoneNumber).exists()){
                    HashMap<String,Object> userDataMap = new HashMap<>();
                    userDataMap.put("phoneNumber",phoneNumber);
                    userDataMap.put("Name",name);
                    userDataMap.put("password",password);
                    reference.child("Users").child(phoneNumber).updateChildren(userDataMap)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        Toast.makeText(RegisterActivity.this, "Congratulation! Your account has been succesfully created", Toast.LENGTH_SHORT).show();
                                        loadingBar.dismiss();
                                        startActivity(new Intent(RegisterActivity.this,LoginActivity.class));
                                        finish();
                                    }else {
                                            loadingBar.dismiss();
                                            Toast.makeText(RegisterActivity.this, "Please try again..", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });

                }else{

                    loadingBar.dismiss();
                    Toast.makeText(RegisterActivity.this, "This "+phoneNumber+" is already linked with an account. Please use another phone number", Toast.LENGTH_LONG).show();
                    startActivity(new Intent(RegisterActivity.this,MainActivity.class));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
