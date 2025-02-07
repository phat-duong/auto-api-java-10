package utils;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.common.ConsoleNotifier;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

public class StubUtils {

    private static WireMockServer refDataServer;
    private static WireMockServer cardServer;
    public static void startStubForCreateCard(){
        startServer(refDataServer,7777);
        startServer(cardServer,7778);
        }


    private static void startServer(WireMockServer server, int port){
        if(server == null){
            server = new WireMockServer(options().port(port)
                    .notifier(new ConsoleNotifier(true))); //No-args constructor will start on port 8080, no HTTPS
            server.start();
        }
        if(!server.isRunning()){
            server.start();
        }
    }
}
