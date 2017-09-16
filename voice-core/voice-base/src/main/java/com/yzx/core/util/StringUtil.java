package com.yzx.core.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.yzx.core.util.BeanUtil.PropertySource;

/**
 * * 字符串处理工具
 * <p>
 * 常用的字符串处理方法。
 * </p>
 * 
 * @author xupiao 2017年6月5日
 *
 */
public class StringUtil {
	private static final char[] CHAR = new char[10 + ('Z' - 'A' + 1) + ('z' - 'a' + 1)];
	static {
		int index = 0;
		for (int i = '0'; i <= '9'; i++)
			CHAR[index++] = (char) i;
		for (int i = 'A'; i <= 'Z'; i++)
			CHAR[index++] = (char) i;
		for (int i = 'a'; i <= 'z'; i++)
			CHAR[index++] = (char) i;
	}
	/** Constant for the list delimiter as char. */
	static final char LIST_ESC_CHAR = '\\';

	public static String random(int expectLen, boolean number) {
		char[] ch = new char[expectLen];
		for (int i = 0; i < expectLen; i++) {
			ch[i] = CHAR[((int) (Math.random() * (number ? 10 : CHAR.length)))];
		}
		return new String(ch);
	}

	public static String getStackTrace(Throwable t) {
		StringWriter w = new StringWriter();
		PrintWriter s = new PrintWriter(w);
		t.printStackTrace(s);
		return w.toString();
	}

	public static String capitalFirst(String s) {
		return String.valueOf(s.charAt(0)).toUpperCase() + s.substring(1);
	}

	public static String uncapitalFirst(String s) {
		return String.valueOf(s.charAt(0)).toLowerCase() + s.substring(1);
	}

	/**
	 * 得到类似str1,str2,str3的值
	 * 
	 * @param limit
	 * @param args
	 * @return
	 */
	public static String join(String limit, String... args) {
		if (args == null)
			return "";
		return join(limit, Arrays.asList(args));
	}

	/**
	 * 得到类似str1,str2,str3的值
	 * 
	 * @param limit
	 * @param args
	 * @return
	 */
	public static String join(String limit, List<String> args) {
		if (args == null || args.size() == 0)
			return "";
		if (args.size() == 1)
			return args.get(0);

		StringBuffer ret = new StringBuffer();
		ret.append(args.get(0));
		for (int i = 1; i < args.size(); i++)
			ret.append(limit).append(args.get(i));
		return ret.toString();
	}

	/**
	 * Check that the given String is neither <code>null</code> nor of length 0.
	 * Note: Will return <code>true</code> for a String that purely consists of
	 * whitespace.
	 * <p>
	 * 
	 * <pre>
	 * StringUtils.hasLength(null) = false
	 * StringUtils.hasLength("") = false
	 * StringUtils.hasLength(" ") = true
	 * StringUtils.hasLength("Hello") = true
	 * </pre>
	 * 
	 * @param str
	 *            the String to check (may be <code>null</code>)
	 * @return <code>true</code> if the String is not null and has length
	 * @see #hasText(String)
	 * 
	 * @see org.springframework.util.StringUtils#hasLength
	 */
	public static boolean hasLength(String str) {
		return (str != null && str.length() > 0);
	}

	/**
	 * Check whether the given String has actual text. More specifically,
	 * returns <code>true</code> if the string not <code>null</code>, its length
	 * is greater than 0, and it contains at least one non-whitespace character.
	 * <p>
	 * 
	 * <pre>
	 * StringUtils.hasText(null) = false
	 * StringUtils.hasText("") = false
	 * StringUtils.hasText(" ") = false
	 * StringUtils.hasText("12345") = true
	 * StringUtils.hasText(" 12345 ") = true
	 * </pre>
	 * 
	 * @param str
	 *            the String to check (may be <code>null</code>)
	 * @return <code>true</code> if the String is not <code>null</code>, its
	 *         length is greater than 0, and it does not contain whitespace only
	 * @see java.lang.Character#isWhitespace
	 * 
	 * @see org.springframework.util.StringUtils#hasLength
	 */
	public static boolean hasText(String str) {
		if (!hasLength(str)) {
			return false;
		}
		int strLen = str.length();
		for (int i = 0; i < strLen; i++) {
			if (!Character.isWhitespace(str.charAt(i))) {
				return true;
			}
		}
		return false;
	}

	private static final ThreadLocal<List<Object>> cache = new ThreadLocal<List<Object>>();
	// 递归调用TOSTRING对象的数量：用于控制递归调用总量
	private static final ThreadLocal<Integer> cacheCount = new ThreadLocal<Integer>();

	public static <K, V> String mapToString(Map<K, V> map) {
		return mapToString(map, null);
	}

