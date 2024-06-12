package com.datn.application.controller.admin;

import com.datn.application.config.Contant;
import com.datn.application.config.PaypalPaymentIntent;
import com.datn.application.config.PaypalPaymentMethod;
import com.datn.application.entity.Order;
import com.datn.application.entity.User;
import com.datn.application.model.request.CreateOrderRequest;
import com.datn.application.repository.OrderRepository;
import com.datn.application.security.CustomUserDetails;
import com.datn.application.service.OrderService;
import com.datn.application.service.PaypalService;
import com.datn.application.utils.PayPalUtil;
import com.paypal.api.payments.Links;
import com.paypal.api.payments.Payment;
import com.paypal.base.Constants;
import com.paypal.base.rest.PayPalRESTException;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;


@Controller
public class PaymentController {
    public static final String URL_PAYPAL_SUCCESS = "pay/success";
    public static final String URL_PAYPAL_CANCEL = "pay/cancel";
    public static long idOrder = 0;

    private Logger log = LoggerFactory.getLogger(getClass());
    @Autowired
    private PaypalService paypalService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderRepository orderRepository;
//    @GetMapping("/")
//    public String index(){
//        return "index";
//    }
    @PostMapping("/pay")
    public String pay(HttpServletRequest request, @RequestParam(defaultValue = "0") double price,long id ){
        String cancelUrl = PayPalUtil.getBaseURL(request) + "/" + URL_PAYPAL_CANCEL;
        String successUrl = PayPalUtil.getBaseURL(request) + "/" + URL_PAYPAL_SUCCESS;
        idOrder = id;
        try {
            Payment payment = paypalService.createPayment(
                    price,
                    "USD",
                    PaypalPaymentMethod.paypal,
                    PaypalPaymentIntent.sale,
                    "payment description",
                    cancelUrl,
                    successUrl,id);
            for(Links links : payment.getLinks()){
                if(links.getRel().equals("approval_url")){
                    return "redirect:" + links.getHref();
                }
            }
        } catch (PayPalRESTException e) {
            log.error(e.getMessage());
        }
        return "redirect:";
    }
    @GetMapping(URL_PAYPAL_CANCEL)
    public String cancelPay(){
        return "redirect:/tai-khoan/lich-su-giao-dich/" ;
    }
    @GetMapping(URL_PAYPAL_SUCCESS)
    public String successPay(@RequestParam("paymentId") String paymentId, @RequestParam("PayerID") String payerId){
        try {
            Payment payment = paypalService.executePayment(paymentId, payerId);
            Order order = orderService.findOrderById(idOrder);
            order.setStatus(Contant.PAYMENT_STATUS);
            orderRepository.save(order);
            if(payment.getState().equals("approved")){
                return "redirect:/tai-khoan/lich-su-giao-dich/";
            }
        } catch (PayPalRESTException e) {
            log.error(e.getMessage());
        }
        return "redirect:/tai-khoan/lich-su-giao-dich/";
    }
}
