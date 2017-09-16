package com.yzx.core.util;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author xupiao 2017年6月1日
 *
 */
public class LangUtil {
    /**
     * 用运行时异常包裹抛出对象，如果抛出对象本身就是运行时异常，则直接返回。
     * <p>
     * 如果是 InvocationTargetException，那么将其剥离，只包裹其 TargetException
     * 
     * @param e 抛出对象
     * @return 运行时异常
     */
    public static RuntimeException wrapThrow(Throwable e) {

        if (e instanceof RuntimeException)
            return (RuntimeException) e;
        if (e instanceof InvocationTargetException)
            return wrapThrow(((InvocationTargetException) e).getTargetException());
        return new RuntimeException(e);
    }

    public static RuntimeException wrapThrow(String message, Throwable e) {
        return new RuntimeException(message, e);
    }

    public static RuntimeException wrapThrow(String format, String... args) {
        throw new RuntimeException(String.format(format, args));
    }

    public static RuntimeException wrapThrow(String format, Throwable e, String... args) {
        return new RuntimeException(String.format(format, args), e);
    }

    public static Throwable unwrapThrow(Throwable e) {
        if (e == null)
            return null;
        if (e instanceof InvocationTargetException) {
            InvocationTargetException itE = (InvocationTargetException) e;
            if (itE.getTargetException() != null)
                return unwrapThrow(itE.getTargetException());
        }
        if (e.getCause() != null)
            return unwrapThrow(e.getCause());
        return e;
    }

    public static Throwable unwrapRuntimeException(Throwable e) {
        if (e == null)
            return null;

        if (e instanceof InvocationTargetException) {
            InvocationTargetException itE = (InvocationTargetException) e;
            if (itE.getTargetException() != null)
                return unwrapRuntimeException(itE.getTargetException());
        }

        // 仅解开RuntimeException
        if (e instanceof RuntimeException && e.getCause() != null)
            return unwrapRuntimeException(e.getCause());

        return e;
    }

    /**
     * 获取当前异常堆栈。
     */
    public static List<StackTraceElement> getStackTrace(Throwable t) {
        List<StackTraceElement> ret = new ArrayList<StackTraceElement>();

        while (t != null) {
            StackTraceElement[] stes = t.getStackTrace();
            if (stes != null && stes.length != 0)
                ret.add(stes[0]);
            else
                break;
            t = t.getCause();
        }

        return ret;

    }

    /**
     * 递归获取异常堆栈信息。
     * 
     * @param t
     * @return
     */
    public static String getStackTraceMessage(Throwable t) {
        StringBuilder sb = new StringBuilder("");

        while (t != null) {
            StackTraceElement[] stes = t.getStackTrace();
            if (stes != null && stes.length != 0) {
                String message = t.getLocalizedMessage();
                sb.append(t.getClass().getName() + ":" + message
                        + "\n\t"
                        + "at " + stes[0].getMethodName()
                        + "(" + stes[0].getFileName() + ":" + stes[0].getLineNumber() + ")");
                sb.append("\n");

                if (t.getCause() == null) {
                    for (int i = 1; i < stes.length; i++) {
                        sb.append("\t"
                                + "at " + stes[i].getMethodName()
                                + "(" + stes[i].getFileName() + ":" + stes[i].getLineNumber() + ")");
                        sb.append("\n");
                    }
                }
            } else {
                break;
            }
            t = t.getCause();
        }

        return sb.toString();
    }

    public static <T> T getException(Class<T> targetException, Throwable t) {
        if (t == null)
            return null;
        if (targetException.isAssignableFrom(t.getClass()))
            return (T) t;
        return getException(targetException, t.getCause());
    }

    public static void println(String msg) {
        System.out.println(msg);
    }

}