	/**
	 * @see java.util.AbstractMap#toString
	 */
	public static <K, V> String mapToString(Map<K, V> map, Map<K, V> parent) {
		List<Object> list = cache.get();
		if (list == null)
			cache.set(list = new ArrayList<Object>());
		if (list.size() == 0)
			cacheCount.set(0);
		if (contains(list, map))
			return "[^" + map.hashCode() + "]";
		if (cacheCount.get() != null && cacheCount.get() > 50)
			return "<TOO MANY OBJECTS:" + cacheCount.get() + ">";

		list.add(map);
		cacheCount.set(cacheCount.get() == null ? 1 : cacheCount.get() + 1);
		try {
			return toString(map, parent);
		} finally {
			list.remove(map);
		}
	}

	private static <K, V> String toString(Map<K, V> map, Map<K, V> parent) {
		StringBuilder sb = new StringBuilder();
		sb.append("{");

		Iterator<Entry<K, V>> i = (parent == null ? map.entrySet().iterator() : parent.entrySet().iterator());
		boolean hasNext = i.hasNext();
		while (hasNext) {
			Entry<K, V> e = i.next();
			Object key = e.getKey();
			toString(sb, e.getKey());
			sb.append("=");

			Object value = null;

			if (parent == null) {
				value = e.getValue();
			} else {
				value = map.keySet().contains(key) ? map.get(key) : e.getValue();
			}

			toString(sb, value);
			hasNext = i.hasNext();
			if (hasNext)
				sb.append(", ");
		}

		sb.append("}");
		return sb.toString();
	}

	private static void toString(StringBuilder sb, Object value) {
		Object[] v = ArrayUtil.asArray(value, false);
		if (v != null && v.length == 1)
			sb.append(value);
		else if (v != null && v.length > 20) {
			sb.append(Arrays.asList(Arrays.copyOfRange(v, 0, 20)));
			sb.append("[...]");
		} else
			sb.append(value);
	}

	private static boolean contains(List<Object> cache, Object value) {
		for (Object o : cache)
			if (o == value)
				return true;
		return false;
	}

	public static boolean isExists(Object obj) {
		if (obj == null) {
			return false;
		}
		return true;
	}

	public static String lpadding(String s, int n, String padding) {
		StringBuffer strbuf = new StringBuffer();
		for (int i = 0; i < n - s.length(); i++) {
			strbuf.append(padding);
		}
		strbuf.append(s);
		return strbuf.toString();
	}

	public static String multiPadding(String str, int multi, char ch) {
		if (str.length() % multi == 0)
			return str;
		return StringUtil.padding(str, (str.length() / multi + 1) * multi, ch);
	}

	public static String padding(String str, int len, char ch) {
		for (int i = str.length(); i < len; i++) {
			str += ch;
		}
		return str;
	}

	public static String unPadding(String str, char ch) {
		while (str.charAt(str.length() - 1) == ch)
			str = str.substring(0, str.length() - 1);
		return str;
	}

	public static String space(int n) {
		return space(' ', n);
	}

	public static String space(char space, int n) {
		String ret = "";
		for (int i = 0; i < n; i++)
			ret += space;
		return ret;
	}

	/**
	 * 将对象转换为字符串. 若对象为NULL则返回缺省字符串. 相当于oracle的Nvl函数
	 * 
	 * @param obj
	 *            待转换对象
	 * @param defaultValue
	 *            缺省值
	 * @return 对象的字符串内容(调用toString()方法)
	 */
	public static String nullable(Object obj, String defaultValue) {
		return StringUtil.isEmpty(obj) ? defaultValue : obj.toString();
	}

	/**
	 * 将对象转换为字符串. 若对象为NULL则返回空(0长度)字符串
	 * 
	 * @param obj
	 *            待转换对象
	 * @return 对象的字符串内容(调用toString()方法)
	 */
	public static String nullable(Object obj) {
		return nullable(obj, "");
	}

	/**
	 * 判断对象是否为"空白"(NULL或仅包含空格字符).
	 * 
	 * @param obj
	 *            对象
	 * @return <ul>
	 *         <li><tt>true</tt> 如果对象为null.
	 *         <li><tt>true</tt> 如果对象是"空白"(长度为0或仅包含空格字符).
	 *         <li><tt>true</tt> 如果对象是数组或集合，但个数为0.
	 *         <li><tt>true</tt> 如果对象是数组或集合，且其中的每个元素都是"空白".
	 *         <li><tt>true</tt> 如果对象是Map，返回Map.isEmpty().
	 *         <li><tt>false</tt> 对象非字符串或字符串数组或集合.
	 *         <li><tt>false</tt> 其它情况.
	 *         </ul>
	 * @see #isEmpty(Object)
	 */
	public static boolean isBlank(Object obj) {
		return isEmptyOrBlank(obj, true);
	}

	/**
	 * 判断给定对象是否为"空"(NULL或空(0长度)字符串).
	 * 
	 * @param obj
	 *            对象
	 * @return <ul>
	 *         <li><tt>true</tt> 如果对象为null.
	 *         <li><tt>true</tt> 如果对象是空(0长度)字符串.
	 *         <li><tt>true</tt> 如果对象是数组或集合，但个数为0.
	 *         <li><tt>true</tt> 如果对象是数组或集合，且其中的每个元素都是"空".
	 *         <li><tt>true</tt> 如果对象是Map，返回Map.isEmpty().
	 *         <li><tt>false</tt> 对象非字符串或字符串数组或集合.
	 *         <li><tt>false</tt> 其它情况.
	 *         </ul>
	 */
	public static boolean isEmpty(Object obj) {
		return isEmptyOrBlank(obj, false);
	}

