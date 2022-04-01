package com.elevenquest.textractdemo;

public enum CreditEnum {
  CR("cr"),
  DR("dr"),
  NA("na");
  String categoryString;
  private CreditEnum(String category) {
    this.categoryString = category;
  }
  public String category() {
    return this.categoryString;
  }
  public static CreditEnum getCategory(String value) {
    if(value == null)
      return NA;
    if (value.indexOf(CR.category()) > 0)
      return CR;
    if (value.indexOf(DR.category()) > 0)
      return DR;
    return NA;
  }
}
