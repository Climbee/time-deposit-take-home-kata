package org.ikigaidigital.interest;

import org.ikigaidigital.TimeDeposit;

public class StudentInterestPolicy implements InterestPolicy {

    private static final double ANNUAL_RATE = 0.03;
    // "No interest after 1 year": interest is still paid on day 365, not from day 366 on.
    private static final int LAST_DAY_WITH_INTEREST = 365;

    @Override
    public String planType() {
        return "student";
    }

    @Override
    public double monthlyInterest(TimeDeposit deposit) {
        if (GracePeriod.covers(deposit) || deposit.getDays() > LAST_DAY_WITH_INTEREST) {
            return 0;
        }
        return deposit.getBalance() * ANNUAL_RATE / 12;
    }
}
