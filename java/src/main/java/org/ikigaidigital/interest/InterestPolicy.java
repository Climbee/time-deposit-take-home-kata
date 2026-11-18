package org.ikigaidigital.interest;

import org.ikigaidigital.TimeDeposit;

/**
 * Interest rule for one plan type. New plans, or more complex rules for an existing plan,
 * are added by implementing this interface - the calculator itself does not change.
 */
public interface InterestPolicy {

    String planType();

    /**
     * Unrounded interest earned by the deposit for one month; rounding is the calculator's concern.
     */
    double monthlyInterest(TimeDeposit deposit);
}
