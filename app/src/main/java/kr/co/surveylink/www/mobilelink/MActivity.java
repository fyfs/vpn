package kr.co.surveylink.www.mobilelink;

/**
 * Created by jsyang on 2016-09-07.
 */
public class MActivity {

    static private MActivity instance;
    static public synchronized MActivity getInstance(){
        if(instance==null){
            instance=new MActivity();
        }
        return instance;
    }

    public void action(){
    }
}
