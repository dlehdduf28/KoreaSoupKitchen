package net.dongyeol.storagehelper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 *
 * 1개의 DB 파일 관리를 담당하는 사용자 정의형 매니저 클래스
 */

public final class DBManager {
    public static final String TAG = "DBManager";

    public int database_version = 1;

    private Context         context;

    private SQLiteDatabase  db;
    private DBOpenHelper    openHelper;
    private String          dbFileStr;

    // 데이터베이스 업그레이드 리스너 변수 //
    DBUpgradeListener       dbUpgradeListener;

    /**
     * 전달된 경로로 DB 파일을 생성하고 RW 가능한 SQLiteDatabase 객체를 생성한다.
     * @param c
     * @param fileStr DB 경로
     * @param version 현재 DB 버전 (가장 최신 버전)
     */

    public DBManager(Context c, String fileStr, int version) {
        this.context    =   c;
        this.dbFileStr     =   fileStr;
        this.database_version   =   version;

        openHelper = new DBOpenHelper(c, fileStr, null, database_version);
        db          =   openHelper.getWritableDatabase();
    }

    public SQLiteDatabase getSQLiteDatabase() {
        return db;
    }

    /**
     * 전달된 양식으로 DB에 테이블을 만든다.
     * @param table_name    테이블 이름
     * @param cd            column_type_options
     */

    public void createTable(String table_name, String[] cd) {
        String sql = "create table " + table_name + "(";

        for(int i = 0; i < cd.length; i++) {

            sql += cd[i];
            // 양식 첨부 //
            if (i == (cd.length - 1))
                sql += ");";
            else
                sql += ", ";
        }

        Log.d(TAG, sql);

        db.execSQL(sql);
    }

    /**
     * 전달된 테이블 이름에 column-data 쌍의 데이터로 레코드를 삽입 한다.
     * @param table_name    테이블 이름
     * @param cv            column-data 쌍의 데이터
     * @return              삽입된 레코드의 행 위치 (인덱스)
     */
    public int insertRecord(String table_name, ColumnValue[] cv) {
        ContentValues recordValues = new ContentValues();

        for(int i = 0; i < cv.length; i++)
            recordValues.put(cv[i].getColumn(), cv[i].getData());

        int rowPosition = (int) db.insert(table_name, null, recordValues);
        return rowPosition;
    }

    /**
     * 전달된 테이블 이름에서 column-data 쌍의 전달된 데이터만을 조건에 맞는 레코드에 삽입 한다.
     * @param table_name    테이블 이름
     * @param cv            column-data 쌍의 데이터
     * @param condition     조건문                             ex) "name = ?"
     * @param whereArgs     조건문의 ?에 대입할 문장              ex) {"Rice"}
     * @return              이 테이블에 삽입 되어 있는 레코드의 개수
     */
    public int updateRecord(String table_name, ColumnValue[] cv, String condition, String[] whereArgs) {
        ContentValues recordValues = new ContentValues();

        for(int i = 0; i < cv.length; i++)
            recordValues.put(cv[i].getColumn(), cv[i].getData());

        int rowAffected = db.update(table_name, recordValues, condition, whereArgs);
        return rowAffected;
    }

    /**
     * 전달된 테이블 이름에서 조건문과 일치하는 레코드는 삭제 한다.
     * @param table_name    테이블 이름
     * @param condition     조건문                             ex) "name = ?"
     * @param whereArgs     조건문의 ?에 대입할 문장              ex) {"Rice"}
     * @return              이 테이블에 삽입 되어 있는 레코드의 개수
     */
    public int deleteRecord(String table_name, String condition, String[] whereArgs) {
        int rowAffected = db.delete(table_name, condition, whereArgs);
        return rowAffected;
    }

    /**
     * 전달된 테이블 이름에서 조건에 일치하는 레코드를 담은 결과 테이블을 가리키는 커서 반환.
     * @param table_name    테이블의 이름
     * @param columns       결과 테이블의 칼럼
     * @param whereStr      조건문           ex) "name = ?", "age > ?"
     * @param whereParams   조건문의 ?에 대입할 문장 ex) {"Rice"}, {"30"}
     * @return              커서
     */
    public Cursor receiveCursor(String table_name, String[] columns,
                                String whereStr, String[] whereParams) {
        Cursor cursor = db.query(table_name, columns, whereStr, whereParams, null, null, null);

        return cursor;
    }

    public void closeDatabase() {
        db.close();

        Log.d(TAG, db.getPath() + " closed.");
    }


    public void setDatabaseVersion(int databaseVersion) {
        this.database_version = databaseVersion;
    }

    public int getDatabaseVersion() {
        return database_version;
    }

    public DBUpgradeListener getDBUpgradeListener() {
        return dbUpgradeListener;
    }

    public void setDBUpgradeListener(DBUpgradeListener dbUpgradeListener) {
        this.dbUpgradeListener = dbUpgradeListener;
    }

    // Inner Class //

    class DBOpenHelper extends SQLiteOpenHelper {

        public DBOpenHelper(Context c, String name, SQLiteDatabase.CursorFactory factory, int version) {
            super(c, name, factory, version);
        }

        @Override
        public void onCreate(SQLiteDatabase sqLiteDatabase) {
            Log.d(TAG, sqLiteDatabase.getPath() + " created.");
        }

        @Override
        public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
            if ( dbUpgradeListener == null )
                return;

            dbUpgradeListener.onUpgrade(sqLiteDatabase, oldVersion, newVersion);
        }

        @Override
        public void onOpen(SQLiteDatabase db) {
            super.onOpen(db);

            Log.d(TAG, db.getPath() + " opened.");
        }
    }

    // Nested Class //
    public static class ColumnValue {
        private String column;
        private String data;

        public ColumnValue(String column, String data) {
            this.column = column;
            this.data = data;
        }

        public String getColumn() {
            return column;
        }

        public void setColumn(String column) {
            this.column = column;
        }

        public String getData() {
            return data;
        }

        public void setData(String data) {
            this.data = data;
        }
    }


    /**
     * 데이터베이스에 업그레이드 기능을 활성화 할려면 이 인터페이스를 구현해서 매니저에 리스너로 설정 해야 한다.
     *
     * 예시)
     * DBManager dbManager = new DBManager(this, "data.db", 2); // context, DB 경로, 현재 버전 (가장 최신 버전)
     * dbManager
     *
     * dbManager.setDBUpgradeListener( new DBManager.DBUpgradeListener() {
     *     @Override
     *     public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
     *          int version = oldVersion;
     *
     *          // 패치해야 할 버전부터 최신 버전까지 모든 업데이트 사항을 실행 하도록 switch 문을 사용 한다. //
     *          switch ( oldVersion ) {
     *              case 1:
     *                  db.execSQL("ALTER TABLE employee ADD COLUMN example text");  // employee 테이블에 example 칼럼(열)을 추가 한다.
     *              case 2:
     *              case 3:
     *          }
     *     }
     * }
     *
     */
    interface DBUpgradeListener {
        void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion);
    }
}
