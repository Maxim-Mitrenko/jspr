import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Path;

public class Request {

    private final String method;
    private final Path path;
    private final String pathString;
    private final String header;
    private final String body;

    public Request(BufferedReader bufferedReader) throws IOException {
        char[] chars = new char[4096];
        int size = bufferedReader.read(chars);
        String request = new String(chars, 0 , size);
        String requestLine = request.split("\r\n")[0];
        String[] info = requestLine.split(" ");
        this.method = info[0];
        this.pathString = info[1];
        this.path = Path.of(".", "public", pathString);
        String headerAndBody = request.substring(requestLine.length());
        if (method.equals("GET")) {
            this.header = headerAndBody;
            this.body = null;
        } else {
            String[] headerBodyArray = headerAndBody.split("\r\n\r\n");
            this.header = headerBodyArray[0];
            this.body = headerBodyArray.length > 1 ? headerBodyArray[1] : null;
        }
    }

    public String getMethod() {
        return method;
    }

    public Path getPath() {
        return path;
    }

    public String getPathString() {
        return pathString;
    }

    public String getHeader() {
        return header;
    }

    public String getBody() {
        return body;
    }
}
