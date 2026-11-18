package org.ikigaidigital;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * A withdrawal from a time deposit. Kept separate from {@link TimeDeposit} because that class is shared
 * and must not change; withdrawals do not take part in the interest calculation.
 */
public record Withdrawal(int id, int timeDepositId, BigDecimal amount, LocalDate date) {
}
