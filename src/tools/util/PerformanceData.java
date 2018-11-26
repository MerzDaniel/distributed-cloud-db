package tools.util;

import java.util.ArrayList;
import java.util.List;

public class PerformanceData {

    public List<Entry> entries = new ArrayList<>();
    public long elapsedTime;

    public static class Entry {
        public String method;
        public String result;
        public long time;
    }
}
