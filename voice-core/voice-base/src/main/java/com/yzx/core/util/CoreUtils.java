package com.yzx.core.util;

import java.io.IOException;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
/**
 * 
 * @author xupiao 2017年6月1日
 *
 */
public class CoreUtils {

    public static Resource[] findResources(String pattern) {
        PathMatchingResourcePatternResolver rr =new PathMatchingResourcePatternResolver();  
        try {
            return rr.getResources(pattern);
        } catch (IOException e) {
            throw LangUtil.wrapThrow(e);
        }
    }
}
