package wook.inc.please;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import devs.mulham.horizontalcalendar.HorizontalCalendar;
import devs.mulham.horizontalcalendar.utils.HorizontalCalendarListener;
import wook.inc.please.Database.Weight_DB;


public class ViewContactFragment extends Fragment {

    private SoundPool soundpool;
    private int sound1 , sound2;

    /////////////////////////////////////////////블루투스 받기 위한 변수들//////////////////////////////////////////////////////////////////////////
    public static float finalWeight;
    public static int finalWeight_int;
    private static final boolean D = true;
    boolean th_ctl = false;

    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;


    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";


    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;


    public BluetoothAdapter BluetoothAdapter;
    public BluetoothChatService mChatService = null;

    private StringBuffer n_stringBuffer = new StringBuffer();
    private StringBuffer stringBuffer = new StringBuffer();
    private StringBuffer mOutStringBuffer;


    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private static final String TAG = "ViewContactFragment";
    private AppBarLayout viewContactsBar;
    private Toolbar toolbar;
    //////////////////////////////////////////재고 확인 위한 변수//////////////////////////////////////////////////////////////////////
    TextView showWeight;
    private Context context;
    Button getleft;
    Weight_DB weight_db;
    public int full = 0;
    public int one = 0;
    public int num = 0;
    public String name = ""; //영양제 이름을 받는 변수
    ImageView leftImage;
    //////////////////////////////////////////복용 확인 알리기위한 변수//////////////////////////////////////////////////////////////////////////////////////
    LinearLayout show;
    TextView showdate; //달력 날짜 제대로 찍히는지
    Button eat;
    String pasteat; //가장최근에 저장된거 받아오는 문자열
    FloatingActionButton fab;
    ImageView noNut;
    TextView no;
    ImageView eatImaage;
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) { //액티비티에서 onCreate와 같은 역할을 하는것
        Log.d(TAG, "onCreateView: Started");
        View view = inflater.inflate(R.layout.fragment_viewcontacts, container, false); //이 레이아웃 파일을 실행한다
        context = container.getContext();

        //////////////////////////////////////////////////////////////sound pool/////////////////////////////////////////////////////////////////////////////////////////////////
//        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){ //버전마다 만드는 방식이 달라서
//            AudioAttributes audioAttributes = new AudioAttributes.Builder()
//                    .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
//                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
//                    .build();
//            soundpool = new SoundPool.Builder()
//                    .setMaxStreams(1)
//                    .setAudioAttributes(audioAttributes)
//                    .build(); //soundpool을 빌드하지만
//        }else{
//            soundpool = new SoundPool(1, AudioManager.STREAM_MUSIC,0); //버전이 낮으면 빌드 하지 않아도 된다
//        }
        soundpool = new SoundPool(10, AudioManager.STREAM_MUSIC,0);
        sound1 = soundpool.load(context,R.raw.bokyong,1); //빌드한 soundpool을 이용해서 소리를 soundpool을 이용해서 만드는것이다.
        sound2 = soundpool.load(context,R.raw.no_bokyong,1);
        ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        show = (LinearLayout) view.findViewById(R.id.status);
        weight_db = new Weight_DB(context);
        showWeight = (TextView) view.findViewById(R.id.showWeight);
        weight_db.giveWeight(this);
        leftImage = (ImageView)view.findViewById(R.id.leftImage);
        eatImaage = (ImageView)view.findViewById(R.id.eat_not);
        //////////////////////////////////////////////////////블루투스 사용 변수////////////////////////////////////////////////////////////////////////////////////////
        BluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        getleft = (Button) view.findViewById(R.id.getleft);
        /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        showdate = (TextView)view.findViewById(R.id.showdate);
        eat = (Button)view.findViewById(R.id.bt_eat);
        fab = (FloatingActionButton) view.findViewById(R.id.fab); //프래그먼트에서 xml과 연동하려면 view.을 써줘야 한다
        noNut = (ImageView)view.findViewById(R.id.noNut);
        no = (TextView)view.findViewById(R.id.say_No);
//////////////////////////////////////////////////////달력 만들기//////////////////////////////////////////////////////////////////////////////////////////

        /* starts before 1 month from now */
        Calendar startDate = Calendar.getInstance();
        startDate.add(Calendar.MONTH, -1);

        /* ends after 1 month from now */
        Calendar endDate = Calendar.getInstance();
        endDate.add(Calendar.MONTH, 1);



        HorizontalCalendar horizontalCalendar = new HorizontalCalendar.Builder(view,R.id.calendarView)
                .range(startDate, endDate)
                .datesNumberOnScreen(5)
                .build();

        horizontalCalendar.setCalendarListener(new HorizontalCalendarListener() {
            @Override
            public void onDateSelected(Calendar date, int position) {
                //여기에 달력에 선택된 날짜값을 받아와서 db에 접근한다.
                String getdate = ""; //선택된 날짜를 저장하는 문자열
                String firstdate = ""; //처음 저장된 날짜를 저장하는 문자열

                if(!weight_db.checkEmpty2()){ //걍 비어 있을때 그냥 선택된날짜 선택
                    Date d = date.getTime();
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                    String seldate = format.format(d);
                    showdate.setText(seldate);
                }
                else{ //날짜 db가 비어 있지 않을때 접근
                    firstdate = weight_db.giveFirstDate(); //가장 처음에 저장되어 있는 날짜를 가져온다

                    Date d = date.getTime(); //선택된 날짜 가져옴
                    SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");
                    getdate = format1.format(d); //선택된 날짜의 문자열 형태

                    Date firstsave = null; //처음 저장된 날을 여기다가 저장
                    Date selDate = null; //지금 선택된 날짜를 여기다가 저장

                    try{ //저장하는부분
                        firstsave = format1.parse(firstdate);
                        selDate = format1.parse(getdate);
                    }catch (ParseException e){
                        e.printStackTrace();
                    }

                    int compare = firstsave.compareTo(selDate); //날짜확인 db에 처음 저장된것이랑 지금선택된 날짜를 비교
                    if(compare>0){ //처음 저장된 날이 지금 선택된 날짜보다 크면은 아직 영양제를 구매전이니까 영양제 구매전 뜨게 한다
                        eatImaage.setImageResource(R.drawable.befor_buy);
                        showdate.setText("영양제 구매 전입니다...");
                    }
                    //이부분은 즉 미래의 일이거나 구매 이후이거나
                    else if(compare<0){ //선택된 날짜가 처음 저장된거보다 크면? 당연히 구매 이후를 나타낸것이니 선택된 날짜가 날짜 확인 db에 있는지 확인후 그에따른 결과를 출력
                        //근데 선택된 날짜가 처음 저장된거보다 큰경우가 두가지 경우가 있다 하나는 과거이지만 처음 저장된거보다 클때 하나는 미래일때

                        Calendar now = Calendar.getInstance(); //지금 시간을 가져온다
                        Date nowd = now.getTime();
                        int compare2 = nowd.compareTo(selDate); //지금 날짜랑 화면에서 선택된 날짜를 비교
                        if(compare2>0){ //현재날짜가 선택된 날짜보다 클때는 선택된게 과가인대다가 처음 저장된날짜보다 큰경우 이니 선택된 날짜의 복용 여부를 가져오면 된다.

                            if(weight_db.checkEmpty3(getdate)){ //선택된 날짜가 날짜 db에 있으면
                                showdate.setText("복용 O");
                                eatImaage.setImageResource(R.drawable.after_eat);

                            }
                            else{ //선택된 날짜가 날짜 db에 없으면
                                showdate.setText("복용 X");
                                eatImaage.setImageResource(R.drawable.before_eat);
                            }
//                            int check = weight_db.getEatData(getdate);
//                            if(check == 1){
//                                showdate.setText("복용 O");
//                                eatImaage.setImageResource(R.drawable.after_eat);
//                            }
//                            else{ //복용하지 않으셨습니다 띄우고 해당 날짜이니 버튼을 만든다
//                                showdate.setText("복용 X");
//                                eatImaage.setImageResource(R.drawable.before_eat);//어째서인지 여기서 이걸 출력을 한다
//                            }
                        }
                        else if(compare2<0){ //현재 날짜보다 선택된 날짜가 더 클때 즉 이건 미래임
                            eatImaage.setImageResource(R.drawable.tomorow);
                            showdate.setText("미래입니다");
                        }
                        else{ //처음 저장된 날보다 큰날짜를 택한 상태에서 오늘 현재 날짜를 택했을때
                            if(weight_db.checkEmpty3(getdate)){
                                showdate.setText("복용 O");
                                eatImaage.setImageResource(R.drawable.after_eat);
                            }
                            else{
                                eat.setVisibility(View.VISIBLE);
                                eatImaage.setImageResource(R.drawable.before_eat);
                            }
//                            int check = weight_db.getEatData(getdate);
//                            if(check == 1){
//                                showdate.setText("복용 O");
//                                eatImaage.setImageResource(R.drawable.after_eat);
//                            }
//                            else{ //복용하지 않으셨습니다 띄우고 해당 날짜이니 버튼을 만든다
//                                eat.setVisibility(View.VISIBLE);
//                                eatImaage.setImageResource(R.drawable.before_eat);
//                            }
                        }
                    }
                    else{ //선택된 날짜랑 처음 저장이 같으면 어찌됐건 저장되어있구 선택된것이니
                        eatImaage.setImageResource(R.drawable.after_eat);
                        showdate.setText("복용 O");
                    }
                }
                 //선택된 날짜가 db에 저장되어있는지 체크 하는 부분 복용했으면 복용했다고 출력

//                Calendar now = Calendar.getInstance();
                //do something
//                Date d = date.getTime();
//                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
//                String seldate = format.format(d);
//                showdate.setText(seldate);
            }
        });
/////////////////////////////////////////////블루투스//////////////////////////////////////////////////////////////////////////////////////////

        if (BluetoothAdapter == null) { //블루투스가 활성화되어 있지 않을때
            Toast.makeText(context, "Bluetooth is not available", Toast.LENGTH_LONG).show(); //이걸 토스트 메시지로 띄운다
        }

        if (!BluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
//            BluetoothAdapter.enable();
//            setupChat();
//            if(mChatService ==null){
//                setupChat();
//            }
        } else { //블루투스가 켜져있으면 들어간다.
//            if (mChatService == null) setupChat(); //함수 호출
            setupChat();
            mChatService.start();
            BluetoothDevice device = BluetoothAdapter.getRemoteDevice("98:D3:32:70:B7:0B"); //버튼 누르면 바로 hc06이랑 연결 시키는 부분
            mChatService.connect(device);
            int check = 1;
            while(mChatService.getState()!= BluetoothChatService.STATE_CONNECTED){
                Log.d(TAG, "onActivityResult: try to connect");
                Toast.makeText(context,"연결중..",Toast.LENGTH_SHORT).show();
                if(check == 10){
                    break;
                }
                check++;
            }
            sendMessage("1");
//            int cbt = mChatService.getState();
//            if( cbt == 3 ){
//                sendMessage("1");
//                btCheck.setImageResource(R.drawable.green_circle);
//            }
        }

//        if (mChatService != null) {
//
//            if (mChatService.getState() == BluetoothChatService.STATE_NONE) { //아무것도 받지 않았을때
//                mChatService.start();//시작하는 것
//                BluetoothDevice device = BluetoothAdapter.getRemoteDevice("98:D3:32:70:B7:0B"); //버튼 누르면 바로 hc06이랑 연결 시키는 부분
//                mChatService.connect(device);
//            }
//        }

        getleft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!BluetoothAdapter.isEnabled()){
                    Toast.makeText(context,"블루투스를 키지 않았습니다",Toast.LENGTH_SHORT).show();
                }
                else{
                    sendMessage("1");
                    if(finalWeight_int<=0){
                        showWeight.setText("다시한번 누르십쇼");
                    }
                    else{
                        int left = num - (full-finalWeight_int);
                        if(left>num){
                            showWeight.setText("다시한번 누르십쇼");
                        }
                        else if(left == 0){
                            weight_db.delete(name);
                            weight_db.delete2();
                            show.setVisibility(View.GONE);
                            eat.setVisibility(View.GONE);
                            fab.show();
                            noNut.setVisibility(View.VISIBLE);
                            no.setVisibility(View.VISIBLE);
                            noNut.setImageResource(R.drawable.empty);
                            no.setText("저장되어있는 영양제가 없습니다 새로 만드세요....");
                            fab.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Log.d(TAG, "onClick: Clicked fab , now move to nutsetup");
                                    NewNutFragment fragment = new NewNutFragment();
                                    FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                                    //프래그먼트 컨테이너에 있는 뷰를 이 프래그먼트로 대체한다
                                    //트랜섹션을 back stack에 더해서 사용자가 찾아갈수 있게 한다
                                    transaction.replace(R.id.fragment_container, fragment);
                                    transaction.addToBackStack(getString(R.string.nut_fragment));
                                    transaction.commit();
                                }
                            });
                        }
                        else{
                            showWeight.setText(left+"개");
                            weight_db.putLeft(left,name);
//                            double devide =(int)((double)left/(double)num*100.0);
                            int persent = (int)((double)left/(double)num*100.0);
                            if(persent>=70&& persent<101){
                                leftImage.setImageResource(R.drawable.yak1);
                            }
                            else if(persent<70&&persent>=30){
                                leftImage.setImageResource(R.drawable.yak2);
                            }
                            else if(persent<30 && persent>=0){
                                leftImage.setImageResource(R.drawable.yak3);
                            }
                        }
                    }
                }
            }
        });
        ImageView ivLink = (ImageView)view.findViewById(R.id.ivBt);
        ivLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!BluetoothAdapter.isEnabled()){
//                    Toast.makeText(context,"블루투스를 키지 않았습니다",Toast.LENGTH_SHORT).show();
                    Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
                }
                else {
                    BluetoothDevice device = BluetoothAdapter.getRemoteDevice("98:D3:32:70:B7:0B"); //버튼 누르면 바로 hc06이랑 연결 시키는 부분
                    mChatService.connect(device);
                    int check = 1;
                    while(mChatService.getState()!= BluetoothChatService.STATE_CONNECTED){
                        Log.d(TAG, "onActivityResult: try to connect");
                        Toast.makeText(context,"연결중..",Toast.LENGTH_SHORT).show();
                        if(check == 10){
                            break;
                        }
                        check++;
                    }
                    sendMessage("1");
//                    int cbt = mChatService.getState();
//                    if(cbt == 3){
//                        sendMessage("1");
//                        btCheck.setImageResource(R.drawable.green_circle);
//                    }
                }
            }
        });
