package com.neverwinterdp.hadoop.yarn.app.master;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.hadoop.yarn.api.records.Container;
import org.apache.hadoop.yarn.api.records.ContainerStatus;
import org.apache.hadoop.yarn.client.api.AMRMClient.ContainerRequest;

import com.neverwinterdp.hadoop.yarn.app.worker.AppWorkerContainerInfo;

@XmlRootElement(name = "AppMonitor")
@XmlAccessorType(XmlAccessType.FIELD)
public class AppMasterMonitor implements Serializable {
  
  private AtomicInteger completedContainerCount = new AtomicInteger() ;
  private AtomicInteger allocatedContainerCount = new AtomicInteger() ;
  private AtomicInteger failedContainerCount = new AtomicInteger() ;
  private AtomicInteger requestedContainerCount = new AtomicInteger() ;
  private Map<Integer, AppWorkerContainerInfo> containerInfos = new LinkedHashMap<Integer, AppWorkerContainerInfo>() ;
  
  public AtomicInteger getCompletedContainerCount() {
    return completedContainerCount;
  }

  public AtomicInteger getAllocatedContainerCount() {
    return allocatedContainerCount;
  }

  public AtomicInteger getFailedContainerCount() {
    return failedContainerCount;
  }

  public AtomicInteger getRequestedContainerCount() {
    return requestedContainerCount;
  }

  public AppWorkerContainerInfo getContainerInfo(int id) { return containerInfos.get(id) ; }
  
  public AppWorkerContainerInfo[] getContainerInfos() {
    return containerInfos.values().toArray(new AppWorkerContainerInfo[containerInfos.size()]) ;
  }
  
  public void setContainerInfos(AppWorkerContainerInfo[] cinfo) {
    for(AppWorkerContainerInfo sel : cinfo) {
      containerInfos.put(sel.getContainerId(), sel) ;
    }
  }
  
  public void onContainerRequest(ContainerRequest containerReq) {
    requestedContainerCount.incrementAndGet() ;
  }
  
  public void onCompletedContainer(ContainerStatus status) {
    AppWorkerContainerInfo cmonitor = containerInfos.get(status.getContainerId().getId()) ;
    completedContainerCount.incrementAndGet();
  }
  
  public void onFailedContainer(ContainerStatus status) {
    AppWorkerContainerInfo cmonitor = containerInfos.get(status.getContainerId().getId()) ;
    failedContainerCount.incrementAndGet();
  }
  
  public void onAllocatedContainer(Container container, List<String> commands) {
    allocatedContainerCount.incrementAndGet() ;
    AppWorkerContainerInfo cmonitor = new AppWorkerContainerInfo(container, commands) ;
    containerInfos.put(cmonitor.getContainerId(), cmonitor) ;
  }
}