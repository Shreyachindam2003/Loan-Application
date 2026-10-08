package com.example.Loan.main.util.EmiPaymentUtil;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class RazorpayUtil {

    private final RazorpayClient razorpayClient;
    private final String keySecret;

    public RazorpayUtil(
            @Value("${razorpay.key-id}") String keyId,
            @Value("${razorpay.key-secret}") String keySecret) {

        try {
            this.razorpayClient = new RazorpayClient(keyId, keySecret);
            this.keySecret = keySecret;
        } catch (RazorpayException e) {
            throw new IllegalStateException(
                    "Unable to initialize Razorpay client", e);
        }
    }

    public String createOrder(
            long amountInPaise,
            String receipt) {

        try {
            JSONObject orderRequest = new JSONObject();

            orderRequest.put("amount", amountInPaise);
            orderRequest.put("currency", "INR");
            orderRequest.put("receipt", receipt);

            Order order = razorpayClient.orders.create(orderRequest);

            return order.get("id");

        } catch (RazorpayException e) {
            throw new RuntimeException(
                    "Unable to create Razorpay order", e);
        }
    }

    public boolean verifyPayment(
            String orderId,
            String paymentId,
            String signature) {

        try {
            JSONObject options = new JSONObject();

            options.put("razorpay_order_id", orderId);
            options.put("razorpay_payment_id", paymentId);
            options.put("razorpay_signature", signature);

            return Utils.verifyPaymentSignature(
                    options,
                    keySecret
            );

        } catch (RazorpayException e) {
            throw new RuntimeException(
                    "Unable to verify Razorpay payment", e);
        }
    }
}