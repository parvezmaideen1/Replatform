package org.superbiz.moviefun.blobstore;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Optional;

public class S3Store implements BlobStore{
    AmazonS3Client s3Client;
    String photoStorageBucket;
    public S3Store(AmazonS3Client s3Client, String photoStorageBucket) {
        this.s3Client=s3Client;
        this.photoStorageBucket=photoStorageBucket;
        if(!s3Client.doesBucketExist(this.photoStorageBucket)){
            s3Client.createBucket(photoStorageBucket);
        }
    }


    public void put(Blob blob) throws IOException {
        ObjectMetadata ObjectMetadata = new ObjectMetadata();
        ObjectMetadata.setContentType(blob.contentType);
        this.s3Client.putObject(this.photoStorageBucket, blob.name, blob.inputStream,  ObjectMetadata);
    }


    public Optional<Blob> get(String name) throws IOException, URISyntaxException {
        if(s3Client.doesObjectExist(photoStorageBucket, name)){
            S3Object s3Object= this.s3Client.getObject(this.photoStorageBucket,name);
            Blob blob = new Blob(s3Object.getKey(),s3Object.getObjectContent(),s3Object.getObjectMetadata().getContentType());
            Optional<Blob> optionalBlob = Optional.of(blob);
            return optionalBlob;
        }else{
            System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$File:"+name+"$$$$$$$$$$$$$$$$$$$$$$"+photoStorageBucket);
            return Optional.empty();
        }

    }

    @Override
    public void deleteAll() {

    }


}
