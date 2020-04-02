package cn.ztuo.bitrade.saxParse;

import cn.ztuo.bitrade.service.MemberWalletService;
import cn.ztuo.bitrade.util.MessageResult;
import cn.ztuo.bitrade.vo.ImportXmlVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class XmlParseHandler extends AbstractXmlParse {
    private List<ImportXmlVO> xmlVOS;
    private String currentTag; // 记录当前解析到的节点名称
    private ImportXmlVO xmlVO; // 记录当前的member信息

    @Autowired
    private MemberWalletService memberWalletService;

    private static MemberWalletService memberWalletServiceLocal;

    @PostConstruct
    public void init() {
        memberWalletServiceLocal=memberWalletService;
    }


    /**
     * 文档解析结束后调用
     */
    @Override
    public void endDocument() throws SAXException {
        super.endDocument();
    }

    /**
     * 节点解析结束后调用
     * @param uri : 命名空间的uri
     * @param localName : 标签的名称
     * @param qName : 带命名空间的标签名称
     */
    @Override
    public void endElement(String uri, String localName, String qName)
            throws SAXException {
        super.endElement(uri, localName, qName);
        //log.info("endElement:"+ qName);
        if("user".equals(qName)){
            xmlVOS.add(xmlVO);
            xmlVO = null;
        }
        currentTag = null;
    }

    /**
     * 文档解析开始调用
     */
    @Override
    public void startDocument() throws SAXException {
        super.startDocument();
        xmlVOS = new ArrayList<ImportXmlVO>();
    }

    /**
     * 节点解析开始调用
     * @param uri : 命名空间的uri
     * @param localName : 标签的名称
     * @param qName : 带命名空间的标签名称
     */
    @Override
    public void startElement(String uri, String localName, String qName,
                             Attributes attributes) throws SAXException {
        if(xmlVOS.size()==MemberWalletService.limit){
            //集合大于设置的阈值的时候，入库，并清空集合，防止内存溢出
            MessageResult result=memberWalletServiceLocal.handleAirdrop(xmlVOS,airdropId);
            if(result.getCode()!=0){
                log.info("saveError,index="+count);
                errorIndex=count;
            }else{
                count+=MemberWalletService.limit;
            }
            log.info("success count:"+count);
            xmlVOS=new ArrayList<>();
        }
        log.info("AirdropId:"+airdropId);
        super.startElement(uri, localName, qName, attributes);
        //log.info("startElement:"+qName);
        if ("user".equals(qName)) { // 是一个用户
            xmlVO= new ImportXmlVO();
        }
        currentTag = qName; // 把当前标签记录下来
    }

    @Override
    public void characters(char[] ch, int start, int length)
            throws SAXException {
        super.characters(ch, start, length);
        String value = new String(ch,start,length); // 将当前TextNode转换为String
        //log.info("characters:"+value);
        if("memberId".equals(currentTag)){
            xmlVO.setMemberId(Long.parseLong(value));
        }else if("memberName".equals(currentTag)){
            xmlVO.setMemberName(value);
        }else if("phone".equals(currentTag)){
            xmlVO.setPhone(value);
        }else if("coinUnit".equals(currentTag)){
            xmlVO.setCoinUnit(value);
        }else if("amount".equals(currentTag)){
            xmlVO.setAmount(Double.parseDouble(value));
        }
    }

    public List<ImportXmlVO> getXmlVOS() {
        return xmlVOS;
    }

}
