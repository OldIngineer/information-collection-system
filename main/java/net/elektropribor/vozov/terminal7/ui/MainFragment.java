package net.elektropribor.vozov.terminal7.ui;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import net.elektropribor.vozov.terminal7.MainActivity;
import net.elektropribor.vozov.terminal7.R;

public class MainFragment extends Fragment {
    private int click_button;

    // Вызывается при первом создании класса
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);//вызов конструктора суперкласса
        click_button = mListener.scanBLE();//возврат значения признака сканирования из активности
    }
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
       View v = inflater.inflate(R.layout.fragment_main, container, false);
        // кнопка подключения к BLE
        final Button but1 = v.findViewById(R.id.button1);
        if (click_button==0){
            but1.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
            }else{
            but1.setBackgroundColor(getResources().getColor(R.color.colorAccent));
        }
        //создание класса-слушателя на событие нажатия кнопки
        but1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               if (click_button==0){
                   click_button = 1;
                //инициализация Bluetooth adapter.
                 mListener.initBluetoothAdapter();
                 //смена цвета кнопки
                 but1.setBackgroundColor(getResources().getColor(R.color.colorAccent));
                } else {
                   click_button = 0;
                   //смена цвета кнопки
                 but1.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                 //прекращение сканирования
                 mListener.stopScanBLE();
               }
            }
            });
        //привязка шаблона отображения строки наименования компании
        TextView nameFirm = v.findViewById(R.id.textView2);
        //запись
        nameFirm.setText(MainActivity.mNameFirm);
        //привязка шаблона отображения строки наименования пользователя
        TextView nameUser = v.findViewById(R.id.textView3);
        //запись
        nameUser.setText(MainActivity.mNameUser);
       return v;
    }

    //====реализовать этот интерфейс, чтобы обрабатывать в активности события===
    public interface CallBackActivity {
       //инициализация Bluetooth adapter.
        void initBluetoothAdapter();
        //остановить сканирование устройств BLE
        void stopScanBLE();
        //получение состояния "поиск BLE"
        int scanBLE();
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
    //========================================================================

}
