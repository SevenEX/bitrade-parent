package cn.ztuo.bitrade.controller;


import cn.ztuo.bitrade.entity.CoinThumb;
import cn.ztuo.bitrade.exception.GeneralException;
import cn.ztuo.bitrade.util.MessageResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
@Api(tags = "交易对")
@RestController
@RequestMapping("open")
@Slf4j
public class OpenApiController extends BaseController{

    @Autowired
    private RestTemplate restTemplate;

    /**
     * 获取支持的交易对
     * @return
     * @throws GeneralException
     */
    @ApiOperation(value = "获取支持的交易对")
    @RequestMapping(value = "symbol_thumb",method = RequestMethod.GET)
    public MessageResult getCoinSymbol()throws  GeneralException{
        List<CoinThumb> thumbs = new ArrayList<>();
        try {
            //远程RPC服务URL,后缀为币种单位
            String serviceName = "BITRADE-MARKET";
            String url = "http://" + serviceName + "/market/symbol-thumb";
            ResponseEntity<List> result = restTemplate.getForEntity(url,List.class);
            log.info("remote call:service={},result={}", serviceName, result);
            if (result.getStatusCode().value() == 200) {
                thumbs.addAll(result.getBody());
            }
        } catch (Exception e) {
            log.info(">>>>>>获取交易对异常>>>>>>"+e);
            throw new GeneralException(msService.getMessage("GET_COIN_SYMBOL_ERROR"));
        }
        return success(thumbs);
    }


}
