import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.RequestContext;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.http.client.utils.URLEncodedUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

public class Request {

    private final String method;
    private final Path path;
    private final String pathString;
    private final String headers;
    private final String body;
    private final List<FileItem> fileItems;

    public Request(byte[] bytes, int size) throws IOException, FileUploadException {
        var upload = new ServletFileUpload(new DiskFileItemFactory());
        var delimiterHeader = new byte[] {'\r', '\n'};
        var requestLineEnd = indexOf(bytes, delimiterHeader, 0, size);
        var requestLine = new String(Arrays.copyOf(bytes, requestLineEnd), StandardCharsets.UTF_8);
        var info = requestLine.split(" ");
        this.method = info[0];
        this.pathString = info[1];
        this.path = Path.of(".", "public", pathString);
        if (method.equals("GET")) {
            this.headers = new String(Arrays.copyOfRange(bytes, requestLineEnd + 1, size), StandardCharsets.UTF_8);
            this.body = null;
            this.fileItems = null;
        } else {
            var delimiterBody = new byte[] {'\r', '\n', '\r', '\n'};
            var endHeader = indexOf(bytes, delimiterBody, requestLineEnd + 1, size);
            this.headers = new String(Arrays.copyOfRange(bytes, requestLineEnd + 1, endHeader), StandardCharsets.UTF_8);
            if (endHeader < size) {
                this.body = new String(Arrays.copyOfRange(bytes, endHeader + 1, size), StandardCharsets.UTF_8);
                String contentType = headers.split("Content-Type: ")[1].split("\r\n")[0];
                if (contentType.contains("multipart")) {
                    int length = Integer.parseInt(headers.split("Content-Length: ")[1].split("\r\n")[0]);
                    RequestContext requestContext = new FileUploadRequestContext(contentType, length, new ByteArrayInputStream(bytes));
                    this.fileItems = ServletFileUpload.isMultipartContent(requestContext) ? upload.parseRequest(requestContext) : null;
                } else {
                    this.fileItems = null;
                }
            } else {
                this.body = null;
                this.fileItems = null;
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

    public String getHeaders() {
        return headers;
    }

    public String getBody() {
        return body;
    }

    private static int indexOf(byte[] array, byte[] target, int start, int max) {
        outer:
        for (int i = start; i < max - target.length + 1; i++) {
            for (int j = 0; j < target.length; j++) {
                if (array[i + j] != target[j]) {
                    continue outer;
                }
            }
            return i;
        }
        return -1;
    }

    public List<FileItem> getFileItems() {
        return fileItems;
    }

    public List<FileItem> getFileItem(String name) {
        if (fileItems == null) return null;
        return fileItems.stream()
                .filter(x -> x.getFieldName().equals(name))
                .toList();
    }

    public List<String> getFileItemsStrings() {
        if (fileItems == null) return null;
        return fileItems.stream()
                .filter(FileItem::isFormField)
                .map(x -> x.getFieldName() + " = " + URLEncodedUtils.parse(x.getString(), StandardCharsets.UTF_8))
                .toList();
    }

    public List<String> getFileItemString(String name) {
        if (fileItems == null) return null;
        return fileItems.stream()
                .filter(FileItem::isFormField)
                .filter(x -> x.getFieldName().equals(name))
                .map(x -> x.getFieldName() + " = " + URLEncodedUtils.parse(x.getString(), StandardCharsets.UTF_8))
                .toList();
    }
}
