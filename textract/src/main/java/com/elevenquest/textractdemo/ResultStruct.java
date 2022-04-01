package com.elevenquest.textractdemo;


/**
 * create table tab_textract_result (image_id varchar(150) primary key, before_after varchar(1), parent_image_id varchar(150), result_json jsonb);
 */
public class ResultStruct {
  String imageId;
  String beforeAfter;   // B/A
  String parentImageId;
  String requestJson;
  String jobId;
  String resultJson;
  String resultStatus;
}
