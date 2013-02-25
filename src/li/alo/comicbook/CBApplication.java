package li.alo.comicbook;

import android.app.Application;
import android.content.Context;

public class CBApplication extends Application{

    private static Context context;
    private CBEngine engine;

    public void onCreate(){
        super.onCreate();
        context = getApplicationContext();
        
        // Make engine ready for use
        engine = CBEngine.getInstance();
        engine.context = context;
        engine.application = this;
        
    }

    public static Context getContext() {
        return context;
    }
    
}