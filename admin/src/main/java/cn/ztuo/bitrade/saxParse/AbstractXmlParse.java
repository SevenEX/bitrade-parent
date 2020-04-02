package cn.ztuo.bitrade.saxParse;

import org.xml.sax.helpers.DefaultHandler;

public class AbstractXmlParse  extends DefaultHandler {
    protected Long airdropId;
    protected int errorIndex;
    protected int count;

    public void startParse(Long airdropId,int errorIndex,int count){
        this.airdropId=airdropId;
        this.errorIndex=errorIndex;
        this.count=count;
    }

    public void resetParse(){
        this.airdropId=null;
        this.errorIndex=0;
        count=0;
    }
}
