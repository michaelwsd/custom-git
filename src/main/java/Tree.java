import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Tree {
    public static void runLsTree(String hash) throws Exception {
        String folderName = hash.substring(0, 2);
        String fileName = hash.substring(2);

        Path objectPath = Paths.get(Main.currentDir, ".git/objects", folderName, fileName);
        if (!Files.exists(objectPath)) throw new RuntimeException("Tree not found: " + hash);

        // decompress tree object file
        byte[] compressedData = Files.readAllBytes(objectPath);
        byte[] decompressedData = Utils.decompressZlib(compressedData);
        List<String> splitData = byteToString(decompressedData);

        // print each file name
        for (int i = 1; i < splitData.size()-1; i++) {
            String curr = splitData.get(i);
            String[] parts = curr.split(" ");
            String name = parts[parts.length-1];
            System.out.println(name);
        }
    }

    public static List<String> byteToString(byte[] data) {
        List<String> result = new ArrayList<>();
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        for (byte b: data) {
            if (b == 0) {
                result.add(buffer.toString(StandardCharsets.UTF_8));
                buffer.reset();
            } else {
                buffer.write(b);
            }
        }

        if (buffer.size() > 0) {
            result.add(buffer.toString(StandardCharsets.UTF_8));
        }

        return result;
    }
}
