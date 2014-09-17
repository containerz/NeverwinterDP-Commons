package com.neverwinterdp.yara.snapshot;

import java.io.Serializable;
import java.util.Map;
import java.util.TreeMap;

import com.neverwinterdp.yara.Counter;
import com.neverwinterdp.yara.MetricRegistry;
import com.neverwinterdp.yara.Timer;

public class MetricRegistrySnapshot implements Serializable {
  private Map<String, Long> counters = new TreeMap<String, Long>() ;
  private Map<String, TimerSnapshot>   timers = new TreeMap<String, TimerSnapshot>() ;

  public MetricRegistrySnapshot() {}
  
  public MetricRegistrySnapshot(MetricRegistry registry) {
    for(Map.Entry<String, Counter> entry : registry.getCounters().entrySet()) {
      counters.put(entry.getKey(), entry.getValue().getCount()) ;
    }
    
    for(Map.Entry<String, Timer> entry : registry.getTimers().entrySet()) {
      timers.put(entry.getKey(), new TimerSnapshot(entry.getValue())) ;
    }
  }
  
  public Map<String, Long> getCounters() { return counters; }
  public void setCounters(Map<String, Long> counters) { this.counters = counters; }
  
  public Map<String, TimerSnapshot> getTimers() { return timers; }
  public void setTimers(Map<String, TimerSnapshot> timers) { this.timers = timers; }
}
