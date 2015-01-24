package db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import java.sql.SQLException;

public class DatabaseHelper extends OrmLiteSqliteOpenHelper {

    // name of the database file
    private static final String DATABASE_NAME = "locks.sqlite";
    // any time you make changes to your database objects, you have to increase the database version
    private static final int DATABASE_VERSION = 1;

    // the DAO object we use to access the MarketApplication table
    private Dao<Lock, String> simpleDao = null;
    private RuntimeExceptionDao<Lock, String> simpleRuntimeDao = null;

    public DatabaseHelper(Context context) {
//        super(context, DATABASE_NAME, null, DATABASE_VERSION, R.raw.ormlite_config);
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // This is called when the database is first created.
    @Override
    public void onCreate(SQLiteDatabase db, ConnectionSource connectionSource) {
        try {
            Log.i(DatabaseHelper.class.getName(), "onCreate");
            TableUtils.createTable(connectionSource, Lock.class);
        } catch (SQLException e) {
            Log.e(DatabaseHelper.class.getName(), "Can't create database", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, ConnectionSource connectionSource, int oldVersion, int newVersion) {
        Log.i(DatabaseHelper.class.getName(), "onUpgrade");

//		if (oldVersion == 1) {
//		  // we added the age column in version 2
//		  dao.executeRaw("ALTER TABLE `account` ADD COLUMN age INTEGER;");
//		}
//		if (oldVersion < 2) {
//		  // we added the weight column in version 3
//		  dao.executeRaw("ALTER TABLE `account` ADD COLUMN weight INTEGER;");
//		}
        Log.i(DatabaseHelper.class.getName(), "DROP TABLE");
        try {
            TableUtils.dropTable(connectionSource, Lock.class, true);
            onCreate(db, connectionSource);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns the Database Access Object (DAO) for our MarketApplication class. It will create it or just give the cached
     * value.
     */
    public Dao<Lock, String> getDao() throws SQLException {
        if (simpleDao == null) {
            simpleDao = getDao(Lock.class);
        }
        return simpleDao;
    }

    /**
     * Returns the RuntimeExceptionDao (Database Access Object) version of a Dao for our SimpleData class. It will
     * create it or just give the cached value. RuntimeExceptionDao only through RuntimeExceptions.
     */
    public RuntimeExceptionDao<Lock, String> getSimpleDataDao() {
        if (simpleRuntimeDao == null) {
            simpleRuntimeDao = getRuntimeExceptionDao(Lock.class);
        }
        return simpleRuntimeDao;
    }

    /**
     * Close the database connections and clear any cached DAOs.
     */
    @Override
    public void close() {
        super.close();
        simpleRuntimeDao = null;
    }
}
