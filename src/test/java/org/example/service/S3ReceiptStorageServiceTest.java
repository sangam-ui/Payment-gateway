package org.example.service;

import org.junit.jupiter.api.Test;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class S3ReceiptStorageServiceTest {

    @Test
    void shouldStoreReceiptInConfiguredBucketAndReturnSameKey() {
        S3Client s3Client = mock(S3Client.class);
        S3ReceiptStorageService service = new S3ReceiptStorageService(s3Client, "demo-bucket");

        String key = "receipt/9000000001/test.json";
        String savedKey = service.store(key, "{\"amount\":100}");

        assertEquals(key, savedKey);
        verify(s3Client).putObject(
                argThat((PutObjectRequest request) -> "demo-bucket".equals(request.bucket())
                        && key.equals(request.key())
                        && "application/json".equals(request.contentType())),
                any(RequestBody.class)
        );
    }
}

