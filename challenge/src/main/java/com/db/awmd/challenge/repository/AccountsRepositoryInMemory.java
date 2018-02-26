package com.db.awmd.challenge.repository;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.exception.DuplicateAccountIdException;
import com.db.awmd.challenge.exception.InsufficientBalanceException;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Repository;

@Repository
public class AccountsRepositoryInMemory implements AccountsRepository {

	private final Map<String, Account> accounts = new ConcurrentHashMap<>();

	@Override
	public void createAccount(Account account) throws DuplicateAccountIdException {
		Account previousAccount = accounts.putIfAbsent(account.getAccountId(), account);
		if (previousAccount != null) {
			throw new DuplicateAccountIdException("Account id " + account.getAccountId() + " already exists!");
		}
	}

	@Override
	public Account getAccount(String accountId) {
		return accounts.get(accountId);
	}

	@Override
	public void clearAccounts() {
		accounts.clear();
	}

	/* 
	 * method is used to transfer the given amount from one acount to another
	 * @see com.db.awmd.challenge.repository.AccountsRepository#transferAmount(java.lang.String, java.lang.String, java.math.BigDecimal)
	 */
	@Override
	public void transferAmount(String accountIdFrom, String accountIdTo, BigDecimal amount) {
		Account fromAccount = accounts.get(accountIdFrom);
		Account toAccount = accounts.get(accountIdFrom);
		// Apply lock on the payer account
		synchronized (fromAccount) {
			// Apply lock on the payee account
			synchronized (toAccount) {

				try {
					// Deduct the amount from payers account
					BigDecimal currentbalance = fromAccount.withdraw(amount);
					// If the payer had sufficient balance deposit the amount to payee
					toAccount.deposit(amount);

				} catch (InsufficientBalanceException ex) {
					throw new InsufficientBalanceException(ex.getMessage());
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		}
	}

}
