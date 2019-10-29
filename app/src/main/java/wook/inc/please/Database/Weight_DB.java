package wook.inc.please.Database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;

import devs.mulham.horizontalcalendar.HorizontalCalendar;
import wook.inc.please.EditNutFragment;
import wook.inc.please.NewNutFragment;
import wook.inc.please.NutFragment;
import wook.inc.please.ViewContactFragment;

public class Weight_DB {

    Context context;
    String DB_NAME = "Nut_1.db";
    String Date_DB = "Dt_1.db";

    public Weight_DB(Context context) {
        this.context = context;
    }

    private void copyDatabase(File dbfile){
        try{
            String folderPath = "/data/data/" + context.getPackageName() + "/databases";
            File folder = new File(folderPath);
            if(!folder.exists()) folder.mkdirs();
            InputStream is = context.getAssets().open(DB_NAME);
            OutputStream os =  new FileOutputStream(dbfile);
            byte[] buffer = new byte[1024];
            while(is.read(buffer)>0) os.write(buffer);
            os.flush();
            is.close(); os.close();
        } catch (Exception e){}
    }
    public void saveData(String name,int fullWeight,int oneWeight, int num,int left){
        File dbFile = context.getDatabasePath(DB_NAME);
        if(!dbFile.exists()) copyDatabase(dbFile);
        SQLiteDatabase db = context.openOrCreateDatabase(DB_NAME , Context.MODE_PRIVATE,null);
        db.execSQL("INSERT INTO Nutrition VALUES('"+name+"','"+fullWeight+"','"+oneWeight+"','"+num+"','"+left+"');");
        Log.d("TEST","-----Data Saved----");
    }

    public void delete(String name){
        File dbFile = context.getDatabasePath(DB_NAME);
        if(!dbFile.exists()) copyDatabase(dbFile);
        SQLiteDatabase db = context.openOrCreateDatabase(DB_NAME,Context.MODE_PRIVATE,null);
        db.execSQL("DELETE FROM Nutrition WHERE NutName ='"+name+"';");
    }
    public boolean checkEmpty(){
        File dbFile = context.getDatabasePath(DB_NAME);
        if(!dbFile.exists()) copyDatabase(dbFile);
        SQLiteDatabase db = context.openOrCreateDatabase(DB_NAME,context.MODE_PRIVATE,null);

        boolean check;

        Cursor cursor = db.rawQuery("SELECT * FROM Nutrition",null);
        if(cursor.moveToFirst()){
            check = true;
        }
        else
            check = false;

        return check;
    }

    public void update(String name, int full , int one , int num , int left){
        File dbFile = context.getDatabasePath(DB_NAME);
        if(!dbFile.exists()) copyDatabase(dbFile);
        SQLiteDatabase db = context.openOrCreateDatabase(DB_NAME,context.MODE_PRIVATE,null);
        String up = "UPDATE Nutrition SET fullweight = '"+full+"',"+
                "oneweight = '"+one+"',"+
                "Nutnum = '"+num+"',"+
                "Left = '"+left+"' WHERE NutName = '"+name+"';";
        db.execSQL(up);
    }
    public void putData(NutFragment NutFragment){
        File dbFile = context.getDatabasePath(DB_NAME);
        if(!dbFile.exists()) copyDatabase(dbFile);
        SQLiteDatabase db = context.openOrCreateDatabase(DB_NAME,context.MODE_PRIVATE,null);

        Cursor cursor = db.rawQuery("SELECT * FROM Nutrition",null);

        if(cursor.getCount()>0){
            while(cursor.moveToNext()){
                NutFragment.nutName= cursor.getString(0);
                NutFragment.full = cursor.getInt(1);
                NutFragment.one = cursor.getInt(2);
                NutFragment.num = cursor.getInt(3);
//                NutFragment.hour = cursor.getInt(4);
//                NutFragment.minute = cursor.getInt(5);
                NutFragment.left = cursor.getInt(4);
            }
        }
    }
    public void getName(EditNutFragment editNutFragment){
        File dbFile = context.getDatabasePath(DB_NAME);
        if(!dbFile.exists()) copyDatabase(dbFile);
        SQLiteDatabase db = context.openOrCreateDatabase(DB_NAME,context.MODE_PRIVATE,null);
        Cursor cursor = db.rawQuery("SELECT NutName FROM Nutrition",null);
        if(cursor.getCount()>0){
            while(cursor.moveToNext()){
                editNutFragment.getname = cursor.getString(0);
            }
        }
    }
    public void setEat(int check, String date){
        File dbFile = context.getDatabasePath(DB_NAME);
        if(!dbFile.exists()) copyDatabase(dbFile);
        SQLiteDatabase db = context.openOrCreateDatabase(DB_NAME , Context.MODE_PRIVATE,null);
        db.execSQL("INSERT INTO Nutdt VALUES('"+date+"','"+check+"');");
    }

