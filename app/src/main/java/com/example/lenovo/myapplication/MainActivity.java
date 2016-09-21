package com.example.lenovo.myapplication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;

public class MainActivity extends AppCompatActivity {

    private EditText BDcard;
    private EditText BDtext;
    private Button send;
    private TextView BDs;
    private static int msgId = 1;
    private LocationManager lm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        BDcard = (EditText) findViewById(R.id.BDcard);
        BDtext = (EditText) findViewById(R.id.BDtext);
        send = (Button) findViewById(R.id.send);
        //判断GPS是否正常启动
        if (!lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Toast.makeText(this, "请开启GPS导航...", Toast.LENGTH_SHORT).show();
            //返回开启GPS导航设置界面
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivityForResult(intent, 0);
            return;
        }

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                String bdnumber = BDcard.getText().toString();
                if (bdnumber.length() <= 0) {
                    Toast.makeText(MainActivity.this, "请输入接收者号码",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                bdnumber = "U" + bdnumber;
                String bdtext = BDtext.getText().toString();
                if (bdtext.length() <= 0) {
                    Toast.makeText(MainActivity.this, "请输入短消息内容",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                sendmessage(bdnumber, bdtext);
            }
        });
    }

    public void sendmessage(String bdcard, String bdtext) {
        if (msgId < 9999 && msgId > 0) {
            msgId++;
        } else {
            msgId = 1;
        }
        Toast.makeText(MainActivity.this, "发送成功",
                Toast.LENGTH_SHORT).show();
        Bundle bundle = new Bundle();
        bundle.putString("number", bdcard);
        byte[] MsgContent;
        try {
            MsgContent = bdtext.getBytes("GB2312");
            if (MsgContent != null) {
                bundle.putInt("msglenth", MsgContent.length);
                bundle.putByteArray("msgcontent", MsgContent);
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            Toast.makeText(MainActivity.this, "系统不支持该编码类型",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent("android.intent.action.beidou.msg.send");
        intent.putExtras(bundle);
        sendBroadcast(intent);
    }

    public class MessageReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.getAction().equals("android.intent.action.beidou.msg.received")) {
                Bundle bundle = intent.getExtras();
                String number = bundle.getString("number");
                number = number.substring(1); // 去掉字母U
                byte[] data = bundle.getByteArray("msgcontent");
                String content = null;
                try {
                    content = new String(data, "GB2312");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                    return;
                }
                Log.i("send", "ok");
                BDs.setText("北斗卡号：" + number + "/r" + "内容" + content);
            }
        }
    }

    private class BeidouModuleInfoReceiver {
        static final String ACTION_MSG_BD_INFO_RECEIVED =
                "android.intent.action.beidou.msg.bd.info.received";

        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(ACTION_MSG_BD_INFO_RECEIVED)) {
                Bundle bundle = intent.getExtras();
                int service_frequency = bundle.getInt("service_frequency");
                int communication_level = bundle.getInt("communication_level");
                String number = bundle.getString("number");
                String version = bundle.getString("version");
                Log.i(number, number);
                Log.i(version, version);
                Log.i("service_frequency", "service_frequency=" + service_frequency);
                Log.i("communication_level", "communication_level" + communication_level);
            }
        }
    }
}
