package org.ikigaidigital.interest;

import org.ikigaidigital.TimeDeposit;

/**
 * "No interest is applied for the first 30 days for any existing plans."
 * Kept per policy rather than in the calculator, so a future plan can opt out of it.
 */
final class GracePeriod {

    static final int DAYS = 30;

    private GracePeriod() {
    }

    static boolean covers(TimeDeposit deposit) {
        return deposit.getDays() <= DAYS;
    }
}
