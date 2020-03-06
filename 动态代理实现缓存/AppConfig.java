package com.bw.configmanager.txn;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.bw.configmanager.util.CglibProxyExample;
import com.bw.systemconfig.service.impl.ShwzErrortypeServiceImpl;

@Configuration
public class AppConfig
{
    @Bean(name = "shwzErrortypeService")
    public ShwzErrortypeServiceImpl shwzErrortypeService()
    {
        CglibProxyExample cpe = new CglibProxyExample();
        ShwzErrortypeServiceImpl obj = (ShwzErrortypeServiceImpl) cpe.getProxy(ShwzErrortypeServiceImpl.class);
        return obj;
    }

}