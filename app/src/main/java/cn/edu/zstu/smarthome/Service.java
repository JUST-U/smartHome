package cn.edu.zstu.smarthome;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;

import org.alljoyn.about.AboutKeys;
import org.alljoyn.bus.AboutListener;
import org.alljoyn.bus.AboutObjectDescription;
import org.alljoyn.bus.AnnotationBusException;
import org.alljoyn.bus.BusAttachment;
import org.alljoyn.bus.BusException;
import org.alljoyn.bus.Mutable;
import org.alljoyn.bus.Observer;
import org.alljoyn.bus.PropertiesChangedListener;
import org.alljoyn.bus.ProxyBusObject;
import org.alljoyn.bus.SessionOpts;
import org.alljoyn.bus.SessionPortListener;
import org.alljoyn.bus.Status;
import org.alljoyn.bus.Variant;
import org.alljoyn.bus.alljoyn.DaemonInit;
import org.alljoyn.bus.annotation.BusSignalHandler;

import java.util.Map;
import java.util.concurrent.ExecutionException;

import cn.edu.zstu.IrTransponder;

import static cn.edu.zstu.smarthome.MainActivity.textView;
import static cn.edu.zstu.smarthome.Service.ControlHandler.CONTROL;
import static cn.edu.zstu.smarthome.Service.ControlHandler.DISCONTROL;
import static cn.edu.zstu.smarthome.Service.ControlHandler.SET_PROPERTY;
import static cn.edu.zstu.smarthome.Service.ObserverHandler.CONNECT;

/**
 * Created by xuhai on 2018/6/12.
 */

public class Service extends android.app.Service {

    private static final String TAG = "Service";

    public static final String[] ANNOUNCE_IFACES = new String[]{cn.edu.zstu.IrTransponder.INTF_NAME};

    private final IBinder observerBinder = new ObserverBinder();
    private ObserverHandler handler;
    private Observer mObserver;
    private ControlHandler mControlHandler;

    static {
        System.loadLibrary("alljoyn_java");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return observerBinder;
    }

    public class ObserverBinder extends Binder {

        void connect() {
            Log.i(TAG, "observer connect");
            HandlerThread handlerThread = new HandlerThread("observer-bus-thread");
            handlerThread.start();
            handler = new ObserverHandler(handlerThread.getLooper());
            handler.sendEmptyMessage(CONNECT);
        }

        void disconnect() {
            if (handler != null) {
                handler.sendEmptyMessage(ObserverHandler.DISCONNECT);
            }
        }

        void controlDevice() {
            HandlerThread handlerThread = new HandlerThread("observer-detail-bus-thread");
            handlerThread.start();
            mControlHandler = new ControlHandler(handlerThread.getLooper());
            mControlHandler.sendEmptyMessage(CONTROL);
        }

        void disControlDevice() {
            if (mControlHandler != null) {
                mControlHandler.sendEmptyMessage(DISCONTROL);
            }
        }

        void setProperty(String command) {
            if (mControlHandler != null) {
                mControlHandler.sendMessage(mControlHandler.obtainMessage(SET_PROPERTY, command));
            }
        }
    }

