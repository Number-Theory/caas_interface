package com.yzx.core.util;

import java.util.HashMap;
import java.util.Map;

/**
 * 
 * @author xupiao 2017年9月21日
 *
 */
public class GenericTokenParserUtil {

	private final String openToken;
	private final String closeToken;
	private final TokenHandler handler;

	public GenericTokenParserUtil(String openToken, String closeToken, TokenHandler handler) {
		this.openToken = openToken;
		this.closeToken = closeToken;
		this.handler = handler;
	}

	public String parse(String text) {
		StringBuilder builder = new StringBuilder();
		if (text != null) {
			String after = text;
			int start = after.indexOf(openToken);
			int end = after.indexOf(closeToken, start + 1);
			while (start > -1) {
				if (end > start) {
					String before = after.substring(0, start);
					String content = after.substring(start + openToken.length(), end);
					String substitution;

					// check if variable has to be skipped
					if (start > 0 && text.charAt(start - 1) == '\\') {
						before = before.substring(0, before.length() - 1);
						substitution = new StringBuilder(openToken).append(content).append(closeToken).toString();
					} else {
						substitution = handler.handleToken(content);
					}

					builder.append(before);
					builder.append(substitution);
					after = after.substring(end + closeToken.length());
				} else if (end > -1) {
					String before = after.substring(0, end);
					builder.append(before);
					builder.append(closeToken);
					after = after.substring(end + closeToken.length());
				} else {
					break;
				}
				start = after.indexOf(openToken);
				end = after.indexOf(closeToken, start + 1);
			}
			builder.append(after);
		}
		return builder.toString();
	}

	public static interface TokenHandler {
		String handleToken(String content);
	}
	
	public static void main(String[] args) {
		final Map<String, Object> params = new HashMap<String, Object>();
		params.put("name", "xupiao");
		params.put("code", 1234);
		String templateContent = "{name} 您好，您的验证码是:{code}";
		GenericTokenParserUtil parser = new GenericTokenParserUtil("{", "}", new TokenHandler() {
			@Override
			public String handleToken(String content) {
				return String.valueOf(params.get(content));
			}
	    });
		System.out.println(templateContent);
		templateContent = parser.parse(templateContent);
		System.out.println(templateContent);
	}
}
