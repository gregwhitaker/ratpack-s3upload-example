package ratpack.example.api.upload;

import ratpack.func.Action;
import ratpack.handling.Chain;

public class UploadHandlers implements Action<Chain> {

    @Override
    public void execute(Chain chain) throws Exception {
        chain.put("api/v1/upload", UploadHandler.class);
    }
}
