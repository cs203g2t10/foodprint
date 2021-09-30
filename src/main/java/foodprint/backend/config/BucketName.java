package foodprint.backend.config;

public enum BucketName {
    PICTURE_IMAGE("foodprint-amazon-storage");
    private final String bucketName;

    private BucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    public String getBucketName() {
        return bucketName;
    }
}
