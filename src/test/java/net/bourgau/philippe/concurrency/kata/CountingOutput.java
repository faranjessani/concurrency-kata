package net.bourgau.philippe.concurrency.kata;

import org.fest.assertions.api.Assertions;
import org.fest.assertions.api.IntegerAssert;

import java.util.concurrent.atomic.AtomicInteger;

public class CountingOutput implements Output {
    private AtomicInteger messageCount = new AtomicInteger();

    @Override
    public void write(String line) {
        messageCount.incrementAndGet();
    }

    void reset() {
        messageCount.set(0);
    }

    IntegerAssert assertMessageCount() {
        return Assertions.assertThat(messageCount.get());
    }
}
