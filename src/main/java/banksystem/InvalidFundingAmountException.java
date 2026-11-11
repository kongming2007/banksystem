package banksystem;

import java.util.Locale;

/**
 * Ngoại lệ khi số tiền giao dịch không hợp lệ.
 */
public class InvalidFundingAmountException extends BankException {
  private static final long serialVersionUID = 1L;

  /**
   * Tạo ngoại lệ cho số tiền giao dịch không hợp lệ.
   *
   * @param amount số tiền không hợp lệ
   */
  public InvalidFundingAmountException(double amount) {
    super("Số tiền không hợp lệ: $" + String.format(Locale.US, "%.2f", amount));
  }
}
