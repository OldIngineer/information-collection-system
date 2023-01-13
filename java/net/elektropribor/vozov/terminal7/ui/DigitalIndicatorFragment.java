package net.elektropribor.vozov.terminal7.ui;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import net.elektropribor.vozov.terminal7.R;

public class DigitalIndicatorFragment extends Fragment {
    // Вызывается при первом создании класса
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);//вызов конструктора суперкласса
    }
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_digital_indicator, container, false);
        TextView typeDevice = v.findViewById(R.id.typeDevice);
        typeDevice.setText(mListener.getIndTypeDevice());
        TextView textValue = v.findViewById(R.id.textValue);
        textValue.setText(mListener.getIndValue());
        TextView vC = v.findViewById(R.id.vC);
        vC.setText(mListener.getIndVC());
        return v;
    }
    //====реализовать этот интерфейс, чтобы обрабатывать в активности события===
    public interface CallBackActivity {
        //методы получения информации для индикации
        String getIndTypeDevice();
        String getIndValue();
        String getIndVC();
    }
    // объявить экземпляр интерфейса
    CallBackActivity mListener;
    // Переопределить метод Fragment.onAttach() для создания экземпляра интерфейса
    @Override
    public void onAttach(@NonNull Activity activity) {
        super.onAttach(activity);
        //Убедитесь, что хост активности реализует интерфейс обратного вызова
        try {
            //Создать экземпляр интерфейса, чтобы мы могли отправить события хосту
            mListener = (CallBackActivity) activity;
        } catch (ClassCastException e) {
            //активность не реализует интерфейс, исключение
            throw new ClassCastException(activity.toString()
                    + " must implement CallBackActivity");
        }
    }
    // переопределить метод Fragment.onDetach() для удаления экземпляра интерфейса
    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }
    //==========================================================================
}