	/**
	 * 判断给定对象是否为"非空".
	 * 
	 * @see #isEmpty(Object)
	 * @param obj
	 *            对象
	 * @return
	 */
	public static boolean isNotEmpty(Object obj) {
		return !isEmptyOrBlank(obj, false);
	}

	/**
	 * 判断给定对象是否为"空"或"空白".
	 * 
	 * @param obj
	 *            对象
	 * @param trim
	 *            是否截除字符串后空格
	 * @return <ul>
	 *         <li><tt>true</tt> 如果对象为null.
	 *         <li><tt>true</tt> 如果对象是空(0长度)字符串.
	 *         <li><tt>true</tt> 如果对象是数组或集合，但个数为0.
	 *         <li><tt>true</tt> 如果对象是数组或集合，且其中的每个元素都是"空"或"空白".
	 *         <li><tt>true</tt> 如果对象是Map，返回Map.isEmpty().
	 *         <li><tt>false</tt> 其它情况.
	 *         </ul>
	 */
	private static boolean isEmptyOrBlank(Object obj, boolean trim) {
		if (obj == null)
			return true;
		if (obj instanceof String) {
			if (((String) obj).length() > 0) {
				return "null".equals(obj);
			}
			String ss = (String) obj;
			return (trim ? ss.trim() : ss).length() == 0;
		}
		if (obj instanceof Object[]) {
			Object[] oo = (Object[]) obj;
			for (int i = 0; i < oo.length; i++)
				if (!isEmptyOrBlank(oo[i], trim))
					return false;
			return true;
		}
		if (obj instanceof Collection) {
			Collection<Object> oo = (Collection<Object>) obj;
			for (Iterator<Object> i = oo.iterator(); i.hasNext();)
				if (!isEmptyOrBlank(i.next(), trim))
					return false;
			return true;
		}
		if (obj instanceof Map) {
			return ((Map) obj).isEmpty();
		}
		return false;
	}

	/**
	 * 分割多行字符串到字符串数组.
	 * <p>
	 * 即分融符为：换行，或回车加换行.
	 * 
	 * @param str
	 *            待分割字符串
	 * @return an array of lines that comprise the string, or null if the string
	 *         specified was null
	 */
	public static String[] splitIntoLines(String str) {
		if (str == null)
			return null;
		BufferedReader br = new BufferedReader(new StringReader(str));

		ArrayList<String> linesList = new ArrayList<String>();

		try {
			String line = br.readLine();
			while (line != null) {
				linesList.add(line);
				line = br.readLine();
			}
		} catch (IOException notGoingToHappenWithAnInMemoryStringReaderEx) {
		}

		return (String[]) linesList.toArray(new String[linesList.size()]);
	}

	/**
	 * 分割字符串(用单个字符). 不使用正则表达式，提高性能。
	 * 
	 * @param s
	 *            待分割字符串
	 * @param delimiter
	 *            分融符
	 * @param trim
	 *            是否截去前后空格
	 * @return List
	 *         <ul>
	 *         <li><tt>空列表</tt> 如果对象为null.
	 *         <li><tt>含一个空字符串的列表</tt> 如果对象是空字符串.
	 *         </ul>
	 */
	public static List<String> split(String s, char delimiter, boolean trim) {
		return split(s, String.valueOf(delimiter), trim);
	}

	/**
	 * 分割字符串(用多个字符). 不使用正则表达式，提高性能。
	 * 
	 * @param s
	 *            待分割字符串
	 * @param delimiter
	 *            分融符
	 * @param trim
	 *            是否截去前后空格
	 * @return List
	 *         <ul>
	 *         <li><tt>空列表</tt> 如果对象为null.
	 *         <li><tt>含一个空字符串的列表</tt> 如果对象是空字符串.
	 *         </ul>
	 */
	public static List<String> split(String s, String delimiter, boolean trim) {
		List<String> ret = new ArrayList<String>();
		if (s == null) {
			return ret;
		}
		int lastIdx = 0;
		int idx = s.indexOf(delimiter);

		while (idx >= 0) {
			String s1 = s.substring(lastIdx, idx);
			if (trim)
				s1 = s1.trim();
			ret.add(s1);

			lastIdx = idx + delimiter.length();
			idx = s.indexOf(delimiter, lastIdx);
		}

		String s1 = s.substring(lastIdx);
		if (trim)
			s1 = s1.trim();
		ret.add(s1);

		return ret;
	}

