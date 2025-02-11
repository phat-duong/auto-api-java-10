package utils;

import io.github.cdimascio.dotenv.Dotenv;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;

public class ConfigUtils {

    private static Dotenv dotenv;
    public static Dotenv getDotenv(){
        String currentProfile = System.getenv("testProfile");
        if(StringUtils.isAllBlank(currentProfile)){
            currentProfile="local";
        }
        if(dotenv==null){
            dotenv = Dotenv.configure()
                    .directory("configs")
                    .filename(String.format("%s.env", currentProfile))
                    .load();
        }
        return dotenv;
    }
}
