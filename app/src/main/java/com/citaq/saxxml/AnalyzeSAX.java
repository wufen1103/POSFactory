package com.citaq.saxxml;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import java.io.InputStream;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

public class AnalyzeSAX {

    public static List<MetroItem> readXML(InputStream inputStream) {
        try {
            SAXParserFactory spf = SAXParserFactory.newInstance();
            SAXParser saxParser = spf.newSAXParser();
            XMLContentHandler handler = new XMLContentHandler();
            saxParser.parse(inputStream, handler);
            inputStream.close();
            return handler.getMetroItems();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static List<MetroItem> readXML(InputSource inputSource) {
        try {
            SAXParserFactory spf = SAXParserFactory.newInstance();
            SAXParser saxParser = spf.newSAXParser();
            XMLReader reader = saxParser.getXMLReader();
            XMLContentHandler handler = new XMLContentHandler();
            reader.setContentHandler(handler);
            reader.parse(inputSource);
            inputSource.getByteStream().close();
            return handler.getMetroItems();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
