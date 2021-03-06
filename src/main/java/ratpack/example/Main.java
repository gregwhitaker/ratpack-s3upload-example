package ratpack.example;

import ratpack.example.api.ApiModule;
import ratpack.example.api.upload.UploadHandlers;
import ratpack.example.config.S3Config;
import ratpack.guice.Guice;
import ratpack.server.BaseDir;
import ratpack.server.RatpackServer;
import ratpack.server.ServerConfig;

/**
 * Starts the ratpack-s3upload-example application.
 */
public class Main {

    public static void main(String... args) throws Exception {
        ServerConfig serverConfig = ServerConfig.builder()
                .yaml("config.yml")
                .require("/s3", S3Config.class)
                .baseDir(BaseDir.find())
                .build();

        RatpackServer.start(s -> s
                .serverConfig(serverConfig)
                .registry(Guice.registry(b -> b
                        .module(ApiModule.class))
                )
                .handlers(chain -> chain
                        .insert(UploadHandlers.class)
                )
        );
    }
}
