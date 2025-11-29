package org.example.springrpcconsumer.serializer;

public interface Serializer {
    <T> byte[] serialize(T obj);

    <T> T deserialize(byte[] bytes, Class<T> classType);
}
