package com.imengli.appletServer.controller;

import com.imengli.appletServer.dto.AddOrderFormInfoPOJO;
import com.imengli.appletServer.dto.ResultDTO;
import com.imengli.appletServer.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @RequestMapping("/insertOrderInfo")
    public ResultDTO insertOrderInfo(@RequestBody() AddOrderFormInfoPOJO orderFormInfo) {
        return orderService.insertOrderInfo(orderFormInfo);
    }

    @RequestMapping("/getMyOrderList")
    public ResultDTO getMyOrderList(@RequestParam String token,
                                    @RequestParam Integer status,
                                    @RequestParam Integer page,
                                    @RequestParam Integer size) {
        return orderService.getMyOrderList(token, status, page, size);
    }

    @RequestMapping("/getOrderList")
    public ResultDTO getOrderList(@RequestParam String token,
                                  @RequestParam Integer status,
                                  @RequestParam Integer page,
                                  @RequestParam Integer size,
                                  @RequestParam String filterList) {
        return orderService.getOrderList(token, status, page, size, filterList);
    }

    @RequestMapping("/confirmCollection")
    public ResultDTO confirmCollection(@RequestParam String token,
                                       @RequestParam Integer orderId) {
        return orderService.confirmCollection(token,orderId);
    }
}