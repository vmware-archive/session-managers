package com.gopivotal.manager;

import java.io.Serializable;

public class SampleSessionObject implements Serializable {
  private static final long serialVersionUID = -7695256507142362389L;

  private String sampleField;
  
  private transient long nonSerializableField;

  public String getSampleField() {
    return sampleField;
  }

  public void setSampleField(String sampleField) {
    this.sampleField = sampleField;
  }

  public long getNonSerializableField() {
    return nonSerializableField;
  }

  public void setNonSerializableField(long nonSerializableField) {
    this.nonSerializableField = nonSerializableField;
  }
}
