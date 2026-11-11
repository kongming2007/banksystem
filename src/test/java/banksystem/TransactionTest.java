package banksystem;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class TransactionTest {
  @Test
  void getTypeStringReturnsDescriptionForEachType() {
    assertEquals("Nạp tiền vãng lai", Transaction.getTypeString(Transaction.TYPE_DEPOSIT_CHECKING));
    assertEquals("Rút tiền vãng lai",
        Transaction.getTypeString(Transaction.TYPE_WITHDRAW_CHECKING));
    assertEquals("Nạp tiền tiết kiệm", Transaction.getTypeString(Transaction.TYPE_DEPOSIT_SAVINGS));
    assertEquals("Rút tiền tiết kiệm",
        Transaction.getTypeString(Transaction.TYPE_WITHDRAW_SAVINGS));
  }

  @Test
  void getTypeStringReturnsUnknownForInvalidType() {
    assertEquals("Không rõ", Transaction.getTypeString(99));
  }

  @Test
  void getTransactionSummaryUsesExpectedFormat() {
    Transaction transaction =
        new Transaction(Transaction.TYPE_DEPOSIT_CHECKING, 500, 2000, 2500);
    assertEquals(
        "- Kiểu giao dịch: Nạp tiền vãng lai. Số dư ban đầu: $2000.00."
            + " Số tiền: $500.00. Số dư cuối: $2500.00.",
        transaction.getTransactionSummary());
  }
}
