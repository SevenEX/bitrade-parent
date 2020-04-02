package cn.ztuo.bitrade.controller.finance;

import cn.ztuo.bitrade.config.AliyunConfig;
import cn.ztuo.bitrade.constant.PageModel;
import cn.ztuo.bitrade.constant.SysConstant;
import cn.ztuo.bitrade.constant.TransactionType;
import cn.ztuo.bitrade.controller.common.BaseAdminController;
import cn.ztuo.bitrade.entity.Admin;
import cn.ztuo.bitrade.entity.Airdrop;
import cn.ztuo.bitrade.entity.QMember;
import cn.ztuo.bitrade.entity.QMemberTransaction;
import cn.ztuo.bitrade.model.screen.MemberTransactionScreen;
import cn.ztuo.bitrade.saxParse.SAXReaderXMLUtil;
import cn.ztuo.bitrade.service.AirdropService;
import cn.ztuo.bitrade.service.LocaleMessageSourceService;
import cn.ztuo.bitrade.service.MemberTransactionService;
import cn.ztuo.bitrade.service.MemberWalletService;
import cn.ztuo.bitrade.util.DateUtil;
import cn.ztuo.bitrade.util.GeneratorUtil;
import cn.ztuo.bitrade.util.MessageResult;
import cn.ztuo.bitrade.util.RedisUtil;
import cn.ztuo.bitrade.vo.ImportXmlVO;
import cn.ztuo.bitrade.vo.MemberTransactionVO;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.xml.sax.SAXException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 通过手动上传xml文件的方式空投
 */
@Slf4j
@RestController
@RequestMapping("/finance/manually")
@Api(tags = "通过手动上传xml文件的方式空投（暂弃）")
public class ManuallyController extends BaseAdminController {
    @Autowired
    private LocaleMessageSourceService sourceService;
    @Autowired
    private AliyunConfig aliyunConfig;
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private MemberWalletService memberWalletService;
    @Autowired
    private AirdropService airdropService;
    @Autowired
    private MemberTransactionService memberTransactionService;

    @RequiresPermissions("finance:manually")
    @PostMapping("upload")
    public MessageResult uploadXml(HttpServletRequest request, HttpServletResponse response,
                                   @SessionAttribute(SysConstant.SESSION_ADMIN) Admin admin,
                                   @RequestParam("file") MultipartFile file) throws MalformedURLException {
        Object code = redisUtil.get(SysConstant.HANDLE_AIRDROP_LOCK);
        if(code!=null){
            return MessageResult.error("当前有一个空投正在进行中");
        }
        log.info(""+request.getSession().getServletContext().getResource("/"));
        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/html; charset=UTF-8");
        if (!ServletFileUpload.isMultipartContent(request)) {
            return MessageResult.error(500, sourceService.getMessage("FORMAT_NOT_SUPPORTED"));
        }
        if (file == null) {
            return MessageResult.error(500, sourceService.getMessage("FILE_NOT_FOUND"));
        }
        String directory = new SimpleDateFormat("yyyy/MM/dd/").format(new Date());
        String uri="";
        OSS ossClient = new OSSClientBuilder().build(aliyunConfig.getOssEndpoint(), aliyunConfig.getAccessKeyId(), aliyunConfig.getAccessKeySecret());
        try {
            String fileName = file.getOriginalFilename();
            String suffix = fileName.substring(fileName.lastIndexOf("."), fileName.length());
            String key = directory + GeneratorUtil.getUUID() + suffix;
            log.info("key:"+key);
            ossClient.putObject(aliyunConfig.getOssBucketName(), key, file.getInputStream());
            log.info("上传oss");
            uri = aliyunConfig.toUrl(key);
            Airdrop airdrop=new Airdrop();
            airdrop.setCreateTime(new Date());
            airdrop.setFileName(uri);
            airdrop.setStatus(0);
            //airdrop.setAdmin(admin);
            airdrop=airdropService.save(airdrop);
            redisUtil.set(SysConstant.HANDLE_AIRDROP_LOCK, "666666", 10, TimeUnit.MINUTES);
            Airdrop finalAirdrop = airdrop;
            log.info("开始解析");
            new Thread(){
                @Override
                public void run(){
                    int errorIndex=0,successCount=0;
                    List<ImportXmlVO> xmlVOS= null;
                    Airdrop parentAirdrop=airdropService.findById(finalAirdrop.getId());
                    try {
                        OSS finalOssClient = new OSSClientBuilder().build(aliyunConfig.getOssEndpoint(), aliyunConfig.getAccessKeyId(), aliyunConfig.getAccessKeySecret());
                        xmlVOS = SAXReaderXMLUtil.getXmlVOS(finalOssClient.getObject(aliyunConfig.getOssBucketName(),key).getObjectContent(), finalAirdrop.getId(),errorIndex,successCount);
                    } catch (ParserConfigurationException | SAXException | IOException e) {
                        parentAirdrop.setErrorMsg("解析失败");
                        log.info("解析失败");
                        e.printStackTrace();
                    }finally {
                        redisUtil.delete(SysConstant.HANDLE_AIRDROP_LOCK);
                    }
                    if(xmlVOS!=null&&xmlVOS.size()>0){
                        MessageResult result=memberWalletService.handleAirdrop(xmlVOS, finalAirdrop.getId());
                        if(result.getCode()==0){
                            parentAirdrop.setStatus(1);
                            successCount+=xmlVOS.size();
                            parentAirdrop.setSuccessCount(successCount);
                        }else{
                            parentAirdrop.setStatus(2);
                            parentAirdrop.setSuccessCount(successCount);
                            parentAirdrop.setErrorIndex(errorIndex);
                            parentAirdrop.setErrorMsg(result.getMessage());
                        }
                        airdropService.save(parentAirdrop);
                    }
                }
            }.start();
        } catch (Throwable e) {
            e.printStackTrace();
            return MessageResult.error("空投失败");
        } finally {
            log.info("空投成功");
            ossClient.shutdown();
        }
        return MessageResult.success();
    }