	/**
	 * 分割字符串(用单个字符)并截去前后空格. 不使用正则表达式，提高性能。 相当于
	 * <code>split(s, delimiter, true)</code>的快捷方式.
	 * <table border="1">
	 * <tr>
	 * <td><strong>源串</strong></td>
	 * <td><strong>结果</strong></td>
	 * </tr>
	 * <tr>
	 * <td>a,&nbsp;,&nbsp;&nbsp;,,&nbsp;b</td>
	 * <td>[a,,,,b]</td>
	 * </tr>
	 * <tr>
	 * <td>a,b&nbsp;&nbsp;&nbsp;c&nbsp;,d</td>
	 * <td>[a,b&nbsp;&nbsp;&nbsp;c,d]</td>
	 * </tr>
	 * </table>
	 * 
	 * @param s
	 *            待分割字符串
	 * @param delimiter
	 *            分融符
	 * @param trim
	 *            是否截去前后空格
	 * @return List
	 *         <ul>
	 *         <li><tt>空列表</tt> 如果对象为null.
	 *         <li><tt>含一个空字符串的列表</tt> 如果对象是空字符串或全空格字符串.
	 *         </ul>
	 */
	public static List<String> split(String s, char delimiter) {
		return split(s, delimiter, true);
	}

	/**
	 * 按空白字符(空格)或逗号分隔字符串. 本方法的返回结果中不包括零长字符串，示例如下：
	 * <table border="1">
	 * <tr>
	 * <td><strong>源串</strong></td>
	 * <td><strong>结果</strong></td>
	 * </tr>
	 * <tr>
	 * <td>a,,,,b</td>
	 * <td>[a, b]</td>
	 * </tr>
	 * <tr>
	 * <td>a,b&nbsp;&nbsp;&nbsp;c,d</td>
	 * <td>[a, b, c, d]</td>
	 * </tr>
	 * </table>
	 * <b>使用了正则表达式，注意性能问题，不能在调用频繁度的地方使用</b>
	 * 
	 * @param input
	 *            源串
	 * @return 字符串列表. <tt>null</tt> 如果源串为null
	 */
	public static List<String> split(String input) {
		return split(input, "[\\s,]+");
	}

	/**
	 * 使用指定的正则表达式分隔字符串. 本方法的返回结果中不包括零长字符串（这是与String.split方法的不同之处）
	 * 
	 * @param input
	 *            源串
	 * @param sep
	 *            正则表达式
	 * @return 字符串列表. <tt>null</tt> 如果源串为null
	 */
	public static List<String> split(String input, String sep) {
		if (input == null)
			return null;
		int index = 0;
		List<String> matchList = new ArrayList<String>();
		Matcher m = Pattern.compile(sep).matcher(input);

		// Add segments before each match found
		while (m.find()) {
			if (index < m.start()) {
				String match = input.subSequence(index, m.start()).toString();
				matchList.add(match);
			}
			index = m.end();
		}

		if (index < input.length())
			matchList.add(input.subSequence(index, input.length()).toString());

		return matchList;
	}

	/**
	 * 将源字符串中所有匹配的字符串替换为提供的新字符串(不使用正则表达式).
	 * 
	 * @param input
	 *            源字符串
	 * @param matchString
	 *            匹配字符串
	 * @param newString
	 *            新字符串
	 * @return 替换后的新字符串
	 */
	public static final String replace(String input, String matchString, String newString) {
		int i = 0;
		if ((i = input.indexOf(matchString, i)) >= 0) {
			char[] line2 = input.toCharArray();
			char[] newString2 = newString.toCharArray();
			int oLength = matchString.length();
			StringBuffer buf = new StringBuffer(line2.length);
			buf.append(line2, 0, i).append(newString2);
			i += oLength;
			int j = i;
			while ((i = input.indexOf(matchString, i)) > 0) {
				buf.append(line2, j, i - j).append(newString2);
				i += oLength;
				j = i;
			}
			buf.append(line2, j, line2.length - j);
			return buf.toString();
		}
		return input;
	}

	/**
	 * 编码HTML标记.
	 * <p>
	 * 将字符串中的HTML关键字符用HTML标记替换 .
	 * <ul>
	 * <li>&amp; &nbsp; &amp;amp;
	 * <li>&lt; &nbsp; &amp;lt;
	 * <li>&gt; &nbsp; &amp;gt;
	 * <li>" &nbsp; &amp;quot;
	 * <li>' &nbsp; &amp;apos;
	 * <li>\ &nbsp; \\
	 * <li>换行 &nbsp; \r
	 * <li>回车 &nbsp; \n
	 * <li>\ &nbsp; \\
	 * </ul>
	 * 
	 * @param input
	 *            待编码字符串.
	 * @return 编码后的字符串.
	 */
	public static final String escapeHTMLTags(String input) {
		// Check if the string is null or zero length -- if so, return
		// what was sent in.
		if (input == null || input.length() == 0) {
			return input;
		}
		// Use a StringBuffer in lieu of String concatenation -- it is
		// much more efficient this way.
		StringBuffer buf = new StringBuffer(input.length());
		char ch = ' ';
		for (int i = 0; i < input.length(); i++) {
			ch = input.charAt(i);
			if (ch == '<') {
				buf.append("&lt;");
			} else if (ch == '>') {
				buf.append("&gt;");
			} else if (ch == '"') {
				buf.append("&quot;");
			} else if (ch == '\'') {
				buf.append("&apos;");
			} else if (ch == '&') {
				buf.append("&amp;");
			} else if (ch == '\\') {
				buf.append('\\');
				buf.append(ch);
			} else if (ch == '\r') {
				buf.append("\\r");
			} else if (ch == '\n') {
				buf.append("\\n");
			} else {
				buf.append(ch);
			}
		}
		return buf.toString();
	}

