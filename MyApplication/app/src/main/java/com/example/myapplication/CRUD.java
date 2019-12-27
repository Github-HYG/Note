package com.example.myapplication;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteCursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class CRUD {
    public CRUD(){
    }
    SQLiteOpenHelper dbHandler;
    SQLiteDatabase  db;

    private static final String[] columns = { //定义DB一条数据
            NoteDatabase.ID,
            NoteDatabase.CONTENT,
            NoteDatabase.TIME,
            NoteDatabase.MODE
    };

    public CRUD(Context context){
        dbHandler = new NoteDatabase(context);//create a Database
    }
    public void open(){
        db = dbHandler.getWritableDatabase();
    }
    public void close(){
        dbHandler.close();
    }


        //增加
    public Note addNote(Note note){                                  //传入一个Note类型，加入到 Database 传入数据库
        //add a note object to database
        // ContentValues /内容值 /处理数据
        // ContentValues /内容值 /处理数据
        ContentValues contentValues = new ContentValues();           // 初始化
        contentValues.put(NoteDatabase.CONTENT, note.getContent());  // Get corespondent DB Value
        contentValues.put(NoteDatabase.TIME, note.getTime());
        contentValues.put(NoteDatabase.MODE, note.getTag());
        long insertId = db.insert(NoteDatabase.TABLE_NAME, null, contentValues);//inert(数据库表名，若第三个参数为插入内容)
        //@return the row ID of the newly inserted row, or -1 if an error occurred
        note.setId(insertId);
        return note;
    }

         //查

    public Note getNote(long id){                                   //获取一条数据                    传出数据
        //get a note from database using cursor index
        String[] strid=new String[]{String.valueOf(id)};
        Cursor cursor = db.query(
                NoteDatabase.TABLE_NAME,
                columns,
                NoteDatabase.ID + "=?",strid,
                null, null, null);
        if (cursor != null) cursor.moveToFirst();//找不到id 就重置最前

        Note e = new Note(cursor.getString(1),cursor.getString(2), cursor.getInt(3));
        //column 0 is ID
        return e;
    }

    public List<Note> getAllNotes(){        //  查
        //get a note from database using cursor index

        List<Note> notelist = new ArrayList<>();

        Cursor cursor = db.query(
                NoteDatabase.TABLE_NAME,
                columns,
                null,null,
                null, null, null);

        int cursorNum = cursor.getCount();
                //加了反而出错？？？？？？   cursor.moveToFirst();
        if(cursorNum>0){
            while(cursor.moveToNext()){

                //坑         cursor初始位置是-1，所以不需要moveToFirst()
                // 直接moveToNext，那么第一条数据有没有加入list呢
                //破案了   忘了定义cursor初始化时 默认位置是-1

                Note note = new Note();
                 note.setContent(cursor.getString(cursor.getColumnIndex(NoteDatabase.CONTENT)));
                 note.setId(cursor.getLong(cursor.getColumnIndex(NoteDatabase.ID)));
                 note.setTime(cursor.getString(cursor.getColumnIndex(NoteDatabase.TIME)));
                 note.setTag(cursor.getInt(cursor.getColumnIndex(NoteDatabase.MODE)));

                 notelist.add(note);
            }
        }


        return notelist;
    }

    public int updateNote(Note note) {      //改
        //update the info of an existing note
        // ContentValues /内容值 /处理数据
        ContentValues values = new ContentValues();
        values.put(NoteDatabase.CONTENT, note.getContent());
        values.put(NoteDatabase.TIME, note.getTime());
        values.put(NoteDatabase.MODE, note.getTag());
        // updating row
        return db.update(NoteDatabase.TABLE_NAME, values,
                NoteDatabase.ID + "=?",new String[] { String.valueOf(note.getId())});

    }

    public void removeNote(Note note) {     //删除
        //remove a note according to ID value
        db.delete(NoteDatabase.TABLE_NAME, NoteDatabase.ID + "=" + note.getId(), null);
    }

    public void removeAllNotes() { //删除
        //remove all notes
        db.delete(NoteDatabase.TABLE_NAME, null, null);
        db.execSQL("update sqlite_sequence set seq=0 where name = 'notes'" );
        ///id复位， 从 0 开始增长
    }


}


/*
        c.move(int offset); //以当前位置为参考,移动到指定行  
        c.moveToFirst();    //移动到第一行  
        c.moveToLast();     //移动到最后一行  
        c.moveToPosition(int position); //移动到指定行  
        c.moveToPrevious(); //移动到前一行  
        c.moveToNext();     //移动到下一行  
        c.isFirst();        //是否指向第一条  
        c.isLast();     //是否指向最后一条  
        c.isBeforeFirst();  //是否指向第一条之前  
        c.isAfterLast();    //是否指向最后一条之后  
        c.isNull(int columnIndex);  //指定列是否为空(列基数为0)  
        c.isClosed();       //游标是否已关闭  
        c.getCount();       //总数据项数  
        c.getPosition();    //返回当前游标所指向的行数  
        c.getColumnIndex(String columnName);//返回某列名对应的列索引值  
        c.getString(int columnIndex);   //返回当前行指定列的值
        ————————————————

*/
