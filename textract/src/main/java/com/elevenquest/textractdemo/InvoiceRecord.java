package com.elevenquest.textractdemo;

import java.util.HashMap;

import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class InvoiceRecord {

  public InvoiceRecord() {

  }

  String fileId = null;
  String imgId = null;
  String ctNo = null;
  String appYrmm = null;
  String saOcrnSno = null;
  String curCd = null;
  Float premium = null;
  Float minimumDepositPremium = null;
  Float adjustmentPremium = null;
  Float reinstatementPremium = null;
  Float premiumPFEnt = null;
  Float premiumPFWds = null;
  Float xLPremium = null;
  Float returnPremium = null;
  Float commission = null;
  Float flatProvisionalCommission = null;
  Float adjustmentCommission = null;
  Float profitCommission = null;
  Float brokerage = null;
  Float tax = null;
  Float overridingComm = null;
  Float charge = null;
  Float premResRtd = null;
  Float premResRld = null;
  Float pFPremResRtd = null;
  Float pFPremResRld = null;
  Float lae = null;
  Float lossPaid = null;
  Float lossRecovery = null;
  Float cashLoss = null;
  Float cashLossRefund = null;
  Float lossResRtd = null;
  Float lossResRld = null;
  Float lossPFEnt = null;
  Float lossPFWda = null;
  Float interest = null;
  Float taxOnInterest = null;
  Float miscellaneous = null;
  Float drPremium = null;
  Float drMinimumDepositPremium = null;
  Float drAdjustmentPremium = null;
  Float drReinstatementPremium = null;
  Float drPremiumPFEnt = null;
  Float drPremiumPFWds = null;
  Float drXLPremium = null;
  Float drReturnPremium = null;
  Float drCommission = null;
  Float drFlatProvisionalCommission = null;
  Float drAdjustmentCommission = null;
  Float drProfitCommission = null;
  Float drBrokerage = null;
  Float drTax = null;
  Float drOverridingComm = null;
  Float drCharge = null;
  Float drPremResRtd = null;
  Float drPremResRld = null;
  Float drPFPremResRtd = null;
  Float drPFPremResRld = null;
  Float drLae = null;
  Float drLossPaid = null;
  Float drLossRecovery = null;
  Float drCashLoss = null;
  Float drCashLossRefund = null;
  Float drLossResRtd = null;
  Float drLossResRld = null;
  Float drLossPFEnt = null;
  Float drLossPFWda = null;
  Float drInterest = null;
  Float drTaxOnInterest = null;
  Float drMiscellaneous = null;
  Float crPremium = null;
  Float crMinimumDepositPremium = null;
  Float crAdjustmentPremium = null;
  Float crReinstatementPremium = null;
  Float crPremiumPFEnt = null;
  Float crPremiumPFWds = null;
  Float crXLPremium = null;
  Float crReturnPremium = null;
  Float crCommission = null;
  Float crFlatProvisionalCommission = null;
  Float crAdjustmentCommission = null;
  Float crProfitCommission = null;
  Float crBrokerage = null;
  Float crTax = null;
  Float crOverridingComm = null;
  Float crCharge = null;
  Float crPremResRtd = null;
  Float crPremResRld = null;
  Float crPFPremResRtd = null;
  Float crPFPremResRld = null;
  Float crLae = null;
  Float crLossPaid = null;
  Float crLossRecovery = null;
  Float crCashLoss = null;
  Float crCashLossRefund = null;
  Float crLossResRtd = null;
  Float crLossResRld = null;
  Float crLossPFEnt = null;
  Float crLossPFWda = null;
  Float crInterest = null;
  Float crTaxOnInterest = null;
  Float crMiscellaneous = null;

  static HashMap<String, String> COLUMN_MAP = null;

  static {
    COLUMN_MAP = new HashMap<String, String>();
    COLUMN_MAP.put("file_id", "fileId");
    COLUMN_MAP.put("img_id", "imgId");
    COLUMN_MAP.put("ct_no", "ctNo");
    COLUMN_MAP.put("app_yrmm", "appYrmm");
    COLUMN_MAP.put("sa_ocrn_sno", "saOcrnSno");
    COLUMN_MAP.put("cur_cd", "curCd");
    COLUMN_MAP.put("premium", "premium");
    COLUMN_MAP.put("minimum_deposit_premium", "minimumDepositPremium");
    COLUMN_MAP.put("adjustment_premium", "adjustmentPremium");
    COLUMN_MAP.put("reinstatement_premium", "reinstatementPremium");
    COLUMN_MAP.put("premium_p_f_ent", "premiumPFEnt");
    COLUMN_MAP.put("premium_p_f_wds", "premiumPFWds");
    COLUMN_MAP.put("x_l_premium", "xLPremium");
    COLUMN_MAP.put("return_premium", "returnPremium");
    COLUMN_MAP.put("commission", "commission");
    COLUMN_MAP.put("flat_provisional_commission", "flatProvisionalCommission");
    COLUMN_MAP.put("adjustment_commission", "adjustmentCommission");
    COLUMN_MAP.put("profit_commission", "profitCommission");
    COLUMN_MAP.put("brokerage", "brokerage");
    COLUMN_MAP.put("tax", "tax");
    COLUMN_MAP.put("overriding_comm", "overridingComm");
    COLUMN_MAP.put("charge", "charge");
    COLUMN_MAP.put("prem_res_rtd", "premResRtd");
    COLUMN_MAP.put("prem_res_rld", "premResRld");
    COLUMN_MAP.put("p_f_prem_res_rtd", "pFPremResRtd");
    COLUMN_MAP.put("p_f_prem_res_rld", "pFPremResRld");
    COLUMN_MAP.put("lae", "lae");
    COLUMN_MAP.put("loss_paid", "lossPaid");
    COLUMN_MAP.put("loss_recovery", "lossRecovery");
    COLUMN_MAP.put("cash_loss", "cashLoss");
    COLUMN_MAP.put("cash_loss_refund", "cashLossRefund");
    COLUMN_MAP.put("loss_res_rtd", "lossResRtd");
    COLUMN_MAP.put("loss_res_rld", "lossResRld");
    COLUMN_MAP.put("loss_p_f_ent", "lossPFEnt");
    COLUMN_MAP.put("loss_p_f_wda", "lossPFWda");
    COLUMN_MAP.put("interest", "interest");
    COLUMN_MAP.put("tax_on_interest", "taxOnInterest");
    COLUMN_MAP.put("miscellaneous", "miscellaneous");
    COLUMN_MAP.put("dr_premium", "drPremium");
    COLUMN_MAP.put("dr_minimum_deposit_premium", "drMinimumDepositPremium");
    COLUMN_MAP.put("dr_adjustment_premium", "drAdjustmentPremium");
    COLUMN_MAP.put("dr_reinstatement_premium", "drReinstatementPremium");
    COLUMN_MAP.put("dr_premium_p_f_ent", "drPremiumPFEnt");
    COLUMN_MAP.put("dr_premium_p_f_wds", "drPremiumPFWds");
    COLUMN_MAP.put("dr_x_l_premium", "drXLPremium");
    COLUMN_MAP.put("dr_return_premium", "drReturnPremium");
    COLUMN_MAP.put("dr_commission", "drCommission");
    COLUMN_MAP.put("dr_flat_provisional_commission", "drFlatProvisionalCommission");
    COLUMN_MAP.put("dr_adjustment_commission", "drAdjustmentCommission");
    COLUMN_MAP.put("dr_profit_commission", "drProfitCommission");
    COLUMN_MAP.put("dr_brokerage", "drBrokerage");
    COLUMN_MAP.put("dr_tax", "drTax");
    COLUMN_MAP.put("dr_overriding_comm", "drOverridingComm");
    COLUMN_MAP.put("dr_charge", "drCharge");
    COLUMN_MAP.put("dr_prem_res_rtd", "drPremResRtd");
    COLUMN_MAP.put("dr_prem_res_rld", "drPremResRld");
    COLUMN_MAP.put("dr_p_f_prem_res_rtd", "drPFPremResRtd");
    COLUMN_MAP.put("dr_p_f_prem_res_rld", "drPFPremResRld");
    COLUMN_MAP.put("dr_lae", "drLae");
    COLUMN_MAP.put("dr_loss_paid", "drLossPaid");
    COLUMN_MAP.put("dr_loss_recovery", "drLossRecovery");
    COLUMN_MAP.put("dr_cash_loss", "drCashLoss");
    COLUMN_MAP.put("dr_cash_loss_refund", "drCashLossRefund");
    COLUMN_MAP.put("dr_loss_res_rtd", "drLossResRtd");
    COLUMN_MAP.put("dr_loss_res_rld", "drLossResRld");
    COLUMN_MAP.put("dr_loss_p_f_ent", "drLossPFEnt");
    COLUMN_MAP.put("dr_loss_p_f_wda", "drLossPFWda");
    COLUMN_MAP.put("dr_interest", "drInterest");
    COLUMN_MAP.put("dr_tax_on_interest", "drTaxOnInterest");
    COLUMN_MAP.put("dr_miscellaneous", "drMiscellaneous");
    COLUMN_MAP.put("cr_premium", "crPremium");
    COLUMN_MAP.put("cr_minimum_deposit_premium", "crMinimumDepositPremium");
    COLUMN_MAP.put("cr_adjustment_premium", "crAdjustmentPremium");
    COLUMN_MAP.put("cr_reinstatement_premium", "crReinstatementPremium");
    COLUMN_MAP.put("cr_premium_p_f_ent", "crPremiumPFEnt");
    COLUMN_MAP.put("cr_premium_p_f_wds", "crPremiumPFWds");
    COLUMN_MAP.put("cr_x_l_premium", "crXLPremium");
    COLUMN_MAP.put("cr_return_premium", "crReturnPremium");
    COLUMN_MAP.put("cr_commission", "crCommission");
    COLUMN_MAP.put("cr_flat_provisional_commission", "crFlatProvisionalCommission");
    COLUMN_MAP.put("cr_adjustment_commission", "crAdjustmentCommission");
    COLUMN_MAP.put("cr_profit_commission", "crProfitCommission");
    COLUMN_MAP.put("cr_brokerage", "crBrokerage");
    COLUMN_MAP.put("cr_tax", "crTax");
    COLUMN_MAP.put("cr_overriding_comm", "crOverridingComm");
    COLUMN_MAP.put("cr_charge", "crCharge");
    COLUMN_MAP.put("cr_prem_res_rtd", "crPremResRtd");
    COLUMN_MAP.put("cr_prem_res_rld", "crPremResRld");
    COLUMN_MAP.put("cr_p_f_prem_res_rtd", "crPFPremResRtd");
    COLUMN_MAP.put("cr_p_f_prem_res_rld", "crPFPremResRld");
    COLUMN_MAP.put("cr_lae", "crLae");
    COLUMN_MAP.put("cr_loss_paid", "crLossPaid");
    COLUMN_MAP.put("cr_loss_recovery", "crLossRecovery");
    COLUMN_MAP.put("cr_cash_loss", "crCashLoss");
    COLUMN_MAP.put("cr_cash_loss_refund", "crCashLossRefund");
    COLUMN_MAP.put("cr_loss_res_rtd", "crLossResRtd");
    COLUMN_MAP.put("cr_loss_res_rld", "crLossResRld");
    COLUMN_MAP.put("cr_loss_p_f_ent", "crLossPFEnt");
    COLUMN_MAP.put("cr_loss_p_f_wda", "crLossPFWda");
    COLUMN_MAP.put("cr_interest", "crInterest");
    COLUMN_MAP.put("cr_tax_on_interest", "crTaxOnInterest");
    COLUMN_MAP.put("cr_miscellaneous", "crMiscellaneous");
  }

  @Override
  public String toString() {
    ObjectMapper mapper = new ObjectMapper();
    mapper.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
    try { 
      return mapper.writeValueAsString(this);
    } catch(JsonProcessingException jpe) {
      jpe.printStackTrace();
    }
    return "";
  }

}
