package org.example.serializer;

import com.caucho.hessian.io.HessianInput;
import com.caucho.hessian.io.HessianOutput;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Hessian 序列化器
 */
public class HessianSerializer implements Serializer {

    //使用ThreadLocal实现线程安全
    private static final ThreadLocal<Kryo> KRYO_THREAD_LOCAL = ThreadLocal.withInitial(()->{
        Kryo kryo = new Kryo();
        return kryo;
    });

    @Override
    public <T> byte[] serialize(T object) throws IOException {
        try (Output output = new Output(4096, -1)) {
            //利用ThreadLocal获取
            KRYO_THREAD_LOCAL.get().writeObject(output, object);
            return output.toBytes();
        } catch (Exception e) {
            throw new RuntimeException("Kryo serialize error", e);
        }
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clazz) {
        try (Input input = new Input(bytes)) {
            //利用ThreadLocal获取
            return KRYO_THREAD_LOCAL.get().readObject(input, clazz);
        } catch (Exception e) {
            throw new RuntimeException("Kryo deserialize error", e);
        }
    }

//    @Override
//    public <T> byte[] serialize(T object) throws IOException {
//        ByteArrayOutputStream bos = new ByteArrayOutputStream();
//        HessianOutput ho = new HessianOutput(bos);
//        ho.writeObject(object);
//        return bos.toByteArray();
//    }
//
//    @Override
//    public <T> T deserialize(byte[] bytes, Class<T> tClass) throws IOException {
//        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
//        HessianInput hi = new HessianInput(bis);
//        @SuppressWarnings("unchecked")
//        T result = (T) hi.readObject(tClass);
//        return result;
//    }
}