	/**
	 * 编码HTML标记为浏览器可显示格式.
	 * <p>
	 * 将字符串中的HTML关键字符用HTML标记替换 .将换行符转为&lt;br&gt;
	 * <ul>
	 * <li>&amp; &nbsp; &amp;amp;
	 * <li>&lt; &nbsp; &amp;lt;
	 * <li>&gt; &nbsp; &amp;gt;
	 * <li>" &nbsp; &amp;quot;
	 * <li>' &nbsp; &amp;apos;
	 * <li>换行 &nbsp; &lt;br&gt;
	 * <li>回车 &nbsp;
	 * </ul>
	 * 
	 * @see StringUtil#escapeHTMLTags(String).
	 * @param input
	 *            待编码字符串.
	 * @return 编码后的字符串.
	 */
	public static final String viewHTMLTags(String input) {
		// Check if the string is null or zero length -- if so, return
		// what was sent in.
		if (input == null || input.length() == 0) {
			return input;
		}
		// Use a StringBuffer in lieu of String concatenation -- it is
		// much more efficient this way.
		StringBuffer buf = new StringBuffer(input.length());
		char ch = ' ';
		for (int i = 0; i < input.length(); i++) {
			ch = input.charAt(i);
			if (ch == '<') {
				buf.append("&lt;");
			} else if (ch == '>') {
				buf.append("&gt;");
			} else if (ch == '"') {
				buf.append("&quot;");
			} else if (ch == '\'') {
				buf.append("&apos;");
			} else if (ch == '&') {
				buf.append("&amp;");
			} else if (ch == '\\') {
				// buf.append('\\');
				buf.append(ch);
			} else if (ch == '\r') {
				buf.append("<br>");
			} else if (ch == '\n') {
				// buf.append("\\n");
			} else {
				buf.append(ch);
			}
		}
		return buf.toString();
	}

	/**
	 * Used by the hash method.
	 */
	private static MessageDigest digest = null;

	/**
	 * 用MD5算法加密字符串.
	 * <p>
	 * Hashes a String using the Md5 algorithm and returns the result as a
	 * String of hexadecimal numbers. This method is synchronized to avoid
	 * excessive MessageDigest object creation. If calling this method becomes a
	 * bottleneck in your code, you may wish to maintain a pool of MessageDigest
	 * objects instead of using this method.
	 * <p>
	 * A hash is a one-way function -- that is, given an input, an output is
	 * easily computed. However, given the output, the input is almost
	 * impossible to compute. This is useful for passwords since we can store
	 * the hash and a hacker will then have a very hard time determining the
	 * original password.
	 * <p>
	 * In Jive, every time a user logs in, we simply take their plain text
	 * password, compute the hash, and compare the generated hash to the stored
	 * hash. Since it is almost impossible that two passwords will generate the
	 * same hash, we know if the user gave us the correct password or not. The
	 * only negative to this system is that password recovery is basically
	 * impossible. Therefore, a reset password method is used instead.
	 * 
	 * @param data
	 *            the String to compute the hash of.
	 * @return a hashed version of the passed-in String
	 */
	public synchronized static final String hash(String data) {
		if (digest == null) {
			try {
				digest = MessageDigest.getInstance("MD5");
			} catch (NoSuchAlgorithmException nsae) {
				System.err.println("Failed to load the MD5 MessageDigest. "
						+ "SCU will be unable to function normally.");
				nsae.printStackTrace();
			}
		}
		// Now, compute hash.
		digest.update(data.getBytes());
		return toHex(digest.digest());
	}

	/**
	 * 将字节转换为16进制字符串显示. Turns an array of bytes into a String representing each
	 * byte as an unsigned hex number.
	 * 
	 * @param hash
	 *            待转换字节数组
	 * @return 16进制表示的字符串
	 */
	public static final String toHex(byte hash[]) {
		StringBuffer buf = new StringBuffer(hash.length * 2);
		String stmp = "";

		for (int i = 0; i < hash.length; i++) {
			stmp = (java.lang.Integer.toHexString(hash[i] & 0XFF));
			if (stmp.length() == 1) {
				buf.append(0).append(stmp);
			} else {
				buf.append(stmp);
			}
		}
		return buf.toString();
	}

	/**
	 * 将16进制表示的字符串转换为字节数组.
	 * 
	 * @param hex
	 *            待转换16进制表示的字符串
	 * @return 字节数组
	 */
	public static final byte[] hexToBytes(String hex) {
		if (null == hex) {
			return new byte[0];
		}
		int len = hex.length();
		byte[] bytes = new byte[len / 2];
		String stmp = null;
		try {
			for (int i = 0; i < bytes.length; i++) {
				stmp = hex.substring(i * 2, i * 2 + 2);
				bytes[i] = (byte) Integer.parseInt(stmp, 16);
			}
		} catch (Exception e) {
			return new byte[0];
		}

		return bytes;
	}

