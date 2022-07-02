import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

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
    private final List<NameValuePair> postParams;

    public Request(String request) throws IOException {
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
                this.postParams = URLEncodedUtils.parse(body, StandardCharsets.UTF_8);
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

    public List<String> getPostParamsString() {
        return postParams.stream()
                .map(Object::toString)
                .toList();
    }

    public List<String> getPostParamString(String name) {
        return postParams.stream()
                .map(Object::toString)
                .filter(x -> x.contains(name))
                .toList();
    }

    public List<NameValuePair> getPostParams() {
        return postParams;
    }

    public List<NameValuePair> getPostParam(String name) {
        return postParams.stream()
                .filter(x -> x.getName().equals(name))
                .toList();
    }
}
