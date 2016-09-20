package kr.co.surveylink.www.mobilelink;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.Browser;
import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Date;

/**
 * 인터넷 사용이력을 가져오는 class
 */
public class MBrowserHistory implements IDataHandler{
    static private MBrowserHistory instance;
    static public synchronized MBrowserHistory getInstance(){
        if(instance==null){
            instance=new MBrowserHistory();
        }
        return instance;
    }

    /**
     * 여러건을 모아서 한 번에 저장하기 위해 data 에 담아둠
     */
    private JSONArray data = new JSONArray();

    /**
     * 인터넷 사용 내역을 확인해 data 변수에 넣음
     * @param context context
     */
    public void checkBrowserHistory(Context context) {
        Common.log("MBrowserHistory checkBrowserHistory called");
        //구버전 안드로이드 기본브라우저 (약 S5 이전단말의 기본브라우저)
        /*
        try {
            String[] proj = new String[]{Browser.BookmarkColumns.TITLE, Browser.BookmarkColumns.URL};
            String sel = Browser.BookmarkColumns.BOOKMARK + " = 1"; // 0 = history, 1 = bookmark
            Cursor mCur = getContentResolver().query(Browser.BOOKMARKS_URI, proj, sel, null, null);
            mCur.moveToFirst();
            String title = "";
            String url = "";

            if (mCur.moveToFirst() && mCur.getCount() > 0) {
                boolean cont = true;
                while (mCur.isAfterLast() == false && cont) {
                    title = mCur.getString(mCur.getColumnIndex(Browser.BookmarkColumns.TITLE));
                    url = mCur.getString(mCur.getColumnIndex(Browser.BookmarkColumns.URL));

                    mCur.moveToNext();
                }


            }
        } catch (Exception e) {
        }
        //크롬 브라우저
        try {
            String[] proj = new String[]{Browser.BookmarkColumns.TITLE, Browser.BookmarkColumns.URL, Browser.BookmarkColumns.DATE};
            Uri uriCustom = Uri.parse("content://com.android.browser/bookmarks");
            String sel = Browser.BookmarkColumns.BOOKMARK + " = 1"; // 0 = history, 1 = bookmark
            Cursor mCur = getContentResolver().query(uriCustom, proj, sel, null, null);
            mCur.moveToFirst();
            String title = "";
            String url = "";

            if (mCur.moveToFirst() && mCur.getCount() > 0) {
                boolean cont = true;
                while (mCur.isAfterLast() == false && cont) {
                    title = mCur.getString(mCur.getColumnIndex(Browser.BookmarkColumns.TITLE));
                    url = mCur.getString(mCur.getColumnIndex(Browser.BookmarkColumns.URL));

                }

                mCur.moveToNext();
            }

        } catch (Exception e) {
        }

        //최신단말의 안드로이드 S브라우저 (약 S5이후 단말에서 많이 사용하는것같음)
        try {
            String[] proj = new String[]{Browser.BookmarkColumns.TITLE, Browser.BookmarkColumns.URL, Browser.BookmarkColumns.DATE};
            Uri uriCustom = Uri.parse("content://com.sec.android.app.sbrowser.browser/bookmarks");
            String sel = Browser.BookmarkColumns.BOOKMARK + " = 1"; // 0 = history, 1 = bookmark
            Cursor mCur = getContentResolver().query(uriCustom, proj, sel, null, null);
            mCur.moveToFirst();
            String title = "";
            String url = "";

            if (mCur.moveToFirst() && mCur.getCount() > 0) {
                boolean cont = true;
                while (mCur.isAfterLast() == false && cont) {
                    if (!TextUtils.isEmpty(url)) {
                        title = mCur.getString(mCur.getColumnIndex(Browser.BookmarkColumns.TITLE));
                        url = mCur.getString(mCur.getColumnIndex(Browser.BookmarkColumns.URL));

                    }
                    mCur.moveToNext();
                }

            }
        } catch (Exception e) {
        }
        */
    }

    /**
     * data 에 저장되어 있는 값들을 전송함
     * @param context context
     */
    public void save(Context context){
        /*
        String currentTime = Long.toString(new Date().getTime());
        Object[][] params = {{"list",data.toString()},{"currentTime",currentTime}};
        Common.getInstance().loadData(Common.HttpAsyncTask.CALLTYPE_INSTALLEDAPP_SAVE, context.getString(R.string.url_MBrowserHistory), params, this);
        */
    }

    /**
     * 전송 후 종류에 따른 분기
     * @param calltype 전송의 종류
     * @param str 결과 문자열
     */
    public void dataHandler(int calltype, String str){
        /*
        switch(calltype){
            case Common.HttpAsyncTask.CALLTYPE_BROWSERHISTORY_SAVE:
                saveHandler(str);
                break;
        }
        */
    }

    /**
     * 저장된 값의 전송 결과 처리
     * @param result 결과 문자열
     */
    private void saveHandler(String result) {
        try {
            JSONObject json = new JSONObject(result);
            String err = json.getString("ERR");
            if(err.equals("")){
                data = new JSONArray();
            }
        } catch (Exception e){
            Common.log(e.toString());
        }
    }
}
