package net.elektropribor.vozov.terminal7.db;

import androidx.room.TypeConverter;

import java.util.Date;

//класс для преобразования объекта Date в форму для записи в базу данных
public class Converters {
    @TypeConverter
    public static Date fromTimestamp(Long value) {
        return value == null ? null : new Date(value);
    }

    @TypeConverter
    public static Long dateToTimestamp(Date date) {
        return date == null ? null : date.getTime();
    }
}
