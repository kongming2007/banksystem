package banksystem;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tài khoản vãng lai.
 */
public class CheckingAccount extends Account {
  private static final Logger LOGGER = LoggerFactory.getLogger(CheckingAccount.class);

  /**
   * Tạo tài khoản vãng lai.
   *
   * @param accountNumber số tài khoản
   * @param balance số dư ban đầu
   */
  public CheckingAccount(long accountNumber, double balance) {
    super(accountNumber, balance);
  }

  @Override
  public void deposit(double amount) {
    double initialBalance = getBalance();
    try {
      doDepositing(amount);
      recordTransaction(Transaction.TYPE_DEPOSIT_CHECKING, amount, initialBalance);
    } catch (BankException e) {
      LOGGER.warn("Deposit rejected: account={}, amount={}, reason={}",
          getAccountNumber(), amount, e.getMessage());
    }
  }

  @Override
  public void withdraw(double amount) {
    double initialBalance = getBalance();
    try {
      doWithdrawing(amount);
      recordTransaction(Transaction.TYPE_WITHDRAW_CHECKING, amount, initialBalance);
    } catch (BankException e) {
      LOGGER.warn("Withdraw rejected: account={}, amount={}, balance={}, reason={}",
          getAccountNumber(), amount, initialBalance, e.getMessage());
    }
  }
}
