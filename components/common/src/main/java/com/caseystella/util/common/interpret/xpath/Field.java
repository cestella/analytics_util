package com.caseystella.util.common.interpret.xpath;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.xpath.XPath;

import java.util.Map;

/**
 * Created by cstella on 9/4/14.
 */
public class Field {
    private String name;
    private String xpath;
    private Map<String, Object> config;
    private XPath path;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getXpath() {
        return xpath;
    }

    public void setXpath(String xpath) {
        this.xpath = xpath;
    }

    public Map<String, Object> getConfig() {
        return config;
    }

    public void setConfig(Map<String, Object> config) {
        this.config = config;
    }

    public String extractXPath(Document doc) throws JDOMException {
        if(path == null)
        {
            path = XPath.newInstance(xpath);
        }
        try {
            Element e = (Element) path.selectSingleNode(doc);
            if(e != null)
            {
                return e.getTextNormalize();
            }
            else
            {
                return null;
            }
        } catch (JDOMException e) {
            return null;
        }
    }
}
