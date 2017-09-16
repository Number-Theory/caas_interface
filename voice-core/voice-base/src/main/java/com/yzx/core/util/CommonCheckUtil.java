package com.yzx.core.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;



/**
 * Created by mzh on 15-7-20.
 * 常用校验工具类
 * 自定义一些常用的校验工具类
 */
public class CommonCheckUtil {
	
	/**
	 * 短信接口使用，待查是否必要
	 * @param phone
	 * @return boolean
	 */
	public static boolean checkTelphoneNumber(String phone) {
		Matcher m = Pattern.compile("^0(([1-9]\\d)|([3-9]\\d{2}))\\d{8}$").matcher(removePrefix(phone.replace(" ", "")));
		return m.find() || phone.startsWith("400");
	}
	
	/**
	 * 短信接口使用，待查是否必要
	 * @param num
	 * @return String
	 */
	public static String removePrefix(String num) {
		if (num != null) {
			try {
				num = num.replaceAll(" ", "");
				num = num.replaceAll("-", "");
				num = num.replace("+", "");
				if (num.matches("^86.*") && num.length() != 7 && num.length() != 8)
					num = num.substring(2);
				if (num.matches("^12593.*|17951.*|17909.*|17911.*")) {
					num = num.substring(5);
				}
				if(num.length() == 12){
					String newNumber = num.substring(1, num.length()); 
					if(isMobilePhone(newNumber)){
						num = newNumber;
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return num;
	}
	
	/**
	 * 短信接口使用，待查是否必要
	 * @param phone
	 * @return
	 */
	public static boolean checkInter(String phone){
		boolean check=false;
		if (isNumber(phone)) {
			if (phone.startsWith("00")&&phone.length()>=10) {
				check=true;
			}
		}
		return check;
	}

    /**
     * 检验是否是正规的电话号码
     * @param mobile 参数
     * @return boolean
     */
    public static boolean isMobilePhone(String mobile){
    	mobile = formatMobilePhone(mobile);
        return Pattern.compile("^1[3,4,5,7,8]\\d{9}$").matcher(mobile != null ? mobile : "").matches();
    }

    /**
     * 检验该字符串是否含有非法字符（只能含常用字符）
     * @param str 参数
     * @return boolean
     */
    public static boolean isCommonCharacter(String str){
        return str.matches("^[a-zA-Z0-9_]+$");
    }

    /**
     * 检验是否是数字（包括小数）
     * @param digit 参数
     * @return boolean
     */
    public static boolean isDigit(String digit){
        return digit.matches("(^0(\\.\\d+)?$)|(^[1-9]\\d*(\\.\\d+)?$)");
    }

    /**
     * 检验是否是整数
     * @param digit 参数
     * @return boolean
     */
    public static boolean isNumber(String digit){
        return digit.matches("^[0-9]+$");
    }
    
    /**
     * 检验是否是0-9的整数
     * @param digit 参数
     * @return boolean
     */
    public static boolean isSingleNumber(String digit){
        return digit.matches("^[0-9]$");
    }
    
    /**
     * 校验是否是整数或者“.”的字符
     * @param str
     * @return boolean
     */
    public static boolean isNumberPoint(String str){
		String regex = "^[0-9.]+$";
		return str.matches(regex);
	}

    /**
     * 格式化电话号码，对电话号码做一些自定义处理
     * @param num 参数
     * @return String
     */
    public static String formatMobilePhone(String num){
        //去除"-","+"字符
        if (num == null)
            return "";
        String oldString = num.replace("-", "");
        oldString = oldString.replace("+", "").replace(" ", "");
        if (oldString.length() < 3) {
            return oldString;
        }
        if(oldString.length() > 4 && oldString.startsWith("0086")){
            oldString = oldString.substring(4);
        }
        //去除86前缀
        if (oldString.matches("^86.*") && oldString.length() != 7 && oldString.length() != 8) {
            oldString = oldString.substring(2);
        }else if(oldString.length() == 7 || oldString.length() == 8){ //没有区号的固话
            return oldString;
        }
        //去除12593 17951 17909 17911前缀
        if (oldString.matches("^12593.*|17951.*|17909.*|17911.*")) {
            oldString = oldString.substring(5);
        }
        //以0起始，以数字串结束，并且8-12位
        if (oldString.matches("^(0){1}[1-9]*$")
                && oldString.matches("[0-9]{8,12}")) {
            return oldString;
        } else {
            if (oldString.startsWith("1")) {
                //以13，14,15,18起始，并且11位数字
                if (oldString.matches("^13.*|14.*|15.*|18.*")) {
                    if (oldString.matches("[0-9]{11}")) {
                        return oldString;
                    } else {
                        return oldString;
                    }
                } else {
                    return oldString;
                }
            }
        }
        return oldString;
    }
    
    
    /**
	 * 判断是否是国籍号码
	 * 
	 * @param phoneNumber
	 *            号码
	 * @return
	 */
	public static Boolean isInternationalPhone(String phoneNumber) { // 判断是否是国际
		boolean isPhone = false;
		if (phoneNumber.matches("^((13[0-9])|(145)|(147)|(15[0-3])|(15[5-9])|(17[0-9])|(18[0-9]))\\d{8}$")) { // 国内号码
			isPhone = false;
		} else if (phoneNumber.matches("^00\\d+$")) { // 国际号码
			isPhone = true;
		} else if (phoneNumber.matches("^400\\d+$")) { // 400号码
			isPhone = false;
		} else if (phoneNumber.matches("^(010|02\\d|0[3-9]\\d{2})?\\d{6,8}$")) { // 固话(带区号或者不带区号)
			isPhone = false;
		}
		return isPhone;
	}
	
	/**
	 * 检测号码类型(手机+固话+400+国际可通过,多合一版本的号码检测工具)
	 * @param phoneNumber
	 * @return
	 */
	public static Boolean checkPhoneType(String phoneNumber) {
		boolean isPhone = false;
		if (phoneNumber.matches("^((13[0-9])|(145)|(147)|(15[0-3])|(15[5-9])|(17[0-9])|(18[0-9]))\\d{8}$")) { // 国内号码
			isPhone = true;
		} else if (phoneNumber.matches("^00\\d+$")) { // 国际号码
			isPhone = true;
		} else if (phoneNumber.matches("^400\\d+$")) { // 400号码
			isPhone = true;
		} else if (phoneNumber.matches("^(010|02\\d|0[3-9]\\d{2})?\\d{6,8}$")) { // 固话(带区号或者不带区号)
			isPhone = true;
		}
		return isPhone;
	}
    
    
    //以下是测试代码
    public static void formatMobilePhone1(String oldString){
    	oldString = oldString + "a";
    	System.out.println("里面oldString = " + oldString);
//        return oldString;
    }
    static String oldString = "b";
    
    static class User {
    	String name;
    	public User(String name) {
    		this.name = name;
    	}
		@Override
		public String toString() {
			return "User [name=" + name + "]";
		}
    }
    
    public static void changeUser(User user){
    	user = new User("lisi");
    	System.out.println("里面user = " + user);
    }
    
    public static void main(String[] args) {
    	/**User user = new User("zhangsan");
    	changeUser(user);
    	System.out.println("user = " + user);
    	
    	
    	formatMobilePhone1(oldString);
    	System.out.println("oldString = " + oldString);
    	
    	CommonCheckUtil com = new CommonCheckUtil();
    	String str = "1.23.";
//    	System.out.println(com.checkNbr(str));
    	
    	//测试电话号码
    	isMobilePhone("1259318665326296");**/
    	/*String toSerNum="008613360511740";
    	toSerNum=CommonCheckUtil.formatMobilePhone(toSerNum);
    	System.out.println(toSerNum);*/
    	
    	//System.out.println(checkTelphoneNumber("13798445566"));
    	System.out.println(isMobilePhone("075583214562"));
	}
}

