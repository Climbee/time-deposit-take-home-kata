package org.ikigaidigital;

import org.ikigaidigital.interest.BasicInterestPolicy;
import org.ikigaidigital.interest.InterestPolicy;
import org.ikigaidigital.interest.PremiumInterestPolicy;
import org.ikigaidigital.interest.StudentInterestPolicy;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class TimeDepositCalculator {

    private final Map<String, InterestPolicy> policiesByPlanType;

    public TimeDepositCalculator() {
        this(List.of(new BasicInterestPolicy(), new StudentInterestPolicy(), new PremiumInterestPolicy()));
    }

    public TimeDepositCalculator(List<InterestPolicy> policies) {
        this.policiesByPlanType = policies.stream()
                .collect(Collectors.toMap(InterestPolicy::planType, Function.identity()));
    }

    public void updateBalance(List<TimeDeposit> deposits) {
        for (TimeDeposit deposit : deposits) {
            deposit.setBalance(deposit.getBalance() + roundToCents(monthlyInterest(deposit)));
        }
    }

    // Assumption: a plan type without a policy earns no interest, as in the original implementation.
    private double monthlyInterest(TimeDeposit deposit) {
        InterestPolicy policy = policiesByPlanType.get(deposit.getPlanType());
        return policy == null ? 0 : policy.monthlyInterest(deposit);
    }

    // new BigDecimal(double) (not BigDecimal.valueOf) rounds the exact binary value; kept to preserve behaviour.
    private static double roundToCents(double amount) {
        return new BigDecimal(amount).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }
}
