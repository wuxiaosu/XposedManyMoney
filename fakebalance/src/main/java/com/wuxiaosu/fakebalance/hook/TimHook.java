package com.wuxiaosu.fakebalance.hook;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.TextView;

import com.wuxiaosu.fakebalance.BuildConfig;
import com.wuxiaosu.fakebalance.util.NumberUtils;
import com.wuxiaosu.widget.SettingLabelView;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;


/**
 * Created by su on 2018/2/05.
 * tim hook
 */

public class TimHook {
    private static XSharedPreferences xsp;

    private static boolean fakeBalance;
    private static String balance;


    private static void reload() {
        xsp.reload();
        balance = NumberUtils.num2num00(xsp.getString("tenpay", "0.00"));
        fakeBalance = xsp.getBoolean("fake_tenpay", false);
    }

    public static void hook(ClassLoader classLoader) {
        xsp = new XSharedPreferences(BuildConfig.APPLICATION_ID, SettingLabelView.DEFAULT_PREFERENCES_NAME);
        xsp.makeWorldReadable();
        try {
            final Class qvipPayWalletActivityClazz = XposedHelpers.findClass("com.qwallet.activity.QvipPayWalletActivity", classLoader);
            final Class qvipPayAccountActivityClazz = XposedHelpers.findClass("com.qwallet.activity.QvipPayAccountActivity", classLoader);
            final Class enClazz = XposedHelpers.findClass("com.tenpay.android.qqplugin.activity.en", classLoader);

            handleHook(qvipPayWalletActivityClazz);
            handleHook(qvipPayAccountActivityClazz);

            XposedBridge.hookAllMethods(qvipPayWalletActivityClazz, "a", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    Object object = param.getResult();
                    if (object != null && object.getClass() == TextView.class) {
                        handleHook((TextView) object);
                    }
                    super.afterHookedMethod(param);
                }
            });

            Method[] methods = qvipPayAccountActivityClazz.getMethods();
            for (Method method : methods) {
                if (method.getParameterTypes().length == 1
                        && method.getParameterTypes()[0] == qvipPayAccountActivityClazz
                        && method.getReturnType() == TextView.class) {

                    XposedBridge.hookAllMethods(qvipPayAccountActivityClazz, method.getName(), new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            Object object = param.getResult();
                            if (object != null && object.getClass() == TextView.class) {
                                handleHook((TextView) object);
                            }
                            super.afterHookedMethod(param);
                        }
                    });
                }
            }

            XposedHelpers.findAndHookMethod(enClazz, "onResume", new XC_MethodHook() {

                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    reload();
                    Field[] fields = enClazz.getDeclaredFields();
                    for (Field field : fields) {
                        if (field.getType() == TextView.class) {
                            final TextView textView = (TextView) XposedHelpers.getObjectField(param.thisObject, field.getName());
                            textView.addTextChangedListener(new TextWatcher() {
                                @Override
                                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                                }

                                @Override
                                public void onTextChanged(CharSequence s, int start, int before, int count) {

                                }

                                @Override
                                public void afterTextChanged(Editable s) {
                                    if (fakeBalance) {
                                        String string = s.toString();
                                        if (!string.equals("记录")) {
                                            boolean isDouble = true;
                                            try {
                                                Double.valueOf(string);
                                            } catch (Exception e) {
                                                isDouble = false;
                                            }
                                            if (isDouble && !string.equals(balance)) {
                                                textView.setText(balance);
                                            }
                                        }
                                    }
                                }
                            });
                        }
                    }
                    super.beforeHookedMethod(param);
                }
            });
        } catch (Error | Exception e) {
            e.printStackTrace();
        }
    }

    private static void handleHook(final Class clazz) {
        try {
            XposedHelpers.findAndHookMethod(clazz, "onResume", new XC_MethodHook() {

                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    reload();
                    super.beforeHookedMethod(param);
                }
            });
        } catch (Error | Exception e) {
            e.printStackTrace();
        }
    }

    private static void handleHook(final TextView textView) {
        textView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (fakeBalance) {
                    String string = s.toString();
                    if (string.startsWith("￥") && !s.toString().equals("￥" + balance)) {
                        textView.setText("￥" + balance);
                        return;
                    }

                    if (string.endsWith(" 元")) {
                        if (!s.toString().equals(balance + " 元")) {
                            textView.setText(balance + " 元");
                        }
                    } else {
                        if (string.endsWith("元")) {
                            if (!s.toString().equals(balance + "元")) {
                                textView.setText(balance + "元");
                            }
                        }
                    }
                }
            }
        });
    }
}
