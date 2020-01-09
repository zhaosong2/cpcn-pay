package com.cpcn.cpcn_pay_sdk;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alipay.sdk.app.PayTask;
import com.tencent.mm.opensdk.modelbiz.WXLaunchMiniProgram;
import com.tencent.mm.opensdk.modelpay.PayReq;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

/**
 * @version v1.0
 * @author: zhaosong
 * @date: 2019-11-22
 */
class CPCNInner {
    private static final String TAG = "CPCN_PAY_SDK";
    private static ZhifubaoCallback sZhifubaoCallback;

    static boolean zhifubaoPay(Activity activity, String authCode, ZhifubaoCallback zhifubaoCallback) {
        boolean result = false;
        try {
            try {
                JSONObject jsonObject = new JSONObject(authCode);
                String qrCode = (String) jsonObject.get("app_param");
                zhifubaoScanPay(activity, qrCode);
                result = true;
            } catch (JSONException e) {
                sZhifubaoCallback = zhifubaoCallback;
                Log.e(TAG, "zhifubao sdk pay JSONException", e);
                result = zhifubaoSDKPay(activity, authCode);
            } catch (Exception e) {
                Log.e(TAG, "zhifubao pay error", e);
            }
        } catch (Exception e) {
            // for no crash
        }
        return result;
    }

    static boolean weixinPay(Context context, String appId, String authCode) {
        boolean result = false;
        try {
            try {
                JSONObject jsonObject = new JSONObject(authCode);
                jsonObject.get("institutionId");
                jsonObject.get("txsn");
                jsonObject.get("sourceTxType");
                weixinxiaochengxuPay(context, appId, authCode);
                result = true;
            } catch (JSONException e) {
                Log.e(TAG, "weixin app pay JSONException", e);
                result = weixinappPay(context, appId, authCode);
            } catch (Exception e) {
                Log.e(TAG, "weixin pay error", e);
            }
        } catch (Exception e) {
            // for no crash
        }
        return result;
    }

    private static boolean zhifubaoSDKPay(final Activity activity, String authCode) {
        try {
            Map params = (Map) JSON.parse(authCode);
            final String orderInfo = OrderInfoUtil2_0.buildOrderParam(params);

            // 必须异步调用
            new Thread(new Runnable() {
                @Override
                public void run() {
                    PayTask alipay = new PayTask(activity);
                    Map<String, String> result = alipay.payV2(orderInfo, true);
                    Log.i(TAG, result.toString());
                    @SuppressWarnings("unchecked")
                    PayResult payResult = new PayResult(result);
                    if (sZhifubaoCallback != null) {
                        sZhifubaoCallback.onResult(payResult);
                        sZhifubaoCallback = null;
                    }
                }
            }).start();
            return true;
        } catch (Exception e) {
            Log.d(TAG, "zhifubaoSDKPay Exception", e);
            return false;
        }
    }

    private static boolean zhifubaoScanPay(final Activity activity, String qrcode) {
        try {
            String skipUrl = "alipays://platformapi/startapp?saId=10000007&qrcode=https://qr.alipay.com/" + qrcode + "?_s=web-other";
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(skipUrl));
            activity.startActivityForResult(intent, 1111);
            return true;
        } catch (Exception e) {
            Log.d(TAG, "zhifubaoScanPay Exception", e);
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(activity, "未安装支付宝APP", Toast.LENGTH_LONG).show();
                }
            });
            return false;
        }
    }

    private static boolean weixinappPay(Context context, String appid, String authCode) {
        IWXAPI api = WXAPIFactory.createWXAPI(context, appid);
        try {
            JSONObject json = new JSONObject(authCode);
            PayReq req = new PayReq();
            req.appId = json.getString("appId");
            req.partnerId = json.getString("partnerId");
            req.prepayId = json.getString("prepayId");
            req.nonceStr = json.getString("nonceStr");
            req.timeStamp = json.getString("timeStamp");
            req.packageValue = json.getString("package");
            req.sign = json.getString("paySign");
            //req.extData = "app data"; // optional
            // 在支付之前，如果应用没有注册到微信，应该先调用IWXMsg.registerApp将应用注册到微信
            api.sendReq(req);
            return true;
        } catch (Exception e) {
            Log.e(TAG, "weixinappPay Exception", e);
            return false;
        }
    }

    private static boolean weixinxiaochengxuPay(Context context, String appId, String authCode) {
        try {
            IWXAPI api = WXAPIFactory.createWXAPI(context, appId);

            WXLaunchMiniProgram.Req req = new WXLaunchMiniProgram.Req();
            req.userName = "gh_fa48eecf9646"; // 填小程序原始id
            req.path = "pages/home/home?authCode=" + authCode;                  //拉起小程序页面的可带参路径，不填默认拉起小程序首页
            req.miniprogramType = WXLaunchMiniProgram.Req.MINIPTOGRAM_TYPE_RELEASE;// 可选打开 开发版，体验版和正式版
            api.sendReq(req);
            return true;
        } catch (Exception e) {
            Log.d(TAG, "weixinxiaochengxuPay Exception", e);
            return false;
        }
    }
}