    @SuppressLint("HandlerLeak")
    Handler discoverHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1001:
                    String deviceInfo = (String) msg.obj;
                    textView.setText(deviceInfo);
                    break;
                case 1002:
                    ProxyBusObject proxyBusObject = (ProxyBusObject) msg.obj;
                    cn.edu.zstu.IrTransponder irTransponder = proxyBusObject.getInterface(cn.edu.zstu.IrTransponder.class);
                    try {
                        String mDeviceData = new DeviceDataAsyncTask().execute(irTransponder).get();
                        Log.i(TAG, "deviceData: " + mDeviceData);
                        textView.setText("设备状态：" + mDeviceData);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case 1003:
                    ProxyBusObject proxyBusObject1 = (ProxyBusObject) msg.obj;
                    cn.edu.zstu.IrTransponder irTransponder1 = proxyBusObject1.getInterface(cn.edu.zstu.IrTransponder.class);
                    try {
                        String mDeviceData = new DeviceDataAsyncTask().execute(irTransponder1).get();
                        Log.i(TAG, "deviceData: " + mDeviceData);
                        textView.setText("设备丢失：" + mDeviceData);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case 1004:
                    String newData = (String) msg.obj;
                    textView.setText(newData);
                    break;
            }
        }
    };

    class ObserverHandler extends Handler {
        private static final String TAG = "AJHandler";
        public static final int CONNECT = 0;
        public static final int DISCONNECT = 1;

        private static final short CONTACT_PORT = 2134;
        private BusAttachment mBus;
        private AboutListener mAboutListener;

        public ObserverHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case CONNECT:
                    DaemonInit.PrepareDaemon(Service.this.getApplication());
                    mBus = new BusAttachment(Service.this.getPackageName(), BusAttachment.RemoteMessage.Receive);
                    Status status = mBus.connect();
                    if (Status.OK != status) {
                        return;
                    }

                    Mutable.ShortValue contactPort = new Mutable.ShortValue(CONTACT_PORT);

                    SessionOpts sessionOpts = new SessionOpts();
                    sessionOpts.traffic = SessionOpts.TRAFFIC_MESSAGES;
                    sessionOpts.isMultipoint = false;
                    sessionOpts.proximity = SessionOpts.PROXIMITY_ANY;
                    sessionOpts.transports = SessionOpts.TRANSPORT_ANY;

                    mBus.bindSessionPort(contactPort, sessionOpts, new SessionPortListener() {
                        @Override
                        public boolean acceptSessionJoiner(short sessionPort,
                                                           String joiner, SessionOpts sessionOpts) {
                            return sessionPort == CONTACT_PORT;
                        }
                    });
                    initAboutListener();

                    break;
                case DISCONNECT:
                    for (String iface : ANNOUNCE_IFACES) {
                        mBus.cancelWhoImplements(new String[]{iface});
                    }
                    mBus.disconnect();
                    mBus.release();
                    getLooper().quit();
                    break;
            }
        }

        private void initAboutListener() {
            if (mAboutListener != null) {
                mBus.unregisterAboutListener(mAboutListener);
            }
            mAboutListener = new AboutListener() {
                @Override
                public void announced(String busName, int version, short port, AboutObjectDescription[] aboutObjectDescriptions, Map<String, Variant> map) {
                    for (int i = 0; i < aboutObjectDescriptions.length; ++i) {

                        AboutObjectDescription description = aboutObjectDescriptions[i];
                        String[] supportedInterfaces = description.interfaces;
                        for (int j = 0; j < supportedInterfaces.length; ++j) {
                            if (cn.edu.zstu.IrTransponder.INTF_NAME.equals(supportedInterfaces[j])) {
                                generateDeviceEntity(map);
                                return;

                            }
                        }
                    }
                }
            };

            mBus.registerAboutListener(mAboutListener);
            for (String iface : ANNOUNCE_IFACES) {
                mBus.whoImplements(new String[]{iface});
            }

        }

        private void generateDeviceEntity(Map<String, Variant> map) {
            Variant varFriendlyName = map.get(AboutKeys.ABOUT_DEVICE_NAME);
            Variant varDeviceId = map.get(AboutKeys.ABOUT_DEVICE_ID);
            String friendlyNameSig = null;
            try {
                friendlyNameSig = (varFriendlyName != null) ? varFriendlyName.getSignature() : "";
                if (!friendlyNameSig.equals("s")) {
                    Log.e(TAG, "Received '" + AboutKeys.ABOUT_DEVICE_NAME + "', that has an unexpected signature: '" + friendlyNameSig + "', the expected signature is: 's'");
                    return;
                }
                String deviceName = varFriendlyName.getObject(String.class);
                String deviceId = varDeviceId.getObject(String.class);
                Log.i(TAG, "generateDeviceEntity: " + deviceName + " : " + deviceId);
                String deviceInfo = "设备名：" + deviceName + " 设备ID：" + deviceId;
                discoverHandler.sendMessage(discoverHandler.obtainMessage(1001, deviceInfo));
            } catch (AnnotationBusException e) {
                e.printStackTrace();
            } catch (BusException e) {
                e.printStackTrace();
            }

        }
    }

    class ControlHandler extends Handler {
        private static final String TAG = "AJBusHandler";
        public static final int CONTROL = 2;
        public static final int DISCONTROL = 3;
        public static final int SET_PROPERTY = 4;
        private static final short CONTACT_PORT = 2135;
        private BusAttachment mBus;
        private ProxyBusObject mDeviceProxyObj;
        private PropertiesChangedListener mPropertiesChangedListener;

        public ControlHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case CONTROL:
                    DaemonInit.PrepareDaemon(Service.this.getApplicationContext());
                    mBus = new BusAttachment("detail-bus", BusAttachment.RemoteMessage.Receive);
                    Log.i(TAG, "ControlHandler: 1111");
                    Status status = mBus.connect();
                    if (Status.OK != status) {
                        return;
                    }

                    Mutable.ShortValue contactPort = new Mutable.ShortValue(CONTACT_PORT);

                    SessionOpts sessionOpts = new SessionOpts();
                    sessionOpts.traffic = SessionOpts.TRAFFIC_MESSAGES;
                    sessionOpts.isMultipoint = false;
                    sessionOpts.proximity = SessionOpts.PROXIMITY_ANY;
                    sessionOpts.transports = SessionOpts.TRANSPORT_ANY;

                    mBus.bindSessionPort(contactPort, sessionOpts, new SessionPortListener() {
                        @Override
                        public boolean acceptSessionJoiner(short sessionPort,
                                                           String joiner, SessionOpts sessionOpts) {
                            // Allow all connections on our contact port.
                            return sessionPort == CONTACT_PORT;
                        }
                    });


                    initPropertiesChangedListener();
                    status = mBus.registerSignalHandlers(mPropertiesChangedListener);
                    if (status != Status.OK) {
                        return;
                    }

                    initObserver();

                    break;
                case DISCONTROL:
                    if (mObserver != null) {
                        mObserver.close();
                    }
                    mBus.disconnect();
                    getLooper().quit();
                    break;
                case SET_PROPERTY:
                    String command = (String) msg.obj;
                    Log.i(TAG, "command: " + command);
                    if (mDeviceProxyObj != null) {
                        try {
                            mDeviceProxyObj.setProperty(cn.edu.zstu.IrTransponder.class, cn.edu.zstu.IrTransponder.PROPERTY_NAME, new Variant(command.toString()));
                        } catch (BusException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
            }
        }

        private void initObserver() {
            mObserver = new Observer(mBus, new Class[]{cn.edu.zstu.IrTransponder.class});
            mObserver.registerListener(new Observer.Listener() {
                @Override
                public void objectDiscovered(ProxyBusObject proxyBusObject) {
                    mDeviceProxyObj = proxyBusObject;
                    discoverHandler.sendMessage(discoverHandler.obtainMessage(1002, proxyBusObject));
                }

                @Override
                public void objectLost(ProxyBusObject proxyBusObject) {
                    discoverHandler.sendMessage(discoverHandler.obtainMessage(1003, proxyBusObject));
                }
            });

        }

        private void initPropertiesChangedListener() {
            mPropertiesChangedListener = new IrTransponderEventListener();
            mBus.addMatch("type='signal',interface='cn.edu.zstu.IrTransponder',member='jsonDataChanged'");
        }

        class IrTransponderEventListener extends PropertiesChangedListener {

            @BusSignalHandler(iface = cn.edu.zstu.IrTransponder.INTF_NAME, signal = cn.edu.zstu.IrTransponder.SIGNAL_NAME)
            public void jsonDataChanged(String newData) {
                Log.i(TAG, "jsonDataChanged: " + newData);
                discoverHandler.sendMessage(discoverHandler.obtainMessage(1004,newData));
            }

            @Override
            public void propertiesChanged(ProxyBusObject proxyBusObject, String ifaceName, Map<String, Variant> map, String[] strings) {
                Log.i(TAG, "propertiesChanged: " + proxyBusObject.getBusName());
            }
        }
    }

    /**
     * AsyncTask to get device data.
     */
    class DeviceDataAsyncTask extends AsyncTask<IrTransponder, Integer, String> {

        @Override
        protected String doInBackground(cn.edu.zstu.IrTransponder... params) {
            try {
                return params[0].getjsonData();
            } catch (BusException e) {
            }
            return null;
        }
    }
}
