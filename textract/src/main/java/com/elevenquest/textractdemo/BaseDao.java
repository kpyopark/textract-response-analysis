package com.elevenquest.textractdemo;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLTimeoutException;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * create table tab_textract_result (image_id varchar(150) primary key,
 * before_after varchar(1), parent_image_id varchar(150), request_json jsonb,
 * job_id varchar(255), result_json jsonb);
 */
public class BaseDao {

  static Logger logger = LogManager.getLogger(BaseDao.class);

  String jdbcUrl;
  String user;
  String pass;

  static BaseDao singleton = null;

  public static BaseDao getBaseDao() {
    if (singleton == null) {
      String jdbcUrl = System.getProperty("jdbcUrl");
      String user = System.getProperty("dbuser");
      String pass = System.getProperty("dbpass");
      new BaseDao(jdbcUrl, user, pass);
    }
    return singleton;
  }

  public BaseDao(String jdbcUrl, String user, String pass) {
    this.jdbcUrl = jdbcUrl;
    this.user = user;
    this.pass = pass;
    if (singleton == null)
      singleton = this;
  }

  public void createRecord(ResultStruct record) {
    PreparedStatement stmt = null;
    try (Connection conn = getConnection()) {
      stmt = conn.prepareStatement(
          "insert into tab_textract_result (image_id, before_after, parent_image_id, result_json, request_json, job_id) values ( ?, ?, ?, ? ::jsonb, ? ::jsonb, ? )");
      stmt.setString(1, record.imageId);
      stmt.setString(2, record.beforeAfter);
      stmt.setString(3, record.parentImageId);
      stmt.setObject(4, record.resultJson);
      stmt.setString(5, record.requestJson);
      stmt.setString(6, record.jobId);
      int cnt = stmt.executeUpdate();
      if (cnt > 0) {
        logger.info("Creating a record - {} - successed.", record.imageId);
      } else {
        logger.info("Creating a record - {} - failed.", record.imageId);
      }
      // conn.commit();
    } catch (Exception e) {
      logger.error("Creating a record failed. {}", e);
    } finally {
      if (stmt != null)
        try {
          stmt.close();
        } catch (Exception e1) {
          logger.warn("Closing connection failed. {}", e1);
        }
    }
  }

  public int updateRecord(ResultStruct record) {
    PreparedStatement stmt = null;
    int cnt = 0;
    try (Connection conn = getConnection()) {
      stmt = conn.prepareStatement(
          "update tab_textract_result set modified_at = now(), result_status = ?, result_json = ? ::jsonb where image_id = ?");
      stmt.setString(1, record.resultStatus);
      stmt.setObject(2, record.resultJson);
      stmt.setString(3, record.imageId);
      cnt = stmt.executeUpdate();
      if (cnt > 0) {
        logger.info("update a record - {} - successed.", record.imageId);
      } else {
        logger.info("update a record - {} - failed.", record.imageId);
      }
      // conn.commit();
    } catch (Exception e) {
      logger.error("Creating a record failed. {}", e);
    } finally {
      if (stmt != null)
        try {
          stmt.close();
        } catch (Exception e1) {
          logger.warn("Closing connection failed. {}", e1);
        }
    }
    return cnt;
  }

  public List<ResultStruct> selectAllRecords() {
    List<ResultStruct> list = new ArrayList<ResultStruct>();
    PreparedStatement stmt = null;
    ResultStruct rtn = null;
    try (Connection conn = getConnection()) {
      stmt = conn.prepareStatement(
          "with unprocessed_files as ( select distinct image_id from tab_textract_result ttr except select distinct file_id from invoice_requests_extracted ire2 ) select image_id, before_after, parent_image_id, job_id from tab_textract_result where image_id in (select image_id from unprocessed_files)");
      ResultSet rs = stmt.executeQuery();
      while(rs.next()) {
        rtn = new ResultStruct();
        list.add(rtn);
        rtn.imageId = rs.getString(1);
        rtn.beforeAfter = rs.getString(2);
        rtn.parentImageId = rs.getString(3);
        rtn.jobId = rs.getString(4);
      }
      rs.close();
    } catch (Exception e) {
      logger.error("select the record failed.", e);
    } finally {
      if (stmt != null)
        try {
          stmt.close();
        } catch (Exception e1) {
          logger.warn("Closing connection failed.", e1);
        }
    }
    return list;
  }

