package org.ikigaidigital.adapter.in.web;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Runs both endpoints through the whole stack (controller, service, calculator, JPA, Flyway schema)
 * against a real PostgreSQL started by Testcontainers.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class TimeDepositApiTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine");

    @Autowired
    MockMvc mockMvc;

    @Autowired
    JdbcTemplate jdbc;

    @BeforeEach
    void resetDatabase() {
        jdbc.execute("TRUNCATE withdrawals, time_deposits");
    }

    @Test
    void getReturnsEveryTimeDepositWithItsWithdrawalsOrderedById() throws Exception {
        insertDeposit(2, "premium", 10, "500.50");
        insertDeposit(1, "basic", 45, "1234567.00");
        jdbc.update("INSERT INTO withdrawals (id, time_deposit_id, amount, date) VALUES (10, 1, 250.00, '2026-10-01')");
        jdbc.update("INSERT INTO withdrawals (id, time_deposit_id, amount, date) VALUES (11, 1, 99.99, '2026-11-01')");

        mockMvc.perform(get("/time-deposits"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id").value(contains(1, 2)))
                .andExpect(jsonPath("$[?(@.id == 1)].planType").value("basic"))
                .andExpect(jsonPath("$[?(@.id == 1)].balance").value(1234567.0))
                .andExpect(jsonPath("$[?(@.id == 1)].days").value(45))
                .andExpect(jsonPath("$[?(@.id == 1)].withdrawals.length()").value(2))
                .andExpect(jsonPath("$[?(@.id == 1)].withdrawals[?(@.id == 10)].amount").value(250.0))
                .andExpect(jsonPath("$[?(@.id == 1)].withdrawals[?(@.id == 10)].date").value("2026-10-01"))
                .andExpect(jsonPath("$[?(@.id == 1)].withdrawals[?(@.id == 11)].amount").value(99.99))
                .andExpect(jsonPath("$[?(@.id == 2)].balance").value(500.5))
                .andExpect(jsonPath("$[?(@.id == 2)].withdrawals[*]").value(empty()));
    }

    @Test
    void getReturnsAnEmptyListWhenThereAreNoTimeDeposits() throws Exception {
        mockMvc.perform(get("/time-deposits"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(empty()));
    }

    @Test
    void balanceUpdateCreditsOneMonthOfInterestToEveryDepositAndPersistsIt() throws Exception {
        insertDeposit(1, "basic", 45, "1234567.00");
        insertDeposit(2, "student", 366, "10000.00");
        insertDeposit(3, "premium", 46, "10000.00");
        insertDeposit(4, "basic", 30, "10000.00");

        mockMvc.perform(post("/time-deposits/balance-updates"))
                .andExpect(status().isNoContent());

        assertThat(balanceOf(1)).isEqualByComparingTo("1235595.81");
        assertThat(balanceOf(2)).isEqualByComparingTo("10000.00");
        assertThat(balanceOf(3)).isEqualByComparingTo("10041.67");
        assertThat(balanceOf(4)).isEqualByComparingTo("10000.00");
    }

    @Test
    void eachBalanceUpdateCreditsInterestAgainOnTheNewBalance() throws Exception {
        insertDeposit(1, "premium", 46, "10000.00");

        mockMvc.perform(post("/time-deposits/balance-updates")).andExpect(status().isNoContent());
        mockMvc.perform(post("/time-deposits/balance-updates")).andExpect(status().isNoContent());

        // 10000.00 + 41.67 = 10041.67; 10041.67 * 5% / 12 = 41.84 -> 10083.51
        mockMvc.perform(get("/time-deposits"))
                .andExpect(jsonPath("$[0].balance").value(10083.51));
    }

    private void insertDeposit(int id, String planType, int days, String balance) {
        jdbc.update("INSERT INTO time_deposits (id, plan_type, days, balance) VALUES (?, ?, ?, ?)",
                id, planType, days, new BigDecimal(balance));
    }

    private BigDecimal balanceOf(int id) {
        return jdbc.queryForObject("SELECT balance FROM time_deposits WHERE id = ?", BigDecimal.class, id);
    }
}
