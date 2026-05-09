package com.sky.task;

import com.sky.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class OrderTask {

    @Autowired
    private OrderService orderService;

    @Scheduled(cron = "0 * * * * ? ")
    public void removeOrder(){
        log.info("开始清理支付过期订单...");
        orderService.cancelTimeoutOrders();
        log.info("支付过期订单清理完成");
    }

    /**
     * 每天凌晨一点将派送中的订单设置为已完成
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void completeDeliveryOrders(){
        log.info("开始将派送中的订单设置为已完成...");
        orderService.completeDeliveryOrders();
        log.info("派送中订单设置完成");
    }
}
