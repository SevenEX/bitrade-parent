package cn.ztuo.bitrade.saxParse;

import cn.ztuo.bitrade.vo.ImportXmlVO;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.*;
import java.util.List;

public class SAXReaderXMLUtil {
    public static List<ImportXmlVO> getXmlVOS(InputStream inputStream,Long airdropId,int errorIndex,int successCount) throws ParserConfigurationException, SAXException, IOException {
        // 加载文件返回文件的输入流
        //InputStream is = new FileInputStream(new File(fileUrl));
        XmlParseHandler handler = new XmlParseHandler();
        // 1. 得到SAX解析工厂
        SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
        // 2. 让工厂生产一个sax解析器
        SAXParser newSAXParser = saxParserFactory.newSAXParser();
        // 3. 传入输入流和handler，解析
        handler.startParse(airdropId,errorIndex,successCount);
        newSAXParser.parse(inputStream, handler);
        inputStream.close();
        handler.resetParse();
        return handler.getXmlVOS();
    }

}
