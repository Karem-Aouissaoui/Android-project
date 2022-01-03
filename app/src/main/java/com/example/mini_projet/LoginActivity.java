package com.example.mini_projet;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "MapsActivity";
    private static final int ERROR_DIALOG_REQUEST = 9001;

    ViewFlipper vFlipper;
    EditText nameReg, telReg, emailReg, pwdReg;
    EditText emailLog, pwdLog;
    Spinner spinner;
    FirebaseFirestore db;
    FirebaseHelper helper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        vFlipper = findViewById(R.id.viewFlipper);
        vFlipper.setDisplayedChild(0);
        nameReg = findViewById(R.id.nameReg);
        telReg = findViewById(R.id.telReg);
        emailReg = findViewById(R.id.emailReg);
        pwdReg = findViewById(R.id.pwdReg);
        spinner = findViewById(R.id.spinner);
        emailLog = findViewById(R.id.editTextEmail);
        pwdLog = findViewById(R.id.editTextPassword);
        db = FirebaseFirestore.getInstance();
        helper = new FirebaseHelper();
        //helper.checkUserIfExists(db,);
        if (db != null )
            Toast.makeText(this, "Firebase connection ok", Toast.LENGTH_SHORT).show();
        else
            Toast.makeText(this, "Firebase connection lost", Toast.LENGTH_SHORT).show();

    }

    public void viewRegisterClicked(View view){
        vFlipper.showNext();
    }
    public void viewLoginClicked(View view){
        vFlipper.showNext();
    }

    public void register(View view){
        String name = nameReg.getText().toString();
        String email = emailReg.getText().toString();
        String tel = telReg.getText().toString();
        String type = spinner.getSelectedItem().toString();
        String pwd = pwdReg.getText().toString();
        if(name.isEmpty())
            nameReg.setError("Please enter your name");
        else if(email.isEmpty())
            emailReg.setError("Please enter your email");
        else if(tel.isEmpty())
            telReg.setError("Please enter your phone number");
        else if(pwd.isEmpty())
            pwdReg.setError("Please set your password");
        else
            checkUserIfExists(new User(name, email, tel, type, pwd));
    }

    public void insertUser(User user){
        CollectionReference dbUsers = db.collection("users");

        dbUsers.add(user).addOnSuccessListener(new OnSuccessListener(){
            @Override
            public void onSuccess(Object o) {
                Toast.makeText(LoginActivity.this, "Registration completed successfully", Toast.LENGTH_SHORT).show();
                emailLog.setText(user.getEmail());
                pwdLog.setText(user.getPassword());
                vFlipper.showNext();
            }
        }).addOnFailureListener(new OnFailureListener(){
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(LoginActivity.this, "Fail to register \n" + e, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void checkUserIfExists(User user){
        CollectionReference dbUsers = db.collection("users");
        Query q = dbUsers.whereEqualTo("email",user.getEmail());
        q.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    if(task.getResult().size()>0){
                        Toast.makeText(LoginActivity.this, "User already exists", Toast.LENGTH_SHORT).show();
                        emailReg.setError("email already exists, try another email");
                    }
                    else {
                        Toast.makeText(LoginActivity.this, "User do not exists", Toast.LENGTH_SHORT).show();
                        insertUser(user);
                    }
                }else
                    System.out.println("task failure");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(LoginActivity.this, "Fail to register \n" + e, Toast.LENGTH_SHORT).show();
            }
        });
    }


    public void login(View v){
        if(isServicesOK()) {
            Intent i = new Intent(this, MapActivity.class);
            String email = emailLog.getText().toString();
            String pwd = pwdLog.getText().toString();
            CollectionReference dbUsers = db.collection("users");
            Query query = dbUsers.whereEqualTo("email", email).whereEqualTo("pwd", pwd);
            query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        if (task.getResult().size() > 0)
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d("TAG", document.getId() + " => " + document.getData());
                                i.putExtra("id", document.getId());
                                i.putExtra("email", email);
                                i.putExtra("pwd", pwd);
                                startActivity(i);
                            }
                        else {
                            emailLog.setError("email or password incorrect");
                            pwdLog.setError("email or password incorrect");
                            Log.d("TAG", "error ");
                        }
                    } else {
                        Log.d("TAG", "Error getting documents: ");
                    }
                }
            });
        }
    }

    public boolean isServicesOK(){
        Log.d(TAG, "isServicesOK: checking google services version");

        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(LoginActivity.this);
        if(available == ConnectionResult.SUCCESS){
            Log.d(TAG, "isServicesOK: Google play services is working");
            return true;
        }
        else if(GoogleApiAvailability.getInstance().isUserResolvableError(available)){
            Log.d(TAG, "isServicesOK: an error occured but we can fix it");
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(LoginActivity.this, available, ERROR_DIALOG_REQUEST);
            dialog.show();
        }
        else{
            Toast.makeText(this, "You can't make map requests", Toast.LENGTH_SHORT).show();
        }
        return false;
    }




}