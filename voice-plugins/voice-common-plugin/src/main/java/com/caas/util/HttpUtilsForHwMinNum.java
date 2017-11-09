package com.caas.util;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yzx.core.util.EncryptUtil;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Random;
import java.util.TimeZone;


/**
 * 
 * HTTP协议传输工具类
 * 
 * @author  wWX326236
 * @version  [版本号, 2017-3-2]
 * @see  [相关类/方法]
 * @since  [产品/模块版本]
 */
public class HttpUtilsForHwMinNum
{
    private static Logger logger = LoggerFactory.getLogger(HttpUtilsForHwMinNum.class);
    
    /** The Constant UTC. */
    private static final String UTC = "UTC";
    
    /** The Constant UTC_FORMAT. */
    private static final String UTC_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";
    
    /** The Constant CHAR_ARRAY. */
    private static final char CHAR_ARRAY[] = new char[] {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b',
        'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x',
        'y', 'z', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T',
        'U', 'V', 'W', 'X', 'Y', 'Z'};

    /**
     * 向指定 URL 发送POST方法的请求
     * 
     * @param url  请求地址
     * @param jsonBody   消息体
     * @return
     * @see [类、类#方法、类#成员]
     */
    public static String sendPost(String appKey, String appSecret, String url, String jsonBody)
    {
        DataOutputStream out = null;
        BufferedReader in = null;
        StringBuffer result = new StringBuffer();

        HttpURLConnection connection = null;
        try
        {
            //申明请求地址
            URL realUrl = new URL(url);
            
            connection = (HttpURLConnection)realUrl.openConnection();

            //设置是否向httpUrlConnection输出, 默认情况下是false; 
            //因为这个是post请求，参数要放在   http正文内，因此需要设为true;  
            connection.setDoOutput(true);
            
            // 设置是否从httpUrlConnection读入，默认情况下是true;   
            connection.setDoInput(true);
            
            // 设置请求方式
            connection.setRequestMethod("POST");
            
            // 设置接收数据的格式 
            connection.setRequestProperty("Accept", "application/json");
            
            // 设置发送数据的格式
            connection.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
            
            // 设置数据的授权
            connection.setRequestProperty("Authorization",
                "WSSE realm=\"SDP\", profile=\"UsernameToken\", type=\"AppKey\"");
            
            //设置WSSE
            generateXWsse(connection, appKey, appSecret);
            
            logger.info("RequestBody is : " + jsonBody);
            
            connection.connect();
            out = new DataOutputStream(connection.getOutputStream());
            
            //写json数据
            out.writeBytes(jsonBody);
            out.flush();
            out.close();
            
            // 定义BufferedReader输入流来读取URL的响应
            in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;
            while ((line = in.readLine()) != null)
            {
                result.append(line);
            }
        }
        catch (Exception e)
        {
            logger.info("Send Post request catch exception: " + e.toString());
        }
        //使用finally块来关闭输出流、输入流
        finally
        {
            IOUtils.closeQuietly(out);
            IOUtils.closeQuietly(in);
            if (null != connection)
            {
                IOUtils.close(connection);
            }
        }

        return result.toString();
    }

