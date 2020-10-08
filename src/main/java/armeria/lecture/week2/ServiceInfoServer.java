package armeria.lecture.week2;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

import com.linecorp.armeria.common.AggregatedHttpRequest;
import com.linecorp.armeria.common.HttpResponse;
import com.linecorp.armeria.common.RequestContext;
import com.linecorp.armeria.server.Server;
import com.linecorp.armeria.server.annotation.Get;
import com.linecorp.armeria.server.annotation.Post;

public class ServiceInfoServer {

    public static void main(String[] args) {
        final Server server =
                Server.builder()
                      .annotatedService(new Object() {
                          private ConcurrentLinkedQueue<String> addresses = new ConcurrentLinkedQueue<>();

                          @Post("/registration")
                          public HttpResponse register(AggregatedHttpRequest request, RequestContext ctx) {
                              String address = request.contentUtf8();
                              addresses.add(address);
                              return HttpResponse.of(200);
                          }

                          @Get("/discovery")
                          public String discover() {
                              return addresses.stream().collect(Collectors.joining(","));
                          }
                      })
                      .http(9000)
                      .build();

        server.start().join();

        Runtime.getRuntime().addShutdownHook(new Thread(() ->{
            System.out.println("Server has been stopped");
        }));
    }
}
