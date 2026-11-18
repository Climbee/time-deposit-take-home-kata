package org.ikigaidigital;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The README states that the original updateBalance "is functioning correctly" and must not change.
 * The example-based tests cover the rules; this test compares the refactored calculator with a verbatim
 * copy of the original over a wide grid of inputs, requiring bit-for-bit identical balances.
 */
class TimeDepositCalculatorEquivalenceTest {

    private static final List<String> PLAN_TYPES = List.of("basic", "student", "premium", "unknown");
    private static final List<Double> BALANCES = List.of(
            0.0, 0.01, 6.0, 18.0, 99.99, 1000.10, 10000.0, 54321.37, 1234567.0, 98765432.12);

    @Test
    void producesExactlyTheSameBalancesAsTheOriginalImplementation() {
        List<TimeDeposit> refactored = new ArrayList<>();
        List<TimeDeposit> original = new ArrayList<>();
        int id = 0;
        for (String planType : PLAN_TYPES) {
            for (int days = 0; days <= 800; days++) {
                for (double balance : BALANCES) {
                    refactored.add(new TimeDeposit(id, planType, balance, days));
                    original.add(new TimeDeposit(id, planType, balance, days));
                    id++;
                }
            }
        }

        new TimeDepositCalculator().updateBalance(refactored);
        originalUpdateBalance(original);

        for (int i = 0; i < refactored.size(); i++) {
            assertThat(refactored.get(i).getBalance())
                    .as("deposit %s/%d days/%s", original.get(i).getPlanType(), original.get(i).getDays(), BALANCES.get(i % BALANCES.size()))
                    .isEqualTo(original.get(i).getBalance());
        }
    }

    // Copied unchanged from the original TimeDepositCalculator (commit c4ea358).
    private static void originalUpdateBalance(List<TimeDeposit> xs) {
        for (int i = 0; i < xs.size(); i++) {
            double interest = 0;

            if (xs.get(i).getDays() > 30) {
                if (xs.get(i).getPlanType().equals("student")) {
                    if (xs.get(i).getDays() < 366) {
                        interest += xs.get(i).getBalance() * 0.03 / 12;
                    }
                } else if (xs.get(i).getPlanType().equals("premium")) {
                    if (xs.get(i).getDays() > 45) {
                        interest += xs.get(i).getBalance() * 0.05 / 12;
                    }
                } else if (xs.get(i).getPlanType().equals("basic")) {
                    interest += xs.get(i).getBalance() * 0.01 / 12;
                }
            }

            double a2d = xs.get(i).getBalance() + (new BigDecimal(interest).setScale(2, RoundingMode.HALF_UP)).doubleValue();
            xs.get(i).setBalance(a2d);
        }
    }
}
