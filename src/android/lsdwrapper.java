package com.heytz.lsdwrapper;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import com.lsd.easy.joine.lib.CRC8;
import com.lsd.easy.joine.lib.ConfigUdpBroadcast;
import com.lsd.easy.joine.lib.ConfigUdpBroadcast.ConfigRetObj;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.eclipse.jetty.server.ResourceCache;
import org.json.JSONArray;
import org.json.JSONException;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import java.io.IOException;
import java.net.*;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
//import com.lsd.easy.joine.test.R;

/**
 * This class starts transmit to activation
 */
public class lsdwrapper extends CordovaPlugin {

    private DatagramPacket datagramPacket;
    private DatagramSocket datagramSocket;
    private static String TAG = "=====lsdwrapper.class====";
    private Context context;
    private String userName;
    private String deviceLoginID;
    private String devicePassword;
    private String mac;
    private boolean gotCallback;
    private boolean didReturned;
    //  private String appSecretKey;
    //  private int easylinkVersion;
    private int activateTimeout;
    private String activatePort;
    private CallbackContext lsdCallbackContext;

    private static int[][] desTables = new int[][]{{15, 12, 8, 2}, {13, 8, 10, 1}, {1, 10, 13, 0}, {3, 15, 0, 6}, {11, 8, 12, 7}, {4, 3, 2, 12}, {6, 11, 13, 8}, {2, 1, 14, 7}};
    private Handler mHandler;
    private static MulticastSocket multicastSocket;
    private static boolean sendFlag = true;
    public static int CODE_INTERVAL_TIMES = 8;
    public static int CODE_INTERVAL_TIME = 500;
    public static int CODE_TIME = 20;
    public static int CODE_TIMES = 5;
    private ConfigUdpBroadcast mConfigBroadUdp;
    private String broadcastIp = "255.255.255.255";
    private Set<String> successMacSet = new HashSet();
    private ResourceCache.Content content;


    private Runnable timeoutRun = new Runnable() {
        public void run() {
            stopSend();
            ConfigRetObj obj = new ConfigRetObj();
            obj.errcode = -1;
            onConfigResult(obj);
        }
    };

    protected void onConfigResult(final ConfigRetObj obj) {
        System.out.println("returned");
        System.out.println(obj);
        stopSend();
        if (obj.errcode == 0) {
            if (!gotCallback) {
                mac = obj.mac;
                startUDPServer(19530);
                gotCallback = true;
            }
//            lsdCallbackContext.success(obj.mac);

//            new Thread(new Runnable() {
//                @Override
//                public void run() {
//                    boolean isReady = false;
//                    while (!isReady) {
//                        Socket client;
//                        try {
//                            Thread.sleep(1000L);
//                        } catch (InterruptedException e) {
//                            Log.e(TAG, e.getMessage());
//                        }
//
//                        try {
//
//                            client = new Socket(obj.ip, 8000);
//                            client.close();
//                            client = null;
//                            isReady = true;
//                        } catch (Exception e) {
//                            Log.e(TAG, e.getMessage());
//                            try {
//                                Thread.sleep(3 * 1000L);
//                            } catch (InterruptedException e1) {
//                                Log.e(TAG, e1.getMessage());
//                            }
//                        }
//                    }
//                    if (isReady) {
//                        HttpPostData(obj.ip, null);
//                        String stringResult = "{\"active_token\": \"" + null + "\", \"mac\": \"" + obj.mac + "\"}";
//                        Log.i(TAG, stringResult);
//                        JSONObject activeJSON = null;
//                        try {
//                            activeJSON = new JSONObject(stringResult);
//                        } catch (JSONException e) {
//                            Log.e(TAG, e.getMessage());
//                        }
//                        lsdCallbackContext.success(activeJSON);
//                    } else {
//                        Log.e(TAG, "activate failed");
//                        lsdCallbackContext.error("JSON obj error");
//                    }
//                }
//            }).start();
        } else if (obj.errcode == -1) {
            lsdCallbackContext.error("-1");
        } else {
            lsdCallbackContext.error("error");
        }
    }


//    private void onConfigResult(ConfigRetObj var1);

