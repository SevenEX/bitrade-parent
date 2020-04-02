package cn.ztuo.bitrade.controller.exchange;

import cn.ztuo.bitrade.constant.SysConstant;
import cn.ztuo.bitrade.entity.ExchangeCoin;
import cn.ztuo.bitrade.entity.InitPlate;
import cn.ztuo.bitrade.pagination.Criteria;
import cn.ztuo.bitrade.pagination.Restrictions;
import cn.ztuo.bitrade.service.ExchangeCoinService;
import cn.ztuo.bitrade.service.InitPlateService;
import cn.ztuo.bitrade.service.LocaleMessageSourceService;
import cn.ztuo.bitrade.util.MessageResult;
import cn.ztuo.bitrade.util.RedisUtil;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("exchange_init")
@Slf4j
@Api(tags = "币币交易-交易对配置")
public class ExchangeInitPlateController {

    @Autowired
    private ExchangeCoinService exchangeCoinService;
    @Autowired
    private InitPlateService initPlateService;
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private LocaleMessageSourceService messageSource;
    /**
     * 查询列表信息
     * @param pageNum
     * @param pageSize
     * @param symbol
     * @return
     * @throws Exception
     */
    @RequiresPermissions("exchange:init-plate:query")
    @PostMapping("query")
    public MessageResult queryExchangeInitPlate(@RequestParam int pageNum,
                                                  @RequestParam int pageSize,
                                                  @RequestParam(required = false)String symbol )throws Exception{
        MessageResult mr =new MessageResult();
        try {
            Sort orders = Criteria.sortStatic("id");
            PageRequest pageRequest = PageRequest.of(pageNum-1, pageSize, orders);

            Criteria<InitPlate> specification = new Criteria<InitPlate>();
            if(StringUtils.isNotEmpty(symbol)){
                specification.add(Restrictions.like("symbol", symbol, false));
            }
            Page<InitPlate>  page = initPlateService.findAllByPage(specification,pageRequest);
            mr.setCode(0);
            mr.setMessage(messageSource.getMessage("SUCCESS"));
            mr.setData(page);
        }catch (Exception e){
            log.info(">>>>queryExchangeCoin Error",e);
            e.printStackTrace();
            throw new Exception(e);
        }
        return mr;

    }

    /**
     * 查询详情 id
     * @param id
     * @return
     * @throws Exception
     */
    @RequiresPermissions("exchange:init-plate:query")
    @GetMapping("detail/{id}")
    public MessageResult queryDetailExchangeInitPlate(@PathVariable("id")long id)throws Exception{
        MessageResult mr = new MessageResult();
        try {
            mr.setData(initPlateService.findByInitPlateId(id));
            mr.setCode(0);
            mr.setMessage(messageSource.getMessage("SUCCESS"));
        }catch (Exception e){
            log.info(">>>>queryDetailExchangeInitPlate Error={}",e);
            e.printStackTrace();
            throw new Exception(e);
        }

        return mr;
    }

    /**
     * 删除信息
     * @param id
     * @return
     * @throws Exception
     */
    @RequiresPermissions("exchange:init-plate:query")
    @GetMapping("delete/{id}")
    public MessageResult deleteExchangeInitPlate(@PathVariable("id")long id)throws Exception{
        MessageResult mr = new MessageResult();
        try {
            InitPlate initPlate = initPlateService.findByInitPlateId(id);
            if(initPlate==null){
                mr.setCode(500);
                mr.setMessage("不存在该记录");
                return mr;
            }
            initPlateService.delete(id);
            String key = SysConstant.EXCHANGE_INIT_PLATE_SYMBOL_KEY+initPlate.getSymbol();
            redisUtil.delete(key);
            redisUtil.delete(SysConstant.EXCHANGE_INIT_PLATE_ALL_SYMBOLS);
            mr.setCode(0);
            mr.setMessage(messageSource.getMessage("SUCCESS"));
        }catch (Exception e){
            log.info(">>>>deleteExchangeInitPlate Error={}",e);
            e.printStackTrace();
            throw new Exception(e);
        }

        return  mr ;
    }
    /**
     * 修改信息
     * @param initPlate
     * @return
     * @throws Exception
     */
    @RequiresPermissions("exchange:init-plate:query")
    @PostMapping("update")
    public MessageResult updateExchangeInitPlate(InitPlate initPlate)throws Exception{
        MessageResult mr = new MessageResult();
        try {
            if (checkInitPlateParams(initPlate, mr)){
                return mr;
            }
            if(initPlate.getId()==null){
                mr.setCode(500);
                mr.setMessage("不存在该记录");
                return mr;
            }
            if(initPlateService.findByInitPlateId(initPlate.getId())==null){
                mr.setCode(500);
                mr.setMessage("不存在该记录");
                return mr;
            }
            initPlateService.saveAndFlush(initPlate);
            redisUtil.delete(SysConstant.EXCHANGE_INIT_PLATE_ALL_SYMBOLS);
            String key = SysConstant.EXCHANGE_INIT_PLATE_SYMBOL_KEY+initPlate.getSymbol();
            redisUtil.delete(key);
            mr.setCode(0);
            mr.setMessage("修改成功");
        }catch (Exception e){
            log.info(".>>>updateInitPlate Error ={}",e);
            e.printStackTrace();
            throw new Exception(e);
        }

        return mr;
    }

    /**
     * 新增initPlate
     * @param initPlate
     * @return
     * @throws Exception
     */

    @RequiresPermissions("exchange:init-plate:query")
    @PostMapping("add")
    public MessageResult addExchangeInitPlate(InitPlate initPlate)throws Exception{
        MessageResult mr = new MessageResult();
        try{
            if (checkInitPlateParams(initPlate, mr)){
                return mr;
            }
            initPlateService.save(initPlate);
            redisUtil.delete(SysConstant.EXCHANGE_INIT_PLATE_ALL_SYMBOLS);
            mr.setCode(0);
            mr.setMessage("保存成功");
        }catch (Exception e){
            log.info(">>>>saveInitPlate  Error={}  ",e);
            e.printStackTrace();
            throw new Exception(e);
        }
        return mr;
    }

    private boolean checkInitPlateParams(@RequestParam InitPlate initPlate, MessageResult mr) {
        if(StringUtils.isEmpty(initPlate.getRelativeTime())||
                StringUtils.isEmpty(initPlate.getInitPrice())||
                StringUtils.isEmpty(initPlate.getFinalPrice())||
                StringUtils.isEmpty(initPlate.getRelativeTime())||
                StringUtils.isEmpty(initPlate.getSymbol())){
            mr.setCode(500);
            mr.setMessage("参数非法，请核实参数");
            return true;
        }
        //校验参数
        ExchangeCoin exchangeCoin = exchangeCoinService.findBySymbol(initPlate.getSymbol());
        if(exchangeCoin==null || exchangeCoin.getEnable()==2){
            mr.setCode(500);
            mr.setMessage("交易对非法，请核实");
            return true;
        }
        int interferenceFactor =Integer.parseInt(initPlate.getInterferenceFactor());
        if(interferenceFactor<0 || interferenceFactor>70){
            mr.setCode(500);
            mr.setMessage("干扰因子非法，请输入1-70之间的整数");
            return true;
        }
        double initPrice = Double.parseDouble(initPlate.getInitPrice());
        double finalPrice =Double.parseDouble(initPlate.getFinalPrice());
        if(initPrice==finalPrice){
            mr.setCode(500);
            mr.setMessage("价格差值为0，请重新调整");
            return true;
        }
        return false;
    }


}
