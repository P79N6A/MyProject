package com.sankuai.octo.statistic.metrics;

/**
 * Created by zava on 15/9/24.
 * 压缩时间
 * <p/>
 * 0~2000ms      0s  ~ 2s      每1ms   一个value, 共 2000 个时间点
 * 2000~50000ms  2s  ~ 50s     每10ms  一个value, 共 4800 个时间点
 * 50s~70s       50s ~ 70s     每100ms 一个value, 共 200  个时间点
 * 70s 一个点
 */
public class TimeBucketUtil {

    private final static int TIME_1 = 2000;
    private final static int TIME_2 = 50000;
    private final static int TIME_3 = 70000;

    private final static int STEP_TIME_1 = 10;
    private final static int STEP_TIME_2 = 100;

    /**
     * 把时间压缩 ,用一个时间点代表 一组时间
     *
     * @param value
     * @return
     */
    public static int compress(final int value) {
        if (value < 0) {
            return 0;
        }
        if (value < TIME_1) {
            return value;
        } else if (value < TIME_2) {
            return value / STEP_TIME_1 * STEP_TIME_1;
        } else if (value < TIME_3) {
            return value / STEP_TIME_2 * STEP_TIME_2;
        }
        return TIME_3;
    }
}
