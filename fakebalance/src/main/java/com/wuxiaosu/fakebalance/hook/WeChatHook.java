package com.wuxiaosu.fakebalance.hook;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.TextView;

import com.wuxiaosu.fakebalance.BuildConfig;
import com.wuxiaosu.fakebalance.util.NumberUtils;
import com.wuxiaosu.widget.SettingLabelView;

import java.lang.reflect.Field;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedHelpers;

import static de.robv.android.xposed.XposedHelpers.getObjectField;


/**
 * Created by su on 2018/2/05.
 * 零钱 hook
 */

public class WeChatHook {
    private static XSharedPreferences xsp;

    private static boolean fakeBalance;
    private static String balance;

    public static void hook(final ClassLoader classLoader) {
        xsp = new XSharedPreferences(BuildConfig.APPLICATION_ID, SettingLabelView.DEFAULT_PREFERENCES_NAME);
        xsp.makeWorldReadable();
        final Class mallIndexUIClazz =
                XposedHelpers.findClass("com.tencent.mm.plugin.mall.ui.MallIndexUI", classLoader);
        final Class walletBalanceManagerUIClazz =
                XposedHelpers.findClass("com.tencent.mm.plugin.wallet.balance.ui.WalletBalanceManagerUI", classLoader);

        handleHook(mallIndexUIClazz, mallIndexUIClazz.getSuperclass().getDeclaredFields());
        handleHook(walletBalanceManagerUIClazz, walletBalanceManagerUIClazz.getDeclaredFields());
    }

    private static void reload() {
        xsp.reload();
        balance = NumberUtils.num2num00(xsp.getString("wechat", "0.00"));
        fakeBalance = xsp.getBoolean("fake_wechat", false);
    }

    private static void handleHook(Class clazz, final Field[] fields) {
        try {
            XposedHelpers.findAndHookMethod(clazz, "onResume", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    reload();
                    if (fakeBalance) {
                        Object object = param.thisObject;
                        for (Field field : fields) {
                            if (field.getType() == TextView.class) {
                                final TextView textView = (TextView) getObjectField(object, field.getName());
                                if (textView != null) {
                                    textView.addTextChangedListener(new TextWatcher() {
                                        @Override
                                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                                        }

                                        @Override
                                        public void onTextChanged(CharSequence s, int start, int before, int count) {

                                        }

                                        @Override
                                        public void afterTextChanged(Editable s) {
                                            String string = s.toString();
                                            if (string.startsWith("¥") && !s.toString().equals("¥" + balance)) {
                                                textView.setText("¥" + balance);
                                            }
                                        }
                                    });
                                }
                            }
                        }
                    }
                    super.afterHookedMethod(param);
                }
            });
        } catch (Error | Exception e) {
            e.printStackTrace();
        }
    }
}
