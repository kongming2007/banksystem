package banksystem;

import java.util.Locale;

/**
 * Ngoại lệ khi số dư tài khoản không đủ.
 */
public class InsufficientFundsException extends BankException {
  private static final long serialVersionUID = 1L;

  /**
   * Tạo ngoại lệ cho giao dịch bị từ chối vì thiếu số dư.
   *
   * @param amount số tiền giao dịch
   */
  public InsufficientFundsException(double amount) {
    super("Số dư tài khoản không đủ $" + String.format(Locale.US, "%.2f", amount)
        + " để thực hiện giao dịch");
  }
}
