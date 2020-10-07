package armeria.lecture.week2;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import com.linecorp.armeria.client.WebClient;
import com.linecorp.armeria.common.HttpMethod;
import com.linecorp.armeria.common.HttpObject;
import com.linecorp.armeria.common.HttpRequest;
import com.linecorp.armeria.common.HttpRequestWriter;
import com.linecorp.armeria.common.HttpResponse;
import com.linecorp.armeria.common.HttpResponseWriter;
import com.linecorp.armeria.common.RequestHeaders;
import com.linecorp.armeria.server.ServerBuilder;
import com.linecorp.armeria.testing.junit5.server.ServerExtension;

public class ServiceInfoServerTest {

    @RegisterExtension
    static final ServerExtension server = new ServerExtension() {
        @Override
        protected void configure(ServerBuilder sb) throws Exception {
            sb.service("/", (ctx, req) -> {
                return HttpResponse.of(201);
            });
            sb.service("/streaming", (ctx, req) -> {
                req.subscribe(new Subscriber<HttpObject>() {
                    @Override
                    public void onSubscribe(Subscription s) {

                    }

                    @Override
                    public void onNext(HttpObject httpObject) {

                    }

                    @Override
                    public void onError(Throwable t) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
                HttpResponseWriter res = HttpResponse.streaming();
                return res;
            });
        }
    };

    @Test
    void aaa() {
        final WebClient client = WebClient.of(server.httpsUri());
        System.err.println(client.get("/").aggregate().join().status());
    }

    @Test
    void streaming() {
        final WebClient client = WebClient.of(server.httpsUri());

        final HttpRequestWriter req = HttpRequest.streaming(
                RequestHeaders.of(HttpMethod.POST, "/streaming"));

        final HttpResponse res = client.execute(req);
        res.subscribe(new Subscriber<HttpObject>() {
            @Override
            public void onSubscribe(Subscription s) {

            }

            @Override
            public void onNext(HttpObject httpObject) {

            }

            @Override
            public void onError(Throwable t) {

            }

            @Override
            public void onComplete() {

            }
        });
    }

    @Test
    void bbb() {

    }
}