    @RequiresPermissions("finance:manually")
    @PostMapping("list")
    public MessageResult airdropList(PageModel pageModel){
        pageModel.setSort();
        Page<Airdrop> airdropList=airdropService.findAll(new BooleanBuilder(),pageModel);
        MessageResult result=MessageResult.success();
        result.setData(airdropList);
        return result;
    }

    @RequiresPermissions("finance:manually")
    @PostMapping("pageQuery")
    public MessageResult pageQuery(
            PageModel pageModel,
            MemberTransactionScreen screen) {
        List<Predicate> predicates = new ArrayList<>();
        if(screen.getMemberId()!=null)
            predicates.add((QMember.member.id.eq(screen.getMemberId())));
        if (!StringUtils.isEmpty(screen.getAccount()))
            predicates.add(QMember.member.username.like("%"+screen.getAccount()+"%")
                    .or(QMember.member.realName.like("%"+screen.getAccount()+"%")));
        if (screen.getStartTime() != null) {
            predicates.add(QMemberTransaction.memberTransaction.createTime.goe(screen.getStartTime()));
        }
        if (screen.getEndTime() != null){
            predicates.add(QMemberTransaction.memberTransaction.createTime.lt(DateUtil.dateAddDay(screen.getEndTime(),1)));
        }
        if(screen.getMinMoney()!=null) {
            predicates.add(QMemberTransaction.memberTransaction.amount.goe(screen.getMinMoney()));
        }
        if(screen.getMaxMoney()!=null) {
            predicates.add(QMemberTransaction.memberTransaction.amount.loe(screen.getMaxMoney()));
        }
        if(screen.getMinFee()!=null) {
            predicates.add(QMemberTransaction.memberTransaction.fee.goe(screen.getMinFee()));
        }
        if(screen.getMaxFee()!=null) {
            predicates.add(QMemberTransaction.memberTransaction.fee.loe(screen.getMaxFee()));
        }
        if(screen.getSymbol()!=null&&!screen.getSymbol().equalsIgnoreCase("")){
            predicates.add(QMemberTransaction.memberTransaction.symbol.eq(screen.getSymbol()));
        }
        if(screen.getAirdropId()!=null){
            predicates.add(QMemberTransaction.memberTransaction.airdropId.eq(screen.getAirdropId()));
        }
        predicates.add(QMemberTransaction.memberTransaction.type.eq(TransactionType.MANUAL_AIRDROP));
        Page<MemberTransactionVO> results = memberTransactionService.joinFind(predicates, pageModel);
        MessageResult result=MessageResult.success();
        result.setData(results);
        return result;
    }
}
