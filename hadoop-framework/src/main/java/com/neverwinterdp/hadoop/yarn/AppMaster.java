package com.neverwinterdp.hadoop.yarn;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.yarn.api.ApplicationConstants;
import org.apache.hadoop.yarn.api.protocolrecords.AllocateResponse;
import org.apache.hadoop.yarn.api.records.Container;
import org.apache.hadoop.yarn.api.records.ContainerExitStatus;
import org.apache.hadoop.yarn.api.records.ContainerLaunchContext;
import org.apache.hadoop.yarn.api.records.ContainerState;
import org.apache.hadoop.yarn.api.records.ContainerStatus;
import org.apache.hadoop.yarn.api.records.FinalApplicationStatus;
import org.apache.hadoop.yarn.api.records.NodeReport;
import org.apache.hadoop.yarn.api.records.Priority;
import org.apache.hadoop.yarn.api.records.Resource;
import org.apache.hadoop.yarn.client.api.AMRMClient;
import org.apache.hadoop.yarn.client.api.AMRMClient.ContainerRequest;
import org.apache.hadoop.yarn.client.api.NMClient;
import org.apache.hadoop.yarn.client.api.async.AMRMClientAsync;
import org.apache.hadoop.yarn.conf.YarnConfiguration;
import org.apache.hadoop.yarn.exceptions.YarnException;
import org.apache.hadoop.yarn.util.Records;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.JCommander;
import com.neverwinterdp.hadoop.yarn.hello.HelloAppContainerManger;

public class AppMaster {
  protected static final Logger LOGGER = LoggerFactory.getLogger(AppMaster.class.getName());
  
  private AMRMClient<ContainerRequest> amrmClient ;
  private AMRMClientAsync<ContainerRequest> amrmClientAsync ;
  private NMClient nmClient;
  private Configuration conf;
  
  private AppMonitor appMonitor = new AppMonitor() ;
  private AppContainerManager containerManager ;
  
  public AppMaster() {
  }
  
  public AppMonitor getAppMonitor() { return this.appMonitor ; }

  public boolean run(String[] args) throws Exception {
    AppOptions appOpts = new AppOptions() ;
    new JCommander(appOpts, args) ;
    
    conf = new YarnConfiguration();
    for(Map.Entry<String, String> entry : appOpts.conf.entrySet()) {
      conf.set(entry.getKey(), entry.getValue()) ;
    }
    
    Class<?> containerClass = Class.forName(appOpts.containerManager) ;
    containerManager = (AppContainerManager)containerClass.newInstance() ;
    
    amrmClient = AMRMClient.createAMRMClient();
    amrmClientAsync = AMRMClientAsync.createAMRMClientAsync(amrmClient, 1000, new AMRMCallbackHandler());
    amrmClientAsync.init(conf);
    amrmClientAsync.start();

    nmClient = NMClient.createNMClient();
    nmClient.init(conf);
    nmClient.start();

    // Register with RM
    amrmClientAsync.registerApplicationMaster(
      "", //appMasterHostname, 
      0,   //appMasterRpcPort, 
      ""  //appMasterTrackingUrl
    );
    
    containerManager.onInit(this);
    containerManager.waitForComplete(this);
    containerManager.onExit(this);
    amrmClientAsync.unregisterApplicationMaster(FinalApplicationStatus.SUCCEEDED, "", "");
    amrmClientAsync.close(); 
    nmClient.close(); 
    return true;
  }

  public ContainerRequest createContainerRequest(int priority, int numOfCores, int memory) {
    //Priority for worker containers - priorities are intra-application
    Priority containerPriority = Records.newRecord(Priority.class);
    containerPriority.setPriority(priority);
    // Resource requirements for worker containers
    Resource resource = Records.newRecord(Resource.class);
    resource.setMemory(memory);
     resource.setVirtualCores(numOfCores);
    ContainerRequest containerReq = 
      new ContainerRequest(resource, null /* hosts*/, null /*racks*/, containerPriority);
    return containerReq;
  }
  
  public Container requestContainer(int priority, int numOfCores, int memory, long maxWait) throws YarnException, IOException, InterruptedException {
    ContainerRequest containerReq = createContainerRequest(priority, numOfCores, memory) ;
    amrmClient.addContainerRequest(containerReq);
    long stopTime = System.currentTimeMillis() + maxWait ;
    while (System.currentTimeMillis() < stopTime) {
      AllocateResponse response = amrmClient.allocate(0);
      List<Container> containers = response.getAllocatedContainers() ;
      if(containers.size() > 0)return containers.get(0) ;
      Thread.sleep(500);
    }
    return null;
  }
  
  public void add(ContainerRequest containerReq) {
    amrmClientAsync.addContainerRequest(containerReq);
    appMonitor.onContainerRequest(containerReq);
  }
  
  public void startContainer(Container container, String command) throws YarnException, IOException {
    ContainerLaunchContext ctx = Records.newRecord(ContainerLaunchContext.class);
    StringBuilder sb = new StringBuilder();
    List<String> commands = Collections.singletonList(
        sb.append(command).
        append(" 1> ").append(ApplicationConstants.LOG_DIR_EXPANSION_VAR).append("/stdout").
        append(" 2> ").append(ApplicationConstants.LOG_DIR_EXPANSION_VAR).append("/stderr")
        .toString()
        );
    ctx.setCommands(commands);
    nmClient.startContainer(container, ctx);
    appMonitor.onAllocatedContainer(container, commands);
  }
  
  class AMRMCallbackHandler implements AMRMClientAsync.CallbackHandler {
    
    public void onContainersCompleted(List<ContainerStatus> statuses) {
      for (ContainerStatus status: statuses) {
        assert (status.getState() == ContainerState.COMPLETE);
        int exitStatus = status.getExitStatus();
        ContainerInfo containerInfo = appMonitor.getContainerInfo(status.getContainerId().getId()) ;
        if (exitStatus != ContainerExitStatus.SUCCESS) {
          containerManager.onFailedContainer(AppMaster.this, status, containerInfo);
          appMonitor.onFailedContainer(status);
        } else {
          containerManager.onCompleteContainer(AppMaster.this, status, containerInfo);
          appMonitor.onCompletedContainer(status);
        }
      }
    }

    public void onContainersAllocated(List<Container> containers) {
      for (int i = 0; i < containers.size(); i++) {
        Container container = containers.get(i) ;
        containerManager.onAllocatedContainer(AppMaster.this, container);
      }
    }


    public void onNodesUpdated(List<NodeReport> updated) {
    }

    public void onError(Throwable e) {
      amrmClientAsync.stop();
    }

    public void onShutdownRequest() {  }

    public float getProgress() { return 0; }
  }

  static public void main(String[] args) throws Exception {
    new AppMaster().run(args) ;
  }
}