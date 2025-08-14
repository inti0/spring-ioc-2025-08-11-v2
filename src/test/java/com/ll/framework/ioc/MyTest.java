package com.ll.framework.ioc;

import com.ll.framework.ioc.annotations.Component;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.reflections.Reflections;

public class MyTest {

    @Test
    @DisplayName("getTypesAnnotatedWith 실험")
    public void test1() throws ClassNotFoundException {
        new ApplicationContext("com.ll");
    }
}
