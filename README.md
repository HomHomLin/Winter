#### winter
##### 性能监测工具
为了精确监控Android的卡顿丢帧现象,而不是靠主观感受,提供本工具.
##### 原理
固定速率采样`mainThread`的堆栈情况,如果发现这一帧的绘制时间超过了16ms,
输出这一帧的堆栈情况:开始时间,过了16ms时刻,结束时间 的堆栈情况.
##### 用法
在代码里调用下面方法开启和关闭检测:
```java
 Monitor.getInstance().init(5,true).start();
 
 ...
 Monitor.getInstance().stop();
```

方法说明:
```java
    /**
     * 启动监控
    */
    public void start() ;
    
    /**
     *
     * @param threshold  设置输出日志的阈值,
     *                   比如设置为10,日志会追踪所有丢帧超过10帧的场景
     * @param noSystemCode  堆栈是否需要含有Android系统代码
     * @return
     */
    public Monitor init(int threshold,boolean noSystemCode) ;
    
    
    /**
     * 结束监控
     */
    public void stop() ;    
```
##### 查看日志
监控完毕,会在手机的/sdcard/winter/monitor/ 目录下生成相应的日志文件 ${date}.log ,文件内容格式类似于:
```java

tag:tag-37274828
type:sample
time:37274611 ms
 sample delta:16ms
com.meiyou.slidingmenu.lib.CustomViewBehind.onMeasure(CustomViewBehind.java:144)

```
tag 用于标示改信息属于哪一帧            
type表示是16ms的那一刻的堆栈情况,还是帧开始结束的那一刻情况,由于可能丢N帧,所以 type:sample的信息可能存在N个.             
time 是那一刻的时间                
sample delta 表示采样速率,一般是16ms             
最后就是在时间time 主线程的堆栈,如果`noSystemCode`为true 则不包含Android系统相关代码              
##### 注意
在某些场景下,打印出的堆栈可能不是最耗时的调用,不过已经打印出开始时间和结束时间的堆栈,可以查看自己的业务代码再做判定.          
