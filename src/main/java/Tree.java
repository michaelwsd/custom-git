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

    public static void runWriteTree() throws Exception {
        Path root = Paths.get(Main.currentDir);
        String sha = writeTree(root);
        System.out.println(sha);
    }

    // Recursive function to write a blob or tree object
    public static String writeTree(Path dir) throws Exception {
        List<byte[]> entries = new ArrayList<>();
        
        try (var stream = Files.list(dir)) {
            for (Path entry: stream.toList()) {
                if (entry.getFileName().toString().equals(".git")) continue;

                // case if it's a directory
                if (Files.isDirectory(entry)) {
                    addObject(entry, entries, true);
                } else {
                    addObject(entry, entries, false);
                }
            }
        }

        ByteArrayOutputStream treeBody = new ByteArrayOutputStream();
        for (byte[] e: entries) treeBody.write(e);
        
        byte[] body = treeBody.toByteArray();
        String header = "tree " + body.length + "\0";
        byte[] headerBytes = header.getBytes(StandardCharsets.UTF_8);

        byte[] full = new byte[headerBytes.length + body.length];
        System.arraycopy(headerBytes, 0, full, 0, headerBytes.length);
        System.arraycopy(body, 0, full, headerBytes.length, body.length);

        // Compute SHA and write object
        String sha = Utils.computeSHA1(full);
        byte[] compressed = Utils.compressZlib(full);

        String folderName = sha.substring(0, 2);
        String fileName = sha.substring(2);
        Path objDir = Paths.get(".git/objects", folderName);
        Files.createDirectories(objDir);
        Files.write(objDir.resolve(fileName), compressed);

        return sha;
    }

    public static void addObject(Path entry, List<byte[]> entries, boolean isDir) throws Exception {
        String sha1 = isDir ? writeTree(entry) : Blob.runHashObject(entry.toString());
        String mode = isDir ? "40000" : (Files.isExecutable(entry) ? "100755" : "100644");
        String name = entry.getFileName().toString();
        byte[] shaRaw = Utils.hexToBytes(sha1);

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        buffer.write((mode + " " + name + "\0").getBytes(StandardCharsets.UTF_8));
        buffer.write(shaRaw);
        entries.add(buffer.toByteArray());
    }

    // Split a byte array to a list of strings, split on null byte
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
