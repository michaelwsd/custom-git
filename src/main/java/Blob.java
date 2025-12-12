import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Blob {

    public static String runCatFile(String hash) throws Exception {
        String folderName = hash.substring(0, 2), fileName = hash.substring(2);
        Path objectPath = Paths.get(Main.currentDir, ".git/objects", folderName, fileName);

        if (!Files.exists(objectPath)) throw new RuntimeException("Object not found: " + hash);
        byte[] compressed = Files.readAllBytes(objectPath);;
        byte[] decompressed = Utils.decompressZlib(compressed);
        String content = extractString(decompressed);

        return content;
    }

    public static String runHashObject(String file) throws Exception {
        Path path = Paths.get(file);

        if (!Files.exists(path)) throw new RuntimeException("Object not found: " + file);
        // get the sha-1 encoded objectId that gives folder and file
        byte[] objectContent = getObjectContent(path);
        String objectId = Utils.computeSHA1(objectContent);
        String folderName = objectId.substring(0, 2), fileName = objectId.substring(2);

        // compress content with zlib
        byte[] compressedData = Utils.compressZlib(objectContent);

        // write file to object id path
        Path objectDir = Paths.get(".git/objects", folderName);
        Files.createDirectories(objectDir);

        Path objectFile = objectDir.resolve(fileName); // add file name to path
        Files.write(objectFile, compressedData);

        return objectId;
    }

    // Turn blob header to byte array
    public static byte[] getObjectContent(Path path) throws Exception {
        byte[] content = Files.readAllBytes(path);
        String header = "blob " + content.length + "\0";
        byte[] headerBytes = header.getBytes(StandardCharsets.UTF_8);

        // create input byte array
        byte[] store = new byte[headerBytes.length + content.length];
        System.arraycopy(headerBytes, 0, store, 0, headerBytes.length);
        System.arraycopy(content, 0, store, headerBytes.length, content.length);

        return store;
    }

    // Extract string content from blob header in bytes
    public static String extractString(byte[] decompressed) {
        int nullIndex = -1;
        for (int i = 0; i < decompressed.length; i++) {
        if (decompressed[i] == 0) { // go until a null byte
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
}
