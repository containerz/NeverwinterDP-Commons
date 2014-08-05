package com.neverwinterdp.hadoop.yarn.app;

import org.junit.Test;

import com.neverwinterdp.hadoop.yarn.app.AppClient;
import com.neverwinterdp.hadoop.yarn.app.AppClientMonitor;

public class AppClientIntegrationTest  {

  @Test
  public void testIntegrationAppClient() throws Exception {
    String[] args = { 
        "--app-home", "/tmp/app/hadoop-framework",
        "--upload-app", "build/libs" ,
        "--app-name", "Hello Yarn",
        
        "--container-manager", "com.neverwinterdp.hadoop.yarn.hello.HelloAppContainerManger",
        "--yarnConf:fs.default.name=hdfs://hadoop:9000",
        "--yarnConf:dfs.replication=1",
        "--yarnConf:yarn.resourcemanager.scheduler.address=hadoop:8030",
        "--yarnConf:yarn.resourcemanager.address=hadoop:8032"
      } ;
      
      AppClient appClient = new AppClient() ;
      AppClientMonitor reporter = appClient.run(args);
      reporter.monitor(); 
      reporter.report(System.out);
  }
}