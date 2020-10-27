package me.qspeng.distributelock.demo;

import me.qspeng.distributelock.demo.NormalCase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class DemoTest {
    @Autowired
    NormalCase normalCase;

    @Test
    void should_only_one_operation_get_lock() {
        normalCase.run();
    }
}
