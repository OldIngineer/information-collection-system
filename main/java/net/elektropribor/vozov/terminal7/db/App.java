package net.elektropribor.vozov.terminal7.db;

import android.app.Application;

import androidx.room.Room;

//класс синглетного типа (singleton). Такие классы допускают создание
// только одного экземпляра, создается один раз при первом запуске приложения.
// Экземпляр синглетного класса существует до тех пор,
// пока приложение остается в памяти
public class App extends Application {
    private static App instance;
    private AppDatabase db;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        db = Room.databaseBuilder(getApplicationContext(), AppDatabase.class,
                "database")
                .allowMainThreadQueries()
                .build();
    }
    public static App getInstance() {
        return instance;
    }
    public AppDatabase getDatabase() {
        return db;
    }
}
