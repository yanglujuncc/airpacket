package ylj.demo.network.netty4;

import java.util.Date;

public class DemoUnixTime {

    private final long value;

    public DemoUnixTime() {
        this(System.currentTimeMillis() / 1000L + 2208988800L);
    }

    public DemoUnixTime(long value) {
        this.value = value;
    }

    public long value() {
        return value;
    }

    @Override
    public String toString() {
        return new Date((value() - 2208988800L) * 1000L).toString();
    }
}
