/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package kr.co.surveylink.www.mobilelink;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.VpnService;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.ArrayList;
import java.util.List;

public class ToyVpnService extends VpnService implements Handler.Callback, Runnable, IDataHandler {

    static private ToyVpnService instance;
    static public synchronized ToyVpnService getInstance(){
        if(instance==null){
            instance=new ToyVpnService();
        }
        return instance;
    }

    private Handler mHandler;
    private Thread mThread;
    private PendingIntent mConfigureIntent;

    private ParcelFileDescriptor mInterface;
    private String mParameters;

    private String mServerAddress = "";
    private String mServerPort = "";
    private byte[] mSharedSecret = "mobilelink!".getBytes();

    static public boolean needRestart = false;
    static public boolean hasPermission = true;
    static private List<String> allowPackages = new ArrayList<>();

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        MPermissions.getInstance().permissionChanged(getApplicationContext(),MPermissions.NEED_VPN,false);

        ToyVpnService.needRestart=false;

        // The handler is only used to show messages.
        if (mHandler == null) {
            mHandler = new Handler(this);
        }

        // Stop the previous session by interrupting the thread.
        if (mThread != null) {
            mThread.interrupt();
        }

        // Start a new session by creating a new thread.
        mThread = new Thread(this, "ToyVpnThread");
        mThread.start();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        if (mThread != null) {
            mThread.interrupt();
        }
    }

    @Override
    public boolean handleMessage(Message message) {
        if (message != null) {
            Toast.makeText(this, message.what, Toast.LENGTH_SHORT).show();
        }
        return true;
    }

    @Override
    public synchronized void run() {
        try {
            Common.log("VPN-Starting");

            if(mServerAddress.equals("")) {
                //Vpn 접속정보를 새로 가져와야됨
                getVpn(getApplicationContext());
                Common.log("Get vpn");
            } else {
                // If anything needs to be obtained using the network, get it now.
                // This greatly reduces the complexity of seamless handover, which
                // tries to recreate the tunnel without shutting down everything.
                // In this demo, all we need to know is the server address.
                InetSocketAddress server = new InetSocketAddress(
                        mServerAddress, Integer.parseInt(mServerPort));

                // We try to create the tunnel for several times. The better way
                // is to work with ConnectivityManager, such as trying only when
                // the network is avaiable. Here we just use a counter to keep
                // things simple.
                for (int attempt = 0; attempt < 1; ++attempt) {
                    //mHandler.sendEmptyMessage(R.string.connecting);
                    Common.log(getString(R.string.connecting));

                    // Reset the counter if we were connected.
                    if (run(server)) {
                        attempt = 0;
                    }

                    // Sleep for a while. This also checks if we got interrupted.
                    //Thread.sleep(3000);
                }
                //접속 실패하면 정보를 다시 가져옴
                mServerAddress="";
                mServerPort="";
            }
            Common.log("VPN-Giving up");
        } catch (Exception e) {
            Common.log("VPN-Got " + e.toString());
        } finally {
            try {
                mInterface.close();
            } catch (Exception e) {
                // ignore
            }
            mInterface = null;
            mParameters = null;

            //mHandler.sendEmptyMessage(R.string.disconnected);
            Common.log(getString(R.string.disconnected));
            Common.log("VPN-Exiting");
            MServiceMonitor.getInstance().startVpn();
        }
    }

    private boolean run(InetSocketAddress server) throws Exception {
        DatagramChannel tunnel = null;
        boolean connected = false;
        try {
            // Create a DatagramChannel as the VPN tunnel.
            tunnel = DatagramChannel.open();

            // Protect the tunnel before connecting to avoid loopback.
            if (!protect(tunnel.socket())) {
                throw new IllegalStateException("Cannot protect the tunnel");
            }
            // Connect to the server.
            tunnel.connect(server);

            // For simplicity, we use the same thread for both reading and
            // writing. Here we put the tunnel into non-blocking mode.
            tunnel.configureBlocking(false);

            // Authenticate and configure the virtual network interface.
            handshake(tunnel);

            // Now we are connected. Set the flag and show the message.
            connected = true;
            MServiceMonitor.getInstance().vpnStartCount=0;
            //mHandler.sendEmptyMessage(R.string.connected);
            Common.log(getString(R.string.connected));

            // Packets to be sent are queued in this input stream.
            FileInputStream in = new FileInputStream(mInterface.getFileDescriptor());

            // Packets received need to be written to this output stream.
            FileOutputStream out = new FileOutputStream(mInterface.getFileDescriptor());

            // Allocate the buffer for a single packet.
            ByteBuffer packet = ByteBuffer.allocate(32767);

            // We use a timer to determine the status of the tunnel. It
            // works on both sides. A positive value means sending, and
            // any other means receiving. We start with receiving.
            int timer = 0;

            // We keep forwarding packets till something goes wrong.
            while (!ToyVpnService.needRestart) {

                // Assume that we did not make any progress in this iteration.
                boolean idle = true;

                // Read the outgoing packet from the input stream.
                int length = in.read(packet.array());
                if (length > 0) {
                    //Common.log("VPN-IN:"+Integer.toString(length));
                    // Write the outgoing packet to the tunnel.
                    packet.limit(length);
                    try {
                        tunnel.write(packet);
                    } catch (Exception e) {
                        Common.log("packet write fail");
                    }
                    packet.clear();

                    // There might be more outgoing packets.
                    idle = false;

                    // If we were receiving, switch to sending.
                    if (timer < 1) {
                        timer = 1;
                    }
                }
                // Read the incoming packet from the tunnel.
                length = tunnel.read(packet);
                if (length > 0) {
                    // Ignore control messages, which start with zero.
                    if (packet.get(0) != 0) {
                        // Write the incoming packet to the output stream.
                        out.write(packet.array(), 0, length);
                        //Common.log("VPN-OUT:"+Integer.toString(length));
                    }
                    packet.clear();

                    // There might be more incoming packets.
                    idle = false;

                    // If we were sending, switch to receiving.
                    if (timer > 0) {
                        timer = 0;
                    }
                }

                // If we are idle or waiting for the network, sleep for a
                // fraction of time to avoid busy looping.
                int delay = 20;
                if (idle) {
                    Thread.sleep(delay);

                    // Increase the timer. This is inaccurate but good enough,
                    // since everything is operated in non-blocking mode.
                    timer += (timer > 0) ? delay : -delay;

                    // We are receiving for a long time but not sending.
                    if (timer < -delay*150) {
                        // Send empty control messages.
                        packet.put((byte) 0).limit(1);
                        for (int i = 0; i < 3; ++i) {
                            packet.position(0);
                            tunnel.write(packet);
                        }
                        packet.clear();

                        // Switch to sending.
                        timer = 1;
                    }
                    // We are sending for a long time but not receiving.
                    if (timer > 60000) {
                        throw new IllegalStateException("Timed out");
                    }
                }
            }
            connected=false;
        } catch (InterruptedException e) {
            throw e;
        } catch (Exception e) {
            Common.log("VPN-Got " + e.toString());
        } finally {
            try {
                tunnel.close();
            } catch (Exception e) {
                // ignore
            }
        }
        return connected;
    }

    private void handshake(DatagramChannel tunnel) throws Exception {
        // To build a secured tunnel, we should perform mutual authentication
        // and exchange session keys for encryption. To keep things simple in
        // this demo, we just send the shared secret in plaintext and wait
        // for the server to send the parameters.

        // Allocate the buffer for handshaking.
        ByteBuffer packet = ByteBuffer.allocate(1024);

        // Control messages always start with zero.
        packet.put((byte) 0).put(mSharedSecret).flip();

        // Send the secret several times in case of packet loss.
        for (int i = 0; i < 3; ++i) {
            packet.position(0);
            tunnel.write(packet);
        }
        packet.clear();

        // Wait for the parameters within a limited time.
        for (int i = 0; i < 50; ++i) {
            Thread.sleep(10);

            // Normally we should not receive random packets.
            int length = tunnel.read(packet);
            if (length > 0 && packet.get(0) == 0) {
                configure(new String(packet.array(), 1, length - 1).trim());
                return;
            }
        }
        throw new IllegalStateException("Timed out");
    }

    private void configure(String parameters) throws Exception {
        // If the old interface has exactly the same parameters, use it!
        if (mInterface != null && parameters.equals(mParameters)) {
            Common.log("VPN-Using the previous interface");
            return;
        }

        // Configure a builder while parsing the parameters.
        Builder builder = new Builder();
        if (Build.VERSION.SDK_INT >= 21) {
            try {builder.addAllowedApplication("com.android.settings");}catch(PackageManager.NameNotFoundException e){Common.log(e.toString());}
            for(int i=0;i<ToyVpnService.allowPackages.size();i++){
                try {
                    builder.addAllowedApplication(ToyVpnService.allowPackages.get(i));
                }catch(PackageManager.NameNotFoundException e){
                    Common.log(e.toString());
                }
            }
        }
        for (String parameter : parameters.split(" ")) {
            String[] fields = parameter.split(",");
            try {
                switch (fields[0].charAt(0)) {
                    case 'm':
                        builder.setMtu(Short.parseShort(fields[1]));
                        break;
                    case 'a':
                        builder.addAddress(fields[1], Integer.parseInt(fields[2]));
                        break;
                    case 'r':
                        builder.addRoute(fields[1], Integer.parseInt(fields[2]));
                        break;
                    case 'd':
                        builder.addDnsServer(fields[1]);
                        break;
                    case 's':
                        builder.addSearchDomain(fields[1]);
                        break;
                }
            } catch (Exception e) {
                throw new IllegalArgumentException("Bad parameter: " + parameter);
            }
        }

        // Close the old interface since the parameters have been changed.
        try {
            mInterface.close();
        } catch (Exception e) {
            // ignore
        }

        // Create a new interface using the builder and save the parameters.
        mInterface = builder.setSession(mServerAddress)
                .setConfigureIntent(mConfigureIntent)
                .establish();
        mParameters = parameters;
        Common.log("VPN-New interface: " + parameters);
    }

    /**
     * Vpn 접속 정보를 가져옴
     * @param context context
     */
    public void getVpn(Context context){
        if(!MPermissions.getInstance().isPermissionOk(context))return;
        Object[][] params = {};
        Common.getInstance().loadData(Common.HttpAsyncTask.CALLTYPE_GETVPN, context.getString(R.string.url_MGetVpn), params, this);
    }

    /**
     * 전송 후 종류에 따른 분기
     * @param calltype 전송의 종류
     * @param str 결과 문자열
     */
    public void dataHandler(int calltype, String str){
        switch(calltype){
            case Common.HttpAsyncTask.CALLTYPE_GETVPN:
                getVpnHandler(str);
                break;
        }
    }

    /**
     * 저장된 값의 전송 결과 처리
     * @param result 결과 문자열
     */
    private void getVpnHandler(String result) {
        try {
            JSONObject json = new JSONObject(result);
            String err = json.getString("ERR");
            if(err.equals("")){
                mServerAddress=json.getString("ADDR");
                mServerPort=json.getString("PORT");
                Common.log("IP:"+mServerAddress);
                Common.log("PORT:"+mServerPort);
                //Vpn 사용할 패키지들
                JSONArray packages=json.getJSONArray("PACKAGES");
                ToyVpnService.allowPackages = new ArrayList<>();
                for(int i=0;i<packages.length();i++){
                    ToyVpnService.allowPackages.add(packages.get(i).toString());
                }
            }
        } catch (Exception e){
            Common.log(e.toString());
        }
    }
}
