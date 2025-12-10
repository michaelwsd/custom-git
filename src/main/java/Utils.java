import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.Inflater;

public class Utils {

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
