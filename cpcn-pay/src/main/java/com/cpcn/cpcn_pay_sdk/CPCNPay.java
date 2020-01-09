package com.cpcn.cpcn_pay_sdk;

import android.app.Activity;
import android.content.Context;

/**
 * @version v1.0
 * @author: zhaosong
 * @date: 2019-12-03
 */
public final class CPCNPay {

    private CPCNPay(){
        //禁止实例化
    }

    /**
     * @param activity         唤起支付的Activity
     * @param authCode         通过中金支付后台API接口下单得到的authCode
     * @param zhifubaoCallback 前台结果回调，如果使用无回调的通道或不想处理回调，可直接设置为null
     * @return SDK调用的同步结果，true表示调用成功，false表示调用失败，调用失败一般是由于传参错误或入网配置不正确
     */
    public static boolean zhifubaoPay(Activity activity, String authCode, ZhifubaoCallback zhifubaoCallback) {
        return CPCNInner.zhifubaoPay(activity, authCode, zhifubaoCallback);
    }


    /**
     * @param context  Application的上下文或当前Activity
     * @param appId    微信入网的appID
     * @param authCode 通过中金支付后台API接口下单得到的authCode
     * @return SDK调用的同步结果，true表示调用成功，false表示调用失败，调用失败一般是由于传参错误或入网配置不正确
     */
        public static boolean weixinPay(Context context, String appId, String authCode) {
        return CPCNInner.weixinPay(context, appId, authCode);
    }
}