    /**
     *  GET请求
     * @param url 请求地址
     * @param params 参数
     * @return
     */
    public static String sendGet(String appKey, String appSecret, String url, String params)
    {
        BufferedReader in = null;
        StringBuffer result = new StringBuffer();
        HttpURLConnection connection = null;
        try
        {
            String realPath = url + (StringUtils.isEmpty(params) ? "" : params);
            URL realUrl = new URL(realPath);
            
            connection = (HttpURLConnection)realUrl.openConnection();
            connection.setDoInput(true);
            
            // 设置请求方式
            connection.setRequestMethod("GET");
            
            // 设置接收数据的格式 
            connection.setRequestProperty("Accept", "application/json");
            
            // 设置发送数据的格式
            connection.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
            
            // 设置数据的授权
            connection.setRequestProperty("Authorization",
                "WSSE realm=\"SDP\", profile=\"UsernameToken\", type=\"AppKey\"");
            
            //设置X-WSSE
            generateXWsse(connection, appKey, appSecret);
            
            connection.connect();
            
            // 定义BufferedReader输入流来读取URL的响应
            in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;
            while ((line = in.readLine()) != null)
            {
                result.append(line);
            }
        }
        catch (Exception e)
        {
            logger.info("Send GET request catch exception: " + e.toString());
        }
        //使用finally块来关闭输出流、输入流
        finally
        {
            IOUtils.closeQuietly(in);
            if (null != connection)
            {
                IOUtils.close(connection);
            }
        }
        return result.toString();
    }
    
    
    /**
     * 短信  向指定 URL 发送POST方法的请求
     * @param url  请求地址
     * @param parameter 其他参数
     * @return
     * @see [类、类#方法、类#成员]
     */
    public static String sendSMSPost(String appKey, String appSecret, String url, String parameter)
    {
        DataOutputStream out = null;
        BufferedReader in = null;
        StringBuffer result = new StringBuffer();
        HttpURLConnection connection = null;
        try
        {
            //申明请求地址
            URL realUrl = new URL(url);
            
            connection = (HttpURLConnection)realUrl.openConnection();
            
            //设置是否向httpUrlConnection输出, 默认情况下是false; 
            //因为这个是post请求，参数要放在   http正文内，因此需要设为true;  
            connection.setDoOutput(true);
            
            // 设置是否从httpUrlConnection读入，默认情况下是true;   
            connection.setDoInput(true);
            
            // 设置请求方式
            connection.setRequestMethod("POST");
            
            // 设置接收数据的格式 
            connection.setRequestProperty("Accept", "application/json");
            
            // 设置发送数据的格式
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            
            // 设置数据的授权
            connection.setRequestProperty("Authorization",
                "WSSE realm=\"SDP\", profile=\"UsernameToken\", type=\"AppKey\"");
            
            //设置WSSE
            generateXWsse(connection, appKey, appSecret);
            
            logger.info("RequestBody is : " + parameter);
            
            connection.connect();
            out = new DataOutputStream(connection.getOutputStream());
            
            //写json数据
            out.writeBytes(parameter);
            out.flush();
            out.close();
            
            // 定义BufferedReader输入流来读取URL的响应
            in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;
            while ((line = in.readLine()) != null)
            {
                result.append(line);
            }
        }
        catch (Exception e)
        {
            logger.info("Send Post request catch exception: " + e.toString());
        }
        //使用finally块来关闭输出流、输入流
        finally
        {
            IOUtils.closeQuietly(out);
            IOUtils.closeQuietly(in);

            if (null != connection)
            {
                IOUtils.close(connection);
            }
        }

        return result.toString();
    }

    /**
     * 生成XWSSE头
     * @param connection http连接对象
     * @param aepAppKey appkey
     * @param aepAppSecret appsecret明文
     */
    private static void generateXWsse(HttpURLConnection connection, String aepAppKey, String aepAppSecret)
    {
        //根据AEP校验方式，封装请求参数
        String pwdDigest = "";
        String created = getUtcTime();
        String nonce = genNonce(25);
        String appSecret = aepAppSecret;
        //加密操作
        try
        {
            // 例如：nonce = "123456789abcdefg", created="2017-03-15T10:22:10Z", appSecret = "demo";
            // 那么要加密的串为：123456789abcdefg2017-03-15T10:22:10Zdemo
            pwdDigest = EncryptUtil.base64(EncryptUtil.SHA256(nonce + created + appSecret));
        }
        catch (NoSuchAlgorithmException e)
        {
            logger.info("pwdDigest can not be base64 and sha256" + e.toString());
        }
        
        String xWsse =
            "UsernameToken Username=\"" + aepAppKey + "\",PasswordDigest=\"" + pwdDigest + "\",Nonce=\"" + nonce
                + "\",Created=\"" + created + "\"";
        
        //设置WSSE
        connection.setRequestProperty("X-WSSE", xWsse);
    }
    

    /**
     * 获得UTC时间
     * 
     * @return
     */
    private static String getUtcTime()
    {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat(UTC_FORMAT);
        sdf.setTimeZone(TimeZone.getTimeZone(UTC));
        return sdf.format(calendar.getTime());
    }
    
    /**
     * 发送请求时生成的一个随机数
     * 
     * @param length 生成长度
     * @return
     */
    private static String genNonce(int length)
    {
        Random random = new Random();
        StringBuffer sb = new StringBuffer();
        int nextPos = CHAR_ARRAY.length;
        int tmp = 0;
        for (int i = 0; i < length; i++)
        {
            tmp = random.nextInt(nextPos);
            sb.append(CHAR_ARRAY[tmp]);
        }
        
        return sb.toString();
    }
}