/////////////////////////////////////영양제 db가 비어있으면 내용 가리고 아니면 내용 보여주기//////////////////////////////////////////////////////////////////////////////////////////////////
        //inflater는 xml에 씌여진 걸 세팅하는것이라고 볼수 있다 즉 그걸 프래그먼트에서 해당 레이아웃 파일을 이용하겠다는 뜻인거 같다
        viewContactsBar = (AppBarLayout) view.findViewById(R.id.viewContactsToolbar);

        ///////////////////////////////////////////////영양제 db가 비어 있지 않으면////////////////////////////////////////////////////////////////////////////////////////
        if (weight_db.checkEmpty()) {  //영양제 db가 비어 있지 않으면


            int checkEmpty = weight_db.giveLeft();

            if(checkEmpty == 0){
                weight_db.delete(name);
                weight_db.delete2();
                noNut.setImageResource(R.drawable.empty);
                show.setVisibility(View.GONE);
                eat.setVisibility(View.GONE);
                fab.show();
                no.setText("저장되어있는 영양제가 없습니다 새로 만드세요....");
                fab.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.d(TAG, "onClick: Clicked fab , now move to nutsetup");
                        NewNutFragment fragment = new NewNutFragment();
                        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                        //프래그먼트 컨테이너에 있는 뷰를 이 프래그먼트로 대체한다
                        //트랜섹션을 back stack에 더해서 사용자가 찾아갈수 있게 한다
                        transaction.replace(R.id.fragment_container, fragment);
                        transaction.addToBackStack(getString(R.string.nut_fragment));
                        transaction.commit();
                    }
                });
            }
            else{
                final Calendar checkNow = Calendar.getInstance(); //지금 현재 시간을 불러 온다
                Date checkDate = checkNow.getTime();
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                final String Nowdate = format.format(checkDate);//현재 날짜 스트링 형태로 변환

                show.setVisibility(View.VISIBLE); //캘린더를 보여준다
                TextView no = (TextView)view.findViewById(R.id.say_No);
                no.setVisibility(View.GONE);
                ImageView noNut = (ImageView)view.findViewById(R.id.noNut);
                noNut.setVisibility(View.GONE);
                fab.hide();
                //////////////////////////퍼센트에 따른 사진 출력//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//                int nam = num - ((full-finalWeight_int)/one);
//                int persent = (nam/num)*100;
//                if(persent>=70){
//                    leftImage.setImageResource(R.drawable.yak1);
//                }
//                else if(persent<70&&persent>30){
//                    leftImage.setImageResource(R.drawable.yak2);
//                }
//                else{
//                    leftImage.setImageResource(R.drawable.yak3);
//                }
                ///////////////////////////////////////복용했는지 안했는지 알려주는 부분//////////////////////////////////////////////////////////////////////////////////////////////////////////////
                if(!weight_db.checkEmpty2()){ //날짜 db가 비어 있으면 접근함. 즉 영양제를 사고 처음 먹었는지 안먹었는지 확인 하는 부분
                    eat.setVisibility(View.VISIBLE); //아예 처음 저장 하는것이니까 버튼 만듬
                    eat.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if(!BluetoothAdapter.isEnabled()){
                                Toast.makeText(context,"블루투스를 키지 않았습니다",Toast.LENGTH_SHORT).show();
                            }
                            else{
                                sendMessage("1");//버튼 클릭하면 지금 현재 날짜랑 먹은 표시를 저장
                                int eat = finalWeight_int;
//                            int ate = Math.round(full - finalWeight);
                                if(eat<=0 || eat>full){
                                    showdate.setText("다시 한번 누르십쇼");
                                }
                                else{
                                    if(full - eat > 0){ //처음 영양제를 복용한것
                                        weight_db.setEat(eat, Nowdate);
                                        int howmany = full - eat;
                                        howmany/=one;
                                        eatImaage.setImageResource(R.drawable.after_eat);
                                        showdate.setText(howmany+"정 복용"); //있으면 복용 출력하고
                                        soundpool.play(sound1,1,1,1,0,1);//숫자가 처음게 왼쪽 음량  , 오른쪽 음량 , 재생순서 , 반복수 , 재생속도이다
//                                    Button button = (Button) v;
//                                    button.setVisibility(View.GONE);
                                    }
                                    else{ //영양제를 복용하지 않은것
                                        showdate.setText("복용 X");
                                        eatImaage.setImageResource(R.drawable.before_eat);
                                        soundpool.play(sound2,1,1,1,0,1);
                                    }
                                }
                            }


//                            weight_db.setEat(eat, Nowdate);
//                            if (weight_db.checkEmpty3(Nowdate)) { //저장하고 나서 db에 오늘 날짜가 있는지 확인
//                                eatImaage.setImageResource(R.drawable.after_eat);
//                                showdate.setText("복용 O"); //있으면 복용 출력하고
//                            }
//                            Button button = (Button) v;
//                            button.setVisibility(View.GONE);
                        }
                    });
                }
                else{ //날짜 db가 비어 있지 않으면 들어간다
                    if(weight_db.checkEmpty3(Nowdate)){ //db가 비어 있지 않고 오늘 날짜가 db에 있으면 들어간다.
                        eatImaage.setImageResource(R.drawable.after_eat);
                        showdate.setText("복용 O"); //복용했다고 출력을 하고
                        eat.setVisibility(View.GONE); //버튼을 감춘다
                    }
                    else{ //db가 비어있지 않고 오늘 날짜가 db에 없으면 접근
                        eat.setVisibility(View.VISIBLE); //버튼 생성하고
                        eat.setOnClickListener(new View.OnClickListener() { //복용 버튼 클릭시에 지금 날씨랑 가장 최근 저장 날짜랑 비교
                            @Override
                            public void onClick(View v) {
                                if(!BluetoothAdapter.isEnabled()){
                                    Toast.makeText(context,"블루투스를 키지 않았습니다",Toast.LENGTH_SHORT).show();
                                }
                                else{
                                    sendMessage("1");
                                    int eat = finalWeight_int;
                                    int ate = Math.round(full - finalWeight);

                                    if(eat<=0 || eat>full){
                                        showdate.setText("다시 한번 누르십쇼");
                                    }
                                    else{
                                        int last_save = weight_db.saveLastSave();
//                                    int lastate = last_save - ate;
                                        if(last_save-eat>0){
                                            weight_db.setEat(eat, Nowdate);

                                            int howmany = last_save - eat;
                                            howmany/=one;
                                            eatImaage.setImageResource(R.drawable.after_eat);
                                            showdate.setText(howmany+"정 복용 ");
                                            soundpool.play(sound1,1,1,1,0,1);
//                                        Button button = (Button) v;
//                                        button.setVisibility(View.GONE);
                                        }
                                        else{
                                            showdate.setText("복용 X");
                                            eatImaage.setImageResource(R.drawable.before_eat);
                                            soundpool.play(sound2,1,1,1,0,1);
                                        }
                                    }
                                }

//                                int last_save = weight_db.saveLastSave();
//                                if(last_save-eat>0){
//                                    weight_db.setEat(eat, Nowdate);
//                                    showdate.setText("복용 O");
//                                    eatImaage.setImageResource(R.drawable.after_eat);
//                                    Button button = (Button) v;
//                                    button.setVisibility(View.GONE);
//                                }
//                                else{
//                                    showdate.setText("복용 X");
//                                    eatImaage.setImageResource(R.drawable.before_eat);
//                                }
                                //
//                                Calendar past = Calendar.getInstance();
//                                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
//                                try {
//                                    pasteat = weight_db.saveLastSave();//가장 최근 저장 날짜 가져와서 넣는 부분
//                                    Date dd = sdf.parse(pasteat);
//                                    past.setTime(dd);
//                                    getDif(past, checkNow); //날짜 비교 해서 오늘 거랑 이전에 먹지 않은 날을 db에 저장 하는부분
//                                    showdate.setText("복용 O");
//                                    eatImaage.setImageResource(R.drawable.after_eat);//다 저장 하고 지금 현재 날짜는 복용했다고 버튼눌렀으니 현재 날짜 복용했다고 출력
//                                } catch (ParseException e) {
//                                    e.printStackTrace();
//                                }
//                                eatImaage.setImageResource(R.drawable.after_eat);
//                                showdate.setText("복용 O");
//                                Button button = (Button) v;
//                                button.setVisibility(View.GONE);
                            }
                        });
                    }
                }
            }

        }
        /////////////////영양제 db비어 있으면/////////////////////////////////////////////////
        else { //영양제 db가 비어 있으면 들어간다
            show.setVisibility(View.GONE);
            eat.setVisibility(View.GONE);
            fab.show();
            noNut.setImageResource(R.drawable.empty);
            no.setText("저장되어있는 영양제가 없습니다 새로 만드세요....");
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "onClick: Clicked fab , now move to nutsetup");
                    NewNutFragment fragment = new NewNutFragment();
                    FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                    //프래그먼트 컨테이너에 있는 뷰를 이 프래그먼트로 대체한다
                    //트랜섹션을 back stack에 더해서 사용자가 찾아갈수 있게 한다
                    transaction.replace(R.id.fragment_container, fragment);
                    transaction.addToBackStack(getString(R.string.nut_fragment));
                    transaction.commit();
                }
            });
        }

        ImageView ivsetup = (ImageView) view.findViewById(R.id.ivSetUp);
        ivsetup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!weight_db.checkEmpty()) {
                    Toast.makeText(context, "저장되어있는 영양제가 없습니다 새로 만드세요....", Toast.LENGTH_SHORT).show();
                } else {
                    NutFragment fragment = new NutFragment();
                    FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                    //프래그먼트 컨테이너에 있는 뷰를 이 프래그먼트로 대체한다
                    // 트랜섹션을 back stack에 더해서 사용자가 찾아갈수 있게 한다
                    transaction.replace(R.id.fragment_container, fragment);
                    transaction.addToBackStack(getString(R.string.view_contanct_fragment));
                    transaction.commit();
                }
            }
        });
        //플로팅 버튼을 누르면 영양제 추가 화면으로 넘어가게 하는 부분
        return view;
    }


    //////////////////////////////////////////블루투스/////////////////////////////////////////////////////////////////////////////////////////////
    private void setupChat() {
        Log.d(TAG, "setupChat()");

        mChatService = new BluetoothChatService(this, mHandler); //이걸 새로 만든다 context는 이 여기고 핸들러는 mhandler를 보낸다

        mOutStringBuffer = new StringBuffer("");
    }

    public void sendMessage(String message) {

        if (mChatService.getState() != BluetoothChatService.STATE_CONNECTED) {
            Log.d("TEST", "Now Bluetooth State is --> " + mChatService.getState());
            Toast.makeText(context, R.string.not_connected, Toast.LENGTH_SHORT).show();
            return;
        }

        else if (message.length() > 0) {

            byte[] send = message.getBytes();
            mChatService.write(send);

            mOutStringBuffer.setLength(0);
        }
    }

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_STATE_CHANGE:
                    if (D) Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
                    switch (msg.arg1) { //이걸 받았을때 해당하는 메시지를 받음
                        case BluetoothChatService.STATE_CONNECTED:
                            Toast.makeText(context, R.string.title_connected_to, Toast.LENGTH_SHORT).show();
                            break;
                        case BluetoothChatService.STATE_CONNECTING:
                            Toast.makeText(context, R.string.title_connecting, Toast.LENGTH_SHORT).show();
                            break;
                        case BluetoothChatService.STATE_LISTEN:
                        case BluetoothChatService.STATE_NONE:
                            Toast.makeText(context, R.string.title_not_connected, Toast.LENGTH_SHORT).show();
                            break;
                    }
                    break;
                case MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;

                    String writeMessage = new String(writeBuf);

                    break;
                case MESSAGE_READ:
                    String str;
                    byte[] readBuf = (byte[]) msg.obj;
                    String readMessage = new String(readBuf, 0, msg.arg1);
                    stringBuffer.append(readMessage); //스트링 버퍼에 readmessage를 저장한다
                    int endOfLine = stringBuffer.indexOf("#");// #으로 끝나는 부분의 위치를 저장
                    if (endOfLine > 0) { //끝나는 라인이 0보다 크면 진입
                        String sbprint = stringBuffer.substring(0, endOfLine);//sbprint변수에 0부터 끝나는 지점까지의 부분만 저장
                        stringBuffer.delete(0, stringBuffer.length()); //String buffer 지움
                        finalWeight = Float.parseFloat(sbprint); //위에서 받은 sbprint를 float형식으로 변환
                        if (finalWeight < 300) {
                            showWeight.setText("다시한번 눌러주십쇼");
                        }
                        if (finalWeight >= 300) {
                            finalWeight_int = Math.round(finalWeight);
//                            showWeight.setText("문자열 형태 : " + sbprint + "\n정수형 형태 : " + finalWeight);
                        } //
                    }

                    break;
