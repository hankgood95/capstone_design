package wook.inc.please;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class BluetoothChatService {

    // 디버깅
    private static final String TAG = "BluetoothChatService";
    private static final boolean D = true;

    // 서버 소켓 생성시 SDP Record의 이름
    private static final String NAME = "MainActivity";

    // 애플리케이션 고유 UUID
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    // 멤버 변수
    private final BluetoothAdapter mAdapter;
    private final Handler mHandler;
    private AcceptThread mAcceptThread; //서버소켓을 만들고 무엇인가 연결될때까지 반복 하는 부분
    private ConnectThread mConnectThread; //다른 기기의 AcceptThread와 연결을 시도하는 부분
    private ConnectedThread mConnectedThread; //연결을 관리하는 부분
    private int mState;

    // 연결 상태
    public static final int STATE_NONE = 0;       // 아무것도 하지 않음
    public static final int STATE_LISTEN = 1;     // 연결된 장치에 데이터를 받기위해 대기중
    public static final int STATE_CONNECTING = 2; // 연결된 장치에 데이터를 보내기위해 초기화중
    public static final int STATE_CONNECTED = 3;  // 블루투스 장치와 연결됨



    /**
     * MenuActivity을 위한 구조체.
     * @param context  UI Activity Context
     * @param handler UI Activity로 메시지를 다시보내는 핸들러
     */
    public BluetoothChatService(Fragment fragment, Handler handler) {
        Log.d("TEST", "+++ ON BluetoothChatService +++");
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        mState = STATE_NONE;
        mHandler = handler;
    }

    private synchronized void setState(int state) { //값이 변경되기 전에 다른게 들어와서 접근 불가하게 만드는것
        if (D) Log.d(TAG, "setState() " + mState + " -> " + state);
        Log.d("TEST", "setState() " + mState + " -> " + state);
        mState = state;

        mHandler.obtainMessage(ViewContactFragment.MESSAGE_STATE_CHANGE, state, -1).sendToTarget();
    }

    /**
     * 연결 상태 반환. */
    public synchronized int getState() {
        Log.d("TEST", "getState() = "  + mState);


        return mState;
    } //이것도 마찬가지 이건 현재 연결상태를 반환

    /**
     * 블루투스 통신 시작.
     * 받기 모드. Activity onResume() */
    public synchronized void start() { //연결서비스를 시작하게 하려는 부분 정확히 AcceptThread에서 앉아서 연결 기다리는걸 시작하는 부분 이건 액티비티 onResume()에 의해 호출됨
        if (D) Log.d(TAG, "start");

        if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;} //mConnecThread가 비어있지 않으면 그 스레드 취소하고 다시 널로 만든다

        if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}

        if (mAcceptThread == null) { //AcceptThread가 비어있으면 새로운걸 만드는 부분
            mAcceptThread = new AcceptThread();
            mAcceptThread.start();//이건 위에 start()와 다르다.이건 mAcceptThread를 시작하는거
        }
        setState(STATE_LISTEN);
        Log.d("TEST", "change state to --> "+mState);
