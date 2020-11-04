package com.max.hobnob;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskExecutors;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.hbb20.CountryCodePicker;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;
import com.wang.avi.AVLoadingIndicatorView;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import de.hdodenhof.circleimageview.CircleImageView;

public class LoginActivity extends AppCompatActivity {

    EditText phoneNumber, verifyCode;
    Button btn_request_code,btn_verify,btn_resend;
    String verificationID;
    FirebaseAuth auth;
    FirebaseUser firebaseUser;
    AVLoadingIndicatorView progressBar;
    CountryCodePicker ccp;
    TextView timer;
    CountDownTimer t;
    @Override
    protected void onStart() {
        super.onStart();

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        //check if user is null
        if (firebaseUser != null){
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        auth = FirebaseAuth.getInstance();
        progressBar=findViewById(R.id.avi_login);
        phoneNumber = findViewById(R.id.phone);
        verifyCode = findViewById(R.id.verification_code);
        btn_verify = findViewById(R.id.verify_btn);
        btn_request_code = findViewById(R.id.btn_request_otp);
        btn_resend=findViewById(R.id.btn_resend_otp);
        timer=findViewById(R.id.timer);
        ccp=findViewById(R.id.ccp);
        ccp.registerCarrierNumberEditText(phoneNumber);
        btn_request_code.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(phoneNumber.length()<1)
                {
                    Toast.makeText(LoginActivity.this, "Enter Phone Number", Toast.LENGTH_SHORT).show();
                }
                else {
                    btn_request_code.setVisibility(View.GONE);
                    phoneNumber.setEnabled(false);
                    verifyCode.setVisibility(View.VISIBLE);
                    btn_verify.setVisibility(View.VISIBLE);
                    timer.setVisibility(View.VISIBLE);
                    sendverificationtouser(ccp.getFullNumberWithPlus());
                }
            }
        });
        btn_verify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                t.cancel();
                timer.setVisibility(View.GONE);
                btn_verify.setVisibility(View.GONE);
                progressBar.setVisibility(View.VISIBLE);
                String code=verifyCode.getText().toString();
                if(code.isEmpty()||code.length()<6)
                {
                    Toast.makeText(LoginActivity.this, "Wrong OTP", Toast.LENGTH_SHORT).show();
                    btn_verify.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.GONE);
                }
                else
                {
                    verifythecode(code);
                }
            }
        });
        btn_resend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btn_resend.setVisibility(View.GONE);
                timer.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
                btn_verify.setVisibility(View.VISIBLE);
                sendverificationtouser(ccp.getFullNumberWithPlus());
            }
        });

    }


    private void sendverificationtouser(String txt_phone) {

        t= new CountDownTimer(60000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timer.setText(String.valueOf(millisUntilFinished / 1000));
            }

            @Override
            public void onFinish() {
                timer.setVisibility(View.GONE);
                btn_resend.setVisibility(View.VISIBLE);
            }
        }.start();
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                txt_phone,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                TaskExecutors.MAIN_THREAD,               // Activity (for callback binding)
                mCallbacks);        // OnVerificationStateChangedCallbacks
    }

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        @Override
        public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(s, forceResendingToken);
            verificationID=s;
        }

        @Override
        public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
           String code = phoneAuthCredential.getSmsCode();
           if(code!=null)
           {
                btn_verify.setVisibility(View.GONE);
                progressBar.setVisibility(View.VISIBLE);
               verifythecode(code);
           }

        }

        @Override
        public void onVerificationFailed(@NonNull FirebaseException e) {
            t.cancel();
            timer.setVisibility(View.GONE);
            phoneNumber.setEnabled(true);
            verifyCode.setVisibility(View.GONE);
            btn_resend.setVisibility(View.GONE);
            btn_request_code.setVisibility(View.VISIBLE);
            Toast.makeText(LoginActivity.this, "verification Failed\nCheck your Internet Connection", Toast.LENGTH_SHORT).show();
        }
    };

    private void verifythecode(String code) {
        progressBar.setVisibility(View.VISIBLE);
        PhoneAuthCredential credential=PhoneAuthProvider.getCredential(verificationID,code);
        signInwithCredential(credential);

    }

    private void signInwithCredential(PhoneAuthCredential credential) {
        FirebaseAuth auth=FirebaseAuth.getInstance();
        auth.signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {


            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful())
                {
                    final String uid=FirebaseAuth.getInstance().getUid();
                    final DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(uid);
                    reference.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if(!dataSnapshot.exists())
                            {
                                HashMap<String, Object> hashMap = new HashMap<>();
                                hashMap.put("id", uid);
                                hashMap.put("username","hobnob");
                                hashMap.put("phone", ccp.getFullNumberWithPlus());
                                hashMap.put("about", "hey");
                                hashMap.put("imageURL", "not_set");
                                hashMap.put("status", "offline");
                                reference.setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(intent);
                                        finish();
                                    }
                                });
                            }
                            else
                            {
                                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                finish();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });



                }
                else
                {
                    Toast.makeText(LoginActivity.this, "Check Your Otp,verification Failed", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                    btn_verify.setVisibility(View.VISIBLE);
                    btn_resend.setVisibility(View.VISIBLE);
                }
            }
        });


    }

}
