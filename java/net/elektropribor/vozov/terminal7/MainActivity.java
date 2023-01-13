package net.elektropribor.vozov.terminal7;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.tabs.TabLayout;

import net.elektropribor.vozov.terminal7.db.Advert;
import net.elektropribor.vozov.terminal7.db.App;
import net.elektropribor.vozov.terminal7.db.AppDatabase;
import net.elektropribor.vozov.terminal7.ui.DigitalIndicatorFragment;
import net.elektropribor.vozov.terminal7.ui.EnterIdentificationDialogFragment;
import net.elektropribor.vozov.terminal7.ui.ListAdvertFragment;
import net.elektropribor.vozov.terminal7.ui.MainFragment;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

//AppCompatActivity - Базовый класс для Activity, которые хотят использовать
// некоторые новые функции платформы на старых устройствах Android
public class MainActivity extends AppCompatActivity
        implements
        //реализуется обратный интерфейс с классом MainFragment
         MainFragment.CallBackActivity,
        //реализуется обратный интерфейс с классом ListAdvertFragment
        ListAdvertFragment.CallBackActivity,
        //реализуется обратный интерфейс с классом DigitalIndicatorFragment
        DigitalIndicatorFragment.CallBackActivity,
        //реализуется обратный интерфейс с классом EnterIdentificationDialogFragment
        EnterIdentificationDialogFragment.CallBackActivity
{
    private static final long RETURN_SKAN = 29*60*1000;//повторный запуск сканирования
    private TextView mainText;
    private BluetoothAdapter mBluetoothAdapter;//ваш локальный адаптер BLE
    private BluetoothLeScanner mLEScanner;
    private static final int REQUEST_ENABLE_BT = 1;
    private static final String comp = "EP";//абвеатура компании
    private FormAdvert mFormAdvert;
    private String deviceMessage;//расшифровонное сообщение от устройства
    private final AudioPlayer mPlayer = new AudioPlayer();
    private int numberItemMenu = 1;//номер подпункта меню
    private FragmentManager fm;
    private int numberTabSelect = 0;//номер выбранной вкладки
    private int scanOn = 0;//признак(=1)сканирования устройств BLE
    private Handler mHandler;
    private Runnable runnable;
    private static final long PERIOD_IND = 7000;//период инд. в мс.
    private static final long PERIOD_REPEAT = 1000;//период повтора сообщений в мс.
    private String oldName = null;//для запоминания полученного имени
    private String messageBuffer;//буфер для хранения сообщений
    //---Telegram----------------------------
    //bot:@Elektropribor_bot
    private static final String token = "bot";//здесь должен быть токен бота изготовителя
    //канал:DeviceElektropribor
    private static final String id = "nk";
    //---------------------------------------
    //---для цифровых устройств:-------------
    private String indTypeDevice;//индицируемый тип устройства
    private String indValue;//индицируемая величина
    private String indVC;//индицируемая размерность в СИ
    //-----------------------------------------------------
    //ОБЪЯВЛЕНИЕ СИСТЕМНЫХ ПЕРЕМЕННЫХ
    static public String mNameFirm = "NAME COMPANY";
    static public String mNameUser = "NAME USER";
    private DialogFragment fragmentDialog;
    //указание места расположения файла профиля
    String path = "Profile";
    //признак первого включения программы
    boolean FistBegin = false;

    @Override
    //Вызывается когда Activity стартует
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);//вызов конструктора super класса
        mHandler = new Handler();//создать поток
        ReadFile();//чтение параметров из файла профиля
        //Установить отображение содержания на основе макета ресурсов.
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //привязка шаблона отображения к объекту
        TabLayout tabLayout = findViewById(R.id.tabLayout);
        //создание вкладок
        tabLayout.addTab(tabLayout.newTab().setText(R.string.tab_text_1));
        tabLayout.addTab(tabLayout.newTab().setText(R.string.tab_text_2));
        //создать FragmentManager для управления фрагментами
        fm = this.getSupportFragmentManager();
        //transaction = fm.beginTransaction();
        //по умолчанию добавляется главный фрагмент
        MainFragment fragment = new MainFragment();
        fm.beginTransaction()
                .replace(R.id.container1, fragment)
                .commit();
        //создание класса-слушателя на событие выбора вкладки
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
            String nameTab = (String)tab.getText();
                assert nameTab != null;
                if (nameTab.equals(getString(R.string.tab_text_1))){
                MainFragment fragment = new MainFragment();
                fm.beginTransaction()
                        .replace(R.id.container1, fragment)
                        .commit();
                numberTabSelect = 0;
                }
            if (nameTab.equals(getString(R.string.tab_text_2))){
                ListAdvertFragment fragment = new ListAdvertFragment();
                fm.beginTransaction()
                        .replace(R.id.container1, fragment)
                        .commit();
                numberTabSelect = 1;
            }

            }
            //происходит после закрытия выбранной вкладки
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }
            //происходит при повторном выборе вкладки
            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        //привязка шаблона отображения строки состояния
         mainText = findViewById(R.id.text_view1);
        //проверьте, поддерживается ли на этом устройстве Bluetooth Low Energy
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported,
                    Toast.LENGTH_LONG).show();
            finish();
        }
        statusUpdate(getString(R.string.ble_supported));
    }

    //вывод текста в строку состояния
    private void statusUpdate (final String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mainText.setText(msg);
                }
        });
    }
    //============методы обратного вызова интерфейса класса MainFragment===========
    //класс получает ссылку на эту деятельность через Fragment.onAttach (),
    //обратный вызов, который он использует для вызова следующих методов

    //инициализация Bluetooth adapter.
    @Override
    public void initBluetoothAdapter(){
        final BluetoothManager mBluetoothManager = (BluetoothManager)
                getSystemService(Context.BLUETOOTH_SERVICE);
        //mBluetoothAdapter = Objects.requireNonNull(mBluetoothManager).getAdapter();
        mBluetoothAdapter = mBluetoothManager.getAdapter();
        //если Bluetooth не доступен на устройстве
        if (mBluetoothAdapter == null) {
            Toast.makeText(MainActivity.this,
                    R.string.ble_not_supported,
                    Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        //Откройте Настройки, если Bluetooth не выбран
        if (!mBluetoothAdapter.isEnabled()) {
            //создание объекта Intent, для обращения к ОС
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            //старт активности до результата
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth disabled", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        if (mBluetoothAdapter.isEnabled()) {
            mLEScanner = BluetoothAdapter.getDefaultAdapter().getBluetoothLeScanner();
            // mLEScanner = mBluetoothAdapter.getBluetoothLeScanner();
            //сброс текста в строке состояния
            statusUpdate ("");
            scanLeDevice(true);//вызов метода сканирования устройств
        }
    }
    //остановить сканирование устройств BLE
    @Override
    public void stopScanBLE(){
        scanLeDevice(false);//вызов метода сканирования устройств
       }
    //получение признака сканирования от активности
    @Override
    public int scanBLE(){
        return scanOn;
    }
    //=========================================================================
    //===========методы обратного вызова интерфейса класса ListAdvertFragment======
    //класс получает ссылку на эту деятельность через Fragment.onAttach (),
    //обратный вызов, который он использует для вызова следующих методов

    //список (полный/отсортированный) полученных сообщений
    @Override
    public List<Advert> getAdverts() {
    //вызов базы данных приложения и интерфейса
        AppDatabase db = App.getInstance().getDatabase();
        //AdvertDao advertDao = db.getAdvertDao();
        switch (numberItemMenu){
       //получение списка сообщений отсортированных по дате начиная с первой
            case 0:
                return db.getAdvertDao().getAllAdvert();
        //получение списка сообщений отсортированных по дате начиная с последней
            case 1:
                List<Advert> dateAdvert = db.getAdvertDao().getAllAdvert();
                int s = dateAdvert.size();
                List<Advert> downDateAdvert = new ArrayList<>();
                for (int i=0; i < s; i++){
                   downDateAdvert.add(0,dateAdvert.get(i));
                }
                return downDateAdvert;
        //получение списка сообщений за выбранную дату - день
          /*  case 2:
                Date advertDate = new Date(2020,7,6,16,35,20);//пример
                return db.getAdvertDao().getByDateAdvert(advertDate); */
        }
        return null;
    }
    //==========================================================================
    //===========методы обратного вызова интерфейса класса DigitalIndicatorFragment======
    //класс получает ссылку на эту деятельность через Fragment.onAttach (),
    //обратный вызов, который он использует для вызова следующих методов
     //-- методы для индикации цифровых устройств с помощью специальной вкладки---
    @Override
    public String getIndTypeDevice(){return indTypeDevice;}
    @Override
    public String getIndValue(){return indValue;}
    @Override
    public String getIndVC(){return indVC;}
    //=============================================================================
    //метод: сканирование устройств BLE
    private void scanLeDevice(final boolean enable) {
        if (enable) {
            statusUpdate(getString(R.string.discover));
            mLEScanner.startScan(mScanCallback);
            scanOn = 1;//запоминание признака сканирования
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {//запуск потока на время, по окончании
                    //выполняется метод run т.к. Android через 30 мин сбрасывает сканирование
                    if (scanOn==1) {
                        mLEScanner.stopScan(mScanCallback);
                        initBluetoothAdapter();
                    }
                }
            },RETURN_SKAN);
        } else {
            mLEScanner.stopScan(mScanCallback);
            statusUpdate(getString(R.string.search_complete));//сообщение
            scanOn = 0;//сброс признака сканирования
        }
    }
    //метод обратного вызова при сканировании
    private final ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int CALLBACK_TYPE_ALL_MATCHES  , ScanResult result) {
            super.onScanResult(CALLBACK_TYPE_ALL_MATCHES, result);
           //получение имени устройства
            String adv = result.toString();//строковое представление результата
            String[] nameArr = adv.split("mDeviceName=");//раздел строки
            String name = nameArr[1].substring(0, 18);//выделение имени
            //проверка совпадения начала имени с названием фирмы ""
            if (name.startsWith(comp)) {
                //исключение дешифрации повторных сообщений за период 1с
                if (!name.equals(oldName)) {//запуск потока на время, по окончании
                    //выполняется метод run
                    mHandler.postDelayed( new Runnable() {
                        @Override
                        public void run() {
                            oldName = null;//обнуляется имя
                        }
                    }, PERIOD_REPEAT);
                    oldName = name;//запоминание полученного имени
                } else return;
                //вывод в строку состояния
                messageBuffer = "ПРИНЯТО СООБЩЕНИЕ: " + name;
                statusUpdate(messageBuffer);
                //запуск программы обработки полученного сообщения
                    mFormAdvert = new FormAdvert();
                    mFormAdvert.setAdvert(name);
                    //если устройство с цифровой индикацией - вызов фрагмента
                    if (mFormAdvert.getType().equals("D")) {
                        //получение информации для индикации
                        indTypeDevice = mFormAdvert.getMod();
                        indValue = mFormAdvert.getValue();
                        indVC = mFormAdvert.getVc();
                        //создание фрагмента для индикации
                DigitalIndicatorFragment fragment = new DigitalIndicatorFragment();
                        fm.beginTransaction()
                                .replace(R.id.container1, fragment)
                                .commit();
                        //запуск потока на время, по окончании которого
                        //выполняется метод run
                 if (runnable != null) {//если предыдущий не закончился - сбросить
                            mHandler.removeCallbacks(runnable);
                        }
                        mHandler.postDelayed(runnable = new Runnable() {
                            @Override
                            public void run() {
                                //вызывается главный фрагмент
                                MainFragment fragment1 = new MainFragment();
                                fm.beginTransaction()
                                        .replace(R.id.container1, fragment1)
                                        .commit();
                            }
                        }, PERIOD_IND);
                    } else {
                        deviceMessage = mFormAdvert.getTextAdvert();
                        //вывод результата на экран
                        Toast.makeText(MainActivity.this,
                                deviceMessage,
                                Toast.LENGTH_LONG).show();
                        //посылка сообщения в telegram
                       new SendAdvertData().execute();
                    }
                    //звук
                    mPlayer.setSong(mFormAdvert.getWav());
                    mPlayer.play(MainActivity.this);
                }
        }
        @Override
        public void onScanFailed(int errorCode) {
            Log.e("Scan Failed", "Error Code: " + errorCode);
            statusUpdate("Scan Failed"+ "\r\n"+ "Error Code: " + errorCode);
        }
    };
    //========методы активности относящиеся к "меню"=========================
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Раздуть меню; это добавляет элементы в панель действий, если она присутствует.
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_activity_main, menu);
        return true;
    }
    //метод, вызывается когда пользователь открывает меню панели
    //метод при нажатии значка меню вызывается дважды и поэтому
    //необходима проверка на ноль для первого раза
    @Override
    public boolean onMenuOpened(int featureId, Menu menu){
        if(menu!=null){
            //варианты отображения групп меню для разных вкладок
            menu.setGroupVisible(R.id.group1, numberTabSelect == 1);
            menu.setGroupVisible(R.id.group2, numberTabSelect == 0);
        }
    //return true;
        return super.onMenuOpened(featureId, menu);
    }
    //метод обработки нажатия элемента меню
    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sort_date_up:
                numberItemMenu = 0;
               ListAdvertFragment fragment = new ListAdvertFragment();
                fm.beginTransaction()
                        .replace(R.id.container1, fragment)
                        .commit();
                return true;
            case R.id.sort_date_down:
                numberItemMenu = 1;
                ListAdvertFragment fragment1 = new ListAdvertFragment();
                fm.beginTransaction()
                    .replace(R.id.container1, fragment1)
                    .commit();
                return true;
             /* не используется
            case R.id.sort_date:
                numberItemMenu = 2;
                //ввод даты через окно-диалог


                ListAdvertFragment fragment2 = new ListAdvertFragment();
                fm.beginTransaction()
                        .replace(R.id.container1, fragment2)
                        .commit();
                return true;

          */
            case R.id.identification_data:
             //ввод имени компании через окно-диалог
                fragmentDialog = new EnterIdentificationDialogFragment();
                fragmentDialog.show(getSupportFragmentManager(), "missiles");
                return true;
            default:
                return super.onOptionsItemSelected(item);//возврат false
        }
    }
    //удаление диалога, обновление главного фрагмента
    public void replaceMainFragment(){
        fragmentDialog.dismiss();//удаление диалога
        MainFragment fragment = new MainFragment();
        fm.beginTransaction()
                .replace(R.id.container1, fragment)
                .commit();
    }
    //Вызывается когда приложение становится не активным
    @Override
    protected void onPause() {
        super.onPause();
        if (mBluetoothAdapter != null && mBluetoothAdapter.isEnabled()) {
            scanLeDevice(false);//отключение сканирования
        }
        //обновление главного фрагмента
        MainFragment fragment = new MainFragment();
        fm.beginTransaction()
                .replace(R.id.container1, fragment)
                .commit();
        //внесение изменений параметров в профиль пользователя
        WriteFile();//запись параметров в файл профиля
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPlayer.stop();//удалить проигрователь
    }
    //переопределение метода вызова другой активности на результат
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_CANCELED) {
                //Bluetooth not enabled.
                finish();
            }
        }
    }
    //==============ЧТЕНИЕ\ЗАПИСЬ ФАЙЛА ПРОФИЛЯ====================================
    //создание файла профиля для записи
    //во внутреннею память (при удалении приложения файл уничтожается)
    private void WriteFile() {
        /*Правила записи параметров в профиль:
        1.Если строка начинается со знака #, это строка коментариев
        2.В строке только один параметр
        3.Наименование параметра совпадает с описанным в программе
        4.Строка с параметром разделяется на две части
           в левой наименование, в правой через знак "пробел :" величина */
        //-----Формирование содержимого файла ввиде строки:--------------------------
        String fileString = "#Identification Data \n";
        fileString += "Name Organization :" + mNameFirm + "\n";
        fileString += "Name User :" + mNameUser + "\n";
        //-----------------------------------------------------------------------------
        FileOutputStream out = null;
        try {//открыть/создать файл с заданным именем
            //в режиме только для этого приложения
            out = openFileOutput(path,MODE_PRIVATE);
        } catch (FileNotFoundException e) {
            //в случае ошибки выдать сообщение
            //e.printStackTrace();
            Toast.makeText(this,R.string.no_file_name,Toast.LENGTH_LONG).show();
        }
        try {//запись содержимого в файл
            assert out != null;
            out.write(fileString.getBytes());
        } catch (IOException e) {
            //в случае ошибки выдать сообщение
            //e.printStackTrace();
            Toast.makeText(this,R.string.error_write,Toast.LENGTH_LONG).show();
        }
        try {//закрыть файл для записи
            out.close();
        } catch (IOException e) {
            //в случае ошибки выдать сообщение
            //e.printStackTrace();
            Toast.makeText(this,R.string.no_clouse_file,Toast.LENGTH_LONG).show();
        }
        //если все прошло нормально, выдается сообщение
        //Toast.makeText(this,R.string.write_file,Toast.LENGTH_LONG).show();
      }
    //чтение файла профиля
    private void ReadFile(){
        FileInputStream in;
        try {//открыть файл с указанным именем
            in = openFileInput(path);
        } catch (FileNotFoundException e) {
            //если файла нет
            FistBegin = true;//признак первого включения
            //выдать сообщение	"Профиль не создан"
            Toast.makeText(this,R.string.no_file_name,Toast.LENGTH_LONG).show();
            return;
        }
        StringBuilder Str = new StringBuilder();//переменная где накапливается файл
        int bt = 0;
        while (bt != -1){
            try {
                bt = in.read();
            } catch (IOException e) {
                //в случае ошибки выдать сообщение
                Toast.makeText(this,R.string.error_read,Toast.LENGTH_LONG).show();
            }
            Str.append((char) bt);
        }
        try {//закрыть файл для чтения
            in.close();
        } catch (IOException e) {
            //в случае ошибки выдать сообщение
            Toast.makeText(this,R.string.no_clouse_file,Toast.LENGTH_LONG).show();
        }
        //Toast.makeText(this,Str,Toast.LENGTH_LONG).show();//проверка(выдача на экран)
        if (!Str.toString().contentEquals(""))//если полученная строка не нулевая
        {
            //преобразование файла в массив строк
            String[] s = Str.toString().split("\n");
            //-----анализ строк---------
            for (String value : s) {
                //запись из профиля наименования организации
                if (value.startsWith("Name Organization")) {
                    //выделить данные (всегда после ":")
                    String[] Ps = value.split(":");
                    //записать в переменную
                    mNameFirm = Ps[1];
                }
                //запись из профиля наименования пользователя
                if (value.startsWith("Name User")) {
                    //выделить данные (всегда после ":")
                    String[] Ps = value.split(":");
                    //записать в переменную
                    mNameUser = Ps[1];
                }
            }
        }
    }
    //=================================================================================
    //=====внутренний класс передачи сообщений в интернет на канал telegram=============
    //передача выполняется в асинхронном потоке в фоновом режиме посредством класса AsyncTask
    @SuppressLint("StaticFieldLeak")
    public class SendAdvertData extends AsyncTask<Void, Void, Void> {
        private String resultString = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                //адресс бота "телеграмм"
                String myURL =
                        "https://api.telegram.org/bot" + token +"/sendMessage";
                String param = "chat_id=" + id + "&text=" + deviceMessage +
                        "\n#" + mNameFirm + "/" + mNameUser;
                byte[] data;
                try {//подключаемся к серверу
                    URL url = new URL(myURL);
                    HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
                    conn.setReadTimeout(2000);//тайм-аут чтения в мс
                    conn.setConnectTimeout(2000);//тайм-аут соединения в мс
                    conn.setRequestMethod("POST");
                    conn.setDoOutput(true);
                    conn.setDoInput(true);
                    //определяем длину передаваемого сообщения
                    conn.setRequestProperty("Content-Length", "" + param.getBytes().length);
                    OutputStream os = conn.getOutputStream();
                    // конвертируем передаваемую строку в UTF-8
                    data = param.getBytes(StandardCharsets.UTF_8);
                    //передаем данные на сервер
                    os.write(data);
                    os.flush();
                    os.close();
                    conn.connect();
                    int responseCode = conn.getResponseCode();//Представляющие три цифры HTTP Status-Code
                    resultString = Integer.toString(responseCode);
                    conn.disconnect();//отключение соединения
                } catch (MalformedURLException e) {//неправильный адрес сайта

                    resultString = "MalformedURLException:" + e.getMessage();
                } catch (IOException e) {//неудачная операция ввода/вывода

                    resultString = "IOException:" + e.getMessage();
                } catch (Exception e) {//что-то пошло не так

                    resultString = "Exception:" + e.getMessage();
                }

                } catch (Exception e) {
                //e.printStackTrace();
                }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            /* для отладки
            if (resultString != null) {
             //вывод результата на экран
                Toast.makeText(MainActivity.this, resultString, Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(MainActivity.this, "no result", Toast.LENGTH_SHORT).show();
            }
            */
            if (resultString.equals("200")) {//kod '200' - ok сообщение доставлено успешно
                statusUpdate( messageBuffer + "\r\n"+"СООБЩЕНИЕ отправлено ИНТЕРНЕТ");
                mFormAdvert.setBD(true);//запись сообщения в базу данных
            } else {
                statusUpdate( messageBuffer + "\r\n"+"ОШИБКА отправки ИНТЕРНЕТ");
                mFormAdvert.setBD(false);//запись сообщения в базу данных
            }
          }
        }
    //=====================================================================================
}