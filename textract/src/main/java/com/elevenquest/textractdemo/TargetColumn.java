package com.elevenquest.textractdemo;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

public class TargetColumn {
  String columnName;
  List<String> pivotTexts;

  
  public static void main(String[] args) {
    calculatePivotColumns();
  }

  public TargetColumn(String columnName, List<String> pivotTexts) {
    this.columnName = columnName;
    this.pivotTexts = pivotTexts;
  }

  public static List<TargetColumn> getTargetColumns() {
    Hashtable<String, List<String>> columns = calculatePivotColumns();
    List<TargetColumn> rtn = new ArrayList<TargetColumn>();
    columns.keySet().forEach(key -> {
      rtn.add(new TargetColumn(key, columns.get(key)));
    });
    return rtn;
  }

  public void updatePivotColumns(List<PivotColumn> pivots) {
  }

  private static String getGeneralizedPivotColumnName(String columnName) {
    if(columnName.indexOf("cr_") == 0)
      return columnName.substring(3);
    if(columnName.indexOf("dr_") == 0)
      return columnName.substring(3);
    return columnName;
  }

  private static Hashtable<String, List<PivotColumn>> splitEachColumn(List<PivotColumn> allapplicants) {
    Hashtable<String, List<PivotColumn>> rtn = new Hashtable<String, List<PivotColumn>>();
    boolean exist = false;
    for(PivotColumn pivot: allapplicants) {
      List<PivotColumn> list = null;
      pivot.pivotColumnName = getGeneralizedPivotColumnName(pivot.pivotColumnName);
      if((list = rtn.get(pivot.pivotColumnName)) == null) {
        list = new ArrayList<PivotColumn>();
        rtn.put(pivot.pivotColumnName, list);
      }
      exist = false;
      for(PivotColumn registered: list) {
        if (registered.columnText.equals(pivot.columnText)) {
          registered.count += pivot.count;
          exist = true;
          break;
        }
      }
      if (!exist)
        list.add(pivot);
    }
    return rtn;
  }

  private static int getTotalCount(List<PivotColumn> applicants) {
    int totalCnt = 0;
    for (PivotColumn applicant: applicants) {
      totalCnt += applicant.count;
    }
    return totalCnt;
  }

  private static boolean isMajorityColumnText(int totalCount, PivotColumn pivotValue) {
    int threshold = Math.round(totalCount * 0.01f) + 1;
    if(pivotValue.columnText.equals("Deposit Premium")) {
      System.out.println("Threshold:[" + threshold + "] pivot count:[" + pivotValue.count + "]");
    }
    return (pivotValue.count >= threshold);
  }

  private static int MAXIMUM_NUMERIC_LETTERS = 4;
  private static int MINIMUM_REPRESENT_LETTERS = 5;

  private static String getSimilarPartsOfString(String prev, String cur) {
    if (prev == null || cur == null) return null;
    int maxlen = Math.min(prev.length(), cur.length());
    int pos = 0;
    int numericlettercount = 0;
    for(; pos < maxlen; pos++) {
      if(cur.charAt(pos) >= '0' && cur.charAt(pos) <= '9')
        numericlettercount++;
      if(prev.charAt(pos) != cur.charAt(pos))
        break;
    }
    if (numericlettercount > MAXIMUM_NUMERIC_LETTERS) return null;
    if (pos < MINIMUM_REPRESENT_LETTERS) return null;
    return prev.substring(0, pos);
  }

  private static void removePreviousSamePivotText(String candidate, List<String> prevCandidates) {
    List<String> removeTargets = new ArrayList<String>();
    prevCandidates.forEach(prev -> {
      String target = getSimilarPartsOfString(prev, candidate);
      if(target != null && (1.0 * target.length() / candidate.length()) > 0.9) {
        removeTargets.add(prev);
      }
    });
    prevCandidates.removeAll(removeTargets);
  }