	/**
	 * 编码字符串内容以便放到XML文档对象中.
	 * 
	 * @param input
	 *            待编码字符串
	 * @return 编码后的字符串.
	 */
	public static final String escapeForXML(String input) {
		// Check if the string is null or zero length -- if so, return
		// what was sent in.
		if (input == null || input.length() == 0) {
			return input;
		}
		char[] sArray = input.toCharArray();
		StringBuffer buf = new StringBuffer(sArray.length);
		char ch;
		for (int i = 0; i < sArray.length; i++) {
			ch = sArray[i];
			if (ch == '<') {
				buf.append("&lt;");
			} else if (ch == '>') {
				buf.append("&gt;");
			} else if (ch == '"') {
				buf.append("&quot;");
			} else if (ch == '&') {
				buf.append("&amp;");
			} else {
				buf.append(ch);
			}
		}
		return buf.toString();
	}

	/**
	 * 反编码XML内容字符串. 以得到编码前的内容. Unescapes the String by converting XML escape
	 * sequences back into normal characters.
	 * 
	 * @see StringUtil#escapeForXML(String)
	 * @param input
	 *            待反编码字符串
	 * @return 反编码后的字符串.
	 */
	public static final String unescapeFromXML(String input) {
		input = replace(input, "&lt;", "<");
		input = replace(input, "&gt;", ">");
		input = replace(input, "&quot;", "\"");
		return replace(input, "&amp;", "&");
	}

	/**
	 * 转换为紧凑格式的字节数表示
	 * 
	 * @param number
	 *            字节数(单位:B)
	 * @return
	 */
	public static final String compactSizeFormat(String number) {
		String[] end = new String[] { "B", "kB", "MB", "GB" };
		double num = 0;
		int i = 0;
		try {
			num = (double) Integer.parseInt(number);
		} catch (Exception e) {
			num = 0;
		}

		while (num > 1024 && i < end.length) {
			num /= 1024;
			i++;
		}
		NumberFormat nf = NumberFormat.getInstance();
		nf.setMaximumFractionDigits(2);

		return nf.format(num) + " " + end[i];
	}

	/**
	 * 将带字符串按Byte位长度取子字符串. 防止带汉字的字符串长度取错 防止出现Exception
	 * 
	 * @param String
	 *            src 源字符串
	 * @param int beginIndex 起始位置,0为第一位
	 * @param int endIndex 终止位置,1为第一位
	 * @return String
	 * 
	 * @auth LBL
	 * @since 2002.11.27
	 */
	public static String substr(String src, int beginIndex, int endIndex) {
		String dest = "";
		if (src == null) {
			return dest;
		}

		byte[] srcByte = src.getBytes();
		byte[] destByte = null;
		int srclen = srcByte.length;
		if (srclen <= beginIndex || beginIndex >= endIndex) {
			return "";
		}

		if (srclen >= endIndex) {
			destByte = new byte[endIndex - beginIndex];
			System.arraycopy(srcByte, beginIndex, destByte, 0, endIndex - beginIndex);
			dest = new String(destByte);
			return dest;
		} else {
			destByte = new byte[srclen - beginIndex];
			System.arraycopy(srcByte, beginIndex, destByte, 0, srclen - beginIndex);
			dest = new String(destByte);
			return dest;
		}
	}

	/**
	 * 处理汉字字串的substr. 将带字符串按Byte位长度取子字符串 防止带汉字的字符串长度取错 防止出现Exception 防止出现半个汉字
	 * 
	 * @param String
	 *            src 源字符串
	 * @param int beginIndex 起始位置,0为第一位
	 * @param int endIndex 终止位置,1为第一位
	 * @param boolean ifAdd ==true 如果最后是半个汉字，返回长度加一 ==false 如果最后是半个汉字，返回长度减一
	 * 
	 * @return String
	 * 
	 */
	public static String gbsubstr(String src, int beginIndex, int endIndex, boolean ifAdd) {
		String dest = "";
		dest = substr(src, beginIndex, endIndex);
		if (dest.length() == 0) {
			if (ifAdd) {
				dest = substr(src, beginIndex, endIndex + 1);
			} else {
				dest = substr(src, beginIndex, endIndex - 1);
			}
		}
		return dest;
	}

	/**
	 * 处理汉字字串的substr 将带字符串按Byte位长度取子字符串 防止带汉字的字符串长度取错 防止出现Exception 防止出现半个汉字
	 * 
	 * @param String
	 *            src 源字符串
	 * @param int beginIndex 起始位置,0为第一位
	 * @param int endIndex 终止位置,1为第一位 如果最后是半个汉字，返回长度减一
	 * 
	 * @return String
	 * 
	 */
	public static String gbsubstr(String src, int beginIndex, int endIndex) {
		return gbsubstr(src, beginIndex, endIndex, false);
	}

