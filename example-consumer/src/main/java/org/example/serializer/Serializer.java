package org.example.serializer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

public interface Serializer {
    <T> byte[] serialize(T obj);

    <T> T deserialize(byte[] bytes, Class<T> classType);
}
