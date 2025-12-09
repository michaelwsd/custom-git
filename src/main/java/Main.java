import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.Inflater;

public class Main {
  private static String currentDir = System.getProperty("user.dir");

  public static void main(String[] args) {
    final String command = args[0];
    
    switch (command) {
      case "init" -> {
        final File root = new File(".git");
        new File(root, "objects").mkdirs();
        new File(root, "refs").mkdirs();
        final File head = new File(root, "HEAD");
    
        try {
          head.createNewFile();
          Files.write(head.toPath(), "ref: refs/heads/main\n".getBytes());
          System.out.println("Initialized git directory");
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      }
      case "cat-file" -> {
        if (args.length < 3) throw new IllegalArgumentException("Usage: cat-file <flag> <hash>");
        String flag = args[1], hash = args[2];

        if (!flag.equals("-p")) throw new IllegalArgumentException("Only -p is supported");

        String folderName = hash.substring(0, 2), fileName = hash.substring(2);
        Path objectPath= Paths.get(currentDir, ".git/objects", folderName, fileName);

        if (!Files.exists(objectPath)) throw new RuntimeException("Object not found: " + hash);
        byte[] compressed;

        try {
          compressed = Files.readAllBytes(objectPath);
        } catch (IOException e) {
          throw new RuntimeException(e);
        }

        byte[] decompressed = decompressZlib(compressed);
        String content = extractString(decompressed);

        // print content
        System.out.print(new String(content));

      }
      default -> System.out.println("Unknown command: " + command);
    }
  }

  public static String extractString(byte[] decompressed) {
    int nullIndex = -1;
    for (int i = 0; i < decompressed.length; i++) {
      System.out.println(decompressed[i]);
      if (decompressed[i] == 0) {
          nullIndex = i;
          break;
      }
    }

    if (nullIndex == -1) throw new RuntimeException("Invalid Git object format");

    // extract content after header
    byte[] content = new byte[decompressed.length - nullIndex - 1];
    System.arraycopy(decompressed, nullIndex + 1, content, 0, content.length);

    return new String(content);
  }

  public static byte[] decompressZlib(byte[] compressedData) {
    try {
      Inflater inflater = new Inflater();
      inflater.setInput(compressedData);

      byte[] buffer = new byte[8192];
      byte[] result = new byte[0];
      int len;

      while (!inflater.finished()) {
        len = inflater.inflate(buffer);

        if (len == 0 && inflater.needsInput()) break;

        byte[] newResult = new byte[result.length + len];
        System.arraycopy(result, 0, newResult, 0, result.length);
        System.arraycopy(buffer, 0, newResult, result.length, len);
        result = newResult;
      }

      inflater.end();
      inflater.close();
      return result;

    } catch (Exception e) {
      throw new RuntimeException("Failed to decompress zlib data", e);
    }
  }
}
