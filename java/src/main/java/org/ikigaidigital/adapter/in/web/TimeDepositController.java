package org.ikigaidigital.adapter.in.web;

import org.ikigaidigital.TimeDeposit;
import org.ikigaidigital.Withdrawal;
import org.ikigaidigital.adapter.in.web.api.TimeDepositsApi;
import org.ikigaidigital.adapter.in.web.api.model.TimeDepositDto;
import org.ikigaidigital.adapter.in.web.api.model.WithdrawalDto;
import org.ikigaidigital.application.TimeDepositService;
import org.ikigaidigital.application.TimeDepositWithWithdrawals;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

@RestController
public class TimeDepositController implements TimeDepositsApi {

    private final TimeDepositService service;

    public TimeDepositController(TimeDepositService service) {
        this.service = service;
    }

    @Override
    public ResponseEntity<List<TimeDepositDto>> getAllTimeDeposits() {
        return ResponseEntity.ok(service.getAllTimeDeposits().stream().map(TimeDepositController::toDto).toList());
    }

    @Override
    public ResponseEntity<Void> updateAllBalances() {
        service.updateAllBalances();
        return ResponseEntity.noContent().build();
    }

    private static TimeDepositDto toDto(TimeDepositWithWithdrawals item) {
        TimeDeposit deposit = item.deposit();
        return new TimeDepositDto(
                deposit.getId(),
                deposit.getPlanType(),
                BigDecimal.valueOf(deposit.getBalance()),
                deposit.getDays(),
                item.withdrawals().stream().map(TimeDepositController::toDto).toList());
    }

    private static WithdrawalDto toDto(Withdrawal withdrawal) {
        return new WithdrawalDto(withdrawal.id(), withdrawal.amount(), withdrawal.date());
    }
}
