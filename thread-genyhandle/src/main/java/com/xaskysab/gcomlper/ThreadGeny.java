package com.xaskysab.gcomlper;

import java.lang.reflect.Method;

/**
 * Created by XaskYSab on 2017/4/26 0026.
 */

public class ThreadGeny {


    public static void run(Runnable runnable){
        try {
            Class<?>looperCls = Class.forName("android.os.Looper");

            Object looper = looperCls.getDeclaredMethod("getMainLooper").invoke(null);

            Class<?>handlerCls = Class.forName("android.os.Handler");

            Object handler =  handlerCls.getConstructor(looperCls).newInstance(looper);

            Method post = handlerCls.getDeclaredMethod("post",Class.forName("java.lang.Runnable"));

            post.invoke(handler,runnable);


        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
