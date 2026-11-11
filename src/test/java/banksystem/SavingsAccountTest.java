package banksystem;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class SavingsAccountTest {
  private static final double DELTA = 0.001;

  @Test
  void withdrawWithinLimitsSucceeds() {
    Account account = new SavingsAccount(2001, 8000);
    account.withdraw(500);
    assertEquals(7500, account.getBalance(), DELTA);
    assertEquals(Transaction.TYPE_WITHDRAW_SAVINGS,
        account.getTransactionList().get(0).getType());
  }

  @Test
  void withdrawOverMaxWithdrawIsRejected() {
    Account account = new SavingsAccount(2001, 10000);
    account.withdraw(SavingsAccount.MAX_WITHDRAW + 1);
    assertEquals(10000, account.getBalance(), DELTA);
    assertTrue(account.getTransactionList().isEmpty());
  }

  @Test
  void withdrawBelowMinBalanceIsRejected() {
    Account account = new SavingsAccount(2001, 5500);
    account.withdraw(600);
    assertEquals(5500, account.getBalance(), DELTA);
    assertTrue(account.getTransactionList().isEmpty());
  }

  @Test
  void depositIncreasesBalance() {
    Account account = new SavingsAccount(2001, 5000);
    account.deposit(250);
    assertEquals(5250, account.getBalance(), DELTA);
    assertEquals(Transaction.TYPE_DEPOSIT_SAVINGS,
        account.getTransactionList().get(0).getType());
  }
}
