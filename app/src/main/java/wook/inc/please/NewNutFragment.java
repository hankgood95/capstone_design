package wook.inc.please;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import wook.inc.please.Database.Weight_DB;


public class NewNutFragment extends Fragment implements NumberPickerDialog.OnInputSelected{

    int[] numList = new int[30];


    private static final String TAG = "NewNutFragment";

    @Override
    public void sendInput(String input) {
        Log.d(TAG, "sendInput: found incoming input: " + input);
        makeNumList();
        nutritionNum = numList[Integer.parseInt(input)];
        left = nutritionNum;
        setNum.setText(nutritionNum+"개");
    }
    //////////////////////////////////////////////////////////////////////////////////////////////////////////
    private static final boolean D = true;
    boolean th_ctl = false;

    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;

    private static final int REQUEST_ENABLE_BT = 2;

    public BluetoothChatService getGram = null; //블루투스로부터 값을 받기 위해 사용하는 것
    public BluetoothAdapter BluetoothAdapter; //현재 블루투스 상태를 알기 위해 만듬
    private StringBuffer mOutStringBuffer; //값을 보낼때 문자열을 버퍼형태로 바꿔주는것


    private Context context;

    private StringBuffer n_stringBuffer = new StringBuffer(); //값을 읽어들일때 쓰기 위한 문자열 버퍼
    //////////////////////////////////////////////////////////////////////////////////////////////////////////
    float fullNutritionGram = 0; //영양제 한정의 무게를 저장하는 변수

    EditText getName;

    Button getFullWeight;
    Button getOneWeight;
    Button getNum;
    Button getTime;
    Button save;
    Button startOver;

    TextView setFullweight;
    TextView setOneWeight;
    TextView setNum;

    TimePicker time;


    int fullNutritionGram_int = 0;
    int oneNutrition_int = 0;
    int nutritionNum;//영양제 개수가 저장되는 변수
//    int hour;
//    int minute;
    int left;

    Weight_DB weight_db;
    //////////////////////////////////////////////////////////////////////////////////////////////////////////
    public static final String CHANNEL_1_ID = "channel1";
    NotificationManager mNotiMgr;
    //////////////////////////////////////////////////////////////////////////////////////////////////////////
//    String[] numList = new String[30]; //NumberPickerDialog에서 받은 인덱스값을 받아와서 해당하는 인덱스안에 값을 받기 위해서

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_nutsetup, container, false);
        Log.d(TAG, "onCreateView: View created");

        context = container.getContext();
        BluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        weight_db = new Weight_DB(context);
        ///////////////////////////////////////////notification 부분//////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        if (!BluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }

        if(BluetoothAdapter.isEnabled()){

            if (getGram == null) setupChat(); //함수 호출
            //블루투스와 연결 시켜주는 부분
        }

/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        getFullWeight = (Button)view.findViewById(R.id.fullGram);
        getOneWeight = (Button)view.findViewById(R.id.oneGram);
        getNum = (Button)view.findViewById(R.id.nutritionNum);
//        getTime = (Button)view.findViewById(R.id.setTime);
        save = (Button)view.findViewById(R.id.saveSetup);
        startOver = (Button)view.findViewById(R.id.startOver);

        getName = (EditText)view.findViewById(R.id.tvName);

        setFullweight = (TextView)view.findViewById(R.id.fullGram_text);
        setOneWeight = (TextView)view.findViewById(R.id.oneGram_text);
        setNum = (TextView)view.findViewById(R.id.nutritionNum_text);

//        time = (TimePicker)view.findViewById(R.id.time_Picker);