  public List<ResultStruct> selectLostsRecords() {
    List<ResultStruct> list = new ArrayList<ResultStruct>();
    PreparedStatement stmt = null;
    ResultStruct rtn = null;
    try (Connection conn = getConnection()) {
      stmt = conn.prepareStatement(
          "select image_id, before_after, parent_image_id, job_id from tab_textract_result where image_id in ('20220207_92533e8f-07dc-467c-8171-3780294bb676_001', '20210521_12f499ee-4554-449b-a58f-13f7e30b949d_002')");
      ResultSet rs = stmt.executeQuery();
      while(rs.next()) {
        rtn = new ResultStruct();
        list.add(rtn);
        rtn.imageId = rs.getString(1);
        rtn.beforeAfter = rs.getString(2);
        rtn.parentImageId = rs.getString(3);
        rtn.jobId = rs.getString(4);
      }
      rs.close();
    } catch (Exception e) {
      logger.error("select the record failed.", e);
    } finally {
      if (stmt != null)
        try {
          stmt.close();
        } catch (Exception e1) {
          logger.warn("Closing connection failed.", e1);
        }
    }
    return list;
  }

  public ResultStruct selectRecord(String imageId) {
    PreparedStatement stmt = null;
    ResultStruct rtn = null;
    try (Connection conn = getConnection()) {
      stmt = conn.prepareStatement(
          "select image_id, before_after, parent_image_id, result_json, request_json, job_id from tab_textract_result where image_id = ?");
      stmt.setObject(1, imageId);
      ResultSet rs = stmt.executeQuery();
      if (rs.next()) {
        rtn = new ResultStruct();
        rtn.imageId = rs.getString(1);
        rtn.beforeAfter = rs.getString(2);
        rtn.parentImageId = rs.getString(3);
        rtn.resultJson = rs.getString(4);
        rtn.requestJson = rs.getString(5);
        rtn.jobId = rs.getString(6);
        rs.close();
      }
    } catch (Exception e) {
      logger.error("select the record failed. {}", imageId, e);
    } finally {
      if (stmt != null)
        try {
          stmt.close();
        } catch (Exception e1) {
          logger.warn("Closing connection failed. {}", e1);
        }
    }
    return rtn;
  }

  private Connection getConnection() throws SQLException, SQLTimeoutException {
    Connection conn = DriverManager.getConnection(this.jdbcUrl, this.user, this.pass);
    return conn;
  }

