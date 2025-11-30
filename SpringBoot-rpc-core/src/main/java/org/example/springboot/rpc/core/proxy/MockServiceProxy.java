package org.example.springboot.rpc.core.proxy;

import lombok.extern.slf4j.Slf4j;
import net.datafaker.Faker;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Locale;

@Slf4j
public class MockServiceProxy implements InvocationHandler {

    /**
     *  调用代理
     * */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        //获取请求方法的返回值类型
        Class<?> returnType = method.getReturnType();
        log.info("invoke {}" ,method.getName());
        return getDefaultObject(returnType);
    }

    /**
     *  指定指定类型的默认值
     * */
    private Object getDefaultObject(Class<?> type){
        //设置中文
        Faker faker = new Faker(new Locale("zh-CN"));
        //基本类型
        if (type.isPrimitive()) {
            if (type==boolean.class) {
                return faker.bool().bool();
            }else if(type==int.class){
                return faker.number().numberBetween(0,100);
            }else if(type==short.class){
                return (short)faker.number().numberBetween(100,199);
            }else if(type==long.class){
                return faker.number().randomNumber();
            }
        }
        //对象类型
        return null;
    }
}
