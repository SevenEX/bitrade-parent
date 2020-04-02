package cn.ztuo.bitrade.job;

import cn.ztuo.bitrade.entity.Order;
import cn.ztuo.bitrade.service.OrderService;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author Seven
 * @date 2019年01月22日
 */
@Component
@Slf4j
public class CheckOrderTask {
    @Autowired
    private OrderService orderService;

//    @Scheduled(fixedRate = 60000)
//    public void checkExpireOrder() {
    @XxlJob("checkExpireOrder")
    public ReturnT<String> checkExpireOrder(String param) throws Exception {
        log.info("=========开始检查过期订单===========");
        List<Order> list = orderService.checkExpiredOrder();
        if(list!=null&&list.size()>0){
            list.stream().forEach(x -> {
                        try {
                            log.info("===========取消过期订单==========");
                            orderService.cancelOrderTask(x);
                        } catch (Exception e) {
                            e.printStackTrace();
                            log.warn("订单编号{}:自动取消失败", x.getOrderSn());
                        }
                    }
            );
        }
        log.info("=========检查过期订单结束===========");
        return ReturnT.SUCCESS;
    }
}
