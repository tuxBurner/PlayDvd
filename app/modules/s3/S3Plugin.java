package modules.s3;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.S3Object;
import com.typesafe.config.ConfigFactory;
import play.Application;
import play.Logger;

/**
 * Tooked from https://raw.github.com/heroku/devcenter-java-play-s3/master/app/plugins/S3Plugin.java
 * If this is enabled the posters and the backdrops will be managed in the amazon s3
 * this is needed when running on heroku
 */
//TODO: FIX ME TO A MODULE
public class S3Plugin {

  public static final String AWS_S3_BUCKET = "dvddb.amazon.aws.bucketS3";
  public static final String AWS_ACCESS_KEY = "dvddb.amazon.aws.keyid";
  public static final String AWS_SECRET_KEY = "dvddb.amazon.aws.secretkey";
  private final Application application;

  public static AmazonS3 amazonS3;

  public static String s3Bucket;

  public  static  String endPoint;

  public S3Plugin(Application application) {
    this.application = application;
  }



  public void onStart() {
    String accessKey = application.configuration().getString(AWS_ACCESS_KEY);
    String secretKey = application.configuration().getString(AWS_SECRET_KEY);
    s3Bucket = application.configuration().getString(AWS_S3_BUCKET);
    endPoint = application.configuration().getString("dvddb.amazon.awd.s3Endpoint");

    if ((accessKey != null) && (secretKey != null)) {
      AWSCredentials awsCredentials = new BasicAWSCredentials(accessKey, secretKey);
      amazonS3 = new AmazonS3Client(awsCredentials);

      if(amazonS3.doesBucketExist(s3Bucket) == false) {
        amazonS3.createBucket(s3Bucket);
        if(Logger.isInfoEnabled() == true) {
          Logger.info("Created new S3 Bucket: "+s3Bucket);
        }
      }


      if(Logger.isInfoEnabled() == true) {
        Logger.info("Using S3 Bucket: " + s3Bucket);
      }
    }
  }


  public boolean enabled() {
    return S3Plugin.pluginEnabled();
  }

  public static boolean pluginEnabled() {
    return ConfigFactory.load().getBoolean("dvddb.amazon.aws.useS3");
  }

  public static String buildUrl(final String key) {
    return endPoint+"/"+s3Bucket+"/"+key;
  }

  public static String buildUrl(final S3Object s3Object) {
    return endPoint+"/"+s3Bucket+"/"+s3Object.getKey();
  }



}