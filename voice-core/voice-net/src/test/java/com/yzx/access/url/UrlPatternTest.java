package com.yzx.access.url;

import org.junit.Test;

/**
 * 
 * @author xupiao 2017年7月4日
 *
 */
public class UrlPatternTest {
	@Test
	public void testMatches() {
		UrlMatcher p = new UrlPattern("/xxx/{a_id}/yyy/{b_id}");
		System.out.println(p.getPattern() + "是否匹配:/xxx/123/yyy/asdfasdfasdf:【" + p.matches("/xxx/123/yyy/asdfasdfasdf")
				+ "】");
		System.out.println(p.getPattern() + "是否匹配:/xxx/123/asdfasdfasdf:【" + p.matches("/xxx/123/asdfasdfasdf") + "】");
	}

	@Test
	public void testGetParameterNames() {
		UrlMatcher p = new UrlPattern("/xxx/{a_id}/yyy/{b_id}");
		System.out.println(p.getPattern() + "匹配:/xxx/{a_id}/yyy/{b_id}获取到的参数名为:【" + p.getParameterNames() + "】");
	}

	@Test
	public void testMatch() {
		UrlMatcher p = new UrlPattern("/xxx/{version}/yyy/{sid}");
		System.out.println(p.getPattern() + "匹配:/xxx/456/yyy/asdfasdfasdf获取到的参数值为:【"
				+ p.match("/xxx/123/yyy/asdfasdfasdf").getParameters() + "】");
	}
}
