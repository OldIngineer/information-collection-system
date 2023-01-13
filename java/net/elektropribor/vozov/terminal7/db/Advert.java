package net.elektropribor.vozov.terminal7.db;


import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Date;

//класс - объект сообщение, используется в базе данных
//https://startandroid.ru/ru/courses/architecture-components/27-course/
// architecture-components/530-urok-6-room-entity.html
@Entity
public class Advert {
    @PrimaryKey(autoGenerate = true)
    private long id;//идентификатор
    private Date mDate;//дата - объект
    private String mName;//полученное сообщение
    private String mType;//тип устройства
    private String mMod;//модификация устройства
    private String mSn;//серийный номер устройства
    private String mKod;//расшифрованный код сообщения
    private boolean mSent;//признак отсылки сообщения на сервер

    //конструктор
    public Advert() {
        // Генерирование уникального идентификатора
        // mId = UUID.randomUUID();
        mDate = new Date();
    }

    public void setName(String mName) {
        this.mName = mName;
    }
    public String getName() {
        return mName;
    }

    public void setType(String mType) {
        this.mType = mType;
    }
    public String getType() {
        return mType;
    }

    public void setMod(String mMod) {
        this.mMod = mMod;
    }
    public String getMod() {
        return mMod;
    }

    public void setSn(String mSn) {
        this.mSn = mSn;
    }
    public String getSn() {
        return mSn;
    }

    public void setKod(String mKod) {
        this.mKod = mKod;
    }
    public String getKod() {
        return mKod;
    }

    public boolean isSent() {
        return mSent;
    }
    public void setSent(boolean mSent) {
        this.mSent = mSent;
    }

    public long getId() {
        return id;
    }
    public void setId(long id) { this.id = id; }

    public Date getDate() {
        return mDate;
    }
    public void setDate(Date mDate) { this.mDate = mDate; }


}