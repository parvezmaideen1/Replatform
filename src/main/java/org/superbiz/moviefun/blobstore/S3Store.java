package org.superbiz.moviefun.blobstore;

import com.amazonaws.services.s3.AmazonS3Client;

public class S3Store implements BlobStore {
    public S3Store(AmazonS3Client s3Client, String photoStorageBucket) {
    }
}
