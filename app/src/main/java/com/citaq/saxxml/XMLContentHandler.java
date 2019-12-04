package com.citaq.saxxml;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;
import java.util.List;

public class XMLContentHandler extends DefaultHandler {

    private List<MetroItem> metroItems = null;
    private MetroItem currentMetroItem;
    private String tagName = null;

    public List<MetroItem> getMetroItems() {
        return metroItems;
    }

    @Override
    public void startDocument() throws SAXException {
        metroItems = new ArrayList<>();
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if (localName.equals("item")) {
            currentMetroItem = new MetroItem();
            currentMetroItem.setNameEN(attributes.getValue("id"));
        }
        this.tagName = localName;
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        if (tagName != null) {
            String data = new String(ch, start, length);
            if (tagName.equals("nameEN")) {
                this.currentMetroItem.setNameEN(data);
            } else if (tagName.equals("nameCH")) {
                this.currentMetroItem.setNameCH(data);
            } else if (tagName.equals("packageName")) {
                this.currentMetroItem.setPackageName(data);
            } else if (tagName.equals("className")) {
                this.currentMetroItem.setClassName(data);
            } else if (tagName.equals("position")) {
                this.currentMetroItem.setPosition(Integer.parseInt(data));
            }else if (tagName.equals("color")) {
                this.currentMetroItem.setColor(data);
            }else if (tagName.equals("show")) {
                this.currentMetroItem.setShow(Boolean.parseBoolean(data));
            }else if (tagName.equals("permission")) {
                this.currentMetroItem.setPermission(data.replace(" ",""));
            }
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (localName.equals("item")) {
            metroItems.add(currentMetroItem);
            currentMetroItem = null;
        }
        this.tagName = null;
    }
}