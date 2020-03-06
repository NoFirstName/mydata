package com.bw.configmanager.util;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.poi.hssf.record.formula.functions.T;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.transaction.annotation.Transactional;

import com.bw.cache.ehcache.EhcacheUtil;
import com.bw.cache.ehcache.EhcacheUtil.CacheType;
import com.bw.configmanager.annotation.WithCache;
import com.bw.configmanager.constrants.StaticTimetamp;
import com.bw.core.model.BaseModel;
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
        Object result = null;
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
        } else if (method.isAnnotationPresent(WithCache.class))
        {//需要添加缓存
            result = addCacheAround(proxy, method, args, methodProxy);
        } else
        {
            result = methodProxy.invokeSuper(proxy, args);
        }
        return result;
    }

    /**
     * @throws Exception 
     * 
     */
    private Object addCacheAround(Object proxy, Method method, Object[] args, MethodProxy methodProxy) throws Exception
    {
        LocalVariableTableParameterNameDiscoverer u = new LocalVariableTableParameterNameDiscoverer();
        String[] paraNameArr = u.getParameterNames(method);//获取方法参数名
        WithCache withCache = method.getAnnotation(WithCache.class);
        String filterfield = withCache.field();//依据的过滤字段
        String key = withCache.key();
        int index = 0;//过滤参数所在参数列表的位置，获取下标后，从args数组中获取其值。
        if (!(key.equals("") && paraNameArr.length > 1))
        {
            for (int i = 0; i < paraNameArr.length; i++)
            {
                if (paraNameArr[i].equals(key))
                {
                    index = i;
                    break;
                }
            }
        } else
        {//注释没有填写key,且参数个数又大于1,报错。
            throw new Exception("参数列表参数个数为多个时,@WithCache注释需指定过滤的参数。");
        }
        Object value = args[index];//过滤的value值。

        Class<?> clz = ((Class<?>) proxy.getClass().getGenericSuperclass());//目标对象的类对象。
        String name = getClass().getSimpleName();//缓存的key

        Object allData = null;
        if (StaticTimetamp.isNeedUpdate(name + "Time"))//从缓存中读取数据
        {
            allData = cach.getCacheObj(cachType, name);
        } else//从数据库中更新数据
        {
            ParameterizedType p = (ParameterizedType) clz.getGenericSuperclass();//含泛型的类型
            Class<? extends BaseModel> modelClass = (Class) p.getActualTypeArguments()[0];//对应的Model的类
            Object query = modelClass.newInstance();//空的过滤model,即获取所有数据。

            Method queryList = clz.getMethod("queryList", BaseModel.class);
            allData = queryList.invoke(proxy, query);//获取所有缓存数据
            cach.putCacheObj(cachType, name, allData);//数据存入缓存
            long timestamp = new Date().getTime();
            StaticTimetamp.setTimes(name + "Time", timestamp);
            cach.putCacheObj(cachType, name + "Time", timestamp);
        }

        if (value == null)
        {
            return allData;
        } else
        {
            filterfield = "get" + filterfield.substring(0, 1).toUpperCase()
                    + filterfield.substring(1, filterfield.length());//
            Method getFieldMethod = clz.getMethod(filterfield);
            List<BaseModel> list = new ArrayList<BaseModel>();
            for (BaseModel se : (ArrayList<? extends BaseModel>) allData)
            {
                if (getFieldMethod.invoke(se).equals(value))
                {
                    list.add(se);
                }
            }
            return list;
        }

    }

}
