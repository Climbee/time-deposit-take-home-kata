package org.ikigaidigital.interest;

import org.ikigaidigital.TimeDeposit;

public class PremiumInterestPolicy implements InterestPolicy {

    private static final double ANNUAL_RATE = 0.05;
    // "Interest starts after 45 days": first paid on day 46. Stricter than the 30-day grace period.
    private static final int INTEREST_STARTS_AFTER_DAYS = 45;

    @Override
    public String planType() {
        return "premium";
    }

    @Override
    public double monthlyInterest(TimeDeposit deposit) {
        if (deposit.getDays() <= INTEREST_STARTS_AFTER_DAYS) {
            return 0;
        }
        return deposit.getBalance() * ANNUAL_RATE / 12;
    }
}
