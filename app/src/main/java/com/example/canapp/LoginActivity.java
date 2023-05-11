package com.example.canapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.canapp.api.ApiService;
import com.example.canapp.api.RetrofitClient;
import com.example.canapp.model.User;
import com.example.canapp.model.UserLogin;
import com.example.canapp.ulti.SharedPrefManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private View loginLayout;

    private float deltaY;
    ApiService apiService;

    private EditText edt_email, edt_password;
    TextView tv_noti_email,tv_noti_pass,tv_register,tv_forgest;
    ImageView img_back;

    Button btnLogin;

    private CheckBox cb_remember;

    User user = new User();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_login);
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.MATCH_PARENT;
        getWindow().setAttributes(params);
        Mapping();
        btnLogin.setOnClickListener(v->Login());
        SetThongBao();
        Register();
        Reset();
        // Thêm TouchListener vào giao diện của LoginActivity
        img_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        loginLayout.setOnTouchListener(new View.OnTouchListener() {
            private float startY; // Tọa độ Y ban đầu của ngón tay
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        startY = event.getY(); // Lấy tọa độ Y ban đầu của ngón tay
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        deltaY = event.getRawY() - startY;
                        // Áp dụng hiệu ứng chuyển động dựa trên khoảng cách chuyển động
                        loginLayout.setTranslationY(deltaY);

                        // Áp dụng hiệu ứng động lực (acceleration) cho chuyển động
                        loginLayout.animate()
                                .setInterpolator(new AccelerateInterpolator())
                                .setDuration(2000)
                                .translationY(0f)
                                .start();
                    case MotionEvent.ACTION_UP:
                        float endY = event.getY(); // Lấy tọa độ Y khi ngón tay rời khỏi màn hình
                        float betaY = event.getRawY() - startY; // Tính khoảng cách di chuyển của ngón tay theo trục Y
                        // Kiểm tra nếu khoảng cách di chuyển lớn hơn một ngưỡng cho phép (ví dụ: 100px)
                        if (Math.abs(deltaY) > 500) {
                            finish();
                        }
                        return true;
                    default:
                        return false;
                }

            }
        });
    }


    private void Mapping() {
        loginLayout = findViewById(R.id.loginlayout);
        edt_email = findViewById(R.id.edt_emaillogin);
        edt_password = findViewById(R.id.edt_passwordlogin);
        cb_remember = findViewById(R.id.cb_rememberlogin);
        tv_noti_email=findViewById(R.id.tv_noti_email);
        tv_noti_pass=findViewById(R.id.tv_noti_pass);
        img_back=findViewById(R.id.img_loginback);
        btnLogin = findViewById(R.id.btn_login2);
        tv_register= findViewById(R.id.tv_register);
        tv_forgest=findViewById(R.id.tv_forgetpass);
    }

    public void Register(){
        tv_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this,RegisterActivity.class);
                startActivity(intent);
            }
        });
    }
    public void Login(){
        String email = edt_email.getText().toString();
        String password = edt_password.getText().toString();
        //Khoi tao apiService
        apiService = RetrofitClient.getRetrofit().create(ApiService.class);

        //Thuc hien API login
        Call<UserLogin> call = apiService.login(email, password);

        call.enqueue(new Callback<UserLogin>() {
            @Override
            public void onResponse(Call<UserLogin> call, Response<UserLogin> response) {
                UserLogin userLogin = response.body();
                if (response.isSuccessful() && !userLogin.isError()){
                    user = response.body().getUser();
                    if (cb_remember.isChecked()){
                        SharedPrefManager.getInstance(getApplicationContext()).userLogin(user);
                    }
                    finish();
                    Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                    startActivity(intent);
                } else {
                    try {
                        Toast.makeText(getApplicationContext(), "Email hoặc mật khẩu không đúng", Toast.LENGTH_SHORT).show();

                    } catch (Exception e){

                    }
                }
            }

            @Override
            public void onFailure(Call<UserLogin> call, Throwable t) {
                Log.d("Error:", t.getMessage());
            }
        });

    }
    public void Reset(){
        tv_forgest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this,ResetPassword.class);
                startActivity(intent);
            }
        });
    }
    public void SetThongBao(){
        //Kiem tra cac truong email vaf password da duoc nhap chua
        edt_email.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String regex = "^([A-Z|a-z|0-9](\\.|_){0,1})+[A-Z|a-z|0-9]\\@([A-Z|a-z|0-9])+((\\.){0,1}[A-Z|a-z|0-9]){2}\\.[a-z]{2,3}$";
                String string = charSequence.toString();
                if (string.length() == 0 || !string.matches(regex)){
                    tv_noti_email.setVisibility(View.VISIBLE);
                }
                else {
                    tv_noti_email.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        edt_password.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String string = charSequence.toString();
                String regex = "^(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?])(?=.*[a-zA-Z]).{8,13}$";
                if (string.length() == 0 || !string.matches(regex)){
                    tv_noti_pass.setVisibility(View.VISIBLE);
                }
                else {
                    tv_noti_pass.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

    }

}