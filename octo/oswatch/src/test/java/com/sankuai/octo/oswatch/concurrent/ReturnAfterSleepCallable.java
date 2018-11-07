package com.sankuai.octo.oswatch.concurrent;

/**
 * Created by chenxi on 7/1/15.
 */

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

public class ReturnAfterSleepCallable implements Callable<Integer>
{
    private int sleepSeconds;

    private int returnValue;

    public ReturnAfterSleepCallable(int sleepSeconds, int returnValue)
    {
        this.sleepSeconds = sleepSeconds;
        this.returnValue = returnValue;
    }

    @Override
    public Integer call() throws Exception
    {
        System.out.println("begin to execute." + "return value: " + returnValue);

        TimeUnit.SECONDS.sleep(sleepSeconds);

        System.out.println("end to execute.");

        return returnValue;
    }
}