    public boolean checkEmpty2(){
        File dbFile = context.getDatabasePath(DB_NAME);
        if(!dbFile.exists()) copyDatabase(dbFile);
        SQLiteDatabase db = context.openOrCreateDatabase(DB_NAME,context.MODE_PRIVATE,null);
        boolean check;
        Cursor cursor = db.rawQuery("SELECT * FROM Nutdt",null); //이 db에 접근 했을때 아무것도 입력 되어있지 않으면
        if(cursor.moveToFirst()){
            check = true; //뭔가 있으면 true
        }
        else
            check = false; //아무것도 없으면 false

        return check;
    }

    public boolean checkEmpty3(String date){
        File dbFile = context.getDatabasePath(DB_NAME);
        if(!dbFile.exists()) copyDatabase(dbFile);
        SQLiteDatabase db = context.openOrCreateDatabase(DB_NAME , Context.MODE_PRIVATE,null);
        Cursor  cursor = db.rawQuery("SELECT * FROM Nutdt WHERE dat_st like'"+date+"%';",null);
        while (cursor.moveToNext()) {
            if (cursor.getString(0).equals(date)) { //인자로 받은 문자열이 존재한다면
                return true; //
            }
        }
        return false;
    }

    public int saveLastSave() {
        File dbFile = context.getDatabasePath(DB_NAME);
        if (!dbFile.exists()) copyDatabase(dbFile);
        SQLiteDatabase db = context.openOrCreateDatabase(DB_NAME, context.MODE_PRIVATE, null);

        boolean check;
        int lastsave = 0;

        Cursor cursor = db.rawQuery("SELECT * FROM Nutdt ", null);
        if (cursor.moveToLast()) {
            lastsave = cursor.getInt(1);
//            viewContactFragment.pasteat = cursor.getString(0);
        }
        return lastsave;
    }

    public int getEatData(String cal){
        File dbFile = context.getDatabasePath(DB_NAME);
        if(!dbFile.exists()) copyDatabase(dbFile);
        SQLiteDatabase db = context.openOrCreateDatabase(DB_NAME , Context.MODE_PRIVATE,null);
        Cursor  cursor = db.rawQuery("SELECT * FROM Nutdt WHERE dat_st like'"+cal+"%';",null);
        int check = 0;
        while (cursor.moveToNext()) {
                check = cursor.getInt(1);

        }
        return check;
    }

    public String giveFirstDate(){
        File dbFile = context.getDatabasePath(DB_NAME);
        if(!dbFile.exists()) copyDatabase(dbFile);
        SQLiteDatabase db = context.openOrCreateDatabase(DB_NAME , Context.MODE_PRIVATE,null);

        String firstDate = "";
        Cursor cursor = db.rawQuery("SELECT * FROM Nutdt",null);
        if(cursor.moveToFirst()){
            firstDate = cursor.getString(0); //뭔가 있으면 true
        }
        return firstDate;
    }
    public void giveWeight(ViewContactFragment viewContactFragment){
        File dbFile = context.getDatabasePath(DB_NAME);
        if(!dbFile.exists()) copyDatabase(dbFile);
        SQLiteDatabase db = context.openOrCreateDatabase(DB_NAME,context.MODE_PRIVATE,null);

        Cursor cursor = db.rawQuery("SELECT * FROM Nutrition",null);
        if(cursor.getCount()>0){
            while(cursor.moveToNext()){
                viewContactFragment.name = cursor.getString(0);
                viewContactFragment.full = cursor.getInt(1);
                viewContactFragment.one = cursor.getInt(2);
                viewContactFragment.num = cursor.getInt(3);
//                viewContactFragment.num+=5;
            }
        }
    }
    public void putLeft(int left,String name){
        File dbFile = context.getDatabasePath(DB_NAME);
        if(!dbFile.exists()) copyDatabase(dbFile);
        SQLiteDatabase db = context.openOrCreateDatabase(DB_NAME,context.MODE_PRIVATE,null);
        db.execSQL("UPDATE Nutrition SET Left = '"+left+"' WHERE NutName = '"+name+"';");
    }

    public int giveLeft(){
        File dbFile = context.getDatabasePath(DB_NAME);
        if(!dbFile.exists()) copyDatabase(dbFile);
        SQLiteDatabase db = context.openOrCreateDatabase(DB_NAME,context.MODE_PRIVATE,null);

        Cursor cursor = db.rawQuery("SELECT * FROM Nutrition",null);

        int left = 0;
        if(cursor.getCount()>0){
            while(cursor.moveToNext()){
                left = cursor.getInt(4);
            }
        }
        return left;
    }

    public void delete2(){
        File dbFile = context.getDatabasePath(DB_NAME);
        if(!dbFile.exists()) copyDatabase(dbFile);
        SQLiteDatabase db = context.openOrCreateDatabase(DB_NAME,Context.MODE_PRIVATE,null);
        db.execSQL("DELETE FROM Nutdt");
    }
}
