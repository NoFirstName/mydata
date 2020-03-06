package com.bw.configmanager.util;

import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.bw.cache.ehcache.EhcacheUtil;
import com.bw.cache.ehcache.EhcacheUtil.CacheType;
import com.bw.configmanager.constrants.StaticTimetamp;
import com.bw.systemconfig.model.ShwzErrortype;
import com.bw.systemconfig.service.impl.ShwzErrortypeServiceImpl;

/**
 * 缓存初始化
 * @author DELL
 *
 */

public class CacheInit
{
    @Autowired
    ShwzErrortypeServiceImpl shwzErrortypeService;
    /**
     * 缓存类 用户缓存文件进度
     */
    private EhcacheUtil cach = EhcacheUtil.getInstance();
    /**
     * 缓存方式 内存
     */
    private CacheType cachType = CacheType.HEAP;

    private Log log = LogFactory.getLog(getClass());

    public void init()
    {
        ShwzErrortype query = new ShwzErrortype();
        query.setSortName("errorShoworder");
        List<ShwzErrortype> list = shwzErrortypeService.queryList(query);
        long timetamp = new Date().getTime();
        try
        {
            cach.putCacheObj(cachType, "ShwzErrortypeServiceImpl", list);
            cach.putCacheObj(cachType, "ShwzErrortypeServiceImplTime", timetamp);
            StaticTimetamp.setTimes("ShwzErrortypeServiceImplTime", timetamp);
        } catch (Exception e)
        {
            log.error("缓存初始化失败!", e);
            e.printStackTrace();
        }

    }

}
