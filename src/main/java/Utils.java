import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.Inflater;

public class Utils {

    /*  
    Input: 40 Character SHA-1
    Each entry (file or directory) in a Git tree object is stored as <mode> <name>\0<20 raw bytes of hash>.
    The last part is the SHA-1 of the file not as text, but as 20 raw bytes. 
    SHA-1 has 40 hex (hexadecimal digits), 1 hex digit is 4 bits -> 2 hex digits is 1 byte. 
    This function essentially converts 40 hex digits to 20 raw bytes. 
    */
    public static byte[] hexToBytes(String sha1) {
        byte[] res = new byte[20];
        for (int i = 0; i < 20; i++) {
            int index = i * 2; // starting index
            res[i] = (byte) Integer.parseInt(sha1.substring(index, index + 2), 16);
        }

        return res;
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
