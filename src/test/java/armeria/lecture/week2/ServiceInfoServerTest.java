package armeria.lecture.week2;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.linecorp.armeria.client.ClientFactory;
import com.linecorp.armeria.client.WebClient;
import com.linecorp.armeria.common.AggregatedHttpResponse;
import com.linecorp.armeria.server.ServerBuilder;
import com.linecorp.armeria.testing.junit5.server.ServerExtension;

public class ServiceInfoServerTest {

    @RegisterExtension
    static final ServerExtension server = new ServerExtension() {
        @Override
        protected void configure(ServerBuilder sb) throws Exception {
            sb.tlsSelfSigned();
        }
    };

    @Test
    void discovery() {
        final WebClient webClient = WebClient.of(server.httpUri());
        webClient.post("/registration", "127.0.0.1:10000").aggregate().join();
        webClient.post("/registration", "127.0.0.1:10001").aggregate().join();
        final AggregatedHttpResponse res = webClient.get("/discovery").aggregate().join();
        assertThat(res.contentUtf8()).isEqualTo("127.0.0.1:10000,127.0.0.1:1001");
    }

    @Test
    void testSelfSigned() {
        WebClient build = WebClient.builder(server.httpsUri())
                                   // tls self signed server의 가짜 인증서를 client에서 검증하면 안됨
                                   .factory(ClientFactory.insecure())
                                   .build();
    }
}
