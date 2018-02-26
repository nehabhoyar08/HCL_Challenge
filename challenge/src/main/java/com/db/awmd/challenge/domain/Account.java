package com.db.awmd.challenge.domain;

import com.db.awmd.challenge.exception.InsufficientBalanceException;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.NotEmpty;

@Data
@Slf4j
public class Account {

	@NotNull
	@NotEmpty
	private final String accountId;

	@NotNull
	@Min(value = 0, message = "Initial balance must be positive.")
	private BigDecimal balance;

	public Account(String accountId) {
		this.accountId = accountId;
		this.balance = BigDecimal.ZERO;
	}

	@JsonCreator
	public Account(@JsonProperty("accountId") String accountId, @JsonProperty("balance") BigDecimal balance) {
		this.accountId = accountId;
		this.balance = balance;
	}

	public synchronized void deposit(BigDecimal amount) {
		log.info(Thread.currentThread().getName() + " is trying to deposite");

		balance = balance.add(amount);
		log.info(Thread.currentThread().getName() + " depositing the amount " + amount + " updated balance =  "
				+ balance);
	}

	public synchronized BigDecimal withdraw(BigDecimal amount) {
		log.info(Thread.currentThread().getName() + " trying to withdraw " + amount + " from the account " + accountId);

		if (balance.compareTo(amount) == -1 || balance.intValue() == 0) {
			log.info("OOPS, NO BALANCE LEFT TO WITHDRAW FOR " + Thread.currentThread().getName());
			throw new InsufficientBalanceException(
					"Insufficient Balance. Cannot withdraw " + amount + " from acount " + accountId);
		}
		balance = balance.subtract(amount);
		log.info(Thread.currentThread().getName() + " successfully withdrow the amount. balance left =  " + balance);
		return balance;
	}
}
