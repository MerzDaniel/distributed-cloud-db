package tools.util;

import java.util.List;

public class PerformanceData {

    public List<Entry> entries;

    public static class Entry {
        public String method;
        public String result;
        public long time;
    }
}
