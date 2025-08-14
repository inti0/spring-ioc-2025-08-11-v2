package com.ll.framework.ioc;

import com.ll.framework.ioc.annotations.Component;
import com.ll.standard.util.Ut;
import com.ll.standard.util.Ut.str;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.reflections.Reflections;

public class ApplicationContext {

    Map<String, Object> beans;

    public ApplicationContext(String basePackage) {
        beans = new HashMap<>();
        Set<Class<?>> scannedClass = scanWithComponentAnnotation(basePackage);
        putInstanceInBeans(scannedClass);
        System.out.println(beans);
    }

    //TODO : 순환참조 문제 고려
    private void putInstanceInBeans(Set<Class<?>> scannedClass) {
        for (Class<?> clazz : scannedClass) {
            System.out.println("클래스 접근" + clazz.getSimpleName());
            try {
                Field[] declaredFields = clazz.getDeclaredFields();
                System.out.println("필드 갯수" + declaredFields.length);
                List<Field> finalFields = new ArrayList<>();
                for (Field field : declaredFields) {
                    System.out.println("필드 접근, 필드명 : " + field.getName());
                    //TODO : finalField가 4개지만 생성자 매개변수가 3개인 경우는 실패할 것
                    if (Modifier.isFinal(field.getModifiers())) {
                        finalFields.add(field);
                    }
                }
                Class<?>[] finalFieldTypes = finalFields.stream().map(Field::getType).toArray(Class<?>[]::new);
                System.out.println("파이널 필드 갯수" + finalFieldTypes.length);
                Constructor<?> constructor = clazz.getDeclaredConstructor(finalFieldTypes);
                System.out.println("생성자 접근" + constructor);

                //의존성이 없다면 기본생성자로 생성 후 bean에 등록
                if (finalFieldTypes.length == 0) {
                    String simpleName = constructor.getDeclaringClass().getSimpleName();
                    String beanName = str.lcfirst(simpleName);
                    Object instance = constructor.newInstance();
                    System.out.println("객체 생성 " + beanName);
                    beans.putIfAbsent(beanName, instance);
                    System.out.println("=============빈에 " + beanName + " 등록==================");
                } else {
                    //재귀호출
                    putInstanceInBeans(new HashSet<>(List.of(finalFieldTypes)));
                    String simpleName = constructor.getDeclaringClass().getSimpleName();
                    String beanName = str.lcfirst(simpleName);
                    Object instance = constructor.newInstance(Arrays.stream(finalFieldTypes).map(key -> beans.get(Ut.str.lcfirst(key.getSimpleName()))).toArray(Object[]::new));
                    System.out.println("객체 생성 " + beanName);
                    beans.putIfAbsent(beanName, instance);
                    System.out.println("=============빈에 " + beanName + " 등록==================");
                }
            } catch (NoSuchMethodException | InstantiationException | InvocationTargetException |
                     IllegalAccessException e) {
                System.out.println("예외 발생" + e.getMessage());
            }
        }
    }

    private Set<Class<?>> scanWithComponentAnnotation(String basePackage) {
        Reflections reflections = new Reflections(basePackage);
        Set<Class<?>> scannedClass = reflections.getTypesAnnotatedWith(Component.class);
        scannedClass.removeIf(Class::isAnnotation);
        return scannedClass;
    }

    public void init() {
    }

    public <T> T genBean(String beanName) {
        return beans.containsKey(beanName) ? (T) beans.get(beanName) : null;
    }
}
