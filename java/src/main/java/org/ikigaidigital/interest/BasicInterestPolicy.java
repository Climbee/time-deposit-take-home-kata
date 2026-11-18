package org.ikigaidigital.interest;

import org.ikigaidigital.TimeDeposit;

public class BasicInterestPolicy implements InterestPolicy {

    private static final double ANNUAL_RATE = 0.01;

    @Override
    public String planType() {
        return "basic";
    }

    @Override
    public double monthlyInterest(TimeDeposit deposit) {
        if (GracePeriod.covers(deposit)) {
            return 0;
        }
        return deposit.getBalance() * ANNUAL_RATE / 12;
    }
}
