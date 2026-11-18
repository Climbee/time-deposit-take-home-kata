package org.ikigaidigital.application;

import org.ikigaidigital.TimeDeposit;
import org.ikigaidigital.Withdrawal;

import java.util.List;

/**
 * Outbound port: how the application reads and stores time deposits, independent of the database used.
 */
public interface TimeDepositRepository {

    List<TimeDeposit> findAll();

    List<Withdrawal> findAllWithdrawals();

    void updateBalances(List<TimeDeposit> deposits);
}
