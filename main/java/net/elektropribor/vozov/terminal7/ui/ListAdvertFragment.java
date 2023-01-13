package net.elektropribor.vozov.terminal7.ui;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;
import androidx.fragment.app.ListFragment;
import androidx.annotation.NonNull;
import net.elektropribor.vozov.terminal7.R;
import net.elektropribor.vozov.terminal7.db.Advert;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ListAdvertFragment extends ListFragment {

    // Вызывается при первом создании класса
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);//вызов конструктора суперкласса
        newArrayListAdvert();//формирование списка сообщений
    }

    //====реализовать этот интерфейс, чтобы обрабатывать в активности события===
    public interface CallBackActivity {
        //метод получения списка объектов Advert из базы данных
        List<Advert> getAdverts();
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

    //========================================================================
    //создание внутреннего класса: отображения списка полученных сообщений
    private class AdvertAdapter extends ArrayAdapter<Advert> {
        public AdvertAdapter(ArrayList<Advert> adverts) {
            super(Objects.requireNonNull(getActivity()), 0, adverts);
        }
        //Метод должен возвращать
        //представление, полученное заполнением пользовательского макета и содержащее
        //правильные данные
        // @Override
        @NonNull
        public View getView(int position, View convertView,
                            @NonNull ViewGroup parent) {
           // Если мы не получили представление, заполняем его
            if (convertView == null) {
                //convertView = Objects.requireNonNull(getActivity()).getLayoutInflater()
                //        .inflate(R.layout.fragment_list_advert,parent);
                convertView = getActivity().getLayoutInflater()
                        .inflate(R.layout.fragment_list_advert, null);
            }
// Настройка представления для объекта Advert
            Advert c = getItem(position);
            TextView titleTextView =
                    convertView.findViewById(R.id.title);
            assert c != null;
            String text = c.getMod()+" s/n"+ c.getSn();
            titleTextView.setText(text);
            TextView messageTextView =
                    convertView.findViewById(R.id.message);
            messageTextView.setText(c.getKod());
            TextView dateTextView =
                    convertView.findViewById(R.id.date);
            dateTextView.setText(c.getDate().toString());
            CheckBox solvedCheckBox =
                    convertView.findViewById(R.id.sign_sent);
            solvedCheckBox.setChecked(c.isSent());
            return convertView;
        }
    }
    //метод: обновление списка сообщений
    public void newArrayListAdvert(){
        //обращение к базе данных: получение списка сообщений
        ArrayList<Advert> mAdvert = (ArrayList<Advert>) mListener.getAdverts();
        //создание Adapter для отображения списка
        AdvertAdapter adapter = new AdvertAdapter(mAdvert);
        setListAdapter(adapter);
    }
//====методы жизненного цикла фрагмента - совпадают с активностью========
//переопределение метода для обновления списка сообщений перед тем
// как он станет видимым
    @Override
    public void onResume() {
    super.onResume();
   // newArrayListAdvert();
}
    // переопределить метод Fragment.onDetach() для удаления экземпляра интерфейса
    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }
}
