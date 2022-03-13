package com.nathit.signinwithgoogle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.nathit.signinwithgoogle.model.ModelUser;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {  //กดปุ่ม Alt+Enter บน keyboard
    //view GoogleWithSignIn
    private GoogleApiClient googleApiClient;
    private GoogleSignInOptions signInOptions;
    //view Firebase
    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;
    //view
    ImageView btnSignOut, back;
    CircleImageView imageView_Profile;
    TextInputEditText etName, etStatus;
    TextView tvName, tvEmail, tvStatus;
    Dialog dialog;
    String uid, textName, textStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        init_screen();

        dialog = new Dialog(this);

        //init view firebase
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();

        uid = firebaseUser.getUid();

        //init view
        //รูปโปรไฟล์
        imageView_Profile = findViewById(R.id.imageView_Profile); //กดปุ่ม Ctrl ค้างไว้แล้วคลิกที่ ID ที่จะไป
        tvName = findViewById(R.id.name);   //ชื่อ
        tvStatus = findViewById(R.id.status); //สถานะ
        tvEmail = findViewById(R.id.email); //อีเมล

        signInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build();

        googleApiClient = new GoogleApiClient.Builder(this).enableAutoManage(this,this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, signInOptions).build();

        //ออกจากระบบ
        btnSignOut = findViewById(R.id.BtnSignOut);
        btnSignOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Auth.GoogleSignInApi.signOut(googleApiClient).setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {
                        if (status.isSuccess()) {
                            backMainActivity(); //ถ้าตรวจสอบแล้วผ่าน ก็จะสามารถออกจากระบบได้
                        } else {
                            Toast.makeText(ProfileActivity.this, "Logout Failed!", Toast.LENGTH_SHORT).show();
                            //หากการตรวจสอบพบปัญหา Toast จะแสดงขึ้นมาว่า "Logout Failed!"
                        }
                    }
                });
            }
        });

        back = findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ProfileActivity.this, MainActivity.class));
                finish();
            }
        });

        tvName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openEditProfileDialog(); //กดปุ่ม Alt+Enter
            }
        });

        tvStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openEditProfileDialog();
            }
        });
    }

    private void openEditProfileDialog() { //เพื่อสร้าง openEditProfileDialog
        dialog.setContentView(R.layout.edit_layout_dialog);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        TextInputLayout text_input_name = dialog.findViewById(R.id.text_input_name);
        TextInputLayout text_input_status = dialog.findViewById(R.id.text_input_status);

        text_input_name.setHintEnabled(false);
        text_input_status.setHintEnabled(false);

        etName = dialog.findViewById(R.id.name);
        etStatus = dialog.findViewById(R.id.status);

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("User");
        reference.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ModelUser modelUser = snapshot.getValue(ModelUser.class);
                if (modelUser != null) {
                    String name = "" + snapshot.child("name").getValue();
                    String status = "" + snapshot.child("status").getValue();

                    etName.setText(name);
                    etStatus.setText(status);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ProfileActivity.this, "Something is wrong!", Toast.LENGTH_SHORT).show();
            }
        });

        Button buttonUpdateProfile = dialog.findViewById(R.id.ButtonUpdateProfile);
        buttonUpdateProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateProfile(firebaseUser);
            }
        });

        Button cancelBtn = dialog.findViewById(R.id.cancel);
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();

    }

    private void updateProfile(FirebaseUser firebaseUser) {
        textName = etName.getText().toString().trim();//แก้ไข Name
        textStatus = etStatus.getText().toString().trim();//แก้ไข Status

        HashMap<String, Object> result = new HashMap<>();
        result.put("name", textName);
        result.put("status", textStatus);

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("User");
        reference.child(uid).updateChildren(result)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        UserProfileChangeRequest profileChangeRequest = new UserProfileChangeRequest.Builder()
                                .setDisplayName(textName).build();
                        firebaseUser.updateProfile(profileChangeRequest);

                        Toast.makeText(ProfileActivity.this, "Profile update finished.", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                        restartApp();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(ProfileActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void restartApp() {
        startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
        finish();
    }

    //แสดงโปรไฟล์ของผู้ใช้งาน
    private void showProfile(GoogleSignInResult result) {
        String uid = firebaseUser.getUid();
        GoogleSignInAccount account = result.getSignInAccount();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("User");
        reference.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ModelUser modelUser = snapshot.getValue(ModelUser.class);
                if (modelUser != null) {
                    String name = "" + snapshot.child("name").getValue();
                    String email = "" + snapshot.child("email").getValue();
                    String status = "" + snapshot.child("status").getValue();

                    tvName.setText(name);   //แสดงชื่อผู้ใช้งานโดยการดึงข้อมูลจาก Realtime Firebase มาแสดง  สามารถแก้ไขได้ในคลิปวิดีโอหน้า
                    tvStatus.setText(status); //แสดงสถานะของผู้ใช้งาน สามารถแก้ไขได้ในคลิปวิดีโอหน้า
                    tvEmail.setText("Email : "+email); //แสดง email ที่ทำการเข้าสู่ระบบโดยการ SignIn ผ่าน Google

                    //แสดงรูปโปรไฟล์ของผู้ใช้งาน โดยการนำข้อมูลรูปภาพบน Google มาแสดง
                    Picasso.get().load(account.getPhotoUrl()).into(imageView_Profile);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ProfileActivity.this, "Login Failed", Toast.LENGTH_SHORT).show();
                backMainActivity();
            }
        });
    }

    // กลับสู่หน้า MainActivity
    private void backMainActivity() {
        startActivity(new Intent(ProfileActivity.this, MainActivity.class));
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();

        OptionalPendingResult<GoogleSignInResult> optionalPendingResult = Auth.GoogleSignInApi.silentSignIn(googleApiClient);

        if (optionalPendingResult.isDone()) {
            GoogleSignInResult result = optionalPendingResult.get();
            showProfile(result);
        } else {
            optionalPendingResult.setResultCallback(new ResultCallback<GoogleSignInResult>() {
                @Override
                public void onResult(@NonNull GoogleSignInResult result) {
                    showProfile(result);
                }
            });
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        //เมื่อเข้าสู่ระบบด้วย google ผิดพลาด
    }

    private void init_screen() {
        final int flags = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        getWindow().getDecorView().setSystemUiVisibility(flags);

        final View view = getWindow().getDecorView();
        view.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
            @Override
            public void onSystemUiVisibilityChange(int visibility) {
                if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                    view.setSystemUiVisibility(flags);
                }
            }
        });
    }
}