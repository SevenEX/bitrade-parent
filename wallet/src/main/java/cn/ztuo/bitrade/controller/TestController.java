package cn.ztuo.bitrade.controller;

import cn.ztuo.bitrade.service.CoinService;
import cn.ztuo.bitrade.util.MessageResult;
import cn.ztuo.bitrade.util.RsaKeyUtil;
import com.alibaba.fastjson.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.TreeMap;


@RestController
@RequestMapping("test")
public class TestController extends BaseController {
    private Logger logger = LoggerFactory.getLogger(TestController.class);
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private CoinService coinService;

    @GetMapping("/height/{unit}")
    public MessageResult test(@PathVariable("unit") String unit) {
        String serviceName = "SERVICE-RPC-" + unit.toUpperCase();
        TreeMap<String, String> map = new TreeMap<>();
        map.put("unit",unit);
        String param = coinService.sign(map);
        String url = "http://" + serviceName + "/rpc/height" + param;
        ResponseEntity<MessageResult> result = restTemplate.getForEntity(url, MessageResult.class);
        logger.info("remote call:service={},result={}" + serviceName + result);
        return success(result);
    }

    /***
     * 获取 所有币种rpc 是否正常
     * MrGao
     * @return
     */
    @GetMapping("/rpc")
    public MessageResult test() {
        logger.info("获取 所有币种rpc 是否正常");
        //rpc coin
        List<String> units = coinService.findAllRpcUnit();
        if (units == null || units.size() == 0) return error("no rpc coin!");
        logger.info("units = {}", units);
        //结果
        LinkedHashMap<String, ResponseEntity<MessageResult>> data = new LinkedHashMap<>(units.size());
        units.forEach(
                x -> {
                    TreeMap<String, String> map = new TreeMap<>();
                    map.put("unit",x);
                    String param = coinService.sign(map);
                    String serviceName = "SERVICE-RPC-" + x.toUpperCase();
                    String url = "http://" + serviceName + "/rpc/height" + param;
                    try {
                        ResponseEntity<MessageResult> result = restTemplate.getForEntity(url, MessageResult.class);
                        data.put(url, result);
                    }catch (Exception e){

                    }

                }
        );
        return success(data);
    }


}
