package com.elevenquest.textractdemo;

import java.io.File;
import java.io.FileInputStream;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.textract.AmazonTextract;
import com.amazonaws.services.textract.AmazonTextractClientBuilder;
import com.amazonaws.services.textract.model.DocumentLocation;
import com.amazonaws.services.textract.model.NotificationChannel;
import com.amazonaws.services.textract.model.S3Object;
import com.amazonaws.services.textract.model.StartDocumentAnalysisRequest;
import com.amazonaws.services.textract.model.StartDocumentAnalysisResult;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * 
 *
 */
public class App 
{
    BaseDao dao = null;
    String bucket = null;
    String sourceDir = null;
    String sqsUrl = null;
    String roleArn = null;
    String topicArn = null;
    private static final int WAITING_TIME_SEC = 5;
    private static final Logger logger = LogManager.getLogger(App.class);
    public static void main(String[] args)
    {
        if(args.length < 8) {
            System.out.println("Usage: java -jar com.elevenquest.com <<local_file_path>> <<bucket_name>> <<sqsurl>> <<iamrole_of_textract>> <<topicArn>> <<upload<<jdbc_url>> <<dbuser>> <<dbpass>>");
            System.exit(1);
        }
        App app = new App(args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7]);
        app.run();
    }

    public App(String imgDir, String bucket, String sqsUrl, String roleArn, String topicArn, String jdbcUrl, String user, String pass) {
        logger.info("Image Directory: {}", imgDir);
        logger.info("JDBC Url: {}", jdbcUrl);
        this.dao = new BaseDao(jdbcUrl, user, pass);
        this.bucket = bucket;
        this.sqsUrl = sqsUrl;
        this.roleArn = roleArn;
        this.topicArn = topicArn;
        this.sourceDir = imgDir;
    }

    public void run() {
        File[] imgs = new File(this.sourceDir).listFiles();
        AmazonTextract textractClient = AmazonTextractClientBuilder.standard().build();
        AmazonS3 s3Client = AmazonS3ClientBuilder.standard().build();
        JobFinishSubscriber subscriber = new JobFinishSubscriber(this.sqsUrl);
        subscriber.start();
        for(File img:imgs) {
            if (dao.selectRecord(img.getName()) != null) {
                logger.info("The image file already processed. {}", img.getName());
                continue;
            }
            logger.trace("Image File is targetted. {}",img.getName());
            try (FileInputStream fis = new FileInputStream(img)) {
                Thread.sleep(WAITING_TIME_SEC * 1000);
                s3Client.putObject(this.bucket, img.getName(), img);
                DocumentLocation documentLocation = new DocumentLocation().withS3Object(new S3Object()
                    .withBucket(this.bucket)
                    .withName(img.getName()));
                StartDocumentAnalysisRequest request = new StartDocumentAnalysisRequest()
                    .withFeatureTypes("TABLES", "FORMS")
                    .withNotificationChannel(new NotificationChannel()
                        .withRoleArn(this.roleArn)
                        .withSNSTopicArn(this.topicArn))
                    .withDocumentLocation(documentLocation);
                StartDocumentAnalysisResult result = textractClient.startDocumentAnalysis(request);
                ResultStruct resultStruct = new ResultStruct();
                resultStruct.beforeAfter = "A";
                resultStruct.imageId = img.getName();
                resultStruct.jobId = result.getJobId();
                resultStruct.parentImageId = null;
                resultStruct.resultJson = null;
                logger.info(result.toString());
                dao.createRecord(resultStruct);
                logger.info(img.getName() + " for starting...");
                if (JobFinishSubscriber.isFinished())
                    break;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
