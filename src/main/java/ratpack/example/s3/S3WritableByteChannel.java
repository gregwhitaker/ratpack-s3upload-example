package ratpack.example.s3;

import com.amazonaws.AmazonClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CompleteMultipartUploadRequest;
import com.amazonaws.services.s3.model.InitiateMultipartUploadRequest;
import com.amazonaws.services.s3.model.InitiateMultipartUploadResult;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PartETag;
import com.amazonaws.services.s3.model.UploadPartRequest;
import com.amazonaws.services.s3.model.UploadPartResult;
import com.amazonaws.util.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.WritableByteChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public class S3WritableByteChannel implements WritableByteChannel {
    private static final Logger LOG = LoggerFactory.getLogger(S3WritableByteChannel.class);

    private final AmazonS3 amazonS3;
    private final String s3Bucket;
    private final String s3Key;

    private final String uploadId;
    private final ByteBuffer buffer = ByteBuffer.allocate(5_000_000);
    private final List<PartETag> etags = new ArrayList<>();

    private boolean open = true;
    private int partNumber = 1;
    private final MessageDigest partHash;

    public S3WritableByteChannel(final AmazonS3 amazonS3, final String s3Bucket, final String s3Key) throws IOException {
        this(amazonS3, s3Bucket, s3Key, null);
    }

    public S3WritableByteChannel(final AmazonS3 amazonS3, final String s3Bucket, final String s3Key, final ObjectMetadata objectMetadata) throws IOException {
        this.amazonS3 = amazonS3;
        this.s3Bucket = s3Bucket;
        this.s3Key = s3Key;

        try {
            this.partHash = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }

        InitiateMultipartUploadRequest request;
        if (objectMetadata != null) {
            request = new InitiateMultipartUploadRequest(s3Bucket, s3Key, objectMetadata);
        } else {
            request = new InitiateMultipartUploadRequest(s3Bucket, s3Key);
        }

        InitiateMultipartUploadResult result;
        try {
            result = amazonS3.initiateMultipartUpload(request);
        } catch (AmazonClientException e) {
            throw new IOException(e);
        }

        this.uploadId = result.getUploadId();
    }

    @Override
    public int write(ByteBuffer src) throws IOException {
        if(!isOpen()) {
            throw new ClosedChannelException();
        }

        int totalBytesWritten = 0;
        while(src.hasRemaining()) {
            int bytesWritten = Math.min(src.remaining(), buffer.remaining());
            totalBytesWritten += bytesWritten;

            byte[] bytes = new byte[bytesWritten];
            src.get(bytes);
            buffer.put(bytes);
            partHash.update(bytes);

            if (!buffer.hasRemaining() || src.hasRemaining()) {
                flush();
            }
        }

        return totalBytesWritten;
    }

    @Override
    public boolean isOpen() {
        return open;
    }

    @Override
    public void close() throws IOException {
        this.open = false;

        if (buffer.remaining() > 0) {
            flush();
        }

        CompleteMultipartUploadRequest request = new CompleteMultipartUploadRequest()
                .withBucketName(s3Bucket)
                .withKey(s3Key)
                .withUploadId(uploadId)
                .withPartETags(etags);

        try {
            amazonS3.completeMultipartUpload(request);
        } catch (AmazonClientException e) {
            throw new IOException(e);
        }
    }

    private void flush() throws IOException {
        buffer.flip();
        ByteArrayInputStream inStream = new ByteArrayInputStream(buffer.array());

        UploadPartRequest request = new UploadPartRequest()
                .withBucketName(s3Bucket)
                .withKey(s3Key)
                .withUploadId(uploadId)
                .withPartNumber(partNumber++)
                .withPartSize(buffer.remaining())
                .withMD5Digest(Base64.encodeAsString(partHash.digest()))
                .withInputStream(inStream);

        UploadPartResult result;
        try {
            result = amazonS3.uploadPart(request);
        } catch (AmazonClientException e) {
            throw new IOException(e);
        }

        buffer.clear();
        partHash.reset();
        etags.add(result.getPartETag());
    }
}
