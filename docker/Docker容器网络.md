# Docker容器网络

## 默认网络

当安装玩Docker,Docker会自动创建三种网络。通过```docker network ls ```来查看:
<pre>
[root@docker-swarm ~]# docker network ls
NETWORK ID          NAME                DRIVER              SCOPE
e2fe46169822        bridge              bridge              local
82895bbd345b        host                host                local
8191326b9ac4        none                null                local
</pre>
这三个网络是建立在Docker之上。当运行一个容器时,可以使用```--network```来指定容器要连接的网络。

```bridge```表示所有Docker创建时候的```docker0```网络。除非你用了```docker run --network=<NETWORK>```。Docker 后台默认连接这个网络,使用```ifconfig```可以查看:
<pre>
[root@docker-swarm ~]# ifconfig
docker0: flags=4099<UP,BROADCAST,MULTICAST>  mtu 1500
        inet 172.17.0.1  netmask 255.255.0.0  broadcast 0.0.0.0
        ether 02:42:04:41:62:b7  txqueuelen 0  (Ethernet)
        RX packets 0  bytes 0 (0.0 B)
        RX errors 0  dropped 0  overruns 0  frame 0
        TX packets 0  bytes 0 (0.0 B)
        TX errors 0  dropped 0 overruns 0  carrier 0  collisions 0
</pre>

```none```网络添加一个容器到一个容器特别网络栈里。这个容器没有网络接口。进入一个容器你可以看到:
<pre>
[root@docker-node1 ~]# docker run -dit --name=nonenetcontainer --network=none centos:latest
cad2130c794f360c56f36e24a4dece2274c23eec0574c350c125db884c6c5770
[root@docker-node1 ~]# docker attach nonenetcontainer
[root@cad2130c794f /]# cat /etc/host
host.conf    hostname     hosts        hosts.allow  hosts.deny   
[root@cad2130c794f /]# cat /etc/hosts
127.0.0.1       localhost
::1     localhost ip6-localhost ip6-loopback
fe00::0 ip6-localnet
ff00::0 ip6-mcastprefix
ff02::1 ip6-allnodes
ff02::2 ip6-allrouters
</pre>