package com.db.awmd.challenge;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.exception.DuplicateAccountIdException;
import com.db.awmd.challenge.service.AccountsService;
import java.math.BigDecimal;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class AccountsServiceTest {

	@Autowired
	private AccountsService accountsService;

	@Test
	public void addAccount() throws Exception {
		Account account = new Account("Id-123");
		account.setBalance(new BigDecimal(1000));
		this.accountsService.createAccount(account);

		assertThat(this.accountsService.getAccount("Id-123")).isEqualTo(account);
	}

	@Test
	public void addAccount_failsOnDuplicateId() throws Exception {
		String uniqueId = "Id-" + System.currentTimeMillis();
		Account account = new Account(uniqueId);
		this.accountsService.createAccount(account);

		try {
			this.accountsService.createAccount(account);
			fail("Should have failed when adding duplicate account");
		} catch (DuplicateAccountIdException ex) {
			assertThat(ex.getMessage()).isEqualTo("Account id " + uniqueId + " already exists!");
		}

	}

	@Test
	public void transferValidAmount() throws Exception {
		String uniqueAccountIdFrom = "Id-" + UUID.randomUUID();
		String uniqueAccountIdTo = "Id-" + UUID.randomUUID();
		Account accountFrom = new Account(uniqueAccountIdFrom, new BigDecimal("1000.45"));
		Account accountTo = new Account(uniqueAccountIdTo, new BigDecimal("500.25"));
		BigDecimal amount = new BigDecimal(100);
		Account debtAccount = new Account(accountFrom.getAccountId(), new BigDecimal("900.45"));
		Account credAccount = new Account(accountTo.getAccountId(), new BigDecimal("600.25"));
		this.accountsService.transferAmount(accountFrom.getAccountId(), accountTo.getAccountId(), amount);

		assertThat(this.accountsService.getAccount(accountFrom.getAccountId()).isEqualTo(debtAccount);
		assertThat(this.accountsService.getAccount(accountTo.getAccountId()).isEqualTo(debtAccount);
	}

	@Test
	public void transferInValidAmount() throws Exception {
		String uniqueAccountIdFrom = "Id-" + UUID.randomUUID();
		String uniqueAccountIdTo = "Id-" + UUID.randomUUID();
		Account accountFrom = new Account(uniqueAccountIdFrom, new BigDecimal("1000.45"));
		Account accountTo = new Account(uniqueAccountIdTo, new BigDecimal("500.25"));
		BigDecimal amount = new BigDecimal(1100);

		this.accountsService.transferAmount(accountFrom.getAccountId(), accountTo.getAccountId(), amount);

		try {
			this.accountsService.createAccount(account);
			fail("Insufficient Balance");
		} catch (InsufficientBalanceException ex) {
			assertThat(ex.getMessage())
					.isEqualTo("Insufficient Balance. Cannot withdraw " + amount + " from acount " + accountFrom.getAccountId());
		}
	}
}
