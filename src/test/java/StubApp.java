import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.common.ConsoleNotifier;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

public class StubApp {
    public static void main(String[] args) {
        WireMockServer refDataServer = new WireMockServer(options().port(7777)
//                .withRootDirectory("src/test/resources/mappings/ref")
                .notifier(new ConsoleNotifier(true))); //No-args constructor will start on port 8080, no HTTPS
        refDataServer.start();

        WireMockServer cardServer = new WireMockServer(options().port(7778)
//                .withRootDirectory("src/test/resources/mappings/card")
                .notifier(new ConsoleNotifier(true))); //No-args constructor will start on port 8080, no HTTPS
        cardServer.start();

    }
}