//                case MESSAGE_DEVICE_NAME:
//
//                    mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
//                    Toast.makeText(context, "Connected to "
//                            + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
//                    break;
//                case MESSAGE_TOAST:
//                    Toast.makeText(context, msg.getData().getString(TOAST),
//                            Toast.LENGTH_SHORT).show();
//                    break;
            }
        }
    };

    public void onActivityResult(int requestCode, int resultCode, Intent data) { //서브 액티비티로부터 받은 결과값을 가지고 처리하는 부분
        if (D) Log.d(TAG, "onActivityResult " + resultCode);
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE: //이것일때 즉 devicelist에서 device 선택했을때 들어가서 연결시켜주는 부분
                // 연결할 장치와 함께 DeviceListActivity가 반환
                if (resultCode == Activity.RESULT_OK) { //DeviveList자바 클래스에서 받은 resultcode가 RESULT_OK면
                    BluetoothDevice device = BluetoothAdapter.getRemoteDevice("98:D3:32:70:B7:0B"); //버튼 누르면 바로 hc06이랑 연결 시키는 부분
                    mChatService.connect(device);
                }
                break;
            case REQUEST_ENABLE_BT: //블루투스의 현상황에 대해서 알려주는 부분
                // 블루투스 사용 가능 요청이 반환되면 실행
                if (resultCode == Activity.RESULT_OK) {
                    // 블루투스가 이제 사용 가능하므로 채팅 세션 설정
                    setupChat();
                    mChatService.start();//시작하는 것
                    BluetoothDevice device = BluetoothAdapter.getRemoteDevice("98:D3:32:70:B7:0B"); //버튼 누르면 바로 hc06이랑 연결 시키는 부분
                    mChatService.connect(device);
                    int check = 1;
                    while(mChatService.getState()!= BluetoothChatService.STATE_CONNECTED){
                        Log.d(TAG, "onActivityResult: try to connect");
                        Toast.makeText(context,"연결중..",Toast.LENGTH_SHORT).show();
                        if(check == 10){
                            break;
                        }
                        check++;
                    }
                    sendMessage("1");
//                    int cbt = mChatService.getState();
//                    if(cbt == 3){
//                        sendMessage("1");
//                        btCheck.setImageResource(R.drawable.green_circle);
//                    }
                } else {
                    // 사용자가 블루투스를 활성화하지 않았거나 오류가 발생
                    Log.d(TAG, "BT01 not enabled");
                    Toast.makeText(context, R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
                }
        }
    }

    //////////////////////////////////////////////////달력 계산/////////////////////////////////////////////////////////////////////////////////////

///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


    //다른 프래그먼트 갓다가 다시 돌아왔을때 이 함수 실행
    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mChatService != null) {
            mChatService.stop();
        }
//        if (BluetoothAdapter.isEnabled()) { //앱꺼지면 블루투스 끔
//            Log.d(TAG, "enableDisableBT: disabling BT.");
//            BluetoothAdapter.disable();
//        }
    }
}
