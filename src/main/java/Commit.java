import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;

public class Commit {

    public static void runCommitTree(String treeSha, String commitSha, String message) throws Exception {
        // hardcode author/commiter info 
        String name = "shiro1729", email = "coder@gmail.com";
        long timestamp = Instant.now().getEpochSecond();
        String timezone = "+0000";

        // build commit body
        StringBuilder body = new StringBuilder();
        body.append("tree ").append(treeSha).append("\n"); // tree part
        body.append("parent ").append(commitSha).append("\n"); // parent part
        body.append("author ")
            .append(name).append(" <").append(email).append("> ")
            .append(timestamp).append(" ").append(timezone).append("\n"); // author info
        body.append("committer ")
            .append(name).append(" <").append(email).append("> ")
            .append(timestamp).append(" ").append(timezone).append("\n"); // committer info

        body.append("\n"); // blank line
        body.append(message).append("\n");

        byte[] bodyBytes = body.toString().getBytes(StandardCharsets.UTF_8);

        // add commit header
        String header = "commit " + bodyBytes.length + "\0";
        byte[] headerBytes = header.getBytes(StandardCharsets.UTF_8);

        // build full object
        byte[] full = new byte[headerBytes.length + bodyBytes.length];
        System.arraycopy(headerBytes, 0, full, 0, headerBytes.length);
        System.arraycopy(bodyBytes, 0, full, headerBytes.length, bodyBytes.length);

        // compute sha-1
        String sha = Utils.computeSHA1(full);

        // compress content
        byte[] compressed = Utils.compressZlib(full);

        // write object
        String folder = sha.substring(0, 2), file = sha.substring(2);
        Path objDir = Paths.get(Main.currentDir, ".git/objects", folder);
        Files.createDirectories(objDir);

        Path objFile = objDir.resolve(file);
        if (!Files.exists(objFile)) Files.write(objFile, compressed);

        System.out.println(sha);
    }
}