  private static List<String> getCandidateValues(List<PivotColumn> oneColumnApplicants) {
    // "Brokerage / Fee Amount"
    // "Brokerage / Fee Amount (x,xxx,xxx @ 2.50%)"
    // Two lines has same pattern. 
    List<String> candidateValues = new ArrayList<String>();
    String candidateValue = null;
    for (PivotColumn applicant: oneColumnApplicants) {
      if(candidateValue == null) {
        candidateValue = applicant.columnText;
        continue;
      }
      candidateValue = getSimilarPartsOfString(candidateValue, applicant.columnText);
      if (candidateValue != null) {
        // If lots of parts of repsent value are same with previously registered pivot value, remove it first. 
        removePreviousSamePivotText(candidateValue, candidateValues);
        candidateValues.add(candidateValue);  
      }
    }
    return candidateValues;
  }

  private static void removeNonexclusivePivotText(Hashtable<String, List<String>> pivotTextApplicants) {
    Hashtable<String, List<String>> candidateValueList = new Hashtable<String, List<String>>();
    for(String columnName: pivotTextApplicants.keySet()) {
      for(String candidateValue: pivotTextApplicants.get(columnName)) {
        List<String> columnList = null;
        if ((columnList = candidateValueList.get(candidateValue)) == null) {
          columnList = new ArrayList<String>();
          candidateValueList.put(candidateValue, columnList);
        }
        columnList.add(columnName);
      }
    }
    for(String candidateValue: candidateValueList.keySet()) {
      if(candidateValueList.get(candidateValue).size() > 1) {
        for(String columnName: candidateValueList.get(candidateValue)) {
          System.out.println("Column Name:[" + columnName + "] removed pivot column:[" + candidateValue + "]");
          pivotTextApplicants.get(columnName).remove(candidateValue);
        }
      }
    }
  }

  private static Pattern NUMERIC_PATTERN = Pattern.compile("-?\\d+(\\.\\d+)?");
  private static String[] INVALID_WORDS = { "2018", "2019", "2020", "2021", "2022", "2023", "2024", "2025", "2026", "2027", "2028"};
  private static boolean isCandidatable(String pivotText) {
    if(pivotText == null || pivotText.trim().length() == 0)
      return false;
    pivotText = pivotText.trim();
    if(pivotText.length()<5)
      return false;
    if(NUMERIC_PATTERN.matcher(pivotText.trim()).matches())
      return false;
    if(pivotText.contains("QTR"))
      return false;
    for(String invalidword : INVALID_WORDS) {
      if(pivotText.indexOf(invalidword) == 0)
        return false;
    }
    return true;
  }

  public static Hashtable<String, List<String>> calculatePivotColumns() {
    // List<PivotColumn> applicants = getAllPivotApplicantsFromDatabase(); //getAllPivotApplicants();
    List<PivotColumn> applicants = getAllPivotApplicants();

    Hashtable<String, List<PivotColumn>> applicantsPerColumn = splitEachColumn(applicants);
    Hashtable<String, List<String>> pivotTextPerColumn = new Hashtable<String, List<String>>();
    
    // 1. If one pivot value has very big count (almost 50%), it could be a pivot.
    // 2. If one pivot value has very similar string values and the summation of counts could have a majority value (over 25%), it could be a pivot.
    // 3. If one pivot value would be used in two other columns, this pivot value would be discarded.
    for(String columnName: applicantsPerColumn.keySet()) {
      List<PivotColumn> columnApplicants = applicantsPerColumn.get(columnName);
      List<String> matchedPivotText = new ArrayList<String>();
      int sum = getTotalCount(columnApplicants);
      for(PivotColumn pivot: columnApplicants) {
        if (!isCandidatable(pivot.columnText))
          continue;
        if (isMajorityColumnText(sum, pivot)) {
          matchedPivotText.add(pivot.columnText);
          // System.out.println(String.format("{%s}:{%s}", columnName, pivot.columnText));
        }
      }
      matchedPivotText.addAll(getCandidateValues(columnApplicants));
      pivotTextPerColumn.put(columnName, matchedPivotText);
      // matchedPivotText.forEach(pivot -> System.out.println(String.format("{%s}:{%s}", columnName, pivot)));
    }
    // removeNonexclusivePivotText(pivotTextPerColumn);
    for(String columnName: pivotTextPerColumn.keySet()) {
      pivotTextPerColumn.get(columnName).forEach(pivotText -> {
        System.out.println(String.format("{%s}:{%s}", columnName, pivotText));
      });
    }
    return pivotTextPerColumn;
  }

  private static String CSV_FILE = "/home/postgres/devel/koreanre_ocr/resources/pivot_columns.csv";

