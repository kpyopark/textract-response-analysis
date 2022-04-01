package com.elevenquest.textractdemo;

public class PivotColumn {
  public PivotColumn(String columnName, String columnText, int columnIndex, int count) {
    this.pivotColumnName = columnName;
    this.columnText = columnText;
    this.columnIndex = columnIndex;
    this.count = count;
  }
  String pivotColumnName;
  String columnText;
  String baseText;
  int columnIndex;
  int count;

}