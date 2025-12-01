package org.example.springboot.rpc.core.registry;


import org.example.springboot.rpc.core.model.ServiceMetaInfo;

import java.util.ArrayList;
import java.util.List;

public class RegistryServiceCache {
    /**
     *  服务缓存
     */
    List<ServiceMetaInfo> serviceMetaInfoList = new ArrayList<>();

    /**
     * 读缓存
     * @return list
     */
    public List<ServiceMetaInfo> readCache(){
        return this.serviceMetaInfoList;
    }

    /**
     * 写缓存
     * @param serviceMetaInfoList
     */
    public void writeCache(List<ServiceMetaInfo> serviceMetaInfoList){
        this.serviceMetaInfoList = serviceMetaInfoList;
    }

    /**
     * 清缓存
     */
    public void clearCache(){
        this.serviceMetaInfoList.clear();
    }

}
