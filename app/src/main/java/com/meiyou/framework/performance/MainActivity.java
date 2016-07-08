package com.meiyou.framework.performance;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.meiyou.framework.winter.Monitor;

import java.util.Random;

public class MainActivity extends AppCompatActivity {

    volatile Boolean flag = true;
    Runnable runnable;
    Monitor monitor;
    Button btn;
    Button btn2;
    Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btn = (Button) findViewById(R.id.btn);
        btn2 = (Button) findViewById(R.id.btn2);
        assert btn != null;
        mHandler = new Handler();
        monitor = Monitor.Default().setThreshold(2);
        monitor.start();
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (runnable == null) {
                    runnable = new Runnable() {
                        @Override
                        public void run() {
                            Random rand = new Random();
                            int randomNum = rand.nextInt((100 - 1) + 1) + 1;
                            try {
                                Thread.sleep(randomNum);
                                int visible = btn.getVisibility();
                                if (visible == View.VISIBLE) {
                                    btn.setVisibility(View.INVISIBLE);
                                } else {
                                    btn.setVisibility(View.VISIBLE);
                                }
                                //LogUtils.d("MainActivity", "running! " + randomNum);
                                mHandler.postDelayed(runnable, randomNum);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    };
                    mHandler.post(runnable);
                }
            }
        });
        assert btn2 != null;
        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btn.setVisibility(View.VISIBLE);
                flag = false;
                runnable = null;
                monitor.stop();

            }
        });

    }

}
