import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Tree {
    public static void runLsTree(String hash) throws Exception {
        String folderName = hash.substring(0, 2);
        String fileName = hash.substring(2);

        Path objectPath = Paths.get(Main.currentDir, ".git/objects", folderName, fileName);
        if (!Files.exists(objectPath)) throw new RuntimeException("Tree not found: " + hash);

        // decompress tree object file
        byte[] compressedData = Files.readAllBytes(objectPath);
        byte[] decompressedData = Utils.decompressZlib(compressedData);
        for (int i = 0; i < decompressedData.length; i++) {
            System.out.println(decompressedData[i]);
        }
    }
}
