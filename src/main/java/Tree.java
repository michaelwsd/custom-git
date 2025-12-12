import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Tree {
    public static void runLsTree(String hash, boolean nameOnly) throws Exception {
        String folderName = hash.substring(0, 2);
        String fileName = hash.substring(2);

        Path objectPath = Paths.get(Main.currentDir, ".git/objects", folderName, fileName);
        if (!Files.exists(objectPath)) throw new RuntimeException("Tree not found: " + hash);

        // decompress tree object file
        byte[] compressedData = Files.readAllBytes(objectPath);
        byte[] decompressedData = Utils.decompressZlib(compressedData);
        
        // skip header
        int i = 0;
        while (decompressedData[i] != 0) i++;
        i++;

        while (i < decompressedData.length) {
            // read mode
            int start = i;
            while (decompressedData[i] != ' ') i++;
            String mode = new String(decompressedData, start, i-start);
            i++;

            // read filename
            start = i;
            while (decompressedData[i] != 0) i++;
            String file = new String(decompressedData, start, i-start);
            i++;
            
            // Read 20-byte binary SHA
            byte[] shaBytes = new byte[20];
            System.arraycopy(decompressedData, i, shaBytes, 0, 20);
            i += 20;
    
            String shaHex = byteToHex(shaBytes);
    
            if (nameOnly) System.out.println(file);
            else System.out.println(mode + " blob " + shaHex + "\t" + file);
        }
    }

    private static String byteToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) sb.append(String.format("%02x", b));
        return sb.toString();
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
            // sort all files/dir names
            List<Path> children = stream
            .filter(p -> !p.getFileName().toString().equals(".git"))
            .sorted((a, b) -> a.getFileName().toString()
                               .compareTo(b.getFileName().toString()))
            .toList();

            for (Path entry: children) {
                if (entry.getFileName().toString().equals(".git")) continue;
                if (Files.isDirectory(entry)) addObject(entry, entries, true);
                else addObject(entry, entries, false);
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

        // compute SHA and write object
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
        String mode = isDir ? GitFileMode.DIRECTORY.getMode() : (Files.isExecutable(entry) ? GitFileMode.EXECUTABLE_FILE.getMode() : GitFileMode.REGULAR_FILE.getMode());
        String name = entry.getFileName().toString();
        byte[] shaRaw = Utils.hexToBytes(sha1);

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        buffer.write((mode + " " + name + "\0").getBytes(StandardCharsets.UTF_8));
        buffer.write(shaRaw);
        entries.add(buffer.toByteArray());
    }
}
