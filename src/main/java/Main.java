import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class Main {
  public static String currentDir = System.getProperty("user.dir");

  public static void main(String[] args) throws Exception {
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

      /*
      Input: SHA-1 Encoded Hash
      1. From the hash, we get the folder name (first 2) and file name (rest 38)
      2. Retrieve file from .git/objects/<first2>/<rest38>
      3. Decompress using zlib, we get the blob header -> blob <size>\0<content>, in bytes
      4. Read until a null byte \0, retrieve the content after it, turn into String
      */
      case "cat-file" -> {
        if (args.length < 3) throw new IllegalArgumentException("Usage: cat-file <flag> <hash>");
        String flag = args[1], hash = args[2];
        if (!flag.equals("-p")) throw new IllegalArgumentException("Only -p is supported");

        Blob.runCatFile(hash);
      }

      /*
      Input: File Name
      1. Create blob header -> "blob <size>\0<content>", in bytes
      2. Compute SHA-1 encoding of the content, giving the folder (first 2) and file (rest 38) in .git/objects
      3. Compress blob header using zlib and store it in .git/objects/<first2>/<rest38> 
      */
      case "hash-object" -> {
        if (args.length < 3) throw new IllegalArgumentException("Usage: hash-object <flag> <file>");
        String flag = args[1], file = args[2];
        if (!flag.equals("-w")) throw new IllegalArgumentException("Only -w is supported");
        
        Blob.runHashObject(file);
      }

      case "ls-tree" -> {
        if (args.length < 3) throw new IllegalArgumentException("Usage: ls-tree <flag> <hash>");
        String flag = args[1], hash = args[2];
        if (!flag.equals("--name-only")) throw new IllegalArgumentException("Only --name-only is supported");

        Tree.runLsTree(hash);
      }

      default -> System.out.println("Unknown command: " + command);
    }
  }
}
