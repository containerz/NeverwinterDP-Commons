package com.neverwinterdp.server.client;

import java.io.Serializable;

import com.beust.jcommander.Parameter;
import com.neverwinterdp.server.cluster.ClusterClient;
import com.neverwinterdp.server.cluster.ClusterMember;
import com.neverwinterdp.server.command.ServerCommand;
import com.neverwinterdp.server.command.ServerCommandResult;
import com.neverwinterdp.server.command.ServiceCommand;
import com.neverwinterdp.server.command.ServiceCommandResult;

public class MemberSelector implements Serializable {
  @Parameter(names = {"--member"}, description = "Select the member by host:port")
  public String member ;
  
  @Parameter(names = {"--member-role"}, description = "Select the member by role")
  public String memberRole ;
  
  @Parameter(names = {"--timeout"}, description = "Command timeout")
  public long timeout = 10000 ;
  
  
  public MemberSelector() {} 
  
  public MemberSelector(CommandParams params) {
    this.member = params.getString("member") ;
    this.memberRole = params.getString("member-role") ;
    this.timeout = params.getLong("timeout", 10000l) ;
  }
  
  public ClusterMember[] getMembers(ClusterClient clusterClient) {
    if(member != null) {
      return new ClusterMember[] { clusterClient.getClusterMember(member)} ;
    } else if(memberRole != null) {
      return clusterClient.getClusterRegistration().findClusterMemberByRole(memberRole) ;
    }
    return null ;
  }
  
  public <T> ServerCommandResult<T>[] execute(ClusterClient client, ServerCommand<T> command) {
    ClusterMember[] members = getMembers(client) ;
    command.setTimeout(timeout) ;
    if(members == null) return client.execute(command) ; 
    else return client.execute(command, members) ;
  }
  
  public <T> ServiceCommandResult<T>[] execute(ClusterClient client, ServiceCommand<T> command) {
    ClusterMember[] members = getMembers(client) ;
    command.setTimeout(timeout) ;
    if(members == null) return client.execute(command) ; 
    else return client.execute(command, members) ;
  }
}