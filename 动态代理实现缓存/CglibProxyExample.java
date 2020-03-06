package com.bw.configmanager.util;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Date;

import org.apache.poi.hssf.record.formula.functions.T;
import org.springframework.transaction.annotation.Transactional;

import com.bw.cache.ehcache.EhcacheUtil;
import com.bw.cache.ehcache.EhcacheUtil.CacheType;
import com.bw.configmanager.constrants.StaticTimetamp;
import com.bw.core.model.IBaseModel;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

public class CglibProxyExample implements MethodInterceptor
{
    private EhcacheUtil cach = EhcacheUtil.getInstance();

    private CacheType cachType = CacheType.HEAP;

    public Object getProxy(Class<?> cls)
    {
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(cls);
        enhancer.setCallback(this);
        return enhancer.create();
    }

    /**
     * 代理对象
     * 方法
     * 方法参数
     * 方法代理
     */
    @SuppressWarnings("unchecked")
    @Override
    public Object intercept(Object proxy, Method method, Object[] args, MethodProxy methodProxy) throws Throwable
    {
        Object result;
        if (method.isAnnotationPresent(Transactional.class))
        {
            Long timestamp = new Date().getTime();
            StaticTimetamp.setTimes(proxy.getClass().getSimpleName(), timestamp);

            result = methodProxy.invokeSuper(proxy, args);//
            Class<?> c = Class.forName(proxy.getClass().getGenericSuperclass().getTypeName());
            Type t = c.getGenericSuperclass();
            ParameterizedType p = (ParameterizedType) t;
            Class<?> cla = (Class<T>) p.getActualTypeArguments()[0];

            Object query;
            try
            {
                query = cla.newInstance();
                Method queryList = proxy.getClass().getMethod("queryList", IBaseModel.class);
                method.setAccessible(true);
                cach.putCacheObj(cachType, c.getSimpleName(), queryList.invoke(proxy, query));//添加缓存
                cach.putCacheObj(cachType, c.getSimpleName() + "Time", timestamp);//更新时间戳
            } catch (Exception e)
            {
                e.printStackTrace();
            }
        } else
        {
            result = methodProxy.invokeSuper(proxy, args);
        }
        return result;
    }

}
