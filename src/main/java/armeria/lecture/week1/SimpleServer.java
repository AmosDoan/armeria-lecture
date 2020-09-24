package armeria.lecture.week1;

import java.util.concurrent.CompletableFuture;

import com.linecorp.armeria.client.WebClient;
import com.linecorp.armeria.common.AggregatedHttpRequest;
import com.linecorp.armeria.common.AggregatedHttpResponse;
import com.linecorp.armeria.common.ContextAwareScheduledExecutorService;
import com.linecorp.armeria.common.HttpData;
import com.linecorp.armeria.common.HttpRequest;
import com.linecorp.armeria.common.HttpResponse;
import com.linecorp.armeria.common.HttpResponseWriter;
import com.linecorp.armeria.common.ResponseHeaders;
import com.linecorp.armeria.server.HttpService;
import com.linecorp.armeria.server.Server;
import com.linecorp.armeria.server.ServiceRequestContext;

public class SimpleServer {

    private static void currentThreadName(String method) {
        System.out.println("Name: " + Thread.currentThread().getName() + " (in " + method + ')');
    }

    public static void main(String[] args) {
        final Server server = Server.builder()
                                    .http(8088)
                                    .service("/hello", (ctx, req) -> {
                                        // response를 미리 채워놓는다.
                                        final HttpResponseWriter res = HttpResponse.streaming();
                                        req.aggregate().thenAccept(aggregatedHttpRequest -> {
                                            res.write(ResponseHeaders.of(200));
                                            res.write(HttpData.ofUtf8(aggregatedHttpRequest.contentUtf8()));
                                            res.close();
                                        });

                                        return res;
                                    })
                                    .service("/hello2", (ctx, req) -> {
                                        CompletableFuture<HttpResponse> future = new CompletableFuture<>();

                                        // thread
                                        currentThreadName("before aggregate");
                                        req.aggregate().thenAcceptAsync(aggregateHttpRequest -> {
                                            //thread
                                            currentThreadName("aggregating");
                                            future.complete(HttpResponse.of("hello" + aggregateHttpRequest.contentUtf8()));
                                        }, ctx.blockingTaskExecutor());

                                        return HttpResponse.from(future);
                                    })
                                    .service("/hello2", new HttpService() {
                                        @Override
                                        public HttpResponse serve(ServiceRequestContext ctx, HttpRequest req)
                                                throws Exception {
                                            CompletableFuture<HttpResponse> future = new CompletableFuture<>();

                                            // RDB 등의 blocking job이 있는 경우는, 별도의 thread를 이용해야 한다.
                                            ContextAwareScheduledExecutorService executorService = ctx.blockingTaskExecutor();
                                            executorService.submit(() -> {
                                                final AggregatedHttpRequest aggregated = req.aggregate().join();
                                                future.complete(HttpResponse.of("hello" + aggregated.contentUtf8()));
                                            });

                                            return HttpResponse.from(future);
                                        }
                                    })
                                    .build();
        server.start().join();
        final WebClient client = WebClient.builder("http://127.0.0.1:8088/")
                                          .build();
        final AggregatedHttpResponse res = client.post("/hello2", "test") .aggregate().join();
        System.err.println(res.headers());
        System.err.println(res.contentUtf8());
        server.stop().join();
    }
}
