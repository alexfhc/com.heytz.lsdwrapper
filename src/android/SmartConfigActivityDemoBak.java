package com.lsd.easy.joine.demo;

import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.Selection;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.lsd.easy.joine.lib.ConfigUdpBroadcast.ConfigRetObj;
import com.lsd.easy.joine.lib.Setting2Activity;
import com.lsd.easy.joine.lib.SmartConfig2Activity;
import com.lsd.easy.joine.lib.WifiAdmin;
import com.lsd.easy.joine.test.R;

/**
 * @description 演示类 
 * 
 * 二次开发：
 * 1. 导入lsd-easy-joine.jar包
 * 2. 继承SmartConfig2Acitivity类，重写：renderView，onShowWifiNotConnectedMsg，onConfigResult方法，详见演示类：SmartConfigActivityDemoBak
 * 3. 调用父类doConnect方法启动一键配置。
 * 4. 调用父类stopConfig停止一键配置。
 * 5. onConfigResult返回配置结果。
 * 
 */
public class SmartConfigActivityDemoBak extends SmartConfig2Activity {

	private SmartConfigActivityDemoBak me = this;
	long start;
	TextView resultTv;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	
	/**
	 * 
	 * 可以在此自定义布局文件、更改提示文字。
	 */
//	@Override
//	protected void renderView(Bundle savedInstanceState) {
//
//		//提示信息设置
//		this.TIP_CONFIGURING_DEVICE = me.getString(R.string.tip_configuring_device);
//		this.TIP_DEVICE_CONFIG_SUCCESS= me.getString(R.string.tip_device_config_success);
//		this.TIP_WIFI_NOT_CONNECTED=me.getString(R.string.tip_wifi_not_connected);
//
//		setContentView(R.layout.smart_config);
//
//		findViewById(R.id.settingBtn).setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				startActivity(new Intent(me,Setting2Activity.class));
//			}
//		});
//		TextView versionName = (TextView)findViewById(R.id.versionName);
//		versionName.setText("V"+getAppVersionName(this));
//
//		//ssidEt,pwdEt,showPwd,deviceCountGroup为约定的控件实例变量，不可更改。
//		connectBtn = (Button)findViewById(R.id.connect);
//		connectBtn.setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				String pwd = me.pwdEt.getText().toString().trim();
//				if(!CONFIGURING){
//					String mSsid = getSsid().replace("\"", "");
//					WifiAdmin mWifiAdmin = new WifiAdmin(me);
//					WifiInfo wifiInfo = mWifiAdmin.getWifiInfo();
//					boolean enabled = mWifiAdmin.getWifiManager().isWifiEnabled();
//					if(!enabled|| mSsid.length()==0 || "0x".equals(mSsid)||"<none>".equals(wifiInfo.getBSSID())){
//						onShowWifiNotConnectedMsg();
//						return;
//					}
//					start = System.currentTimeMillis();
//					doConnect(mSsid,pwd);
//					//doConnect(pwd);
//					connectBtn.setText("正在配置...");
//					CONFIGURING = true;
//				}else{
//					Toast.makeText(me, "正在配置...", 3000).show();
//				}
//			}
//		});
//		ssidEt = (TextView)findViewById(R.id.ssid);
//		pwdEt = (EditText)findViewById(R.id.pwd);
//		showPwd = (CheckBox)findViewById(R.id.showPwd);
//		resultTv = (TextView)findViewById(R.id.result);
//
//		showPwd.setOnCheckedChangeListener(new OnCheckedChangeListener() {
//			@Override
//			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//				pwdEt.setInputType(isChecked?EditorInfo.TYPE_CLASS_TEXT:EditorInfo.TYPE_TEXT_VARIATION_PASSWORD);
//				if(isChecked) {
//					pwdEt.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
//		        } else {
//		        	pwdEt.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
//		        }
//				Editable etable = pwdEt.getText();
//	            Selection.setSelection(etable, etable.length());
//			}
//		});
//	}

	@Override
	protected void onShowWifiNotConnectedMsg() {
		Toast.makeText(me, TIP_WIFI_NOT_CONNECTED, Toast.LENGTH_LONG).show();
	}

	@Override
	protected void onConfigResult(ConfigRetObj obj) {
//		connectBtn.setText("连接");
//		CONFIGURING = false;
		long end = System.currentTimeMillis();
		long period = end-start;
		String result="";
		if(obj.errcode==0){
			result = "配置成功,MAC:"+obj.mac+",耗时："+period+"ms";
			Toast.makeText(me, result, Toast.LENGTH_LONG).show();
		}else if(obj.errcode == -1){
			result = "配置超时";
			Toast.makeText(me, result, Toast.LENGTH_LONG).show();
		}
	}


}
