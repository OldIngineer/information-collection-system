package net.elektropribor.vozov.terminal7.ui;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import net.elektropribor.vozov.terminal7.MainActivity;
import net.elektropribor.vozov.terminal7.R;
//класс окна-диалога для ввода идентификационных данных пользователя
public class EnterIdentificationDialogFragment extends DialogFragment {
    private String sNameFirm;
    private String sNameUser;

    @Override
    public void onCreate (Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
           ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.dialog_identification_data, container,
                false);
        //получение ссылок на виджет
        //объявление экземпляра ввода названия фирмы
        EditText nameFirm = v.findViewById(R.id.name_company);
        //объявление экземпляра ввода названия пользователя
        EditText nameUser = v.findViewById(R.id.user);
        Button button_positive = v.findViewById(R.id.botton_positive);
        Button button_negative = v.findViewById(R.id.button_negative);
        //назначение слушателя ввода текста
        nameFirm.addTextChangedListener(new TextWatcher() {
        //метод заносит строку "с" в конец имени
        public void onTextChanged(CharSequence c, int start, int before, int count) {
           sNameFirm = c.toString();
        }
            public void beforeTextChanged(
                    CharSequence c, int start, int count, int after) {
                // Здесь намеренно оставлено пустое место
            }
            public void afterTextChanged(Editable c) {
                // И здесь тоже
            }
        });
        //назначение слушателя ввода текста
        nameUser.addTextChangedListener(new TextWatcher() {
            //метод заносит строку "с" в конец имени
            public void onTextChanged(CharSequence c, int start, int before, int count) {
                sNameUser = c.toString();
            }
            public void beforeTextChanged(
                    CharSequence c, int start, int count, int after) {
                // Здесь намеренно оставлено пустое место
            }
            public void afterTextChanged(Editable c) {
                // И здесь тоже
            }
        });
        // Следим за нажатием кнопки
        button_positive.setOnClickListener(new View.OnClickListener(){
        public void onClick(View v) {
            // Когда кнопка нажата, вызывается действие владельца
            MainActivity.mNameFirm = sNameFirm;
            MainActivity.mNameUser = sNameUser;
            mListener.replaceMainFragment();
        }
        });
        button_negative.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                // Когда кнопка нажата, вызывается действие владельца
                mListener.replaceMainFragment();
            }
        });
        return v;
    }
    //====реализовать этот интерфейс, чтобы обрабатывать в активности события===
    public interface CallBackActivity {
        //запись идентификационных данных в активности
        void replaceMainFragment();
     }
    // объявить экземпляр интерфейса
    EnterIdentificationDialogFragment.CallBackActivity mListener;
    // Переопределить метод Fragment.onAttach() для создания экземпляра интерфейса
    @Override
    public void onAttach(@NonNull Activity activity) {
        super.onAttach(activity);
        //Убедитесь, что хост активности реализует интерфейс обратного вызова
        try {
            //Создать экземпляр интерфейса, чтобы мы могли отправить события хосту
            mListener = (EnterIdentificationDialogFragment.CallBackActivity) activity;
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
