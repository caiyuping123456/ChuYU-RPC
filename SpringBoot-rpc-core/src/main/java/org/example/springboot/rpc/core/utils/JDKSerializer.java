package org.example.springboot.rpc.core.utils;

import java.io.*;

public class JDKSerializer implements Serializer{

    //序列化
    @Override
    public <T> byte[] serialize(T object) throws IOException {
        //字节流
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        //封装对象流
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
        //序列化
        objectOutputStream.writeObject(object);
        objectOutputStream.close();
        return byteArrayOutputStream.toByteArray();
    }

    //反序列化
    @Override
    public <T> T deserialize(byte[] bytes, Class<T> type) throws IOException {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
        ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
        try {
            return (T) objectInputStream.readObject();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } finally {
            objectInputStream.close();
        }
    }
}
