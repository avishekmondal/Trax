package com.utility;

import com.dd.processbutton.ProcessButton;
import android.content.Context;
import android.os.Handler;
import java.util.Random;

public class ProgressGenerator {

    private Context context;
    private int mProgress;

    public ProgressGenerator(Context context) {
        this.context = context;
    }

    public void progress(final ProcessButton button, int progress) {
        mProgress = progress;
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                if(mProgress == 1000){
                    button.setProgress(1000);
                }
                else if(mProgress < 100){
                    button.setProgress(mProgress);
                    handler.postDelayed(this, generateDelay());
                    mProgress += 10;
                }
                else{
                    mProgress = 10;
                    button.setProgress(mProgress);
                    handler.postDelayed(this, generateDelay());

                }
            }
        }, generateDelay());

    }

    private Random random = new Random();

    private int generateDelay() {
        return random.nextInt(1000);
    }
}
