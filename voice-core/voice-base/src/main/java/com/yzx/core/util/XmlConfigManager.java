package com.yzx.core.util;

import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.PropertyException;
import javax.xml.bind.Unmarshaller;
/**
 * 
 * @author xupiao 2017年6月1日
 *
 */
public class XmlConfigManager {

    private JAXBContext context;

    public XmlConfigManager(Class<?>[] classes) {
        try {
            context = JAXBContext.newInstance(classes);
        } catch (JAXBException e) {
            throw LangUtil.wrapThrow(e);
        }
    }

    protected Marshaller getMarshaller() {
        try {
            Marshaller ret = context.createMarshaller();
            ret.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
//            ret.setProperty(Marshaller.JAXB_NO_NAMESPACE_SCHEMA_LOCATION, "ltts-model.xsd");
            return ret;
        } catch (PropertyException e) {
            throw new IllegalArgumentException(e);
        } catch (JAXBException e) {
            throw new IllegalArgumentException(e);
        }
    }

    protected Unmarshaller getUnmarshaller() {
        try {
            Unmarshaller unmarshaller = context.createUnmarshaller();
            return unmarshaller;
        } catch (JAXBException e) {
            throw new IllegalArgumentException(e);
        }
    }

    //一般的加载模型，不需要代理
    @SuppressWarnings("unchecked")
    public <T> T load(InputStream input) {
        try {
            return (T) this.getUnmarshaller().unmarshal(input);
        } catch (Exception e) {
            throw LangUtil.wrapThrow(e);
        }
    }

    public void save(Object o, OutputStream output) {
        try {
            this.getMarshaller().marshal(o, output);
        } catch (JAXBException e) {
            throw LangUtil.wrapThrow(e);
        }
    }

}
