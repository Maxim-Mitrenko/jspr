import org.apache.commons.fileupload.FileUploadException;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {

    private final ExecutorService executorService = Executors.newFixedThreadPool(64);
    private final ConcurrentHashMap<String, Handler> handlers = new ConcurrentHashMap<>();

    public void start(int port) {
        try (final ServerSocket serverSocket = new ServerSocket(port)) {
            while (true) {
                Socket socket = serverSocket.accept();
                executorService.execute(() -> work(socket));
            }
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    private void work(Socket socket) {
        try (socket;
             final var in = new BufferedInputStream(socket.getInputStream());
             final var out = new BufferedOutputStream(socket.getOutputStream())) {
            var bytes = new byte[in.available()];
            var size = in.read(bytes);
            if (size == -1 || bytes.length == 0) return;
            Request request = new Request(bytes, size);
            Handler handler = handlers.get(request.getMethod() + request.getPathString());
            if (handler == null) {
                out.write((
                        "HTTP/1.1 404 Not Found\r\n" +
                                "Content-Length: 0\r\n" +
                                "Connection: close\r\n" +
                                "\r\n"
                ).getBytes());
                out.flush();
                return;
            }
            handler.handle(request, out);
        } catch (IOException | FileUploadException exception) {
            exception.printStackTrace();
        }
    }

    public void addHandler(String methodAndPath, Handler handler) {
        handlers.put(methodAndPath, handler);
    }
}
