import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.Inflater;

public class Blob {

    public static void runCatFile(String hash) throws Exception {
        String folderName = hash.substring(0, 2), fileName = hash.substring(2);
        Path objectPath = Paths.get(Main.currentDir, ".git/objects", folderName, fileName);

        if (!Files.exists(objectPath)) throw new RuntimeException("Object not found: " + hash);
        byte[] compressed = Files.readAllBytes(objectPath);;
        byte[] decompressed = decompressZlib(compressed);
        String content = extractString(decompressed);

        // print content
        System.out.print(content);
    }

    public static void runHashObject(String file) throws Exception {
        Path path = Paths.get(file);

        if (!Files.exists(path)) throw new RuntimeException("Object not found: " + file);
        // get the sha-1 encoded objectId that gives folder and file
        byte[] objectContent = getObjectContent(path);
        String objectId = computeSHA1(objectContent);
        String folderName = objectId.substring(0, 2), fileName = objectId.substring(2);

        // compress content with zlib
        byte[] compressedData = compressZlib(objectContent);

        // write file to object id path
        Path objectDir = Paths.get(".git/objects", folderName);
        Files.createDirectories(objectDir);

        Path objectFile = objectDir.resolve(fileName); // add file name to path
        Files.write(objectFile, compressedData);

        System.out.println(objectId);
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

    // SHA-1 Encoding
    public static String computeSHA1(byte[] store) throws Exception { // path of the file
        MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
        byte[] hashBytes = sha1.digest(store);

        // compute sha-1
        StringBuilder sb = new StringBuilder();
        for (byte b: hashBytes) sb.append(String.format("%02x", b));
        String objectId = sb.toString();

        return objectId; // first 2 is dir, last 38 is file name
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

    // Zlib Compression
    public static byte[] compressZlib(byte[] decompressedData) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(); // in memory stream that collects bytes, don't need to write to file, everything in memory
        DeflaterOutputStream deflaterOutputStream = new DeflaterOutputStream(outputStream); // compresses any data written to it using zlib
        deflaterOutputStream.write(decompressedData);
        deflaterOutputStream.close(); // flush all remaining compressed bytes to outputStream
        
        return outputStream.toByteArray();
    }

    // Zlib Decompression
    public static byte[] decompressZlib(byte[] compressedData) {
        try {
            Inflater inflater = new Inflater(); // zlib decompressor 
            inflater.setInput(compressedData);

            byte[] buffer = new byte[8192];
            byte[] result = new byte[0];
            int len;

            while (!inflater.finished()) {
                len = inflater.inflate(buffer);

                if (len == 0 && inflater.needsInput()) break;

                byte[] newResult = new byte[result.length + len]; // resize
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
