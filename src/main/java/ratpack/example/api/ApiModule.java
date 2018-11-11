package ratpack.example.api;

import com.google.inject.AbstractModule;
import ratpack.example.api.upload.UploadHandler;
import ratpack.example.api.upload.UploadHandlers;

public class ApiModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(UploadHandlers.class);
        bind(UploadHandler.class);
    }
}
