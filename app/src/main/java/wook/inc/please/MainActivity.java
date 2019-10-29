package wook.inc.please;


import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity{

    private static final String TAG = "MainActivity";

    BluetoothAdapter bluetoothAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        bluetoothAdapter =bluetoothAdapter.getDefaultAdapter();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG, "onCreate: Started");
        init();
    }
    //첫번째 프래그먼트를 실행시킨다
    private void init(){
        ViewContactFragment fragment = new ViewContactFragment();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        //프래그먼트 컨테이너에 있는 뷰를 이 프래그먼트로 대체한다
        //트랜섹션을 back stack에 더해서 사용자가 찾아갈수 있게 한다
        transaction.replace(R.id.fragment_container,fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    public void onBackPressed() {

        int fragments = getSupportFragmentManager().getBackStackEntryCount();
        if(fragments == 1){
            Log.d(TAG, "onBackPressed: this is last fragment");
            AlertDialog.Builder alert_Exit = new AlertDialog.Builder(this);
            alert_Exit.setTitle("영양제 알림이");
            alert_Exit.setMessage("어플을 종료하시겠습니까?");

            alert_Exit.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    bluetoothAdapter.disable();
//                    ViewContactFragment viewContactFragment = new ViewContactFragment();
//                    viewContactFragment.mChatService.stop();
                    finish();
                }
            });
            alert_Exit.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    init();
                }
            });
            AlertDialog alert = alert_Exit.create();
            alert.show();
        }
        super.onBackPressed();
    }




    //    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        finish();
//    }

}
