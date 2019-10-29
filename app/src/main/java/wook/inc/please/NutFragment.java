package wook.inc.please;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import wook.inc.please.Database.Weight_DB;

public class NutFragment extends Fragment {
/////////////////////////////////////////블루투스를 사용하기 위해 필요한 변수들////////////////////////////////////////////////
//    public static float finalWeight;
//    private static final boolean D = true;
//    boolean th_ctl = false;
//
//    public static final int MESSAGE_STATE_CHANGE = 1;
//    public static final int MESSAGE_READ = 2;
//    public static final int MESSAGE_WRITE = 3;
//    public static final int MESSAGE_DEVICE_NAME = 4;
//    public static final int MESSAGE_TOAST = 5;
//
//
//    public static final String DEVICE_NAME = "device_name";
//    public static final String TOAST = "toast";
//
//
//    private static final int REQUEST_CONNECT_DEVICE = 1;
//    private static final int REQUEST_ENABLE_BT = 2;
//
//
//    public BluetoothAdapter BluetoothAdapter;
//    public static BluetoothChatService mChatService = null;
//
//    private StringBuffer n_stringBuffer = new StringBuffer();
//    private StringBuffer stringBuffer = new StringBuffer();
//    private StringBuffer mOutStringBuffer;

    /////////////////////////////////////////////////////////////////////////////////////////
    private Context context;

    private static final String TAG = "NutFragment";

//    //메인 액티비티로부터 받을때 nullpoint exception을 피하기 위해
//    public NutFragment(){
//        super();
//        setArguments(new Bundle());
//    }

    private String mConnectedDeviceName = null;

    private Toolbar toolbar;

    /////////////////////////////////데이터 베이스에 있는 정보를 받아오기 위한 변수들//////////////////////////////////////////////////////////
    public String nutName="";
    public int full=0;
    public int one=0;
    public int num=0;
//    public int hour=0;
//    public int minute=0;
    public int left=0;

    Weight_DB weight_db;
    TextView showWeight;
    TextView name;
    TextView fullweight;
    TextView oneweight;
    TextView Num;
//    TextView time;
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_contact , container , false);



        context = container.getContext();
        weight_db = new Weight_DB(context);
        toolbar = (Toolbar)view.findViewById(R.id.nutToolbar);
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        weight_db.putData(this);
//        BluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        ////////////////////////////////////////////////변수 생성////////////////////////////////////////////////////////////////
        Typeface typefaceAppgoollim = Typeface.createFromAsset(context.getAssets(),"fonts/appgoollimL.ttf");
        name = (TextView)view.findViewById(R.id.tvName);
        name.setTypeface(typefaceAppgoollim);
        name.setText(nutName);
        showWeight = (TextView)view.findViewById(R.id.real_left);
        showWeight.setText(left+"개");
        fullweight = (TextView)view.findViewById(R.id.full_Weight);
        fullweight.setText(full+"g");
        oneweight = (TextView)view.findViewById(R.id.one_Weight);
        oneweight.setText(one+"g");
        Num = (TextView)view.findViewById(R.id.real_num);
        Num.setText(num+"개");
//        time = (TextView)view.findViewById(R.id.real_time);
//        time.setText(hour+"시 "+minute+"분");
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        Log.d(TAG, "onCreateView: started");
        //toolbar를 만들어주는 부분
        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);
        setHasOptionsMenu(true);

        //뒤로가는 화살표 누르면 이전화면 보여주는것
        ImageView ivBackArrow = (ImageView)view.findViewById(R.id.ivBackArrow);
        ivBackArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: clicked backArrow");
                //이전 fragment로 돌아가게 하는것
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });


/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        //edit 이미지를 눌렀을때의 이벤트 처리
        ImageView ivEdit = (ImageView)view.findViewById(R.id.ivEdit);
        ivEdit.setOnClickListener(new View.OnClickListener() {
            @Override //edtiteNut프래그먼트로 이동시켜주는 부분
            public void onClick(View v) {
                Log.d(TAG, "onClick: clicked Edit");
                EditNutFragment fragment = new EditNutFragment();
                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                //프래그먼트 컨테이너에 있는 뷰를 이 프래그먼트로 대체한다
                //트랜섹션을 back stack에 더해서 사용자가 찾아갈수 있게 한다
                transaction.replace(R.id.fragment_container,fragment);
                transaction.addToBackStack(getString(R.string.view_contanct_fragment));
                transaction.commit();
            }
        });
        //재고확인 버튼을 누르면 재고를 확인 해주는 부분


        return view;
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.nut_menu,menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.menuitem_delete :
                Log.d(TAG, "onOptionsItemSelected: deleting nut");
                weight_db.delete(nutName);
                weight_db.delete2();
                getActivity().getSupportFragmentManager().popBackStack();
        }
        return super.onOptionsItemSelected(item);
    }
}