/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        ImageView ivBackArrow = (ImageView)view.findViewById(R.id.ivBackArrow);
        ivBackArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: clicked backArrow");
                //이전 fragment로 돌아가게 하는것
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });

        ImageView ivLink = (ImageView)view.findViewById(R.id.ivLink);
        ivLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!BluetoothAdapter.isEnabled()) {
                    Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
                }
                if(BluetoothAdapter.isEnabled()) {
                    int check = 1;
                    BluetoothDevice device = BluetoothAdapter.getRemoteDevice("98:D3:32:70:B7:0B"); //버튼 누르면 바로 hc06이랑 연결 시키는 부분
                    getGram.connect(device);
                    while(getGram.getState()!=BluetoothChatService.STATE_CONNECTED){
                        Toast.makeText(context,"연결중...",Toast.LENGTH_SHORT).show();
                        if(check==20){
                            break;
                        }
                        check++;
                    }
                    sendMessage("1");
//                    int cbt = getGram.getState();
//                    if(cbt == 3 ){
//                        checkBt.setImageResource(R.drawable.green_circle);
//                    }

//                    if(getGram.getState() == BluetoothChatService.STATE_CONNECTED){
//
//                    }
                }
            }
        });

        /////////////////////////////////////////////////////총무게 얻는 버튼/////////////////////////////////////////////////////////////////
        getFullWeight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alert_All = new AlertDialog.Builder(context);
                alert_All.setTitle("영양제 설정");
                alert_All.setMessage("영양제 한통을 올려 놓으십시오");
                alert_All.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        sendMessage("1"); //아두이노에서 "1"을 받아야 값을 받도록 설정했기 때문에 이걸 1을 보낸다
                        fullNutritionGram_int = Math.round(fullNutritionGram);
                        if(fullNutritionGram_int<300){
                            Toast.makeText(context,"다시한번 눌러 주십시오",Toast.LENGTH_SHORT).show();
                        }
                        else if(fullNutritionGram_int>300){
                            setFullweight.setText(fullNutritionGram_int+"g");
                        }
                    }
                });
                alert_All.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                AlertDialog alert = alert_All.create();
                alert.show();
            }
        });
        /////////////////////////////////////////////////////한정 무게 얻는 버튼////////////////////////////////////////////////////////////////
        getOneWeight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alert_one = new AlertDialog.Builder(context);
                alert_one.setTitle("영양제 설정");
                alert_one.setMessage("올려놓으신 영양제 통에서 영양제 한알을 빼고 뚜껑을 닫으신후 무게센서에 다시 올려 놓으십시오");

                alert_one.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int full= fullNutritionGram_int;
                        sendMessage("1");
                        int oneNutrition = Math.round(fullNutritionGram);
                        oneNutrition_int = full-oneNutrition;

                        if(oneNutrition_int<=0||oneNutrition_int>=4){
                            Toast.makeText(context,"다시한번 눌러 주십시오",Toast.LENGTH_SHORT).show();
//                            setOneWeight.setText("다시한번 눌러 주십쇼");
                        }
                        else {
                            Log.d("TEST","OneNutrition Gram = "+oneNutrition_int);
                            setOneWeight.setText(oneNutrition_int+"g");
                        }
                    }
                });

                alert_one.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                AlertDialog alert = alert_one.create();
                alert.show();
            }
        });
        //////////////////////////////////////////////////////개수 얻는 버튼///////////////////////////////////////////////////////////////
        getNum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                NumberPickerDialog dialog = new NumberPickerDialog();
                dialog.setTargetFragment(NewNutFragment.this,1);
                dialog.show(getFragmentManager(),"NumberPickerDialog");

            }
        });
        ////////////////////////////////////////////////////////시간 얻는 버튼/////////////////////////////////////////////////////////////
//        getTime.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
//                    hour = time.getHour();
//                    minute = time.getMinute();
//                    Toast.makeText(context, hour + "시 " +minute+"분" , Toast.LENGTH_SHORT).show();
//                    showNoti();
//                }
//            }
//        });
        /////////////////////////////////////저장 버튼////////////////////////////////////////////////////////////////////////////////
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String gotName = getName.getText().toString();
                weight_db.saveData(gotName,fullNutritionGram_int,oneNutrition_int,nutritionNum,left);
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });
        /////////////////////////////////////취소 버튼//////////////////////////////////////////////////////////////////////////////
        startOver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fullNutritionGram_int = 0;
                oneNutrition_int = 0;
                nutritionNum = 0;
