package banksystem;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class CheckingAccountTest {
  private static final double DELTA = 0.001;

  @Test
  void depositIncreasesBalanceAndRecordsTransaction() {
    Account account = new CheckingAccount(1001, 1000);
    account.deposit(500);
    assertEquals(1500, account.getBalance(), DELTA);
    assertEquals(1, account.getTransactionList().size());
    Transaction transaction = account.getTransactionList().get(0);
    assertEquals(Transaction.TYPE_DEPOSIT_CHECKING, transaction.getType());
    assertEquals(1000, transaction.getInitialBalance(), DELTA);
    assertEquals(1500, transaction.getFinalBalance(), DELTA);
  }

  @Test
  void depositNonPositiveAmountIsRejected() {
    Account account = new CheckingAccount(1001, 1000);
    account.deposit(-50);
    account.deposit(0);
    assertEquals(1000, account.getBalance(), DELTA);
    assertTrue(account.getTransactionList().isEmpty());
  }

  @Test
  void withdrawDecreasesBalance() {
    Account account = new CheckingAccount(1001, 1000);
    account.withdraw(400);
    assertEquals(600, account.getBalance(), DELTA);
    assertEquals(Transaction.TYPE_WITHDRAW_CHECKING,
        account.getTransactionList().get(0).getType());
  }

  @Test
  void withdrawMoreThanBalanceIsRejected() {
    Account account = new CheckingAccount(1001, 1000);
    account.withdraw(1000.01);
    assertEquals(1000, account.getBalance(), DELTA);
    assertTrue(account.getTransactionList().isEmpty());
  }
}
