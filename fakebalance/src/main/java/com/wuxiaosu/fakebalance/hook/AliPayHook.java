package com.wuxiaosu.fakebalance.hook;


import com.wuxiaosu.fakebalance.BuildConfig;
import com.wuxiaosu.fakebalance.util.NumberUtils;
import com.wuxiaosu.widget.SettingLabelView;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;


/**
 * Created by su on 2018/2/05.
 * alipay hook
 */

public class AliPayHook {
    private static XSharedPreferences xsp;

    private static boolean fakeBalance;
    private static String balance;

    public AliPayHook(String versionName) {
        xsp = new XSharedPreferences(BuildConfig.APPLICATION_ID, SettingLabelView.DEFAULT_PREFERENCES_NAME);
        xsp.makeWorldReadable();
        switch (versionName) {
            case "10.1.0":
                break;
            case "10.1.2":
                break;
            case "10.1.5":
                break;
            case "10.1.8":
                break;
            case "10.1.10":
                break;
            case "10.1.12":
                break;
            case "10.1.15":
                break;
            case "10.1.18":
                break;
        }
    }

    private void reload() {
        xsp.reload();
        balance = NumberUtils.num2num00WithComma(xsp.getString("alipay", "0.00"));
        fakeBalance = xsp.getBoolean("fake_alipay", false);
    }

    public void hook(ClassLoader classLoader) {
        securityCheckHook(classLoader);
        try {
            Class clazz = XposedHelpers.findClass("com.flybird.FBDocument", classLoader);
            XposedHelpers.findAndHookMethod(clazz, "updateLayout", String.class,
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            reload();
                            if (fakeBalance) {
                                String strings = (String) param.args[0];
                                String b = new String(new byte[]{(byte) 1});
                                String c = new String(new byte[]{2});

                                String pattern = "余额账户\\(元\\)[\\s\\S]*?text(.+?)up_css";

                                Pattern r = Pattern.compile(pattern);
                                Matcher m = r.matcher(strings);
                                if (m.find()) {
                                    strings = strings.replace(m.group(1), c + balance + c + b);
                                }
                                param.args[0] = strings;
                            }
                            super.beforeHookedMethod(param);
                        }
                    });
            Class viewClazz = XposedHelpers.findClass("com.alipay.asset.common.view.BaseWealthWidgetView", classLoader);
            XposedBridge.hookAllMethods(viewClazz, "setWidgetModule", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    reload();
                    if (fakeBalance) {
                        Object object = param.args[0];
                        String title = (String) XposedHelpers.callMethod(object, "getTitle");
                        if (title.equals("余额")) {
                            XposedHelpers.callMethod(object, "setMainInfo", balance + " 元");
                        }
                        param.args[0] = object;
                    }
                    super.beforeHookedMethod(param);
                }
            });
        } catch (Error | Exception e) {
            e.printStackTrace();
        }
    }

    private void securityCheckHook(ClassLoader classLoader) {
        try {
            Class securityCheckClazz = XposedHelpers.findClass("com.alipay.mobile.base.security.CI", classLoader);
            XposedHelpers.findAndHookMethod(securityCheckClazz, "a", String.class, String.class, String.class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    Object object = param.getResult();
                    XposedHelpers.setBooleanField(object, "a", false);
                    param.setResult(object);
                    super.afterHookedMethod(param);
                }
            });

            XposedHelpers.findAndHookMethod(securityCheckClazz, "a", Class.class, String.class, String.class, new XC_MethodReplacement() {
                @Override
                protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
                    return (byte) 1;
                }
            });
            XposedHelpers.findAndHookMethod(securityCheckClazz, "a", ClassLoader.class, String.class, new XC_MethodReplacement() {
                @Override
                protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
                    return (byte) 1;
                }
            });
            XposedHelpers.findAndHookMethod(securityCheckClazz, "a", new XC_MethodReplacement() {
                @Override
                protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
                    return false;
                }
            });

        } catch (Error | Exception e) {
            e.printStackTrace();
        }
    }
}
