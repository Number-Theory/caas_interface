package com.caas.model.xmlParse;

import com.caas.model.GxInfo;
import com.yzx.core.util.XMLUtil;

public class XmlParseTest {
	public static void main(String[] args) {
		GxInfo gxInfo = new GxInfo();
		gxInfo.setBindId("1234567");
		
		System.out.println(XMLUtil.convertToXml(gxInfo));
	}
}
