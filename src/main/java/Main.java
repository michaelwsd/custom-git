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
      Input: Blob Hash
      1. From the hash, we get the folder name (first 2) and file name (rest 38).
      2. Retrieve file from .git/objects/<first2>/<rest38>.
      3. Decompress using zlib, we get the blob header -> blob <size>\0<content>, in bytes.
      4. Read until a null byte \0, retrieve the content after it, turn into String.
      */
      case "cat-file" -> {
        if (args.length != 3) throw new IllegalArgumentException("Usage: cat-file <flag> <hash>");
        String flag = args[1], hash = args[2];
        if (!flag.equals("-p")) throw new IllegalArgumentException("Only -p is supported");

        String content = Blob.runCatFile(hash);
        System.out.print(content);
      }

      /*
      Input: File Name
      1. Create blob header -> "blob <size>\0<content>", in bytes.
      2. Compute SHA-1 encoding of the blob header + content in bytes, giving the folder (first 2) and file (rest 38) in .git/objects.
      3. Compress blob header + content in bytes using zlib and store it in .git/objects/<first2>/<rest38>.
      */
      case "hash-object" -> {
        if (args.length != 3) throw new IllegalArgumentException("Usage: hash-object <flag> <file>");
        String flag = args[1], file = args[2];
        if (!flag.equals("-w")) throw new IllegalArgumentException("Only -w is supported");
        
        String objectId = Blob.runHashObject(file);
        System.out.println(objectId);
      }

      /*
      Input: Tree Hash
      1. Get object path from tree hash, decode the object file using zlib.
      2. Parse the decoded object, print out each name from <mode> <name>\0<20-byte-sha>.
      */
      case "ls-tree" -> {
        if (args.length != 3) throw new IllegalArgumentException("Usage: ls-tree <flag> <hash>");
        String flag = args[1], hash = args[2];
        if (!flag.equals("--name-only")) throw new IllegalArgumentException("Only --name-only is supported");

        Tree.runLsTree(hash, true);
      }

      /*
      Input: None
      If file -> Write blob
      If directory -> Recursively write blob
      */
      case "write-tree" -> {
        if (args.length > 1) throw new IllegalArgumentException("Usage: write-tree");

        Tree.runWriteTree();
      }

      /*
      Input: Tree-SHA, Parent-SHA, Message
      1. Build header and body in bytes.
      2. Computer SHA-1 of the object to get folder and file. 
      3. Compress the object using zlib, store it in the folder and file. 
      */
      case "commit-tree" -> {
        if (args.length != 6) throw new IllegalArgumentException("Usage: commit-tree <tree_sha> -p <commit_sha> -m <message>");
        String treeSha = args[1], flag1 = args[2], commitSha = args[3], flag2 = args[4], message = args[5];
        if (!flag1.equals("-p") || !flag2.equals("-m")) throw new IllegalArgumentException("Only -p and -m are supported");

        Commit.runCommitTree(treeSha, commitSha, message);
      }

      default -> System.out.println("Unknown command: " + command);
    }
  }
}
