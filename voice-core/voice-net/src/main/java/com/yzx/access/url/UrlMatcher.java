package com.yzx.access.url;

import java.util.List;

/**
 * @author toddf
 * @since Jan 7, 2011
 */
public interface UrlMatcher {
	public boolean matches(String url);

	public UrlMatch match(String url);

	public String getPattern();

	public List<String> getParameterNames();
}