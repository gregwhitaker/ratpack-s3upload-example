package ratpack.example.api.upload;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import io.netty.buffer.ByteBuf;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ratpack.example.s3.S3WritableByteChannel;
import ratpack.exec.Blocking;
import ratpack.handling.Context;
import ratpack.handling.Handler;

import java.io.IOException;
import java.util.UUID;

/**
 * Handler responsible for uploading the incoming file to S3.
 */
public class UploadHandler implements Handler {
    private static final Logger LOG = LoggerFactory.getLogger(UploadHandler.class);

    @Override
    public void handle(Context ctx) throws Exception {
        ctx.getRequest().getBodyStream()
                .subscribe(new Subscriber<ByteBuf>() {
                    final String s3Bucket = "ratpack-s3upload-example";
                    final String s3Key =  UUID.randomUUID().toString();

                    private AmazonS3 client = AmazonS3ClientBuilder.standard()
                            .withCredentials(new DefaultAWSCredentialsProviderChain())
                            .withRegion(Regions.US_WEST_2)
                            .build();

                    private Subscription subscription;
                    private S3WritableByteChannel s3Channel;

                    @Override
                    public void onSubscribe(Subscription s) {
                        subscription = s;

                        try {
                            s3Channel = new S3WritableByteChannel(client, s3Bucket, s3Key);
                        } catch (IOException e) {
                            throw new IllegalStateException(e);
                        }

                        subscription.request(1);
                    }

                    @Override
                    public void onNext(ByteBuf byteBuf) {
                        Blocking.get(() -> {
                            return s3Channel.write(byteBuf.nioBuffer());
                        }).onError(throwable -> {
                            byteBuf.release();
                            subscription.cancel();
                            s3Channel.close();
                            ctx.error(throwable);
                        }).then(bytesWritten -> {
                            byteBuf.release();
                            subscription.request(1);
                        });
                    }

                    @Override
                    public void onError(Throwable t) {
                        ctx.error(t);
                    }

                    @Override
                    public void onComplete() {
                        try {
                            s3Channel.close();
                        } catch (IOException e) {
                            // noop
                        } finally {
                            ctx.getResponse().send();
                        }
                    }
                });
    }
}
