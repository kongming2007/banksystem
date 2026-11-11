package banksystem;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class DataFilesTest {
  @TempDir
  Path tempDir;

  @Test
  void resolveUsesPlatformSeparator() {
    Path path = DataFiles.resolve("data", "customers.txt");
    assertEquals("data" + File.separator + "customers.txt", path.toString());
  }

  @Test
  void resolveMatchesPathApi() {
    Path path = DataFiles.resolve(tempDir.toString(), "customers.txt");
    assertEquals(tempDir.resolve("customers.txt"), path);
  }

  @Test
  void readCustomerListFromFileFoundByResolvedPath() throws IOException {
    Files.write(tempDir.resolve("customers.txt"),
        "Nguyen Van A 123456789\n1001 CHECKING 2000.00\n".getBytes(StandardCharsets.UTF_8));

    Bank bank = new Bank();
    bank.readCustomerListFromFile(DataFiles.resolve(tempDir.toString(), "customers.txt"));

    assertEquals(1, bank.getCustomerList().size());
    assertEquals(1, bank.getCustomerList().get(0).getAccountList().size());
  }
}
