package com.wuxiaosu.fakebalance.hook;

import android.widget.TextView;

import com.wuxiaosu.fakebalance.BuildConfig;
import com.wuxiaosu.fakebalance.util.NumberUtils;
import com.wuxiaosu.widget.SettingLabelView;

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

    private String mallIndexUIMethodName;
    private String mallIndexUIFiledName;

    private String walletBalanceManagerUIMethodName;
    private String walletBalanceManagerUIFiledName;

    public WeChatHook(String versionName) {
        xsp = new XSharedPreferences(BuildConfig.APPLICATION_ID, SettingLabelView.DEFAULT_PREFERENCES_NAME);
        xsp.makeWorldReadable();
        switch (versionName) {
            case "6.6.0":
                mallIndexUIMethodName = "aSR";
                mallIndexUIFiledName = "nya";

                walletBalanceManagerUIMethodName = "au";
                walletBalanceManagerUIFiledName = "rFA";
                break;
            case "6.6.1":
                mallIndexUIMethodName = "aTu";
                mallIndexUIFiledName = "nCe";

                walletBalanceManagerUIMethodName = "au";
                walletBalanceManagerUIFiledName = "rJK";
                break;
            case "6.6.2":
                mallIndexUIMethodName = "aYm";
                mallIndexUIFiledName = "olV";

                walletBalanceManagerUIMethodName = "au";
                walletBalanceManagerUIFiledName = "szP";
                break;
            case "6.6.3":
                mallIndexUIMethodName = "aYm";
                mallIndexUIFiledName = "olV";

                walletBalanceManagerUIMethodName = "au";
                walletBalanceManagerUIFiledName = "szP";
                break;
            default:
            case "6.6.5":
                mallIndexUIMethodName = "aYS";
                mallIndexUIFiledName = "orA";

                walletBalanceManagerUIMethodName = "av";
                walletBalanceManagerUIFiledName = "sFT";
                break;
        }
    }

    private void reload() {
        xsp.reload();
        balance = NumberUtils.num2num00(xsp.getString("wechat", "0.00"));
        fakeBalance = xsp.getBoolean("fake_wechat", false);
    }

    public void hook(ClassLoader classLoader) {
        try {
            Class clazz = XposedHelpers.findClass("com.tencent.mm.plugin.mall.ui.MallIndexUI", classLoader);
            if (clazz != null) {
                XposedHelpers.findAndHookMethod(clazz, mallIndexUIMethodName, new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        reload();
                        if (fakeBalance) {
                            Object object = param.thisObject;
                            TextView textView = (TextView) getObjectField(object, mallIndexUIFiledName);
                            textView.setText("￥" + balance);
                        }
                        super.afterHookedMethod(param);
                    }
                });
            }

            XposedHelpers.findAndHookMethod("com.tencent.mm.plugin.wallet.balance.ui.WalletBalanceManagerUI", classLoader,
                    walletBalanceManagerUIMethodName, new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            reload();
                            if (fakeBalance) {
                                Object object = param.thisObject;
                                TextView textView = (TextView) getObjectField(object, walletBalanceManagerUIFiledName);
                                textView.setText("￥" + balance);
                            }
                            super.afterHookedMethod(param);
                        }
                    });
        } catch (Error | Exception e) {
            e.printStackTrace();
        }
    }
}
