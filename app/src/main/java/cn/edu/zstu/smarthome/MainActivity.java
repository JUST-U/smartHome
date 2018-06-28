package cn.edu.zstu.smarthome;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private Button discoverBtn;
    private Button statusDeviceBtn;
    private Button openCurtainBtn;
    private Button pauseCurtainBtn;
    private Button closeCurtainBtn;
    private Button unStatusDeviceBtn;
    private Button unDisCoverBtn;
    private Button testBtn;
    private Button mIrCommand;
    private Button mFansPower;
    private Button mAddController;
    public static TextView textView;
    private boolean test = false;



    int irFansPowerIndex = 0;
    String[] irFansPower = {
            "30000103FC01FE00002F",
            "30003703FC01FE000065",
            "30006320DF0303000098",
            "30006320DF030700009C",
            "30004B4B4BCC000000DD",
            "30000105FA40BF00002F",
            "300063200003030000B9",
            "30003703FC01FE000065",
            "30000103FC01FE00002F",
            "30000103FC01FE00002F",
            "300063200003030000B9",
            "30004B4B4BCC000000DD",
            "30006320DF0303000098",
            "30006320DF0303000098",
            "30000103FC01FE00002F",
            "30000102FD01FE00002F",
            "30000102FD00FF00002F",
            "30000120DF01FE00002F",
            "3000010AF500FF00002F",
            "3000010AF501FE00002F",
            "30000103FC01FE00002F",
            "30000106F900FF00002F",
            "30000103FC01FE00002F",
            "30006320DF0300000095",
            "30000103FC01FE00002F",
            "30006320DF0303000098",
            "30000103FC01FE00002F",
            "30000100FF01FE00002F",
            "30000101FE00FF00002F",
            "3000010AF501FE00002F",
            "3000019C6301FE00002F",
            "30000103FC01FE00002F",
            "30000106F900FF00002F",
            "30000103FC01FE00002F",
            "30006320DF0300000095",
            "30006320DF0300000095",
            "30000103FC01FE00002F",
            "30000103FC01FE00002F",
            "30000106F900FF00002F",
            "30000103FC01FE00002F",
            "30006320DF0300000095",
            "30006320DF0300000095",
            "30000103FC01FE00002F",
            "30000103FC01FE00002F",
            "30000106F900FF00002F",
            "30000103FC01FE00002F"
    };

    private Service.ObserverBinder observerService;

    private ServiceConnection connecttion = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.i(TAG, "onServiceConnected");
            observerService = (Service.ObserverBinder) service;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.i(TAG, "onServiceDisconnected");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        discoverBtn =(Button) findViewById(R.id.discover_device_btn);
        statusDeviceBtn = (Button)findViewById(R.id.status_device_btn);
        openCurtainBtn = (Button)findViewById(R.id.open_curtain_btn);
        pauseCurtainBtn = (Button)findViewById(R.id.pause_curtain_btn);
        closeCurtainBtn = (Button)findViewById(R.id.close_curtain_btn);
        unStatusDeviceBtn = (Button)findViewById(R.id.dis_status_device_btn);
        unDisCoverBtn = (Button)findViewById(R.id.undiscover_device_btn);
        testBtn = (Button)findViewById(R.id.test_device_btn);
        mIrCommand = (Button)findViewById(R.id.ir_code);
        mFansPower = (Button)findViewById(R.id.fans_power);
        mAddController = (Button)findViewById(R.id.add_controller);
        textView = (TextView) findViewById(R.id.status_device_tv);
        Intent intent = new Intent(this, Service.class);
        bindService(intent, connecttion, BIND_AUTO_CREATE);
        discoverBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                observerService.connect();
            }
        });
        statusDeviceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                observerService.controlDevice();
            }
        });
        openCurtainBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                observerService.setProperty(generateCommand("curtain_open", 1));
            }
        });
        pauseCurtainBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                observerService.setProperty(generateCommand("curtain_pause", 1));
            }
        });
        closeCurtainBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                observerService.setProperty(generateCommand("curtain_close", 1));
            }
        });
        testBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (test) {
                    observerService.setProperty(generateCommand("switch", "255"));
                } else {
                    observerService.setProperty(generateCommand("switch", "0"));
                }
                test = !test;
            }
        });
        unStatusDeviceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                observerService.disControlDevice();
            }
        });
        unDisCoverBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                observerService.disconnect();
            }
        });

        mIrCommand.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "[Ir]: code[" + irFansPowerIndex + "]: " + irFansPower[irFansPowerIndex]);
                String _command = generateCommand("ir_command", irFansPower[irFansPowerIndex++]);
                Log.d(TAG, "[Ir]: " + _command);
                observerService.setProperty(_command);
                if (irFansPowerIndex >= 46) {
                    irFansPowerIndex = 0;
                }
            }
        });
        mFansPower.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                observerService.setProperty(generateCommand("fans_power", 1));
            }
        });

        mAddController.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, IrTypeActivity.class));
            }
        });
    }

    private String generateCommand(String name, int value) {
        JSONObject json = new JSONObject();
        try {
            json.put(name, value);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json.toString();
    }

    private String generateCommand(String name, String value) {
        JSONObject json = new JSONObject();
        try {
            json.put(name, value);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json.toString();
    }
}
