package banksystem;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Lớp trừu tượng đại diện cho một tài khoản ngân hàng.
 */
public abstract class Account {
  public static final String CHECKING_TYPE = "CHECKING";
  public static final String SAVINGS_TYPE = "SAVINGS";

  private static final Logger LOGGER = LoggerFactory.getLogger(Account.class);

  private long accountNumber;
  private double balance;
  private List<Transaction> transactionList;

  /**
   * Tạo tài khoản với số tài khoản và số dư ban đầu.
   *
   * @param accountNumber số tài khoản
   * @param balance số dư ban đầu
   */
  public Account(long accountNumber, double balance) {
    this.accountNumber = accountNumber;
    this.balance = balance;
    this.transactionList = new ArrayList<>();
  }

  /**
   * Trả về số tài khoản.
   *
   * @return số tài khoản
   */
  public long getAccountNumber() {
    return accountNumber;
  }

  /**
   * Đặt số tài khoản.
   *
   * @param accountNumber số tài khoản
   */
  public void setAccountNumber(long accountNumber) {
    this.accountNumber = accountNumber;
  }

  /**
   * Trả về số dư hiện tại.
   *
   * @return số dư hiện tại
   */
  public double getBalance() {
    return balance;
  }

  /**
   * Đặt số dư (chỉ dành cho lớp con).
   *
   * @param balance số dư mới
   */
  protected void setBalance(double balance) {
    this.balance = balance;
  }

  /**
   * Trả về danh sách giao dịch.
   *
   * @return danh sách giao dịch
   */
  public List<Transaction> getTransactionList() {
    return transactionList;
  }

  /**
   * Đặt danh sách giao dịch; nếu truyền null thì dùng danh sách rỗng.
   *
   * @param transactionList danh sách giao dịch mới
   */
  public void setTransactionList(List<Transaction> transactionList) {
    if (transactionList == null) {
      this.transactionList = new ArrayList<>();
    } else {
      this.transactionList = transactionList;
    }
  }

  /**
   * Nạp tiền vào tài khoản.
   *
   * @param amount số tiền cần nạp
   */
  public abstract void deposit(double amount);

  /**
   * Rút tiền khỏi tài khoản.
   *
   * @param amount số tiền cần rút
   */
  public abstract void withdraw(double amount);

  /**
   * Thực hiện nạp tiền, kiểm tra số tiền hợp lệ.
   *
   * @param amount số tiền cần nạp
   * @throws InvalidFundingAmountException nếu số tiền không dương
   */
  protected void doDepositing(double amount) throws InvalidFundingAmountException {
    if (amount <= 0) {
      throw new InvalidFundingAmountException(amount);
    }
    balance += amount;
  }

  /**
   * Thực hiện rút tiền, kiểm tra số tiền và số dư.
   *
   * @param amount số tiền cần rút
   * @throws BankException nếu số tiền không hợp lệ hoặc số dư không đủ
   */
  protected void doWithdrawing(double amount) throws BankException {
    if (amount <= 0) {
      throw new InvalidFundingAmountException(amount);
    }
    if (amount > balance) {
      throw new InsufficientFundsException(amount);
    }
    balance -= amount;
  }

  /**
   * Thêm một giao dịch vào lịch sử (bỏ qua nếu null).
   *
   * @param transaction giao dịch cần thêm
   */
  public void addTransaction(Transaction transaction) {
    if (transaction != null) {
      transactionList.add(transaction);
    }
  }

  /**
   * Ghi nhận giao dịch vừa thành công: lưu vào lịch sử và ghi log.
   *
   * @param type loại giao dịch (hằng số Transaction.TYPE_*)
   * @param amount số tiền giao dịch
   * @param initialBalance số dư trước giao dịch
   */
  protected void recordTransaction(int type, double amount, double initialBalance) {
    addTransaction(new Transaction(type, amount, initialBalance, balance));
    LOGGER.info("Transaction OK: account={}, type={}, amount={}, balance {} -> {}",
        accountNumber, Transaction.getTypeString(type), amount, initialBalance, balance);
  }

  /**
   * Trả về lịch sử giao dịch dạng text.
   *
   * @return chuỗi lịch sử giao dịch của tài khoản
   */
  public String getTransactionHistory() {
    String summaries = transactionList.stream()
        .map(Transaction::getTransactionSummary)
        .collect(Collectors.joining("\n"));
    LOGGER.debug("Built transaction history: account={}, count={}",
        accountNumber, transactionList.size());
    return "Lịch sử giao dịch của tài khoản " + accountNumber + ":\n" + summaries;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof Account)) {
      return false;
    }
    Account other = (Account) obj;
    return this.accountNumber == other.accountNumber;
  }

  @Override
  public int hashCode() {
    return (int) (accountNumber ^ (accountNumber >>> 32));
  }
}
