package wook.inc.please;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.NumberPicker;
import android.widget.TextView;

public class NumberPickerDialog extends DialogFragment {

    private static final String TAG = "NumberPickerDialog";

    public interface OnInputSelected{
        void sendInput(String input);
    }
    public OnInputSelected mOnInputSelected;

    private NumberPicker numberPicker;
    private TextView ok , cancel;
    private NumberPicker.OnValueChangeListener valueChangeListener;
    int minValue = 10;
    int step = 10;
    int maxValue = 300;
    String [] myValue;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_numberpicker, container, false);
        ok = (TextView)view.findViewById(R.id.action_ok);
        cancel = (TextView)view.findViewById(R.id.action_cancel);
        numberPicker = (NumberPicker)view.findViewById(R.id.num_pick);


        myValue = getArrayWithSteps(minValue,maxValue,step);
        int maxlength = myValue.length-1;
        myValue = getArrayWithSteps(minValue,maxValue,step);

        numberPicker.setMinValue(0); //myvalue에서 시작하는 시작 범위
        numberPicker.setMaxValue(maxlength); //myvalue길이만큼까지 numberpicker를 만든다
        numberPicker.setDisplayedValues(myValue);
        numberPicker.setValue(0);

        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int pickedValue = numberPicker.getValue();


                mOnInputSelected.sendInput(String.valueOf(pickedValue));

                getDialog().dismiss();
            }
        });


        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDialog().dismiss();
            }
        });
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try{
            mOnInputSelected = (OnInputSelected) getTargetFragment();
        }catch (ClassCastException e){
            Log.e(TAG, "onAttach: ClassCastException : " + e.getMessage() );
        }
    }

    public  String[] getArrayWithSteps(int min, int max, int step) { //numberPicker의 값 사이의 범위를 정해주는 배열을 만드는 부분

        int number_of_array = max/step;

        String[] result = new String[number_of_array];

        for (int i = 0; i < number_of_array; i++) {
            result[i] = String.valueOf(min  * (i+1));
        }
        return result;
    }

}
