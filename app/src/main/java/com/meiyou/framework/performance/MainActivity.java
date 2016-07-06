package com.meiyou.framework.performance;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.meiyou.framework.winter.Monitor;
import com.meiyou.sdk.core.LogUtils;

import java.util.Random;

public class MainActivity extends AppCompatActivity {
    boolean flag = true;
    Runnable runnable;
    Monitor monitor;
    Button btn;
    Button btn2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btn = (Button) findViewById(R.id.btn);
        btn2 = (Button) findViewById(R.id.btn2);
        assert btn != null;
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (runnable == null) {
                    runnable = new Runnable() {
                        @Override
                        public void run() {
                            while (flag) {
                                Random rand = new Random();
                                int randomNum = rand.nextInt((30 - 1) + 1) + 1;
                                try {
                                    Thread.sleep(randomNum);
                                    if (randomNum > 20) {
                                        int visible = btn.getVisibility();
                                        switch (visible) {
                                            case View.INVISIBLE:
                                                btn.setVisibility(View.VISIBLE);
                                                break;
                                            case View.VISIBLE:
                                                btn.setVisibility(View.INVISIBLE);
                                                break;
                                        }

                                        LogUtils.d("MainActivity", "running! " + randomNum);
                                    }

                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                            LogUtils.d("MainActivity", "runnable finish");
                        }
                    };
                    runnable.run();
                }
            }
        });
        assert btn2 != null;
        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                flag = false;
                runnable = null;
                monitor.stop();
            }
        });
        monitor = new Monitor();
        monitor.start();
    }

}
