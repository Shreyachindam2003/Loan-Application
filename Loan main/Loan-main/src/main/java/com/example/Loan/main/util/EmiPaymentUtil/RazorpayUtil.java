package com.example.Loan.main.util.EmiPaymentUtil;

import java.math.BigDecimal;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.Utils;

@Component
public class RazorpayUtil {

    @Value("${razorpay.key-id}")
    private String keyId;

    @Value("${razorpay.key-secret}")
    private String keySecret;

    public Order createOrder(BigDecimal amount, String receipt)
            throws Exception {

        RazorpayClient client =
                new RazorpayClient(keyId, keySecret);

        JSONObject orderRequest = new JSONObject();

        orderRequest.put(
                "amount",
                amount.multiply(BigDecimal.valueOf(100)).intValue());

        orderRequest.put("currency", "INR");
        orderRequest.put("receipt", receipt);

        return client.orders.create(orderRequest);
    }

    public boolean verifyPayment(
            String orderId,
            String paymentId,
            String signature) throws Exception {

        JSONObject options = new JSONObject();

        options.put("razorpay_order_id", orderId);
        options.put("razorpay_payment_id", paymentId);
        options.put("razorpay_signature", signature);

        return Utils.verifyPaymentSignature(
                options,
                keySecret);
    }

    public String getKeyId() {
        return keyId;
    }
}
