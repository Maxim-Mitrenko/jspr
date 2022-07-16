import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Request {

    private final String method;
    private final Path path;
    private final String pathString;
    private final String headers;
    private final String body;
    private final List<NameValuePair> queryList;
    private final Map<String, String> query = new HashMap<>();

    public Request(String request) throws IOException, URISyntaxException {
        String requestLine = request.split("\r\n")[0];
        String[] info = requestLine.split(" ");
        this.method = info[0];
        this.queryList = URLEncodedUtils.parse(new URI(info[1]), StandardCharsets.UTF_8);
        queryList.forEach(x -> query.put(x.getName(), x.getValue()));
        this.pathString = info[1].split("\\?")[0];
        this.path = Path.of(".", "public", pathString);
        String headerAndBody = request.substring(requestLine.length());
        if (method.equals("GET")) {
            this.headers = headerAndBody;
            this.body = null;
        } else {
            String[] headerBodyArray = headerAndBody.split("\r\n\r\n");
            this.headers = headerBodyArray[0];
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

    public String getHeaders() {
        return headers;
    }

    public String getBody() {
        return body;
    }

    public Map<String, String> getQuery() {
        return query;
    }

    public String getQueryParam(String name) {
        return query.get(name);
    }

    public List<NameValuePair> getQueryList() {
        return queryList;
    }

    public List<NameValuePair> getQueryListParam(String name) {
        return queryList.stream()
                .filter(x -> x.getName().equals(name))
                .toList();
    }
}