//        setState(STATE_CONNECTED);
    }

    //AcceptThread 연결이 올때까지 start되어 지고 그러면 connectThread 는 다른 디바이스의 AcceptThread와 연결을 시도하는 부분


    public synchronized void connect(BluetoothDevice device) { //connectThread 실행시키는 부분
        if (D) Log.d(TAG, "connect to: " + device);

        if (mState == STATE_CONNECTING) {
            if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}
    }

        if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}

        mConnectThread = new ConnectThread(device);
        mConnectThread.start();
        setState(STATE_CONNECTING);
        Log.d("TEST", "change state to --> "+mState);
    }

    public synchronized void connected(BluetoothSocket socket, BluetoothDevice device) { //각각의 스레드들을 실행시키는 부분
        if (D) Log.d(TAG, "connected");

        if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}

        if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}

        if (mAcceptThread != null) {mAcceptThread.cancel(); mAcceptThread = null;}

        mConnectedThread = new ConnectedThread(socket); //ConnectedThread 실행시키는 부분
        mConnectedThread.start();

        Message msg = mHandler.obtainMessage(ViewContactFragment.MESSAGE_DEVICE_NAME);//핸들러를 통해 전송
        Bundle bundle = new Bundle();
        bundle.putString(ViewContactFragment.DEVICE_NAME, device.getName());
        msg.setData(bundle);
        mHandler.sendMessage(msg);

        setState(STATE_CONNECTED);
        Log.d("TEST", "change state to --> "+mState);
    }

    public synchronized void stop() {
        if (D) Log.d(TAG, "stop");
        if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}
        if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}
        if (mAcceptThread != null) {mAcceptThread.cancel(); mAcceptThread = null;}
        setState(STATE_NONE);
        Log.d("TEST", "change state to --> "+mState);
    }

    public void write(byte[] out) { //메인액티비티에서 접근이 가능하도록 스레드를 실행시키는 함수를 만든것
        ConnectedThread r;
        synchronized (this) {
            if (mState != STATE_CONNECTED) return;
            r = mConnectedThread;
        }
        r.write(out);
    }


    private void connectionFailed() { //연결이 이루어 지지않았을때 호출되는 함수
        setState(STATE_LISTEN);
        Log.d("TEST", "change state to --> "+mState);
        Message msg = mHandler.obtainMessage(ViewContactFragment.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(ViewContactFragment.TOAST, "연결 장치를 사용할 수 없습니다.");
        msg.setData(bundle);
        mHandler.sendMessage(msg);
    }

    private void connectionLost() { //연결을 관리하는 ConnectedThread에서 문제가 생겨 데이터를 받아오지 못하면 이함수를 호출
        setState(STATE_LISTEN);
        Log.d("TEST", "change state to --> "+mState);
        Message msg = mHandler.obtainMessage(ViewContactFragment.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(ViewContactFragment.TOAST, "Device connection was lost");
        msg.setData(bundle);
        mHandler.sendMessage(msg);
    }
    //실시간으로 연결이 오는지 확인하고 올때까지 계속해서 확인 하는 부분, 즉 연결이 올때까지 작동하는 스레드(혹은 종료되거나)
    private class AcceptThread extends Thread {
        //지역서버 소켓
        private final BluetoothServerSocket mmServerSocket;

        public AcceptThread() {
            BluetoothServerSocket tmp = null;

            try {
                //듣는 서버 소켓을 만든다
                tmp = mAdapter.listenUsingRfcommWithServiceRecord(NAME, MY_UUID); //서버소켓의 이름과 uuid를 위에서 설정해준걸로 만든 부분
            } catch (IOException e) {
                Log.e(TAG, "listen() failed", e);
            }
            mmServerSocket = tmp;
        }

        public void run() { //위에 mAcceptThread 시작 하는 부분. 이건 스레드를 시작하면 자동적으로 호출하는 부분
            if (D) Log.d(TAG, "BEGIN mAcceptThread" + this);
            setName("AcceptThread");
            BluetoothSocket socket = null; //서버소켓이 아닌 블루투스 소켓을 만든다

            while (mState != STATE_CONNECTED) { //무엇인가 연결이 될때까지 여기서 계속 반복?
                    try {
                    socket = mmServerSocket.accept(); //코드가 앉아서 연결이 될때까지 기다리는 부분 연결이 성공적으로 되면 이걸 실행하고 아니면 밑에 캐치부분 들어간다
                } catch (IOException e) {
                    Log.e(TAG, "accept() failed", e);
                    //break;
                }

                if (socket != null) { //이건 이제 다음 단계 즉 연결 하겠다는 소리이다
                    synchronized (BluetoothChatService.this) {
                        switch (mState) {
                            case STATE_LISTEN:
                            case STATE_CONNECTING:

                                connected(socket, socket.getRemoteDevice());
                                break;
                            case STATE_NONE:
                            case STATE_CONNECTED:

                                try {
                                    socket.close();
                                } catch (IOException e) {
                                    Log.e(TAG, "Could not close unwanted socket", e);
                                }
                                break;
                        }
                    }
                }
            }
            if (D) Log.i(TAG, "END mAcceptThread");
        }

        public void cancel() { //이건 위에서 서버소켄 만드는 그 AcceptThread를 멈추게 하는 부분
            if (D) Log.d(TAG, "cancel " + this);
            try {
                mmServerSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of server failed", e);
            }
        }
    }

    private class ConnectThread extends Thread { //이 스레드는 장치와 나가는 연결을 시도하는 동안 실행된다. 이건 연결이 되든 안되는 곧장 실행된다. 즉 어떤 기기가 소켓을 잡기전까지 실행?
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) { //connectThread가 시작 되는 부분
            mmDevice = device;
            BluetoothSocket tmp = null;

            try {
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID); //블루투스 장치와의 연결을 위해 블루투스 소켓을 가져오는부분,uuid로 가져온다
            } catch (IOException e) {
                Log.e(TAG, "create() failed", e);
            }
            mmSocket = tmp;
        }

        public void run() {//스레드 안에서 자동적으로 실행된다. 그래서 따로 호출하지 않아도 된다.
            Log.i(TAG, "BEGIN mConnectThread");
            setName("ConnectThread");

            mAdapter.cancelDiscovery(); //연결이 이루어지면 이걸 해줘야 연결이 느려지지 않는다


            try {
                //블루투스 소켓과 연결을 하는 부분
                //이건 연결이 성공 되거나 예외형상일때만 들어간다
                mmSocket.connect();
            } catch (IOException e) {
                connectionFailed(); //연결이 이루어 지지 않았을때 이걸 호출
                Log.d("TEST", "change state to --> "+mState);

                try { //연결이 이루어지지 않으면 소켓을 닫는다 즉 종료 한다.
                    mmSocket.close();
                } catch (IOException e2) {
                    Log.e(TAG, "unable to close() socket during connection failure", e2);
                }

                BluetoothChatService.this.start();
                return;
            }

            synchronized (BluetoothChatService.this) {
                mConnectThread = null;
            }

            connected(mmSocket, mmDevice); //연결이 성공적으로 이루어 졌다면 이함수를 호출해서 소켓과 uuid를 전송한다
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }
    }

    private class ConnectedThread extends Thread {//연결을 관리하는 부분
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {//이걸 시작하고 위에서 선언한걸 설명해준다 즉 값을 집어넣어준다
            Log.d(TAG, "create ConnectedThread");
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;


            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "temp sockets not created", e);
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {//스레드가 작동되면 저절로 실행되는 부분
            Log.i(TAG, "BEGIN mConnectedThread");
                byte[] buffer = new byte[1024]; //mmInStream으로 부터 온값을 저장 하는 부분
                int bytes; //read() 로부터 리턴되어온 값 (바이트를 읽어들일거라서 int형식임)
                String str;
                while (true) {//연결이 이루어지면 계속해서 mmInStream을 읽어들이는걸 반복되는 부분
                try { //이게 실행되어지면 이걸 하고 아니면 catch로 넘어감
                        bytes = mmInStream.read(buffer); //버퍼를 읽어들이는 부분
                        mHandler.obtainMessage(ViewContactFragment.MESSAGE_READ, bytes, -1, buffer)//이건 이제 읽어 들이는 부분이다. 이걸 메인액티비티에서받은 핸들러를 통해 전송한다다
                                .sendToTarget();

                } catch (IOException e) { //이게 실행이 안되어지면 반복문을 break문을 통해서 나간다
                    Log.e(TAG, "disconnected", e);
                    connectionLost(); //이함수를 호출한다
                    Log.d("TEST", "change state to --> "+mState);
                    break;
                }
            }
        }

        public void write(byte[] buffer) { //메인액티비티에서 호출되어지고 디바이스에 데이터를 전송하는 부분
            try {
                mmOutStream.write(buffer); //메인택티비티에서 받은걸로 이제 보낸다


                mHandler.obtainMessage(ViewContactFragment.MESSAGE_WRITE, -1, -1, buffer)
                        .sendToTarget();
            } catch (IOException e) { //이게 보내지지않으면 이걸 찍는다
                Log.e(TAG, "Exception during write", e);
            }
        }

        public void cancel() { //이것역시 취소하는 부분
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }
    }
}