	/**
	 * 取带汉字字串的length 将带字符串按Byte位长度取子字符串 防止带汉字的字符串长度取错
	 * 
	 * @param String
	 *            str 源字符串
	 * 
	 * @return int Byte位长度
	 * 
	 */
	public static int gbStrLen(String str) {
		if (str == null) {
			return 0;
		}
		byte[] strByte;
		try {
			strByte = str.getBytes("GB18030");
		} catch (UnsupportedEncodingException e) {
			strByte = str.getBytes();
		}
		return strByte.length;
	}

	/** 产生重复字符串 */
	public static String replicateStr(char ch, int len) {
		String tmpstr = null;
		char[] tmparr = null;

		if (len <= 0) {
			return "";
		}

		tmparr = new char[len];
		for (int i = 0; i < len; i++) {
			tmparr[i] = ch;
		}
		tmpstr = new String(tmparr);

		return tmpstr;
	}

	/**
	 * 左对齐填充定长字符串 向字符串尾部添加字符填充长度 可以有汉字
	 * */
	public static String lFillStr(String src, char ch, int len) {
		String dest = src;

		int srclen = gbStrLen(src);
		if (srclen > len) {
			dest = gbsubstr(src, 0, len);
			srclen = gbStrLen(dest);
		}

		dest += replicateStr(ch, len - srclen);

		return dest;
	}

	/**
	 * 右对齐填充定长字符串 向字符串前部添加字符 不处理汉字
	 * */
	public static String rFillStr(String src, char ch, int len) {
		return rFillStr(src, ch, len, false);
	}

	public static String rFillStr(String src, char ch, int len, boolean gb) {
		String dest = src;
		int srclen = gb ? gbStrLen(src) : src.length();
		if (srclen > len) {
			dest = gbsubstr(src, 0, len);
			srclen = gb ? gbStrLen(dest) : dest.length();
		}

		dest = replicateStr(ch, len - srclen) + dest;

		return dest;
	}

	/**
	 * 截取小于指定长度部分的字符串(长度按字节计算)(默认utf-8编码)
	 * 
	 * @param s
	 *            源字符串
	 * @param maxlength
	 *            指定最大长度
	 * @return 字符串
	 *         <ul>
	 *         <li><tt>null</tt> 如果指定的编码不支持.
	 *         <li><tt>原值</tt> 如果小于或等于指定长度.
	 *         <li><tt>""</tt> 如果字符串为null.
	 *         <li><tt>新字符串</tt> 字节长度为指定长度，且最后三位替换为"...".
	 *         </ul>
	 */
	public static String maxstr(String s, int maxlength) {
		return maxstr(s, "UTF-8", maxlength);
	}

	/**
	 * 截取小于指定长度部分的字符串
	 * 
	 * @param s
	 *            源字符串
	 * @param encoding
	 *            编码
	 * @param maxlength
	 *            指定最大长度
	 * @return 字符串
	 *         <ul>
	 *         <li><tt>null</tt> 如果指定的编码不支持.
	 *         <li><tt>原值</tt> 如果小于或等于指定长度.
	 *         <li><tt>""</tt> 如果字符串为null.
	 *         <li><tt>新字符串</tt> 字节长度为指定长度，且最后三位替换为"...".
	 *         </ul>
	 */
	public static String maxstr(String s, String encoding, int maxlength) {
		if (s == null)
			return "";
		String ret;
		try {
			byte[] bytes = encoding == null ? s.getBytes() : s.getBytes(encoding);
			if (bytes.length <= maxlength)
				return s;

			int sublength = maxlength - 3;

			ret = new String(bytes, 0, sublength, encoding);

			if (ret.getBytes(encoding).length > sublength)
				ret = _maxstr(s, encoding, sublength);

			return ret + "...";

		} catch (UnsupportedEncodingException e) {
			return null;
		}
	}

	private static String _maxstr(String s, String encoding, int maxlength) {
		try {
			char[] srcStrChars = s.toCharArray();
			int cnt = 0;
			Charset cs = Charset.forName(encoding);
			CharBuffer cb = CharBuffer.allocate(1);
			ByteBuffer resultBuff = ByteBuffer.allocate(maxlength);
			for (int i = 0; i < srcStrChars.length; i++) {
				char c = srcStrChars[i];
				cb.put(c);
				cb.flip();
				ByteBuffer bb = cs.encode(cb);
				cnt += bb.array().length;
				if (cnt > maxlength) {
					break;
				}
				resultBuff.put(bb);
				cb.clear();
			}
			return new String(resultBuff.array(), 0, maxlength, encoding);
		} catch (UnsupportedEncodingException e) {
			return null;
		}
	}

	/**
	 * 比较两个字符串是否相等
	 * 
	 * @param val1
	 * @param val2
	 * @return <ul>
	 *         <li>val1,val2都为null时，true</li>
	 *         <li>val1为null,val2不为null,false</li>
	 *         <li>其它,val1.equals(val2)</li>
	 *         </ul>
	 */
	public static boolean equals(String val1, String val2) {
		if (val1 == null) {
			if (val2 == null)
				return true;
			else
				return false;
		}
		return val1.equals(val2);
	}

