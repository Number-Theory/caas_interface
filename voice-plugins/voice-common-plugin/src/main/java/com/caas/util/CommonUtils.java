package com.caas.util;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.yzx.core.config.ConfigUtils;

/**
 * 
 * @author xupiao 2017年8月19日
 *
 */
public class CommonUtils {

	private static final Logger logger = LogManager.getLogger(CommonUtils.class);

	public static String ip = "";

	// 电话号码类型
	public static final int NATIONAL_PHONE = 0; // 国内号码
	public static final int INTERNATIONAL_PHONE = 1; // 国际号码
	public static final int FIXED_PHONE = 2; // 固话
	public static final int UNKONE_PHONE = 3; // 未知电话号码类型

	public static final Pattern PHONEZONE_PATTERN = Pattern.compile("^(010|02\\d|0[3-9]\\d{2})(\\d{6,8}$)");// 区号判断

	/**
	 * 截取区号或者净号
	 * 
	 * @param phone
	 * @param type1为区号
	 *            2为净号
	 * @return
	 */
	public static String getPhoneOrZone(String phone, int type) {
		String phoneZone = "";
		Matcher ma = PHONEZONE_PATTERN.matcher(phone);
		if (ma.find()) {
			phoneZone = ma.group(type);
		}
		return phoneZone;
	}

	/**
	 * 截取区号,去除0，拼接0086为cityid
	 * 
	 * @param phone
	 * @return
	 */
	public static String getPhoneCityId(String phone) {
		String phoneZone = getPhoneOrZone(phone, 1);
		if (StringUtils.isNotBlank(phoneZone)) {
			phoneZone = "0086" + phoneZone.substring(1);
		}
		return phoneZone;
	}

	/**
	 * 截取电话号码 0086or0087
	 * 
	 * @param mobile
	 * @return String
	 */
	public static String getMobile(String mobile) {
		if (StringUtils.isNotEmpty(mobile)) {
			if (mobile.startsWith("0086") || mobile.startsWith("0087")) {
				return mobile.substring(4, mobile.length());
			}
		}
		return mobile;
	}

	// linux/unix下面获取ip地址通过网卡绑定获取，否则只会获取host文件的ip地址
	// 如果同一网卡绑定了多个ip地址，程序会不识别使用哪个ip，则最好直接读取配置文件的真实ip的方式
	// 此处默认获取第一张网卡的第一个ip地址
	public static String getUnixIP() {
		if (ip.equals("")) {
			try {
				Enumeration<?> e1 = (Enumeration<?>) NetworkInterface.getNetworkInterfaces();
				while (e1.hasMoreElements()) {
					NetworkInterface ni = (NetworkInterface) e1.nextElement();
					Enumeration<?> e2 = ni.getInetAddresses();
					while (e2.hasMoreElements()) {
						InetAddress ia = (InetAddress) e2.nextElement();
						if (ia instanceof Inet6Address)
							continue;
						ip = ia.getHostAddress();
						break;
					}
					break;
				}
			} catch (SocketException e) {
				logger.info("获取ip地址错误：", e);
			}
		}
		return ip;
	}

	/**
	 * 获取某个字符串在一个字符串中出现的频率
	 * 
	 * @param str1
	 * @param str2
	 * @return
	 */
	public static int countSameStr(String str1, String str2) {
		int sum = 0;
		int str1Index = 0;
		while ((str1Index = str1.indexOf(str2)) != -1) {
			sum++;
			str1 = str1.substring(str1Index + 1);
		}
		return sum;
	}

	/**
	 * 判断电话号码类型 2017年6月2日修改前缀为86或87
	 * 
	 * @param phone
	 * @return
	 */
	public static int getPhoneType(String phone) {
		// 第一步：将号码前缀去掉，截取裸号
		// 将"-"，"+"," "替换为""
		phone = phone.replace("-", "").replace("+", "").replace(" ", "");
		// 去除86or87前缀
		if ((phone.matches("^86.*") || phone.matches("^87.*")) && phone.length() != 7 && phone.length() != 8) {
			phone = phone.substring(2);
		}
		// 去除0086or0087
		phone = getMobile(phone);
		// 去除12593 17951 17909 17911前缀
		if (phone.matches("^12593.*|17951.*|17909.*|17911.*")) {
			phone = phone.substring(5);
		}
		String phoneReg = ConfigUtils.getProperty("phoneReg", "^((13[0-9])|(145)|(147)|(15[0-3])|(15[5-9])|(170)|(17[6-8])|(18[0-3])|(18[5-9]))\\d{8}$",
				String.class);
		// 第二步：根据裸号判断类型
		if (phone.matches(phoneReg)) {
			return NATIONAL_PHONE; // 0国内号码
		} else if (phone.matches("^00.*")) {
			return INTERNATIONAL_PHONE; // 1国际号码
		} else if (phone.matches("^((010|02\\d|0[3-9]\\d{2})|(10|2\\d|[3-9]\\d{2}))?\\d{6,8}$")) {
			return FIXED_PHONE; // 2固话
		} else {
			return UNKONE_PHONE; // 3其他类型
		}
	}

	/**
	 * 获取净号，取消前缀
	 * 
	 * @param phone
	 * @return
	 */
	public static String getSimplePhone(String phone) {
		// 第一步：将号码前缀去掉，截取裸号
		// 将"-"，"+"," "替换为""
		phone = phone.replace("-", "").replace("+", "").replace(" ", "");
		// 去除86前缀
		if (phone.matches("^86.*") && phone.length() != 7 && phone.length() != 8) {
			phone = phone.substring(2);
		}
		// 去除0086
		phone = getMobile(phone);
		// 去除12593 17951 17909 17911前缀
		if (phone.matches("^12593.*|17951.*|17909.*|17911.*")) {
			phone = phone.substring(5);
		}
		return phone;
	}

	/**
	 * 判断是否是正确的电话号码
	 * 
	 * @param phone
	 * @return
	 */
	public static boolean isPhoneNum(String phone) {
		boolean isPhoneNum = true;
		if (phone.matches(ConfigUtils.getProperty("phoneReg", "^((13[0-9])|(145)|(147)|(15[0-4])|(15[5-9])|(17[0-5])|(17[6-8])|(18[0-4])|(18[5-9]))\\d{8}$",
				String.class))) {
			isPhoneNum = true; // 0国内号码
		} else if (phone.matches("^00.*")) {
			isPhoneNum = true; // 1国际号码
		} else if (phone.matches("^(12599|010|02\\d|0[3-9]\\d{2}|400)?\\d{6,8}$")) {
			isPhoneNum = true; // 2固话
		} else {
			isPhoneNum = false; // 其他类型,非电话号码
		}
		return isPhoneNum;
	}
}
