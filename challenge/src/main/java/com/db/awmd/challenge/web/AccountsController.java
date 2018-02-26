package com.db.awmd.challenge.web;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.exception.DuplicateAccountIdException;
import com.db.awmd.challenge.exception.InsufficientBalanceException;
import com.db.awmd.challenge.service.AccountsService;
import com.db.awmd.challenge.service.NotificationService;

import java.math.BigDecimal;

import javax.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/accounts")
@Slf4j
public class AccountsController {

	private final AccountsService accountsService;
	private final NotificationService notificationService;

	@Autowired
	public AccountsController(AccountsService accountsService, NotificationService notificationService) {
		this.accountsService = accountsService;
		this.notificationService = notificationService;
	}

	@PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Object> createAccount(@RequestBody @Valid Account account) {
		log.info("Creating account {}", account);

		try {
			this.accountsService.createAccount(account);
		} catch (DuplicateAccountIdException daie) {
			return new ResponseEntity<>(daie.getMessage(), HttpStatus.BAD_REQUEST);
		}

		return new ResponseEntity<>(HttpStatus.CREATED);
	}

	@GetMapping(path = "/{accountId}")
	public Account getAccount(@PathVariable String accountId) {
		log.info("Retrieving account for id {}", accountId);
		return this.accountsService.getAccount(accountId);
	}

	/**
	 * @author Neha Bhoyar
	 * @param accountIdFrom
	 * @param accountIdTo
	 * @param amount
	 * @return Http status code 200 OK
	 */
	@RequestMapping(value = "/transferAmount", method = RequestMethod.POST)
	public ResponseEntity<Object> transferAmount(@PathVariable String accountIdFrom, @PathVariable String accountIdTo,
			@PathVariable BigDecimal amount) {

		try {
			// call accountservice to deduct the transfer amount from the payer and to
			// credit the same in payee's account
			this.accountsService.transferAmount(accountIdFrom, accountIdTo, amount);
			
			// send the debit notification to the payer
			this.notificationService.notifyAboutTransfer(this.accountsService.getAccount(accountIdFrom),
					"Successfully debited the amount " + amount + " to " + accountIdTo);
			
			// send the credit notification to the payee
			this.notificationService.notifyAboutTransfer(this.accountsService.getAccount(accountIdTo),
					"Successfully credited the amount " + amount + " from " + accountIdFrom);
		} catch (InsufficientBalanceException message) {
			return new ResponseEntity<>(message.getMessage(), HttpStatus.BAD_REQUEST);
		}

		return new ResponseEntity<>(HttpStatus.OK);
	}

}