	/**
	 * 获取平台缺省的字符集
	 * 
	 * @return JVM中配置使用的字符集
	 */
	public static String getDefaultCharacterEncoding() {
		// Not available on all platforms
		String charEnc = System.getProperty("file.encoding");
		if (charEnc != null)
			return charEnc;

		// JDK1.4 onwards
		charEnc = new java.io.OutputStreamWriter(new java.io.ByteArrayOutputStream()).getEncoding();

		// jdk1.5
		// charEnc = Charset.defaultCharset().name();
		return charEnc != null ? charEnc : "<unknown charset encoding>";
	}

	/**
	 * 将第一个字母大写,其它不变
	 * 
	 * @param name
	 * @return
	 */
	public static String capitalize(String name) {
		if (name == null || name.length() == 0) {
			return name;
		}
		char chars[] = name.toCharArray();
		chars[0] = Character.toUpperCase(chars[0]);
		return new String(chars);
	}

	/**
	 * 将第一个字母小写,其它不变
	 * 
	 * @param name
	 * @return
	 */
	public static String unCapitalize(String name) {
		if (name == null || name.length() == 0) {
			return name;
		}
		char chars[] = name.toCharArray();
		chars[0] = Character.toLowerCase(chars[0]);
		return new String(chars);
	}

	/**
	 * 用提供的属性替换字符串中的${NAME}变量
	 * 
	 * @param value
	 *            待替换的字符串
	 * @param staticProp
	 *            静态属性对照表
	 * @param dynamicProp
	 *            动态属性数组
	 * @return 替换后的字符串
	 */
	public static String replaceProperties(final String value, final Map staticProp, final PropertySource dynamicProp[]) {
		StringBuffer sb = new StringBuffer();
		int prev = 0;
		// assert value!=nil
		int pos;
		while ((pos = value.indexOf("$", prev)) >= 0) {
			if (pos > 0) {
				sb.append(value.substring(prev, pos));
			}
			if (pos == (value.length() - 1)) {// $在最后
				sb.append('$');
				prev = pos + 1;
				break;
			} else if (value.charAt(pos + 1) != '{') {
				sb.append('$');
				// sb.append(value.charAt(pos + 1));
				prev = pos + 1; // XXX
			} else {
				int endName = value.indexOf('}', pos);
				if (endName < 0) {
					sb.append(value.substring(pos));
					prev = value.length();
					continue;
				}
				String n = value.substring(pos + 2, endName);
				String v = null;
				if (n != null && staticProp != null && staticProp.get(n) != null) {
					v = staticProp.get(n).toString();
				}
				if (n != null && v == null && dynamicProp != null) {
					for (int i = 0; i < dynamicProp.length; i++) {
						v = dynamicProp[i].getProperty(n);
						if (v != null) {
							break;
						}
					}
				}
				if (v == null)
					v = "${" + n + "}";

				sb.append(v);
				prev = endName + 1;
			}
		}
		if (prev < value.length())
			sb.append(value.substring(prev));
		return sb.toString();
	}

	/**
	 * 根据当前数据库字符集获取带汉字字串的length 将带字符串按Byte位长度取子字符串 防止带汉字的字符串长度取错 2015.5.7
	 * 
	 * @param String
	 *            源字符串
	 * @param String
	 *            数据库字符集
	 * @return int Byte位长度
	 */
	public static int getStrLenByEncoding(String str, String dbEncoding) {
		if (str == null) {
			return 0;
		}
		byte[] strByte;
		try {
			strByte = str.getBytes(dbEncoding);
		} catch (UnsupportedEncodingException e) {
			strByte = str.getBytes();
		}
		return strByte.length;
	}

	/**
	 * 获取当前字符串的长度，不对中文汉字做特殊处理 2015.5.9
	 * 
	 * @param String
	 *            源字符串
	 * @return int
	 */
	public static int getStrLen(String str) {
		if (str == null)
			return 0;
		return str.length();
	}

	/**
	 * 获取字符串中的不重复的${NAME}变量的名称
	 * 
	 * @param str
	 * @return
	 */
	public static List<String> getProperties(String str) {
		List<String> propertiesList = new ArrayList<String>();
		if (str == null)
			return propertiesList;
		int prev = 0;
		int pos;
		while ((pos = str.indexOf("$", prev)) >= 0) {
			if (pos == (str.length() - 1)) {// $在最后
				break;
			} else if (str.charAt(pos + 1) != '{') {
				prev = pos + 1; // XXX
			} else {
				if (pos == (str.length() - 2))// ${在最后
					break;
				int endName = str.indexOf('}', pos);
				if (endName < 0) {
					continue;
				}
				String n = str.substring(pos + 2, endName);
				if (!propertiesList.contains(n))
					propertiesList.add(n);

				prev = endName + 1;
			}
		}
		return propertiesList;
	}

}
