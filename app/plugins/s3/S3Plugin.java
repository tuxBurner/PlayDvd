package plugins.s3;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import play.Application;
import play.Logger;
import play.Plugin;

/**
 * Tooked from     https://raw.github.com/heroku/devcenter-java-play-s3/master/app/plugins/S3Plugin.java
 * If this is enabled the posters and the backdrops will be managed in the amazon s3
 * this is needed when running on heroku
 */
public class S3Plugin extends Plugin {

  public static final String AWS_S3_BUCKET = "dvddb.amazon.aws.bucketS3";
  public static final String AWS_ACCESS_KEY = "dvddb.amazon.aws.keyid";
  public static final String AWS_SECRET_KEY = "dvddb.amazon.aws.secretkey";
  private final Application application;

  public static AmazonS3 amazonS3;

  public static String s3Bucket;

  public S3Plugin(Application application) {
    this.application = application;
  }

   // only for intellij
  public void $init$() {

  }

  @Override
  public void onStart() {
    String accessKey = application.configuration().getString(AWS_ACCESS_KEY);
    String secretKey = application.configuration().getString(AWS_SECRET_KEY);
    s3Bucket = application.configuration().getString(AWS_S3_BUCKET);

    if ((accessKey != null) && (secretKey != null)) {
      AWSCredentials awsCredentials = new BasicAWSCredentials(accessKey, secretKey);
      amazonS3 = new AmazonS3Client(awsCredentials);
      amazonS3.createBucket(s3Bucket);
      if(Logger.isInfoEnabled()) {
        Logger.info("Using S3 Bucket: " + s3Bucket);
      }
    }
  }

  @Override
  public boolean enabled() {
    return application.configuration().getBoolean("dvddb.amazon.aws.useS3");
  }

}