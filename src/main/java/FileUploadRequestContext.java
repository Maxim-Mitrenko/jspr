import org.apache.commons.fileupload.RequestContext;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class FileUploadRequestContext implements RequestContext {

    private final String contentType;
    private final int length;
    private final InputStream inputStream;

    public FileUploadRequestContext(String contentType, int length, InputStream inputStream) {
        this.contentType = contentType;
        this.length = length;
        this.inputStream = inputStream;
    }

    @Override
    public String getCharacterEncoding() {
        return StandardCharsets.UTF_8.displayName();
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    @Override
    public int getContentLength() {
        return length;
    }

    @Override
    public InputStream getInputStream() {
        return inputStream;
    }
}
