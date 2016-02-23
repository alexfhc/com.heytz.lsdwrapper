package com.heytz.mxsdkwrapper;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.lsd.easy.joine.lib.ConfigUdpBroadcast.ConfigRetObj;
import com.lsd.easy.joine.lib.Setting2Activity;
import com.lsd.easy.joine.lib.SmartConfig2Activity;
import com.lsd.easy.joine.lib.WifiAdmin;
import com.lsd.easy.joine.test.R;

/**
 * This class starts transmit to activation
 */
public class lsdwrapper extends CordovaPlugin {

    private static String TAG = "=====lsdwrapper.class====";
    private Context context;
    private String userName;
    private String deviceLoginID;
    private String devicePassword;
    //  private String appSecretKey;
    //  private int easylinkVersion;
    private int activateTimeout;
    private String activatePort;


    private Handler mHandler;
    private static MulticastSocket multicastSocket;
    private static boolean sendFlag = true;
    public static int CODE_INTERVAL_TIMES = 8;
    public static int CODE_TIME = 20;


    private Runnable timeoutRun = new Runnable() {
        public void run() {
            SmartConfigTool2.stopSend();
            ConfigRetObj obj = new ConfigRetObj();
            obj.errcode = -1;
            SmartConfig2Activity.this.onConfigResult(obj);
        }
    };

    protected void onConfigResult(ConfigRetObj var1);

    public static void send(final String ssid, final String password) {
        if(multicastSocket == null) {
            try {
                multicastSocket = new MulticastSocket();
            } catch (IOException var3) {
                var3.printStackTrace();
            }
        }

        sendFlag = true;
        (new Thread(new Runnable() {
            public void run() {
                while(sendFlag) {
                    int count = 0;

                    while(count < CODE_INTERVAL_TIMES) {
                        sendContent(password, 1);
                        if(ssid != null && ssid.length() > 0) {
                            sendContent(ssid, 0);
                        }

                        ++count;

                        try {
                            Thread.sleep((long)CODE_INTERVAL_TIME);
                        } catch (InterruptedException var3) {
                            var3.printStackTrace();
                        }
                    }
                }

            }
        })).start();
    }

    public static void sendContent(String content, int type) {
        byte[] contents = content.getBytes();
        int len;
        if(contents.length == 0) {
            contents = new byte[8];

            for(len = 0; len < 8; ++len) {
                contents[len] = 1;
            }
        }

        len = contents.length;

        for(int i = 0; i < contents.length && sendFlag; ++i) {
            byte b = contents[i];
            int index = i;
            if(type == 0) {
                index = i & 63;
            } else if(type == 1) {
                index = i | 64;
            }

            int checksum = 255 & CRC8.calcCrc8(new byte[]{(byte)len, (byte)index, b});
            int[] desTable = getDesTable();
            int ip1 = len ^ desTable[0];
            int ip2 = index ^ desTable[1];
            int ip3 = b ^ desTable[2];
            int pLen = checksum ^ desTable[3];
            String ip = "228." + ip1 + "." + ip2 + "." + ip3;
            Log.i(TAG, "enc:" + ip + ",type:" + type);

            for(int e = 0; e < CODE_TIMES; ++e) {
                sendPacket(ip, pLen);
            }

            try {
                Thread.sleep((long)CODE_TIME);
            } catch (InterruptedException var15) {
                var15.printStackTrace();
            }
        }

    }

    private static void sendPacket(String ip, int pLen) {
        if(sendFlag) {
            try {
                InetAddress inetAddress = InetAddress.getByName(ip);
                byte[] e = getBytes(pLen);
                DatagramPacket datagramPacket = new DatagramPacket(e, pLen, inetAddress, 8888);
                if(multicastSocket != null) {
                    multicastSocket.send(datagramPacket);
                }
            } catch (UnknownHostException var5) {
                var5.printStackTrace();
            } catch (IOException var6) {
                var6.printStackTrace();
            }
        }

    }

    public static void stopSend() {
        sendFlag = false;
        multicastSocket = null;
        Log.i(TAG, "stopSend................" + sendFlag);
    }

    private static byte[] getBytes(int capacity) {
        byte[] data = new byte[capacity];

        for(int i = 0; i < capacity; ++i) {
            data[i] = 65;
        }

        return data;
    }