//                hour = 0;
//                minute = 0;
                setFullweight.setText("버튼을 눌러 값을 받으시오");
                setOneWeight.setText("버튼을 눌러 값을 받으시오");
                setNum.setText(nutritionNum+"개");
            }
        });
        /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        if (getGram == null) setupChat(); //BluetoothChatService의 객체가 널값이면 setupchat함수 호출

        if (getGram != null) { //널값이 아니면
            if (getGram.getState() == BluetoothChatService.STATE_NONE) { //아무것도 받지 않았을때

                getGram.start();//BluetoothChatService 시작
            }
        }


        return view;
    }


    ////////////////////////////notification 만드는 부분///////////////////////////////////////////////////////////////////

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //이뷰가 끝날떄 mchatService를 끈다
    @Override
    public void onDestroyView(){
        super.onDestroyView();
        if(getGram!=null) {
            getGram.stop();
        }
//        if (BluetoothAdapter.isEnabled()) { //앱꺼지면 블루투스 끔
//            Log.d(TAG, "enableDisableBT: disabling BT.");
//            BluetoothAdapter.disable();
//        }
    }



    private void setupChat() { //BluetoothChatService의 객체를 형성하는 부분
        Log.d("TEST", "setupChat()");

        getGram = new BluetoothChatService(this, nutritionHandler); //이걸 새로 만든다 context는 이 여기고 핸들러는 mhandler를 보낸다

        mOutStringBuffer = new StringBuffer("");
    }

    public void sendMessage(String message) { //블루투스로 값을 보내기 위해 있는 변수

        if (getGram.getState() != BluetoothChatService.STATE_CONNECTED) {
            Log.d("TEST", "Now Bluetooth State is --> "+getGram.getState());
            Toast.makeText(context, R.string.not_connected, Toast.LENGTH_SHORT).show();
            return;
        }

        if (message.length() > 0) {

            byte[] send = message.getBytes();
            getGram.write(send);
            mOutStringBuffer.setLength(0);
        }
    }

    public void makeNumList() { //numberPicker 값을 넣는 배열을 만드는 함수
        for (int i = 0; i < 30; i++) {
            numList[i] = (i + 1) * 10;
        }
    }

    public final Handler nutritionHandler = new Handler() { //BluetoothChatService에서 값을 받아오는 핸들러를 만든 부분
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_STATE_CHANGE:
                    if(D) Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
                    switch (msg.arg1) { //이걸 받았을때 해당하는 메시지를 받음
                        case BluetoothChatService.STATE_CONNECTED:
                            Toast.makeText(context,R.string.title_connected_to,Toast.LENGTH_SHORT).show();
                            break;
                        case BluetoothChatService.STATE_CONNECTING:
                            Toast.makeText(context,R.string.title_connecting,Toast.LENGTH_SHORT).show();
                            break;
                        case BluetoothChatService.STATE_LISTEN:
                        case BluetoothChatService.STATE_NONE:
                            Toast.makeText(context,R.string.title_not_connected,Toast.LENGTH_SHORT).show();
                            break;
                    }
                    break;
                case MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;

                    String writeMessage = new String(writeBuf);

                    break;
                case MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    String readMessage = new String(readBuf, 0, msg.arg1);
                    n_stringBuffer.append(readMessage); //스트링 버퍼에 readmessage를 저장한다
                    int endOfLine = n_stringBuffer.indexOf("#");// #으로 끝나는 부분의 위치를 저장
                    if(endOfLine>0){ //끝나는 라인이 0보다 크면 진입
                        String sbprint = n_stringBuffer.substring(0,endOfLine);//sbprint변수에 0부터 끝나는 지점까지의 부분만 저장
                        n_stringBuffer.delete(0,n_stringBuffer.length()); //String buffer 지움
                        fullNutritionGram = Float.parseFloat(sbprint); //위에서 받은 sbprint를 float형식으로 변환
                        if(fullNutritionGram<300){Toast.makeText(context,"다시한번 눌러 주십쇼",Toast.LENGTH_SHORT).show(); } //300이하로 센서값이 오면 다시한번 눌러야된다.
                        if(fullNutritionGram>=300){ } //
                    }

                    break;
//                case MESSAGE_DEVICE_NAME:
//
//                    mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
//                    Toast.makeText(getApplicationContext(), "Connected to "
//                            + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
//                    break;
//                case MESSAGE_TOAST:
//                    Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST),
//                            Toast.LENGTH_SHORT).show();
//                    break;
            }
        }
    };

    public void onActivityResult(int requestCode, int resultCode, Intent data) { //서브 액티비티로부터 받은 결과값을 가지고 처리하는 부분
        switch (requestCode) {
            case REQUEST_ENABLE_BT: //블루투스의 현상황에 대해서 알려주는 부분
                // 블루투스 사용 가능 요청이 반환되면 실행
                if (resultCode == Activity.RESULT_OK) {
                    // 블루투스가 이제 사용 가능하므로 채팅 세션 설정
                    setupChat();
                    int check = 1;
                    BluetoothDevice device = BluetoothAdapter.getRemoteDevice("98:D3:32:70:B7:0B"); //버튼 누르면 바로 hc06이랑 연결 시키는 부분
                    getGram.connect(device);
                    while(getGram.getState()!=BluetoothChatService.STATE_CONNECTED){
                        Toast.makeText(context,"연결중...",Toast.LENGTH_SHORT).show();
                        if(check == 20){
                            break;
                        }
                        check++;
                    }
                    sendMessage("1");
//                    int cbt = getGram.getState();
//                    if(cbt == 3){
//                        checkBt.setImageResource(R.drawable.green_circle);
//                    }
//                    if(getGram.getState() == BluetoothChatService.STATE_CONNECTED){
//
//                    }
                } else {
                    // 사용자가 블루투스를 활성화하지 않았거나 오류가 발생
                    Log.d(TAG, "BT01 not enabled");
                    Toast.makeText(context,"블루투스를 켜야 무게를 잴수 있습니다", Toast.LENGTH_SHORT).show();
//                    onDestroy();
                }
        }
    }
}
