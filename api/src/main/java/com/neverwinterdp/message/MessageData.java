package com.neverwinterdp.message;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.type.TypeReference;
import com.neverwinterdp.util.JSONSerializer;

/**
 * @author Tuan Nguyen
 * @email  tuan08@gmail.com
 */
public class MessageData implements Serializable {
  static public enum SerializeType { json, xml, binary }
  
  private String        type;
  private byte[]        data;
  private SerializeType serializeType;

  public MessageData() {
  }
  
  public MessageData(String type, byte[] data) {
    this.type = type ;
    this.data = data ;
  }
  
  public <T> MessageData(T obj) {
    this.type = obj.getClass().getName() ;
    setDataAs(obj) ;
  }
  
  public String getType() { return type; }
  public void setType(String type) { this.type = type; }

  public byte[] getData() { return data; }
  public void setData(byte[] data) { this.data = data; }

  public SerializeType getSerializeType() {
    return serializeType;
  }

  public void setSerializeType(SerializeType serializeType) {
    this.serializeType = serializeType;
  }

  @JsonIgnore
  public <T> T getDataAs(Class<T> type)  {
    if(data == null) return null ;
    return JSONSerializer.INSTANCE.fromBytes(data, type);
  }
  
  @JsonIgnore
  public <T> List<T> getDataAs(TypeReference<List<T>> tref) throws Exception {
    return JSONSerializer.INSTANCE.fromBytes(data, tref);
  }
  
  @JsonIgnore
  public <T> void setDataAs(T obj) {
    if(obj == null) {
      data = null; 
    } else {
      data = JSONSerializer.INSTANCE.toBytes(obj);
    }
  }
}
