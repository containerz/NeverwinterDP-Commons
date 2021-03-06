package com.neverwinterdp.server;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.neverwinterdp.server.ActivityLog.Type;

/**
 * @author Than Nguyen
 * @email  tuan08@gmail.com
 * 
 * TODO: remove this class and the relation, replace by the MonitorRegistry
 */
public class ActivityLogs implements Serializable {
  private List<ActivityLog> activityLogs = new CopyOnWriteArrayList<ActivityLog>() ;
  
  public List<ActivityLog> getActivityLog() { return this.activityLogs ; }
  
  public void add(ActivityLog log) {
    this.activityLogs.add(log) ;
  }
  
  public int size() { return this.activityLogs.size(); }
  
  public void clear() {
    this.activityLogs.clear(); 
  }
  
  public List<ActivityLog> find(Type type, String activity) { 
    List<ActivityLog> holder = new ArrayList<ActivityLog>() ;
    for(int i = 0; i < activityLogs.size(); i++) {
      ActivityLog log = activityLogs.get(i) ;
      if(type.equals(log.getType()) && activity.equalsIgnoreCase(log.getActivity())) {
        holder.add(log) ;
      }
    }
    return holder ; 
  }
}
