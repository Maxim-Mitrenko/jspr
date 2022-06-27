import org.apache.http.client.utils.URLEncodedUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class Request {

    private final String method;
    private final Path path;
    private final String pathString;
    private final String header;
    private final String body;
    private final List<String> postParams;

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
            this.postParams = new ArrayList<>();
        } else {
            String[] headerBodyArray = headerAndBody.split("\r\n\r\n");
            this.header = headerBodyArray[0];
            if (headerBodyArray.length > 1) {
                this.body = headerBodyArray[1];
                this.postParams = URLEncodedUtils.parse(body, StandardCharsets.UTF_8)
                        .stream()
                        .map(Object::toString)
                        .toList();
            } else {
                this.body = null;
                this.postParams = new ArrayList<>();
            }
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

    public List<String> getPostParams() {
        return postParams;
    }

    public List<String> getPostParam(String name) {
        return postParams.stream()
                .filter(x -> x.contains(name))
                .toList();
    }
}
