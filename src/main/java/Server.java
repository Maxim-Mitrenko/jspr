import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {

    private final ExecutorService executorService = Executors.newFixedThreadPool(64);
    private final ConcurrentHashMap<String, Handler> handlers;

    public Server(ConcurrentHashMap<String, Handler> handlers) {
        this.handlers = handlers;
    }

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
            if (size == -1) return;
            var stringRequest = new String(bytes, 0, size, StandardCharsets.UTF_8);
            if (stringRequest.isEmpty()) return;
            var request = new Request(stringRequest);
            var handler = handlers.get(request.getMethod() + request.getPathString());
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
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }
}
