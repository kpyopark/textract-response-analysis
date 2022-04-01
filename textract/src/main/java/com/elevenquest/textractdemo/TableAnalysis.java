package com.elevenquest.textractdemo;

import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import com.amazonaws.services.textract.model.Block;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class TableAnalysis {
  
  BaseDao dao = null;

  public TableAnalysis() {
    this.dao = BaseDao.getBaseDao();
  }

  public ResultJson getResultFromDatabase(String imageId) {
    ResultStruct result = dao.selectRecord(imageId);
    String jsonStr = result.resultJson;
    ObjectMapper mapper = new ObjectMapper();
    ResultJson json = null;
    try {
      json = mapper.readValue(jsonStr, ResultJson.class);
    } catch (JsonMappingException jme) {
      jme.printStackTrace();
    } catch (JsonProcessingException jpe) {
      jpe.printStackTrace();
    } catch (Exception e) {
      e.printStackTrace();
    }
    return json;
  }

  private static boolean isMatched(TargetColumn targetColumn, String cellValue, int columnIndex) {
    boolean isMatched = false;
    if (cellValue == null || cellValue.trim().length() == 0) return false;
    for(String pivotText: targetColumn.pivotTexts) {
      isMatched = cellValue.indexOf(pivotText) == 0 && columnIndex == 1;
      if(isMatched)
        break;
    }
    return isMatched;
  }

  private static Float parseFloat(String value) throws NumberFormatException {
    if (value == null || value.trim().length() == 0) return null;
    String numericValue = value.toLowerCase().replaceAll(",", "").replaceAll("cr", "").replaceAll("dr", "");
    StringBuffer refinedValue = new StringBuffer();
    int pointCnt = 0;
    for (int pos=0; pos < numericValue.length(); pos++) {
      if(numericValue.charAt(pos) >='0' && numericValue.charAt(pos) <='9') {
        refinedValue.append(numericValue.charAt(pos));
      } else if (numericValue.charAt(pos) == '.') {
        refinedValue.append(numericValue.charAt(pos));
        pointCnt++;
      }
    }
    if (refinedValue.toString().trim().length() == 0)
      return null;
    try {
      if(pointCnt > 1) {
        StringTokenizer st = new StringTokenizer(value, " ");
        Float lastValue = null;
        while(st.hasMoreTokens()) {
          String element = st.nextToken();
          if (parseFloat(element) != null)
            lastValue = parseFloat(element);
        }
        return lastValue;
      } else {
        Float rtnValue = Float.parseFloat(refinedValue.toString());
        return rtnValue;
      }
    } catch (NumberFormatException nfe) {
      System.out.println("input value:" + value + " refined value:" + refinedValue.toString());
      nfe.printStackTrace();
    }
    return null;
  }

  private static String getFieldNameFromColumnName(String columnName) {
    columnName = columnName.trim();
    StringTokenizer st = new StringTokenizer(columnName, "_");
    StringBuffer fieldName = new StringBuffer();
    while(st.hasMoreTokens()) {
      String word = st.nextToken();
      fieldName.append(word.substring(0,1).toUpperCase()).append(word.substring(1).toLowerCase());
    }
    return fieldName.substring(0,1).toLowerCase() + fieldName.substring(1);
  }

  public static void setValue(InvoiceRecord record, String columnName, String value) {
    System.out.println("columnName: [" + columnName + "] value:[" + value + "]");
    if(value == null) return;
    if(value.trim().length() < 1) return;
    CreditEnum crdr;
    if (value.toLowerCase().contains(CreditEnum.CR.category())) {
      crdr = CreditEnum.CR;
      columnName = "cr_" + columnName;
    } else if (value.toLowerCase().contains(CreditEnum.DR.category())) {
      crdr = CreditEnum.DR;
      columnName = "dr_" + columnName;
    } else {
      crdr = CreditEnum.NA;
    }
    
    String fieldName = InvoiceRecord.COLUMN_MAP.get(columnName.trim());
    if(fieldName == null)
      fieldName = getFieldNameFromColumnName(columnName);
    try {
      Float floatValue = parseFloat(value);
      Field targetField = record.getClass().getDeclaredField(fieldName);
      targetField.set(record, floatValue);
      System.out.println("filedName: [" + fieldName  + "] columnName: [" + columnName + "] value:[" + floatValue + "]");
    } catch (Exception e) {
      System.out.println("filedName: [" + fieldName  + "] columnName: [" + columnName + "]");
      e.printStackTrace();
    }
  }
  static DateFormat DF1 = new SimpleDateFormat("dd/MM/yyyy");
  static DateFormat DF2 = new SimpleDateFormat("dd MMM yyyy");
  static DateFormat DF3 = new SimpleDateFormat("MMM yyyy");
  static DateFormat DF4 = new SimpleDateFormat("dd-MMM-yyyy");

  static DateFormat OF = new SimpleDateFormat("yyyyMM");

  private static Date tryParse(DateFormat format, String value) {
    try {
      return format.parse(value);
    } catch (ParseException pe) {}
    return null;
  }

  private static String getCtNo(String ctno) {
    if(ctno == null) return null;
    if(ctno.length() > 14)
      return ctno.substring(0, 14);
    return ctno;
  }

  private static String getCurCd(String curCd) {
    if(curCd == null) return null;
    if(curCd.length() > 5)
      return curCd.substring(0,5);
    return curCd;
  }
  
  private static String getAppYrMM(String invoiceDate) {
    System.out.println("invoiceDate:" + invoiceDate);
    Date refinedValue = null;
    if (refinedValue == null)
      refinedValue = tryParse(DF1, invoiceDate);
    if (refinedValue == null)
      refinedValue = tryParse(DF2, invoiceDate);
    if (refinedValue == null)
      refinedValue = tryParse(DF3, invoiceDate);
    if (refinedValue == null)
      refinedValue = tryParse(DF4, invoiceDate);
    if (refinedValue == null)
      return null;
    return OF.format(refinedValue);
  }

  static Pattern CURRENCY_PATTERN = Pattern.compile(Pattern.quote("currency"), Pattern.CASE_INSENSITIVE);
  static Pattern CT_NO_PATTERN = Pattern.compile(Pattern.quote("your ref no"), Pattern.CASE_INSENSITIVE);
  static Pattern INVOICE_DATE_PATTERN = Pattern.compile(Pattern.quote("invoice date"), Pattern.CASE_INSENSITIVE);

  private static List<Integer> separateEachInvoices(ResultJson json, List<Block> keyCells) {
    List<Integer> rtn = new ArrayList<Integer>();
    int ctnoCnt = 0;
    for(Block keyCell : keyCells) {
      if(CT_NO_PATTERN.matcher(json.getKeyText(keyCell)).find()) {
        System.out.println("ctno:" + json.getValueText(keyCell));
        ctnoCnt++;
        if(rtn.size() < ctnoCnt) {
          rtn.add(keyCell.getPage());
        }
      }
    }
    System.out.println("separateEachInvoices - finished.");
    return rtn;
  }

  static final List<TargetColumn> targetColumns = TargetColumn.getTargetColumns();

  
  public void analyzeTableCells(String fileId, boolean isStored) {
    System.out.println("analyzeTableCells - " + fileId);
    ResultJson json = getResultFromDatabase(fileId);
    if(json == null)
      return;
    List<InvoiceRecord> rtn = new ArrayList<InvoiceRecord>();
    List<Integer> pages = separateEachInvoices(json, json.getKeyCells());
    if (pages.size() == 0) {
      pages.add(1);
    }
    pages.add(1000);
    int minPage = 1;
    int maxPage = 1;
    for(int invoiceCnt = 1; invoiceCnt < pages.size(); invoiceCnt++) {
      minPage = pages.get(invoiceCnt-1);
      maxPage = pages.get(invoiceCnt);
      System.out.println("analyzeTableCells - min:" + minPage + " max:" + maxPage);
      InvoiceRecord record = new InvoiceRecord();
      record.fileId = fileId;
      rtn.add(record);
      for(Block keyCell : json.getKeyCells()) {
        if(keyCell.getPage() < maxPage 
        && keyCell.getPage() >= minPage
        && keyCell.getPage() < (minPage+2)) {
          if(CT_NO_PATTERN.matcher(json.getKeyText(keyCell)).find()) record.ctNo = getCtNo(json.getValueText(keyCell));
          if(INVOICE_DATE_PATTERN.matcher(json.getKeyText(keyCell)).find()) record.appYrmm = getAppYrMM(json.getValueText(keyCell));
          if(CURRENCY_PATTERN.matcher(json.getKeyText(keyCell)).find()) record.curCd =getCurCd(json.getValueText(keyCell));
        }
      }
      System.out.println("analyzeTableCells - kv");
      for(Block cell:json.getCells()) {
        if(cell.getPage() < maxPage
        && cell.getPage() >= minPage
        && cell.getPage() < (minPage+2)) {
          String cellValue = json.getChildText(cell.getId());
          // 
          for(TargetColumn targetColumn: targetColumns) {
            if(isMatched(targetColumn, cellValue, cell.getColumnIndex())) {
              System.out.println("target columnd:[" + targetColumn.columnName + "] matched value: [" + cellValue + "] last cell value:[" + json.getChildText(json.getLastColumn(cell)) +"]" );
              setValue(record, targetColumn.columnName, json.getChildText(json.getLastColumn(cell)));
            }
          }
        }
      }
      System.out.println("analyzeTableCells - record.");
    }
    if (isStored) {
      rtn.forEach(record -> {
        dao.createInvoiceRecordExtractedFromTextract(record);
      });
    }
  }

  public void analyzeAllTextractRecords() {
    List<ResultStruct> list = dao.selectAllRecords();
    for(ResultStruct textractResult: list) {
      analyzeTableCells(textractResult.imageId, true);
    }
  }

  public static void main(String[] args) {
    TableAnalysis analysis = new TableAnalysis();
    //String fileId = "20210225_961b8b9d-09a4-4ad2-a383-77a1295a17c2_001";
    //analysis.analyzeTableCells(fileId, false);
    analysis.analyzeAllTextractRecords();
  }
}
