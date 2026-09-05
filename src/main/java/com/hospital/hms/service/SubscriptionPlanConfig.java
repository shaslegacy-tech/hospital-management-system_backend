package com.hospital.hms.service;
 
import com.hospital.hms.model.enums.SubscriptionPlan;
import org.springframework.stereotype.Component;
 
import java.util.Map;
 
@Component
public class SubscriptionPlanConfig {
    private static final Map<SubscriptionPlan, Double> MONTHLY_PRICE_INR = Map.of(
        SubscriptionPlan.TRIAL, 0.0,
        SubscriptionPlan.BASIC, 2999.0,
        SubscriptionPlan.PREMIUM, 7999.0
    );
 
    public double getMonthlyPrice(SubscriptionPlan plan) {
        return MONTHLY_PRICE_INR.getOrDefault(plan, 0.0);
    }
}