    public static void send(final String ssid, final String password) {
        if (multicastSocket == null) {
            try {
                multicastSocket = new MulticastSocket();
            } catch (IOException var3) {
                var3.printStackTrace();
            }
        }

        sendFlag = true;
        (new Thread(new Runnable() {
            public void run() {
                while (sendFlag) {
                    int count = 0;

                    while (count < CODE_INTERVAL_TIMES) {
                        sendContent(password, 1);
                        if (ssid != null && ssid.length() > 0) {
                            sendContent(ssid, 0);
                        }

                        ++count;

                        try {
                            Thread.sleep((long) CODE_INTERVAL_TIME);
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
        if (contents.length == 0) {
            contents = new byte[8];

            for (len = 0; len < 8; ++len) {
                contents[len] = 1;
            }
        }

        len = contents.length;

        for (int i = 0; i < contents.length && sendFlag; ++i) {
            byte b = contents[i];
            int index = i;
            if (type == 0) {
                index = i & 63;
            } else if (type == 1) {
                index = i | 64;
            }

            int checksum = 255 & CRC8.calcCrc8(new byte[]{(byte) len, (byte) index, b});
            int[] desTable = getDesTable();
            int ip1 = len ^ desTable[0];
            int ip2 = index ^ desTable[1];
            int ip3 = b ^ desTable[2];
            int pLen = checksum ^ desTable[3];
            String ip = "228." + ip1 + "." + ip2 + "." + ip3;
            Log.i(TAG, "enc:" + ip + ",type:" + type);

            for (int e = 0; e < CODE_TIMES; ++e) {
                sendPacket(ip, pLen);
            }

            try {
                Thread.sleep((long) CODE_TIME);
            } catch (InterruptedException var15) {
                var15.printStackTrace();
            }
        }

    }

    private static void sendPacket(String ip, int pLen) {
        if (sendFlag) {
            try {
                InetAddress inetAddress = InetAddress.getByName(ip);
                byte[] e = getBytes(pLen);
                DatagramPacket datagramPacket = new DatagramPacket(e, pLen, inetAddress, 8888);
                if (multicastSocket != null) {
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

    private void startUDPServer(final int port) {
        // Run the UDP transmitter initialization on its own thread (just in case, see sendMessage comment)

        new Thread(new Runnable() {
            public void run() {
                this.initialize(port);
            }

            private void initialize(int port) {
                // create packet
                try {
                    byte[] buf = new byte[1024];
                    datagramPacket = new DatagramPacket(buf, buf.length);
                    // create socket
                    datagramSocket = new DatagramSocket(port);
                    String validate = "{\"app_id\":\"123123c45213cg2454354hg\",\"product_key\":\"0665bc7a5a62cc538070373cc507446b\"," +
                            "\"user_token\":\"69c09f9d-3920-4d4a-ab7e-1f4e2827c2f2\",\"uid\":\"69c09f9d-3920-4d4a-ab7e-1f4e2827c2f2\"}";
                    broadcastData(19531, validate);

                    datagramSocket.receive(datagramPacket);
//                    String clientIP = datagramPacket.getAddress().getHostAddress();
                    String data = new String(datagramPacket.getData(), 0, datagramPacket.getLength());
//                    data = data + "=ip:" + clientIP;
                    String did = data.substring(data.indexOf("device_id\":") + 11, data.indexOf("device_id\":") + 30);
                    String returnData = "{\"did\":" + did + "}";
                    didReturned = true;
                    broadcastData(19531, returnData);

                    datagramSocket.close();
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void broadcastData(final int port, final String data) {
//        final String message = "{\"app_id\":\"testid\"}";
        // Run the UDP transmission on its own thread (it fails on some Android environments if run on the same thread)
        new Thread(new Runnable() {
            public void run() {
                this.sendMessage(port, data);
            }

            private void sendMessage(int port, String data) {
                try {
                    DatagramSocket ds = new DatagramSocket();
                    byte[] bytes = data.getBytes();
                    DatagramPacket dp = new DatagramPacket(bytes, bytes.length, InetAddress.getByName(getMobileIP()), port);
                    for (int i = 0; i < 5; i++) {
                        ds.send(dp);
                        Thread.sleep(1000);
                    }
                    lsdCallbackContext.success(mac);
                    ds.close();
//                    lsdCallbackContext.success("done");
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
//                }
            }
        }).start();
    }

    private void sendUDPData(final String ip, final int port, final String data) {
        final String message = "{\"appId\":\"testid\"}";
        // Run the UDP transmission on its own thread (it fails on some Android environments if run on the same thread)
        new Thread(new Runnable() {
            public void run() {
                this.sendMessage(ip, port, message);
            }

            private void sendMessage(String ip, int port, String data) {
                try {
                    datagramSocket = new DatagramSocket();
                    byte[] bytes = data.getBytes();
                    datagramPacket = new DatagramPacket(bytes, bytes.length, InetAddress.getByName(ip), port);
                    datagramSocket.send(datagramPacket);
                    datagramSocket.close();
                    lsdCallbackContext.success("done");
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
//                }
            }
        }).start();
    }

    private static byte[] getBytes(int capacity) {
        byte[] data = new byte[capacity];

        for (int i = 0; i < capacity; ++i) {
            data[i] = 65;
        }

        return data;
    }

    private static int[] getDesTable() {
        Random rand = new Random();
        int index = rand.nextInt(desTables.length);
        return desTables[index];
    }

    protected void initConfig() {
        mConfigBroadUdp = new ConfigUdpBroadcast(broadcastIp, new ConfigUdpBroadcast.DataListener() {
            public void onReceive(ConfigRetObj obj) {
                String mac = obj.mac;
                Message msg = mHandler.obtainMessage();
                msg.what = 4097;
                msg.obj = obj;
                mHandler.sendMessage(msg);
                mHandler.removeCallbacks(timeoutRun);
                successMacSet.add(mac);

            }
        });
        this.mConfigBroadUdp.open();
        this.mConfigBroadUdp.receive();
        Log.i(this.TAG, "...initConfig....");
    }

    /**
     */
//    private FTC_Listener ftcListener;//new FTCLisenerExtension(easyLinkCallbackContext);
    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        context = cordova.getActivity().getApplicationContext();
        initConfig();
        mHandler = new Handler() {
            public void handleMessage(Message msg) {
                stopSend();
                onConfigResult((ConfigRetObj) msg.obj);
            }
        };
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
            mHandler.postDelayed(timeoutRun, 40000L);
            gotCallback = false;
            didReturned = false;
            send(wifiSSID, wifiKey);
            // todo: replace with EasylinkAPI
            //ftcService = new FTC_Service();
//            SmartConfigActivity.onConfigResult = callbackContext;
            lsdCallbackContext = callbackContext;
            //ftcListener = new FTCLisenerExtension(callbackContext);
//            this.transmitSettings(wifiSSID, wifiKey);
            return true;
        }
        if (action.equals("sendVerification")) {
//            startUDPServer();
//            broadcastData();
            return true;
        }
        if (action.equals("dealloc")) {
            stopSend();
            if (datagramSocket != null) {
                datagramSocket.close();
            }
            return true;
        }
        if (action.equals("startUDPServer")) {
            int port = args.getInt(0);
            startUDPServer(port);
            lsdCallbackContext = callbackContext;
            return true;
        }
        if (action.equals("sendUDPData")) {
            int port = args.getInt(0);
            String data = args.getString(1);
            String ip = args.getString(2);
            if (data.length() > 10) {
                sendUDPData(ip, port, data);
            } else {
                broadcastData(port, data);
            }
            lsdCallbackContext = callbackContext;
            return true;
        }
        return false;
    }


    /**
     * @return 0 if we don't get the mobile device ip, else the mobile device ip
     */
    private String getMobileIP() {
        try {
            WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            int IP = wifiInfo.getIpAddress();
            String localIP = ((IP & 0xff) + "." + (IP >> 8 & 0xff) + "."
                    + (IP >> 16 & 0xff) + ".255");
            return localIP;
//
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            return "";
        }
    }

    /**
     * MD5 algorithm for plain text
     *
     * @param plainText input string
     * @return plainText after md5
     */
//    private String markMd5(String plainText) {
//        try {
//            MessageDigest md = MessageDigest.getInstance("MD5");
//            md.update(plainText.getBytes());
//            byte b[] = md.digest();
//            int i;
//            StringBuffer buf = new StringBuffer("");
//            for (int offset = 0; offset < b.length; offset++) {
//                i = b[offset];
//                if (i < 0)
//                    i += 256;
//                if (i < 16)
//                    buf.append("0");
//                buf.append(Integer.toHexString(i));
//            }
//            return buf.toString();
//
//        } catch (NoSuchAlgorithmException e) {
//            Log.e(TAG, e.getMessage());
//        }
//        return null;
//    }
}
