package com.elevenquest.textractdemo;

import java.util.ArrayList;
import java.util.List;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.textract.AmazonTextract;
import com.amazonaws.services.textract.AmazonTextractClientBuilder;
import com.amazonaws.services.textract.model.Block;
import com.amazonaws.services.textract.model.DocumentMetadata;
import com.amazonaws.services.textract.model.GetDocumentAnalysisRequest;
import com.amazonaws.services.textract.model.GetDocumentAnalysisResult;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class JobFinishSubscriber extends Thread {

  private static final int MAX_WAITING_TIME_SEC = 60;
  static Logger logger = LogManager.getLogger(JobFinishSubscriber.class);
  String queueUrl;
  AmazonSQS client;
  AmazonTextract textractClient;

  public JobFinishSubscriber(String queueUrl) {
    this.queueUrl = queueUrl;
    this.client = AmazonSQSClientBuilder.standard().build();
    this.textractClient = AmazonTextractClientBuilder.standard().build();
  }

  static boolean isProgressing = false;

  static boolean isFinished() {
    return !isProgressing;
  }

  public void run() {

    isProgressing = true;
    BaseDao dao;

    while( (dao = BaseDao.getBaseDao()) == null) {
      try { Thread.sleep(10); } catch (InterruptedException ie) {
        break;
      }
    }
    logger.info("Start Receiver...");
    
    long lastProcessedTime = System.currentTimeMillis();
    while(true) {
      if ((System.currentTimeMillis() - lastProcessedTime) > MAX_WAITING_TIME_SEC * 1000)
        break;
      ReceiveMessageRequest receive_request = new ReceiveMessageRequest()
        .withQueueUrl(this.queueUrl)
        .withWaitTimeSeconds(5);
      List<Message> messages = client.receiveMessage(receive_request).getMessages();
      if (messages.size() == 0)
          continue;
      for(Message message: messages) {
        try {
          logger.info(message.getBody());
          ObjectMapper mapper = new ObjectMapper();
          JsonNode body = mapper.readTree(message.getBody());
          ObjectMapper smapper = new ObjectMapper();
          JsonNode sourceBody = smapper.readTree(body.get("Message").asText());
          ResultStruct record = new ResultStruct();
          record.imageId = sourceBody.get("DocumentLocation").get("S3ObjectName").asText();
          record.jobId = sourceBody.get("JobId").asText();
          record.resultStatus = sourceBody.get("Status").asText();
          record.resultJson = record.resultStatus.equals("SUCCEEDED") ? 
            getDocumentAnalysisResult(record.jobId) : null;
          if (dao.updateRecord(record) > 0) {
            DeleteMessageRequest deleteRequest = new DeleteMessageRequest()
            .withQueueUrl(this.queueUrl)
            .withReceiptHandle(message.getReceiptHandle());
            client.deleteMessage(deleteRequest);
            logger.info("img_id: {}, job_id: {} cleared.", record.imageId, record.jobId);
          }
        } catch (Exception e) {
          logger.error(e);
        }
        lastProcessedTime = System.currentTimeMillis();
      }
    }
    isProgressing = false;
    logger.info("End Receiver...");
  }

  public String getDocumentAnalysisResult(String jobId) {
    String paginationToken=null;
    GetDocumentAnalysisResult response=null;
    Boolean finished=false;
    List<Block> fullBlocks = new ArrayList<Block>();
    DocumentMetadata metadata = null;
    while(!finished) {
      GetDocumentAnalysisRequest request = new GetDocumentAnalysisRequest()
      .withJobId(jobId)
      .withMaxResults(1000)
      .withNextToken(paginationToken);
      response = textractClient.getDocumentAnalysis(request);
      if (metadata == null)
        metadata = response.getDocumentMetadata();
      fullBlocks.addAll(response.getBlocks());
      paginationToken=response.getNextToken();
      if (paginationToken==null)
        finished=true;
    }
    ResultJson json = new ResultJson();
    json.blocks = fullBlocks;
    json.documentMetaData = metadata;
    ObjectMapper mapper = new ObjectMapper();
    mapper.setVisibility(PropertyAccessor.GETTER, Visibility.PUBLIC_ONLY);
    String result = null;
    try {
      result = mapper.writeValueAsString(json);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return result;
  }
}
