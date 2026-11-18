package org.ikigaidigital.application;

import org.ikigaidigital.TimeDeposit;
import org.ikigaidigital.Withdrawal;

import java.util.List;

public record TimeDepositWithWithdrawals(TimeDeposit deposit, List<Withdrawal> withdrawals) {
}
