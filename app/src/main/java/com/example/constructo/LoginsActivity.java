package com.example.constructo;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.constructo.Model.Users;
import com.example.constructo.Prevalent.Prevalent;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import io.paperdb.Paper;

public class LoginsActivity extends AppCompatActivity {

    private EditText phoneNumber, passwordText;
    private Button loginButton;
    private ProgressDialog loadingBar;
    private TextView admin, notAdmin;

    private String parentDbName = "customer";
    private CheckBox chkBoxRememberMe;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logins);

        passwordText = (EditText) findViewById(R.id.login_password);
        phoneNumber = (EditText) findViewById(R.id.login_phone_number);
        admin = (TextView) findViewById(R.id.admin);
        notAdmin = (TextView) findViewById(R.id.not_admin);
        loginButton = (Button) findViewById(R.id.login_btn);
        loadingBar = new ProgressDialog(this);


        chkBoxRememberMe = (CheckBox) findViewById(R.id.remember_me);
        Paper.init(this);


        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                LoginUser();
            }
        });


        admin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                loginButton.setText("Login Seller");
                admin.setVisibility(View.INVISIBLE);
                notAdmin.setVisibility(View.VISIBLE);
                parentDbName = "admin";
            }
        });

        notAdmin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                loginButton.setText("Login");
                admin.setVisibility(View.VISIBLE);
                notAdmin.setVisibility(View.INVISIBLE);
                parentDbName = "customer";
            }
        });
    }



    private void LoginUser()
    {
        String phone = phoneNumber.getText().toString();
        String password = passwordText.getText().toString();

        if (TextUtils.isEmpty(phone))
        {
            Toast.makeText(this, "Enter your phone number", Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(password))
        {
            Toast.makeText(this, "Enterr your password...", Toast.LENGTH_SHORT).show();
        }
        else
        {
            loadingBar.setTitle("Login");
            loadingBar.setMessage("Please wait, while we are checking your credentials.");
            loadingBar.setCanceledOnTouchOutside(false);
            loadingBar.show();


            AllowAccessToAccount(phone, password);
        }
    }



    private void AllowAccessToAccount(final String phone, final String password)
    {
        if(chkBoxRememberMe.isChecked())
        {
            Paper.book().write(Prevalent.UserPhoneKey, phone);
            Paper.book().write(Prevalent.UserPasswordKey, password);
        }


        final DatabaseReference RootRef;
        RootRef = FirebaseDatabase.getInstance().getReference().child("user");


        RootRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if (dataSnapshot.child(parentDbName).child(phone).exists())
                {
                    Users usersData = dataSnapshot.child(parentDbName).child(phone).getValue(Users.class);

                    if (usersData.getPhone().equals(phone))
                    {
                        if (usersData.getPassword().equals(password))
                        {
                            if (parentDbName.equals("admin"))
                            {
                                Toast.makeText(LoginsActivity.this, "Welcome Seller", Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();

                                Intent intent = new Intent(LoginsActivity.this, HomesActivtiy.class);
                                startActivity(intent);
                            }
                            else if (parentDbName.equals("customer"))
                            {
                                Toast.makeText(LoginsActivity.this, "Logged in Successfully.", Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();

                                Intent intent = new Intent(LoginsActivity.this, HomesActivtiy.class);
                                Prevalent.currentUser = usersData;
                                startActivity(intent);
                            }
                        }
                        else
                        {
                            loadingBar.dismiss();
                            Toast.makeText(LoginsActivity.this, "Password is incorrect.", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
                else
                {
                    Toast.makeText(LoginsActivity.this, "Account with this " + phone + " number do not exists.", Toast.LENGTH_SHORT).show();
                    loadingBar.dismiss();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}