package com.carlos.payflowguard.payment.service;

public class FraudCheckResult {

    private final boolean passed;
    private final String reason;

    public FraudCheckResult(boolean passed, String reason) {
        this.passed = passed;
        this.reason = reason;
    }

    public boolean isPassed() {
        return passed;
    }

    public String getReason() {
        return reason;
    }

    public static FraudCheckResult passed() {
        return new FraudCheckResult(true, null);
    }

    public static FraudCheckResult failed(String reason) {
        return new FraudCheckResult(false, reason);
    }
}