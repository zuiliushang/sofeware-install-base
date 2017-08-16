# Docker network

## Docker 默认网络

Docker 支持使用网络驱动来支持容器的网络。

默认情况下 Docker 提供了两种网络驱动,分别是```bridge```和```overlay```驱动。

也可以自定义自己的网路驱动。

### Docker 查看网络

<pre>
[root@docker-node1 file]# docker network ls
b37b9f7c847c        bridge              bridge              local
a1b5f7ef31b2        docker_gwbridge     bridge              local
24f78225c44f        host                host                local
1124bo7w1p5k        ingress             overlay             swarm
aa2eca268db6        none                null                local
qbxgxyztgn64        raindrops_webnet    overlay             swarm
</pre>

```bridge```是一个特别的网络。如果在创建容器的时候设置网络驱动,不然Docker会将用此驱动。

### 查看 Docker 容器的IP地址

<pre>
[root@docker-node1 file]# docker network inspect bridge
[
    {
        "Name": "bridge",
        "Id": "b37b9f7c847c72a397b353bf1c0a0e0c42bfa49df393926e9297b121b64131f1",
        "Created": "2017-08-15T10:42:10.449947749+08:00",
        "Scope": "local",
        "Driver": "bridge",
        "EnableIPv6": false,
        "IPAM": {
            "Driver": "default",
            "Options": null,
            "Config": [
                {
                    "Subnet": "172.17.0.0/16",
                    "Gateway": "172.17.0.1"
                }
            ]
        },
        "Internal": false,
        "Attachable": false,
        "Ingress": false,
        "ConfigFrom": {
            "Network": ""
        },
        "ConfigOnly": false,
        "Containers": {},
        "Options": {
            "com.docker.network.bridge.default_bridge": "true",
            "com.docker.network.bridge.enable_icc": "true",
            "com.docker.network.bridge.enable_ip_masquerade": "true",
            "com.docker.network.bridge.host_binding_ipv4": "0.0.0.0",
            "com.docker.network.bridge.name": "docker0",
            "com.docker.network.driver.mtu": "1500"
        },
        "Labels": {}
    }
]
</pre>

### 删除容器的network driver

```[root@docker-node1 file]# docker network disconnect bridge 7fc43fa1370e```

### 创建容器的network driver

<pre>
[root@docker-node1 file]# docker network create -d bridge my_bridge
5f9060f5b8242a2f5e409db84da73862f99abf39ec9b07bc05052971744d48f0
</pre>

如果执行错误
<pre>
[root@docker-node1 file]# docker network create -d bridge my_bridge
Error response from daemon: Failed to Setup IP tables: Unable to enable SKIP DNAT rule:  (iptables failed: iptables --wait -t nat -I DOCKER -i br-3519efb10ad5 -j RETURN: iptables: No chain/target/match by that name.
 (exit status 1))
</pre>

解决方式:

<pre>
[root@docker-node1 file]# vim /etc/sysconfig/iptables-config 
# sample configuration for iptables service  
# you can edit this manually or use system-config-firewall  
# please do not ask us to add additional ports/services to this default configuration  
*nat  
:PREROUTING ACCEPT [27:11935]  
:INPUT ACCEPT [0:0]  
:OUTPUT ACCEPT [0:0]  
:POSTROUTING ACCEPT [0:0]  
:DOCKER -[0:0]  
-A PREROUTING -m addrtype --dst-type LOCAL -j DOCKER  
-A OUTPUT !-d 127.0.0.0/8-m addrtype --dst-type LOCAL -j DOCKER  
-A POSTROUTING -s 172.17.0.0/16!-o docker0 -j MASQUERADE  
COMMIT  
#  
*filter  
:INPUT ACCEPT [0:0]  
:FORWARD ACCEPT [0:0]  
:OUTPUT ACCEPT [0:0]  
:DOCKER -[0:0]  
-A FORWARD -o docker0 -j DOCKER  
-A FORWARD -o docker0 -m conntrack --ctstate RELATED,ESTABLISHED -j ACCEPT  
-A FORWARD -i docker0 !-o docker0 -j ACCEPT  
-A FORWARD -i docker0 -o docker0 -j ACCEPT  
-A INPUT -m state --state RELATED,ESTABLISHED -j ACCEPT  
-A INPUT -p icmp -j ACCEPT  
-A INPUT -i lo -j ACCEPT  
-A INPUT -p tcp -m state --state NEW -m tcp --dport 22-j ACCEPT  
-A INPUT -p tcp -m state --state NEW -m tcp --dport 9090-j ACCEPT  
-A INPUT -p tcp -m state --state NEW -m tcp --dport 1521-j ACCEPT  
-A INPUT -p tcp -m state --state NEW -m tcp --dport 6379-j ACCEPT  
-A INPUT -j REJECT --reject-with icmp-host-prohibited  
-A FORWARD -j REJECT --reject-with icmp-host-prohibited  
COMMIT
[root@docker-node1 file]# systemctl restart firewalld
[root@docker-node1 file]# docker network create -d bridge my_bridge
5f9060f5b8242a2f5e409db84da73862f99abf39ec9b07bc05052971744d48f0
</pre>

### 创建容器的时候添加 network driver

<pre>
[root@docker-node1 file]# docker run -d --net=my_bridge --name rotos centos
WARNING: IPv4 forwarding is disabled. Networking will not work.
033de30bfeff831848f2a0de5ed092660d4ece402c213838b01583d369a3e067
[root@docker-node1 file]# docker inspect --format='{{json .NetworkSettings.Networks}}'  rotos
{"my_bridge":{"IPAMConfig":null,"Links":null,"Aliases":["033de30bfeff"],"NetworkID":"5f9060f5b8242a2f5e409db84da73862f99abf39ec9b07bc05052971744d48f0","EndpointID":"","Gateway":"","IPAddress":"","IPPrefixLen":0,"IPv6Gateway":"","GlobalIPv6Address":"","GlobalIPv6PrefixLen":0,"MacAddress":"","DriverOpts":null}}
</pre>


### 给已运行的容器添加 network driver 

<pre> docker network connect my_bridge web </pre>