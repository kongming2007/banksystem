package banksystem;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class BankTest {
  private static final String DATA =
      "Nguyen Van A 123456789\n"
          + "1001 CHECKING 2000.00\n"
          + "1002 SAVINGS 8000.00\n"
          + "Bui B 111111111\n"
          + "2001 CHECKING 50\n";

  private static Bank loadBank(String data) {
    Bank bank = new Bank();
    bank.readCustomerList(new ByteArrayInputStream(data.getBytes(StandardCharsets.UTF_8)));
    return bank;
  }

  @Test
  void readCustomerListParsesCustomersAndAccounts() {
    Bank bank = loadBank(DATA);
    assertEquals(2, bank.getCustomerList().size());
    Customer first = bank.getCustomerList().get(0);
    assertEquals(123456789L, first.getIdNumber());
    assertEquals("Nguyen Van A", first.getFullName());
    assertEquals(2, first.getAccountList().size());
    assertTrue(first.getAccountList().get(0) instanceof CheckingAccount);
    assertTrue(first.getAccountList().get(1) instanceof SavingsAccount);
  }

  @Test
  void readCustomerListWithNullStreamDoesNothing() {
    Bank bank = new Bank();
    bank.readCustomerList(null);
    assertTrue(bank.getCustomerList().isEmpty());
  }

  @Test
  void getCustomersInfoByIdOrderSortsById() {
    Bank bank = loadBank(DATA);
    assertEquals(
        "Số CMND: 111111111. Họ tên: Bui B.\n"
            + "Số CMND: 123456789. Họ tên: Nguyen Van A.",
        bank.getCustomersInfoByIdOrder());
  }

  @Test
  void getCustomersInfoByNameOrderSortsByName() {
    Bank bank = loadBank(DATA);
    assertEquals(
        "Số CMND: 111111111. Họ tên: Bui B.\n"
            + "Số CMND: 123456789. Họ tên: Nguyen Van A.",
        bank.getCustomersInfoByNameOrder());
  }

  @Test
  void getCustomersInfoOfEmptyBankIsEmpty() {
    assertEquals("", new Bank().getCustomersInfoByIdOrder());
  }
}
