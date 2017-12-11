package com.nuopushi.sys.app.agedcare.ui;

import android.app.Activity;
import android.content.Intent;
import android.net.sip.SipManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.nuopushi.sys.app.agedcare.R;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Author chzjy
 * Date 2016/12/19.
 * 登录界面
 */

public class Login extends Activity {


    @Bind(R.id.setting)
    Button setting;
    @Bind(R.id.login)
    Button login;
    @Bind(R.id.root)
    LinearLayout root;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);//控件绑定

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ButterKnife.unbind(this);//空间解绑
    }

    @Override
    public void onBackPressed() {

    }

    @OnClick({R.id.setting, R.id.login})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.setting:
                Intent mIntent = new Intent(Settings.ACTION_SETTINGS);
                startActivity(mIntent);
                break;
            case R.id.login:
                Intent intent = new Intent(Login.this, Main.class);
                startActivity(intent);
                break;
        }
    }
}
