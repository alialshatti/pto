package com.ota.storage;

import com.ota.exception.StorageException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.core.sync.ResponseBytes;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
public class MinioStorageService {

    private final S3Client s3Client;

    @Value("${oci.report-bucket}")
    private String reportBucket;

    public MinioStorageService(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    public byte[] downloadXml(String bucketName, String objectName) {
        try {
            GetObjectRequest request = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(objectName)
                .build();
            ResponseBytes<GetObjectResponse> responseBytes = s3Client.getObjectAsBytes(request);
            return responseBytes.asByteArray();
        } catch (Exception e) {
            throw new StorageException("Failed to download XML from bucket: " + bucketName + ", object: " + objectName, e);
        }
    }

    public String uploadSvrl(String validationRunId, String svrlContent) {
        String objectName = "validation-reports/" + validationRunId + ".svrl.xml";
        try {
            PutObjectRequest request = PutObjectRequest.builder()
                .bucket(reportBucket)
                .key(objectName)
                .contentType("application/xml")
                .build();
            s3Client.putObject(request, RequestBody.fromString(svrlContent));
            return objectName;
        } catch (Exception e) {
            throw new StorageException("Failed to upload SVRL report to bucket: " + reportBucket, e);
        }
    }
}
