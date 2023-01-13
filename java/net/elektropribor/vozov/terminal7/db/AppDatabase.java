package net.elektropribor.vozov.terminal7.db;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

//класс - формирование базы данных приложения
//https://dajver.blogspot.com/2017/11/room.html
//https://startandroid.ru/ru/courses/architecture-components/27-course/
@Database(entities = {Advert.class},version = 1,exportSchema = false)
@TypeConverters(Converters.class)
public abstract class AppDatabase extends RoomDatabase {
    public abstract AdvertDao getAdvertDao();
}
