package org.ikigaidigital.adapter.out.persistence;

import org.ikigaidigital.TimeDeposit;
import org.ikigaidigital.Withdrawal;
import org.ikigaidigital.application.TimeDepositRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
class TimeDepositPersistenceAdapter implements TimeDepositRepository {

    private static final Sort BY_ID = Sort.by("id");

    private final TimeDepositJpaRepository timeDeposits;
    private final WithdrawalJpaRepository withdrawals;

    TimeDepositPersistenceAdapter(TimeDepositJpaRepository timeDeposits, WithdrawalJpaRepository withdrawals) {
        this.timeDeposits = timeDeposits;
        this.withdrawals = withdrawals;
    }

    @Override
    public List<TimeDeposit> findAll() {
        return timeDeposits.findAll(BY_ID).stream()
                .map(entity -> new TimeDeposit(
                        entity.getId(), entity.getPlanType(), entity.getBalance().doubleValue(), entity.getDays()))
                .toList();
    }

    @Override
    public List<Withdrawal> findAllWithdrawals() {
        return withdrawals.findAll(BY_ID).stream()
                .map(entity -> new Withdrawal(
                        entity.getId(), entity.getTimeDepositId(), entity.getAmount(), entity.getDate()))
                .toList();
    }

    // Must run inside the caller's transaction: the loaded entities are flushed by dirty checking.
    @Override
    public void updateBalances(List<TimeDeposit> deposits) {
        Map<Integer, TimeDeposit> depositsById = deposits.stream()
                .collect(Collectors.toMap(TimeDeposit::getId, Function.identity()));
        for (TimeDepositEntity entity : timeDeposits.findAllById(depositsById.keySet())) {
            entity.setBalance(toMoney(depositsById.get(entity.getId()).getBalance()));
        }
    }

    // The calculator works in double; the column is NUMERIC(19,2). valueOf uses the shortest decimal
    // representation of the double (1235595.81, not 1235595.8100000000558...), so this only drops float noise.
    private static BigDecimal toMoney(double amount) {
        return BigDecimal.valueOf(amount).setScale(2, RoundingMode.HALF_UP);
    }
}
