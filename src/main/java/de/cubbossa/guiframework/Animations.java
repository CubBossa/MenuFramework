package de.cubbossa.guiframework;

import com.google.common.base.Strings;

import java.util.function.Function;

public class Animations {

    public static Function<Integer, String> textShift(String text, int spaceCount, boolean left) {
        return integer -> {
            int interval = text.length() + spaceCount;
            int progress = left ? interval - (integer % interval) : integer % interval;
            if (progress == 0) {
                return text;
            }
            String s = text + Strings.repeat(" ", spaceCount);
            String result = s.substring(progress) + s.substring(0, progress);
            return result.substring(0, text.length());
        };
    }

    public static Function<Integer, Double> randomProgress() {
        return integer -> Math.random();
    }

    public static Function<Integer, Double> sinusProgress(int intervalSize, double amplitude) {
        return integer -> Math.sin(Math.PI * 2 / intervalSize * integer) * amplitude + amplitude / 2;
    }

    public static Function<Integer, Double> linearProgress(int intervalSize, double from, double to) {
        return integer -> (integer % (double) intervalSize / intervalSize) * (to - from);
    }

    public static Function<Integer, Double> ziczacProgress(int intervalSize, double from, double to) {
        return integer -> {
            if (integer <= intervalSize / 2.) {
                return linearProgress(intervalSize / 2 + intervalSize % 2, from, to).apply(integer);
            } else {
                return linearProgress(intervalSize / 2, from, to).apply(integer);
            }
        };
    }

    public static Function<Integer, Double> bounceProgress(int intervalSize, double from, double to) {
        return integer -> Math.abs(Math.sin(Math.PI * 2 / intervalSize * integer)) * (to - from) + to + (to < from ? 1 : 0);
    }
}
