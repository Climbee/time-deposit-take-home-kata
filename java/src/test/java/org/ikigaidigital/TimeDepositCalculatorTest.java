package org.ikigaidigital;

import org.ikigaidigital.interest.InterestPolicy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Characterisation tests: they pin down the behaviour of {@link TimeDepositCalculator#updateBalance}
 * as it was delivered, so that the refactoring can prove it changed nothing.
 */
class TimeDepositCalculatorTest {

    private final TimeDepositCalculator calculator = new TimeDepositCalculator();

    @ParameterizedTest(name = "{0} plan, {1} days: {2} -> {3}")
    @CsvSource({
            // no plan earns interest during the first 30 days
            "basic,   30,  10000.00, 10000.00",
            "student, 30,  10000.00, 10000.00",
            "premium, 30,  10000.00, 10000.00",
            // basic: 1% p.a. paid monthly from day 31, no upper limit
            "basic,   31,  10000.00, 10008.33",
            "basic,   5000, 10000.00, 10008.33",
            // student: 3% p.a. from day 31 until day 365 inclusive
            "student, 31,  10000.00, 10025.00",
            "student, 365, 10000.00, 10025.00",
            "student, 366, 10000.00, 10000.00",
            // premium: 5% p.a. only from day 46
            "premium, 31,  10000.00, 10000.00",
            "premium, 45,  10000.00, 10000.00",
            "premium, 46,  10000.00, 10041.67",
            "premium, 5000, 10000.00, 10041.67",
    })
    void appliesMonthlyInterestAccordingToPlanAndAge(String planType, int days, double balance, double expected) {
        TimeDeposit deposit = new TimeDeposit(1, planType, balance, days);

        calculator.updateBalance(List.of(deposit));

        assertThat(deposit.getBalance()).isEqualTo(expected);
    }

    @Test
    void roundsInterestHalfUpToCents() {
        // 1234567.00 * 1% / 12 = 1028.8058... -> 1028.81
        TimeDeposit deposit = new TimeDeposit(1, "basic", 1234567.00, 45);

        calculator.updateBalance(List.of(deposit));

        assertThat(deposit.getBalance()).isEqualTo(1235595.81);
    }

    @Test
    void roundsTheBinaryDoubleValueNotTheDecimalOne() {
        // Quirk of the original implementation that must be preserved:
        // 6 * 0.01 / 12 is stored as 0.005000000000000000104 -> rounds up to 0.01, while
        // 18 * 0.01 / 12 is stored as 0.014999999999999999444 -> rounds down to 0.01 (not 0.02).
        TimeDeposit roundsUp = new TimeDeposit(1, "basic", 6.00, 31);
        TimeDeposit roundsDown = new TimeDeposit(2, "basic", 18.00, 31);

        calculator.updateBalance(List.of(roundsUp, roundsDown));

        assertThat(roundsUp.getBalance()).isEqualTo(6.01);
        assertThat(roundsDown.getBalance()).isEqualTo(18.01);
    }

    @Test
    void leavesBalanceOfUnknownPlanTypeUnchanged() {
        TimeDeposit deposit = new TimeDeposit(1, "gold", 10000.00, 100);

        calculator.updateBalance(List.of(deposit));

        assertThat(deposit.getBalance()).isEqualTo(10000.00);
    }

    @Test
    void updatesEveryDepositInTheListIndependently() {
        TimeDeposit basic = new TimeDeposit(1, "basic", 10000.00, 31);
        TimeDeposit student = new TimeDeposit(2, "student", 10000.00, 400);
        TimeDeposit premium = new TimeDeposit(3, "premium", 10000.00, 46);

        calculator.updateBalance(List.of(basic, student, premium));

        assertThat(basic.getBalance()).isEqualTo(10008.33);
        assertThat(student.getBalance()).isEqualTo(10000.00);
        assertThat(premium.getBalance()).isEqualTo(10041.67);
    }

    @Test
    void appliesAdditionalPlanPoliciesWithoutChangingTheCalculator() {
        InterestPolicy flatTenPerMonth = new InterestPolicy() {
            @Override
            public String planType() {
                return "flat";
            }

            @Override
            public double monthlyInterest(TimeDeposit deposit) {
                return 10;
            }
        };
        TimeDepositCalculator extended = new TimeDepositCalculator(List.of(flatTenPerMonth));
        TimeDeposit deposit = new TimeDeposit(1, "flat", 100.00, 1);

        extended.updateBalance(List.of(deposit));

        assertThat(deposit.getBalance()).isEqualTo(110.00);
    }

    @Test
    void doesNotChangeAnythingButTheBalance() {
        TimeDeposit deposit = new TimeDeposit(7, "premium", 10000.00, 46);

        calculator.updateBalance(List.of(deposit));

        assertThat(deposit.getId()).isEqualTo(7);
        assertThat(deposit.getPlanType()).isEqualTo("premium");
        assertThat(deposit.getDays()).isEqualTo(46);
    }
}
