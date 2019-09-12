package com.example.simplememo;

import java.util.HashMap;
import java.util.Map;

public class Memo {

    private int _memoId;
    private String _title;
    private String _body;
    private String _createDatetime;
    private String _updateDatetime;
    private String _createDateWeekTime;
    private String _updateDateWeekTime;

    public Memo() {
        //super();
    }

    public Memo(int memoId, String title, String body, String createDatetime, String updateDatetime) {
        //super();
        this(memoId,title,body,createDatetime,updateDatetime,null,null);
    }

    public Memo(int memoId, String title, String body, String createDatetime, String updateDatetime, String createDateWeekTime, String updateDateWeekTime) {
        //super();
        _memoId = memoId;
        _title = title;
        _body = body;
        _createDatetime = createDatetime;
        _updateDatetime = updateDatetime;
        _createDateWeekTime = createDateWeekTime;
        _updateDateWeekTime = updateDateWeekTime;
    }


    public void setId(int memoId) {
        _memoId = memoId;
    }

    public int getId() {
        return _memoId;
    }

    public String getTitle() {
        return _title;
    }

    public String getBody() {
        return _body;
    }

    public String getCreateDatetime() {
        return _createDatetime;
    }

    public String getUpdateDatetime() {
        return _updateDatetime;
    }

    public String getCreateDateWeekTime() {
        return _createDateWeekTime;
    }

    public String getUpdateDateWeekTime() {
        return _updateDateWeekTime;
    }


    /** フィールドの値を元にメモ・マップを返す **/
    public Map<String,Object> getMemoMap() {
        Map<String,Object> memoMap = new HashMap<>();
        memoMap.put("memoId", _memoId);
        memoMap.put("title", _title);
        memoMap.put("body", _body);
        memoMap.put("createDateWeekTime", _createDateWeekTime);
        memoMap.put("updateDateWeekTime", _updateDateWeekTime);
        return memoMap;
    }


}
