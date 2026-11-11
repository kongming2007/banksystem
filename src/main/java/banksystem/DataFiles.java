package banksystem;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Tiện ích dựng đường dẫn tới file dữ liệu, độc lập với hệ điều hành.
 */
public final class DataFiles {
  private DataFiles() {
  }

  /**
   * Ghép thư mục và tên file thành một đường dẫn.
   *
   * <p>Dùng {@link Paths#get(String, String...)} để Java tự chọn dấu phân cách
   * phù hợp (dấu gạch chéo trên Linux/macOS, dấu gạch ngược trên Windows)
   * thay vì viết cứng.
   *
   * @param directory thư mục chứa file
   * @param fileName tên file
   * @return đường dẫn đầy đủ tới file
   */
  public static Path resolve(String directory, String fileName) {
    return Paths.get(directory, fileName);
  }
}
