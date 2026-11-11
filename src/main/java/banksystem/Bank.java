package banksystem;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Ngân hàng: quản lý danh sách khách hàng và đọc dữ liệu từ luồng nhập.
 */
public class Bank {
  private static final Logger LOGGER = LoggerFactory.getLogger(Bank.class);
  private static final Pattern ID_PATTERN = Pattern.compile("\\d{9}");
  private static final int ACCOUNT_FIELD_COUNT = 3;

  private List<Customer> customerList;

  /**
   * Tạo ngân hàng với danh sách khách hàng rỗng.
   */
  public Bank() {
    this.customerList = new ArrayList<>();
  }

  /**
   * Trả về danh sách khách hàng.
   *
   * @return danh sách khách hàng
   */
  public List<Customer> getCustomerList() {
    return customerList;
  }

  /**
   * Đặt danh sách khách hàng; nếu truyền null thì dùng danh sách rỗng.
   *
   * @param customerList danh sách khách hàng mới
   */
  public void setCustomerList(List<Customer> customerList) {
    if (customerList == null) {
      this.customerList = new ArrayList<>();
    } else {
      this.customerList = customerList;
    }
  }

  /**
   * Đọc danh sách khách hàng và tài khoản từ một luồng nhập.
   *
   * <p>Dòng kết thúc bằng số CMND 9 chữ số là một khách hàng mới; các dòng
   * "số_tài_khoản loại số_dư" tiếp theo thuộc về khách hàng đó.
   *
   * @param inputStream luồng dữ liệu (không đóng luồng này); null thì bỏ qua
   */
  public void readCustomerList(InputStream inputStream) {
    if (inputStream == null) {
      LOGGER.warn("Input stream is null, no customer data to read");
      return;
    }
    LOGGER.debug("Start reading customer data");
    BufferedReader reader =
        new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
    Customer current = null;
    try {
      for (String line = reader.readLine(); line != null; line = reader.readLine()) {
        current = processLine(line.trim(), current);
      }
      LOGGER.info("Finished reading customer data: {} customers", customerList.size());
    } catch (IOException | NumberFormatException e) {
      LOGGER.error("Failed to read customer data, stopped after {} customers",
          customerList.size(), e);
    }
  }

  /**
   * Đọc danh sách khách hàng và tài khoản từ một file.
   *
   * @param file đường dẫn tới file dữ liệu
   * @throws IOException nếu không mở được file
   */
  public void readCustomerListFromFile(Path file) throws IOException {
    LOGGER.debug("Reading customer data from file: {}", file);
    try (InputStream inputStream = Files.newInputStream(file)) {
      readCustomerList(inputStream);
    }
  }

  private Customer processLine(String line, Customer current) {
    int lastSpace = line.lastIndexOf(' ');
    if (lastSpace <= 0) {
      return current;
    }
    String token = line.substring(lastSpace + 1).trim();
    if (ID_PATTERN.matcher(token).matches()) {
      String name = line.substring(0, lastSpace).trim();
      Customer customer = new Customer(Long.parseLong(token), name);
      customerList.add(customer);
      LOGGER.debug("Added customer: id={}, name={}", token, name);
      return customer;
    }
    if (current != null) {
      addAccountFromLine(line, current);
    }
    return current;
  }

  private void addAccountFromLine(String line, Customer owner) {
    String[] parts = line.split("\\s+");
    if (parts.length < ACCOUNT_FIELD_COUNT) {
      LOGGER.warn("Skipped malformed account line: '{}'", line);
      return;
    }
    long number = Long.parseLong(parts[0]);
    double balance = Double.parseDouble(parts[2]);
    if (Account.CHECKING_TYPE.equals(parts[1])) {
      owner.addAccount(new CheckingAccount(number, balance));
    } else if (Account.SAVINGS_TYPE.equals(parts[1])) {
      owner.addAccount(new SavingsAccount(number, balance));
    } else {
      LOGGER.warn("Unknown account type '{}' for account {}", parts[1], number);
    }
  }

  /**
   * Trả về thông tin khách hàng, sắp xếp tăng dần theo số CMND.
   *
   * @return thông tin khách hàng, mỗi khách hàng một dòng
   */
  public String getCustomersInfoByIdOrder() {
    customerList.sort(Comparator.comparingLong(Customer::getIdNumber));
    return joinCustomerInfo(customerList);
  }

  /**
   * Trả về thông tin khách hàng, sắp xếp theo họ tên rồi đến số CMND.
   *
   * @return thông tin khách hàng, mỗi khách hàng một dòng
   */
  public String getCustomersInfoByNameOrder() {
    List<Customer> copy = new ArrayList<>(customerList);
    copy.sort(Comparator.comparing(Customer::getFullName)
        .thenComparingLong(Customer::getIdNumber));
    return joinCustomerInfo(copy);
  }

  private static String joinCustomerInfo(List<Customer> customers) {
    return customers.stream()
        .map(Customer::getCustomerInfo)
        .collect(Collectors.joining("\n"));
  }
}