  public static List<PivotColumn> getAllPivotApplicantsFromDatabase() {
    String[] columnNames = {"premium",
    "minimum_deposit_premium",
    "adjustment_premium",
    "commission",
    "flat_provisional_commission",
    "brokerage",
    "tax",
    "overriding_comm",
    "charge",
    "loss_paid",
    "dr_premium",
    "dr_minimum_deposit_premium",
    "dr_adjustment_premium",
    "dr_reinstatement_premium",
    "dr_premium_p_f_ent",
    "dr_premium_p_f_wds",
    "dr_x_l_premium",
    "dr_return_premium",
    "dr_commission",
    "dr_flat_provisional_commission",
    "dr_adjustment_commission",
    "dr_profit_commission",
    "dr_brokerage",
    "dr_tax",
    "dr_overriding_comm",
    "dr_charge",
    "dr_prem_res_rtd",
    "dr_prem_res_rld",
    "dr_p_f_prem_res_rtd",
    "dr_p_f_prem_res_rld",
    "dr_lae",
    "dr_loss_paid",
    "dr_loss_recovery",
    "dr_cash_loss",
    "dr_cash_loss_refund",
    "dr_loss_res_rtd",
    "dr_loss_res_rld",
    "dr_loss_p_f_ent",
    "dr_loss_p_f_wda",
    "dr_interest",
    "dr_tax_on_interest",
    "dr_miscellaneous",
    "cr_premium",
    "cr_minimum_deposit_premium",
    "cr_adjustment_premium",
    "cr_reinstatement_premium",
    "cr_premium_p_f_ent",
    "cr_premium_p_f_wds",
    "cr_x_l_premium",
    "cr_return_premium",
    "cr_commission",
    "cr_flat_provisional_commission",
    "cr_adjustment_commission",
    "cr_profit_commission",
    "cr_brokerage",
    "cr_tax",
    "cr_overriding_comm",
    "cr_charge",
    "cr_prem_res_rtd",
    "cr_prem_res_rld",
    "cr_p_f_prem_res_rtd",
    "cr_p_f_prem_res_rld",
    "cr_lae",
    "cr_loss_paid",
    "cr_loss_recovery",
    "cr_cash_loss",
    "cr_cash_loss_refund",
    "cr_loss_res_rtd",
    "cr_loss_res_rld",
    "cr_loss_p_f_ent",
    "cr_loss_p_f_wda",
    "cr_interest",
    "cr_tax_on_interest",
    "cr_miscellaneous",
    };
    List<PivotColumn> rtn = new ArrayList<PivotColumn>();
    BaseDao dao = BaseDao.getBaseDao();
    for(String columnName: columnNames) {
      System.out.println("Column Detection...[" + columnName + "]");
      rtn.addAll(dao.getPivotColumns(columnName));
    }
    Writer writer = null;
    CSVPrinter printer = null;
    try {
      writer = new FileWriter(CSV_FILE);
      printer = new CSVPrinter(writer, CSVFormat.DEFAULT);
      for(PivotColumn column: rtn) {
        printer.printRecord(column.pivotColumnName, column.columnText, column.columnIndex, column.count);
      }
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      if(printer != null) try { printer.close(); } catch (Exception ne) {}
    }
    
    return rtn;
  }

  public static List<PivotColumn> getAllPivotApplicants() {
    List<PivotColumn> rtn = new ArrayList<PivotColumn>();
    Reader in = null;
    Iterable<CSVRecord> records = null;
    try {
      in = new FileReader(CSV_FILE);
      records = CSVFormat.DEFAULT.parse(in);
      for (final CSVRecord record : records) {
        String columnName = record.get(0);
        String pivotText = record.get(1);
        String columnIndex = record.get(2);
        String count = record.get(3);
        if(columnIndex.equals("1")) {
          columnName = columnName.replaceAll("[^\\x00-\\x7F]", "");
          rtn.add(new PivotColumn(columnName, pivotText, Integer.parseInt(columnIndex), Integer.parseInt(count)));
        }
      }
    } catch (FileNotFoundException ffe) {
      ffe.printStackTrace();
    } catch (IOException ioe) {
      ioe.printStackTrace();
    }
    return rtn;
  }

}
