package net.elektropribor.vozov.terminal7;

import net.elektropribor.vozov.terminal7.db.Advert;
import net.elektropribor.vozov.terminal7.db.AdvertDao;
import net.elektropribor.vozov.terminal7.db.App;
import net.elektropribor.vozov.terminal7.db.AppDatabase;

//класс обработки "рекламы" от устройств BLE
//!!!пакет данных вер.4 - макс.39байт; вер.5 - макс.257байт.
//Сообщение принимаемое от устройства имеет формат String(два байта на символ)
// и следующий вид:
//0-1 позиции: код предприятия "ЕР";
//2-3 позиции: модификация изделия:
//  H_01 - "УВНБУ 6-35"
//  L_01 - "УННО-СИНХРО"
//  D_01 - "ИТ-04"
//  H_02 - "УВНБУ 35-110"
//4 позиция: тип устройства -   "Н" указатель высокого напряжения;
//                              "L" указатель низкого напряжения;
//                              "D" цифровой индикатор;
//5-12 позиция: серийный номер;
//13-15 позиция: численные показания для цифровых устройств;
//16-17 позиция: код сообщения:
//  01 - "ОПАСНО!!!ВЫСОКОЕ НАПРЯЖЕНИЕ!!!";
//  02 - "Тест проверки прошел";
//  03 - "Тест проверки не прошел!!"
//------------------------------------
//  90 - "A";
//  91 - "V";
//  92 - "kV";
public class FormAdvert {
    private String mName;
    private String  type;
    private String mType;
    private String mMod;
    private String sn;
    private String mKod;
    private String value;
    private String vC;
    private int mWav;
    //МЕТОД - составление текстового варианта принятого сообщения
    public void setAdvert(String reAdvert) {
        mName = reAdvert;
        type = reAdvert.substring(4,5);//выделение типа устройства
        if (type.equals("H")) {
            mType = "указатель высокого напряжения";
        }
        if (type.equals("L")) {
            mType = "указатель низкого напряжения";
        }
        if (type.equals("D")) {
            mType = "цифровой индикатор";
        }
        String mod = reAdvert.substring(2,4);//выделение модификации
            if (mod.equals("01")){
                if (type.equals("H")) {
                    mMod = "УВНБУ 6-35";
                }
                if (type.equals("L")) {
                    mMod = "УННО-СИНХРО";
                }
                if (type.equals("D")) {
                    mMod = "ИТ-04";
                }
            }
            if((mod.equals("02"))&(type.equals("H"))){
                mMod = "УВНБУ 35-110";
            }
            sn = reAdvert.substring(5, 13);//выделение серийного номера
            value = reAdvert.substring(13,16);//цифровые показания
            String kod = reAdvert.substring(16);//выделение кода
            if(type.equals("D")){
                if(kod.equals("90")){
                 vC = "A";
                }
                if(kod.equals("91")){
                    vC = "V";
                }
                if(kod.equals("92")){
                    vC = "kV";
                }
                mKod = "Цифровые показания:"+value+" "+vC;
                mWav = R.raw.dveri;
            }
            if (kod.equals("01")) {
                mKod = "ОПАСНО!!!ВЫСОКОЕ НАПРЯЖЕНИЕ!!!";
                mWav = R.raw.ambulance;
            }
            if (kod.equals("02")) {
                mKod = "Тест проверки прошел";
                mWav = R.raw.gudok;
            }
            if(kod.equals("03")){
                mKod = "Тест проверки не прошел!!";
                mWav = R.raw.avaria;
            }

           }
    public void setBD(boolean attributeSent){
        //вызов базы данных приложения и интерфейса
        AppDatabase db = App.getInstance().getDatabase();
        AdvertDao advertDao = db.getAdvertDao();
        //запись события сообщения во вновь созданном объекте
        Advert mAdvert = new Advert();
        mAdvert.setName(mName);
        mAdvert.setType(mType);
        mAdvert.setMod(mMod);
        mAdvert.setSn(sn);
        mAdvert.setKod(mKod);
        mAdvert.setSent(attributeSent);
        advertDao.insert(mAdvert);//запись в базе
    }
    public String getTextAdvert() {
        return mType + "\r\n" +
                mMod + "\r\n" + "s/n:" + sn + "\r\n" + mKod;
    }
    public String getType() {
        return type;
    }
    public String getMod(){
        return mMod;
    }
    public String getVc(){ return vC;   }
    public String getValue(){  return value; }
    public int getWav() {  return mWav;  }
}