    private static int[] getDesTable() {
        Random rand = new Random();
        int index = rand.nextInt(desTables.length);
        return desTables[index];
    }

//    protected void initConfig() {
//        this.init();
//        this.mConfigBroadUdp = new ConfigUdpBroadcast(this.broadcastIp, new DataListener() {
//            public void onReceive(ConfigRetObj obj) {
//                String mac = obj.mac;
//                if(SmartConfig2Activity.this.CONFIGURING && !SmartConfig2Activity.this.successMacSet.contains(mac)) {
//                    Message msg = SmartConfig2Activity.this.me.getHandler().obtainMessage();
//                    msg.what = 4097;
//                    msg.obj = obj;
//                    SmartConfig2Activity.this.me.getHandler().sendMessage(msg);
//                    SmartConfig2Activity.this.me.getHandler().removeCallbacks(SmartConfig2Activity.this.timeoutRun);
//                    SmartConfig2Activity.this.successMacSet.add(mac);
//                }
//
//            }
//        });
//        this.mConfigBroadUdp.open();
//        this.mConfigBroadUdp.receive();
//        Log.i(this.TAG, "...initConfig....");
//    }
    /**
     */
//    private FTC_Listener ftcListener;//new FTCLisenerExtension(easyLinkCallbackContext);
    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        context = cordova.getActivity().getApplicationContext();
    }

    @Override
    public boolean execute(String action, JSONArray args, final CallbackContext callbackContext) throws JSONException {
        if (action.equals("setDeviceWifi")) {

            String wifiSSID = args.getString(0);
            String wifiKey = args.getString(1);
            userName = args.getString(2);
            //easylinkVersion = args.getInt(3);
            activateTimeout = args.getInt(4);
            activatePort = args.getString(5);
            deviceLoginID = args.getString(6);
            devicePassword = args.getString(7);

            if (wifiSSID == null || wifiSSID.length() == 0 ||
                    wifiKey == null || wifiKey.length() == 0 ||
                    userName == null || userName.length() == 0 ||
                    activatePort == null || activatePort.length() == 0 ||
                    devicePassword == null || devicePassword.length() == 0 ||
                    deviceLoginID == null || deviceLoginID.length() == 0
                    ) {
                Log.e(TAG, "arguments error ===== empty");
                return false;
            }
            SmartConfigActivity.doConnect(wifiSSID, wifiKey);
            // todo: replace with EasylinkAPI
            //ftcService = new FTC_Service();
            SmartConfigActivity.onConfigResult = callbackContext;
//            easyLinkCallbackContext = callbackContext;
            //ftcListener = new FTCLisenerExtension(callbackContext);
//            this.transmitSettings(wifiSSID, wifiKey);
            return true;
        }
        return false;
    }

    /**
     * Step1. Call FTC Service to start transmit settings.
     *
     * @param wifiSSID @desc wifi ssid
     * @param wifiKey  @desc corresponding wifi key
     */
    private void transmitSettings(String wifiSSID, String wifiKey) {
        Log.i(TAG, " Step1. Call FTC Service to transmit settings. SSID = " + wifiSSID + ", Password = " + wifiKey);
        int mobileIp = getMobileIP();
        Log.i(TAG, String.valueOf(mobileIp));
        if (wifiSSID != null && wifiSSID.length() > 0 && wifiKey != null && wifiKey.length() > 0 && mobileIp != 0) {
            final EasyLinkAPI elapi = new EasyLinkAPI(context);
            elapi.startFTC(wifiSSID, wifiKey, new FTCListener() {
                @Override
                public void onFTCfinished(String ip,
                                          String data) {
                    elapi.stopEasyLink();

                    if (!"".equals(data)) {
                        JSONObject jsonObj;
                        try {
                            jsonObj = new JSONObject(data);

                            String deviceName = jsonObj.getString("N");
                            final String deviceIP = jsonObj.getJSONArray("C")
                                    .getJSONObject(1).getJSONArray("C")
                                    .getJSONObject(3).getString("C");
                            Log.i(TAG, "findedDeviceIP:" + deviceIP);
                            final String deviceMac = "C89346"
                                    + deviceName.substring(
                                    deviceName.indexOf("(") + 1,
                                    deviceName.length() - 1);

                            //Call Step 2.2
                            //setDevicePwd(socket, deviceLoginID);
                            final String activeToken = markMd5(deviceMac + userName + devicePassword);
                            //Call Step 3,4,5.
                            Log.d(TAG, String.valueOf(activateTimeout));

                            // we need to check the module port has started yet,
                            // may cause the problem that it is always running
                            // to fix it, introduce a timeoutValue to 240 seconds
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    boolean isReady = false;
                                    int timeoutValue = activateTimeout;
                                    while (!isReady || !(timeoutValue == 0)) {
                                        Socket client;
                                        try {
                                            Thread.sleep(1000L);
                                            timeoutValue--;
                                        } catch (InterruptedException e) {
                                            Log.e(TAG, e.getMessage());
                                        }

                                        try {

                                            client = new Socket(deviceIP, Integer.parseInt(activatePort));
                                            client.close();
                                            client = null;
                                            isReady = true;
                                        } catch (Exception e) {
                                            Log.e(TAG, e.getMessage());
                                            try {
                                                Thread.sleep(3 * 1000L);
                                                timeoutValue = timeoutValue - 3;
                                            } catch (InterruptedException e1) {

                                                Log.e(TAG, e1.getMessage());

                                            }
                                        }
                                    }

                                    if (isReady) {
                                        HttpPostData(deviceIP, activeToken);
                                        String stringResult = "{\"active_token\": \"" + activeToken + "\", \"mac\": \"" + deviceMac + "\"}";
                                        Log.i(TAG, stringResult);
                                        JSONObject activeJSON = null;
                                        try {
                                            activeJSON = new JSONObject(stringResult);
                                        } catch (JSONException e) {
                                            Log.e(TAG, e.getMessage());
                                        }
                                        easyLinkCallbackContext.success(activeJSON);
                                    } else {
                                        Log.e(TAG, "activate failed");
                                        easyLinkCallbackContext.error("JSON obj error");
                                    }
                                }
                            }).start();


                            //Call Step 6. - pls. DO NOT REMOVE
                            //Authorize(activeToken);
                            //easyLinkCallbackContext.success("{\"ip\": \"" + deviceIP + ", \"user_token\": \"" + activeToken + "\"}");
                        } catch (JSONException e) {
                            Log.e(TAG, e.getMessage());
                            easyLinkCallbackContext.error("parse JSON obj error");
                        }
                    } else {
                        Log.e(TAG, "socket data is empty!");
                        easyLinkCallbackContext.error("FTC socket data empty");
                    }
                }

                @Override
                public void isSmallMTU(int MTU) {
                }
            });
        } else {
            easyLinkCallbackContext.error("args error");
        }
    }


    /**
     * Step 3,4,5. Send activate request to module,
     * module sends the request to MXChip cloud and then get back device id and return to app.
     *
     * @param activateDeviceIP    device ip need to-be activated
     * @param activateDeviceToken device token need to-be activated
     */
    private void HttpPostData(String activateDeviceIP, String activateDeviceToken) {
        Log.i(TAG, " Step 3. Send activate request to MXChip model.");

        try {
            HttpClient httpclient = new DefaultHttpClient();
            String ACTIVATE_PORT = activatePort;//"8000";
            String ACTIVATE_URL = "/dev-activate";
            String urlString = "http://" + activateDeviceIP + ":" + ACTIVATE_PORT
                    + ACTIVATE_URL;
            Log.i(TAG, "urlString:" + urlString);
            HttpPost httppost = new HttpPost(urlString);
            httppost.addHeader("Content-Type", "application/json");
            httppost.addHeader("Cache-Control", "no-cache");
            JSONObject obj = new JSONObject();
            obj.put("login_id", deviceLoginID);
            obj.put("dev_passwd", devicePassword);
            obj.put("user_token", activateDeviceToken);
            Log.i(TAG, "" + obj.toString());
            httppost.setEntity(new StringEntity(obj.toString()));
            HttpResponse response;
            response = httpclient.execute(httppost);
            int respCode = response.getStatusLine().getStatusCode();
            Log.i(TAG, "respCode:" + respCode);
            String responsesString = EntityUtils.toString(response.getEntity());
            Log.i(TAG, "responsesString:" + responsesString);
            if (respCode == HttpURLConnection.HTTP_OK) {
                JSONObject jsonObject = new JSONObject(responsesString);
                //Get device ID and save in class variable.
                String deviceID = jsonObject.getString("device_id");
                Log.i(TAG, "deviceID:" + deviceID);

            } else {
                easyLinkCallbackContext.error("Device activate failed.");
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }


    /**
     * @return 0 if we don't get the mobile device ip, else the mobile device ip
     */
    private int getMobileIP() {
        try {
            WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            return wifiInfo.getIpAddress();

        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            return 0;
        }
    }

    /**
     * MD5 algorithm for plain text
     *
     * @param plainText input string
     * @return plainText after md5
     */
    private String markMd5(String plainText) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(plainText.getBytes());
            byte b[] = md.digest();
            int i;
            StringBuffer buf = new StringBuffer("");
            for (int offset = 0; offset < b.length; offset++) {
                i = b[offset];
                if (i < 0)
                    i += 256;
                if (i < 16)
                    buf.append("0");
                buf.append(Integer.toHexString(i));
            }
            return buf.toString();

        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, e.getMessage());
        }
        return null;
    }
}
