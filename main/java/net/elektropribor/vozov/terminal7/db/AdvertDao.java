package net.elektropribor.vozov.terminal7.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.Date;
import java.util.List;

//Интерфейс работы с базой данных
@Dao
public interface AdvertDao {
    //запрос на получение всех объектов в виде перечня
    @Query("SELECT * FROM Advert")
    List<Advert> getAllAdvert();
    //запрос данных по "сообщению"
    @Query("SELECT * FROM Advert WHERE mName =:advertName")
    List<Advert> getByNameAdvert(String advertName);
    //запрос данных по id
    @Query("SELECT * FROM Advert WHERE id =:advertId")
    List<Advert> getByIdAdvert(long advertId);
    //запрос данных по дате
    @Query("SELECT * FROM Advert WHERE mDate =:advertDate")
    List<Advert> getByDateAdvert(Date advertDate);
    //запрос данных по типу устройства
    @Query("SELECT * FROM Advert WHERE mType =:advertType")
    List<Advert> getByTypeAdvert(String advertType);
    //запрос данных по модификации устройства
    @Query("SELECT * FROM Advert WHERE mMod =:advertMod")
    List<Advert> getByModAdvert(String advertMod);
    //запрос данных по серийному номеру устройства
    @Query("SELECT * FROM Advert WHERE mSn =:advertSn")
    List<Advert> getBySnAdvert(String advertSn);
    //запрос данных по полученному сообщению
    @Query("SELECT * FROM Advert WHERE mKod =:advertKod")
    List<Advert> getByKodAdvert(String advertKod);
    //запрос данных по признаку отсылки сообщения
    @Query("SELECT * FROM Advert WHERE mSent =:advertSent")
    List<Advert> getBySentAdvert(boolean advertSent);
    @Insert
//вставить объект в базу данных
    void insert(Advert advert);
}