  public void createInvoiceRecordExtractedFromTextract(InvoiceRecord record) {
    PreparedStatement stmt = null;
    try (Connection conn = getConnection()) {
      stmt = conn.prepareStatement(
          "insert into invoice_requests_extracted (file_id,img_id,ct_no,app_yrmm,sa_ocrn_sno,cur_cd,premium,minimum_deposit_premium,adjustment_premium,reinstatement_premium,premium_p_f_ent,premium_p_f_wds,x_l_premium,return_premium,commission,flat_provisional_commission,adjustment_commission,profit_commission,brokerage,tax,overriding_comm,charge,prem_res_rtd,prem_res_rld,p_f_prem_res_rtd,p_f_prem_res_rld,lae,loss_paid,loss_recovery,cash_loss,cash_loss_refund,loss_res_rtd,loss_res_rld,loss_p_f_ent,loss_p_f_wda,interest,tax_on_interest,miscellaneous,dr_premium,dr_minimum_deposit_premium,dr_adjustment_premium,dr_reinstatement_premium,dr_premium_p_f_ent,dr_premium_p_f_wds,dr_x_l_premium,dr_return_premium,dr_commission,dr_flat_provisional_commission,dr_adjustment_commission,dr_profit_commission,dr_brokerage,dr_tax,dr_overriding_comm,dr_charge,dr_prem_res_rtd,dr_prem_res_rld,dr_p_f_prem_res_rtd,dr_p_f_prem_res_rld,dr_lae,dr_loss_paid,dr_loss_recovery,dr_cash_loss,dr_cash_loss_refund,dr_loss_res_rtd,dr_loss_res_rld,dr_loss_p_f_ent,dr_loss_p_f_wda,dr_interest,dr_tax_on_interest,dr_miscellaneous,cr_premium,cr_minimum_deposit_premium,cr_adjustment_premium,cr_reinstatement_premium,cr_premium_p_f_ent,cr_premium_p_f_wds,cr_x_l_premium,cr_return_premium,cr_commission,cr_flat_provisional_commission,cr_adjustment_commission,cr_profit_commission,cr_brokerage,cr_tax,cr_overriding_comm,cr_charge,cr_prem_res_rtd,cr_prem_res_rld,cr_p_f_prem_res_rtd,cr_p_f_prem_res_rld,cr_lae,cr_loss_paid,cr_loss_recovery,cr_cash_loss,cr_cash_loss_refund,cr_loss_res_rtd,cr_loss_res_rld,cr_loss_p_f_ent,cr_loss_p_f_wda,cr_interest,cr_tax_on_interest,cr_miscellaneous) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
      stmt.setString(1, record.fileId);
      stmt.setString(2, record.imgId);
      stmt.setString(3, record.ctNo);
      stmt.setString(4, record.appYrmm);
      stmt.setString(5, record.saOcrnSno);
      stmt.setString(6, record.curCd);
      stmt.setObject(7, record.premium);
      stmt.setObject(8, record.minimumDepositPremium);
      stmt.setObject(9, record.adjustmentPremium);
      stmt.setObject(10, record.reinstatementPremium);
      stmt.setObject(11, record.premiumPFEnt);
      stmt.setObject(12, record.premiumPFWds);
      stmt.setObject(13, record.xLPremium);
      stmt.setObject(14, record.returnPremium);
      stmt.setObject(15, record.commission);
      stmt.setObject(16, record.flatProvisionalCommission);
      stmt.setObject(17, record.adjustmentCommission);
      stmt.setObject(18, record.profitCommission);
      stmt.setObject(19, record.brokerage);
      stmt.setObject(20, record.tax);
      stmt.setObject(21, record.overridingComm);
      stmt.setObject(22, record.charge);
      stmt.setObject(23, record.premResRtd);
      stmt.setObject(24, record.premResRld);
      stmt.setObject(25, record.pFPremResRtd);
      stmt.setObject(26, record.pFPremResRld);
      stmt.setObject(27, record.lae);
      stmt.setObject(28, record.lossPaid);
      stmt.setObject(29, record.lossRecovery);
      stmt.setObject(30, record.cashLoss);
      stmt.setObject(31, record.cashLossRefund);
      stmt.setObject(32, record.lossResRtd);
      stmt.setObject(33, record.lossResRld);
      stmt.setObject(34, record.lossPFEnt);
      stmt.setObject(35, record.lossPFWda);
      stmt.setObject(36, record.interest);
      stmt.setObject(37, record.taxOnInterest);
      stmt.setObject(38, record.miscellaneous);
      stmt.setObject(39, record.drPremium);
      stmt.setObject(40, record.drMinimumDepositPremium);
      stmt.setObject(41, record.drAdjustmentPremium);
      stmt.setObject(42, record.drReinstatementPremium);
      stmt.setObject(43, record.drPremiumPFEnt);
      stmt.setObject(44, record.drPremiumPFWds);
      stmt.setObject(45, record.drXLPremium);
      stmt.setObject(46, record.drReturnPremium);
      stmt.setObject(47, record.drCommission);
      stmt.setObject(48, record.drFlatProvisionalCommission);
      stmt.setObject(49, record.drAdjustmentCommission);
      stmt.setObject(50, record.drProfitCommission);
      stmt.setObject(51, record.drBrokerage);
      stmt.setObject(52, record.drTax);
      stmt.setObject(53, record.drOverridingComm);
      stmt.setObject(54, record.drCharge);
      stmt.setObject(55, record.drPremResRtd);
      stmt.setObject(56, record.drPremResRld);
      stmt.setObject(57, record.drPFPremResRtd);
      stmt.setObject(58, record.drPFPremResRld);
      stmt.setObject(59, record.drLae);
      stmt.setObject(60, record.drLossPaid);
      stmt.setObject(61, record.drLossRecovery);
      stmt.setObject(62, record.drCashLoss);
      stmt.setObject(63, record.drCashLossRefund);
      stmt.setObject(64, record.drLossResRtd);
      stmt.setObject(65, record.drLossResRld);
      stmt.setObject(66, record.drLossPFEnt);
      stmt.setObject(67, record.drLossPFWda);
      stmt.setObject(68, record.drInterest);
      stmt.setObject(69, record.drTaxOnInterest);
      stmt.setObject(70, record.drMiscellaneous);
      stmt.setObject(71, record.crPremium);
      stmt.setObject(72, record.crMinimumDepositPremium);
      stmt.setObject(73, record.crAdjustmentPremium);
      stmt.setObject(74, record.crReinstatementPremium);
      stmt.setObject(75, record.crPremiumPFEnt);
      stmt.setObject(76, record.crPremiumPFWds);
      stmt.setObject(77, record.crXLPremium);
      stmt.setObject(78, record.crReturnPremium);
      stmt.setObject(79, record.crCommission);
      stmt.setObject(80, record.crFlatProvisionalCommission);
      stmt.setObject(81, record.crAdjustmentCommission);
      stmt.setObject(82, record.crProfitCommission);
      stmt.setObject(83, record.crBrokerage);
      stmt.setObject(84, record.crTax);
      stmt.setObject(85, record.crOverridingComm);
      stmt.setObject(86, record.crCharge);
      stmt.setObject(87, record.crPremResRtd);
      stmt.setObject(88, record.crPremResRld);
      stmt.setObject(89, record.crPFPremResRtd);
      stmt.setObject(90, record.crPFPremResRld);
      stmt.setObject(91, record.crLae);
      stmt.setObject(92, record.crLossPaid);
      stmt.setObject(93, record.crLossRecovery);
      stmt.setObject(94, record.crCashLoss);
      stmt.setObject(95, record.crCashLossRefund);
      stmt.setObject(96, record.crLossResRtd);
      stmt.setObject(97, record.crLossResRld);
      stmt.setObject(98, record.crLossPFEnt);
      stmt.setObject(99, record.crLossPFWda);
      stmt.setObject(100, record.crInterest);
      stmt.setObject(101, record.crTaxOnInterest);
      stmt.setObject(102, record.crMiscellaneous);
      int cnt = stmt.executeUpdate();
      if (cnt > 0) {
        logger.info("Creating a record - {} - successed.", record.imgId);
      } else {
        logger.info("Creating a record - {} - failed.", record.imgId);
      }
      // conn.commit();
    } catch (Exception e) {
      logger.error("Creating a record failed. {}", e);
    } finally {
      if (stmt != null)
        try {
          stmt.close();
        } catch (Exception e1) {
          logger.warn("Closing connection failed. {}", e1);
        }
    }
  }

  public List<PivotColumn> getPivotColumns(String columnName) {
    PreparedStatement stmt = null;
    List<PivotColumn> rtn = new ArrayList<PivotColumn>();
    try (Connection conn = getConnection()) {
      stmt = conn.prepareStatement(
          "select * from columnmatch(?) where columnindex = '1'");
      stmt.setString(1, columnName);
      ResultSet rs = stmt.executeQuery();
      while (rs.next()) {
        PivotColumn pivot = new PivotColumn(columnName, rs.getString(1), rs.getInt(2), rs.getInt(3));
        rtn.add(pivot);
      }
      rs.close();
    } catch (Exception e) {
      logger.error("select the record failed. {}", columnName, e);
    } finally {
      if (stmt != null)
        try {
          stmt.close();
        } catch (Exception e1) {
          logger.warn("Closing connection failed. {}", e1);
        }
    }
    return rtn;

  }

}
