package com.wuxiaosu.fakebalance;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.wuxiaosu.fakebalance.base.BaseActivity;
import com.wuxiaosu.widget.SettingLabelView;


public class MainActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getToolbar().setNavigationIcon(null);

        if (!isModuleActive()) {
            Toast.makeText(this, "模块未激活", Toast.LENGTH_SHORT).show();
        }
        initView();
    }

    private void initView() {
        SharedPreferences sharedPreferences =
                getSharedPreferences(SettingLabelView.DEFAULT_PREFERENCES_NAME, Context.MODE_WORLD_READABLE);

        bindPreferences(R.id.et_wechat, sharedPreferences, R.string.pre_key_wechat, "0.00");
        bindPreferences(R.id.et_tenpay, sharedPreferences, R.string.pre_key_tenpay, "0.00");
        bindPreferences(R.id.et_alipay, sharedPreferences, R.string.pre_key_alipay, "0.00");
        bindPreferences(R.id.et_alipay_tts, sharedPreferences, R.string.pre_key_alipay_tts, "5000000.00");

        ((SettingLabelView) findViewById(R.id.slv_hide_icon)).
                setCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        hideLauncherIcon(isChecked);
                    }
                });
    }

    /**
     * 绑定设置
     *
     * @param viewId
     * @param sharedPreferences
     * @param preStrResId
     * @param defaultValue
     */
    private void bindPreferences(int viewId, final SharedPreferences sharedPreferences,
                                 final int preStrResId, Object defaultValue) {
        View view = findViewById(viewId);
        if (view instanceof EditText) {
            String temp = sharedPreferences.getString(getString(preStrResId), (String) defaultValue);
            ((EditText) view).setText(temp);
            ((EditText) view).addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    sharedPreferences.edit().putString(getString(preStrResId), s.toString()).apply();
                }
            });
        }

    }

    public void hideLauncherIcon(boolean isHide) {
        PackageManager packageManager = this.getPackageManager();
        int hide = isHide ? PackageManager.COMPONENT_ENABLED_STATE_DISABLED
                : PackageManager.COMPONENT_ENABLED_STATE_ENABLED;
        packageManager.setComponentEnabledSetting(getAliasComponentName(),
                hide, PackageManager.DONT_KILL_APP);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_about) {
            showInfo();
        }
        return super.onOptionsItemSelected(item);
    }


    @SuppressLint("SetTextI18n")
    private void showInfo() {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_about_content, null);
        TextView mTvVersionName = view.findViewById(R.id.tv_version_name);
        TextView mTvInfo = view.findViewById(R.id.tv_info);
        final TextView mTvUrl = view.findViewById(R.id.tv_url);
        mTvUrl.setText(Html.fromHtml("<a href=''>https://github.com/wuxiaosu/XposedManyMoney</a>"));
        mTvUrl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendURLIntent(((TextView) v).getText().toString());
            }
        });
        mTvVersionName.setText(getString(R.string.app_name) + " v" + BuildConfig.VERSION_NAME);
        mTvInfo.setText(getString(R.string.app_description)
                + "\n作者没什么想说的，就祝你们多多发财吧"
                + "\n更多详情：");
        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle("关于")
                .setView(view)
                .create();
        alertDialog.show();
    }

    private void sendURLIntent(String url) {
        Intent intent = new Intent();
        intent.setAction("android.intent.action.VIEW");
        Uri contentUrl = Uri.parse(url);
        intent.setData(contentUrl);
        startActivity(intent);
    }

    private ComponentName getAliasComponentName() {
        return new ComponentName(MainActivity.this, "com.wuxiaosu.fakebalance.MainActivity_Alias");
    }

    /**
     * 模块是否启用
     *
     * @return
     */
    private static boolean isModuleActive() {
        return false;
    }
}
