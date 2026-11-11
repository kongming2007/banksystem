package banksystem;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tài khoản tiết kiệm, có giới hạn số tiền rút và số dư tối thiểu.
 */
public class SavingsAccount extends Account {
  /** Số tiền tối đa được rút trong một lần. */
  public static final double MAX_WITHDRAW = 1000.0;

  /** Số dư tối thiểu phải giữ lại sau khi rút. */
  public static final double MIN_BALANCE = 5000.0;

  private static final Logger LOGGER = LoggerFactory.getLogger(SavingsAccount.class);

  /**
   * Tạo tài khoản tiết kiệm.
   *
   * @param accountNumber số tài khoản
   * @param balance số dư ban đầu
   */
  public SavingsAccount(long accountNumber, double balance) {
    super(accountNumber, balance);
  }

  @Override
  public void deposit(double amount) {
    LOGGER.debug("Processing deposit: account={}, amount={}", getAccountNumber(), amount);
    double initialBalance = getBalance();
    try {
      doDepositing(amount);
      recordTransaction(Transaction.TYPE_DEPOSIT_SAVINGS, amount, initialBalance);
    } catch (BankException e) {
      LOGGER.warn("Deposit rejected: account={}, amount={}, reason={}",
          getAccountNumber(), amount, e.getMessage());
    }
  }

  @Override
  public void withdraw(double amount) {
    double initialBalance = getBalance();
    try {
      if (amount > MAX_WITHDRAW) {
        throw new InvalidFundingAmountException(amount);
      }
      if (initialBalance - amount < MIN_BALANCE) {
        throw new InsufficientFundsException(amount);
      }
      doWithdrawing(amount);
      recordTransaction(Transaction.TYPE_WITHDRAW_SAVINGS, amount, initialBalance);
    } catch (BankException e) {
      LOGGER.warn("Withdraw rejected: account={}, amount={}, balance={}, reason={}",
          getAccountNumber(), amount, initialBalance, e.getMessage());
    }
  }
}
