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

``` host```网络添加一个容器到主机网络栈。对于网络来说,主机和容器并没有隔离。例如:如果使用```host```网络来运行容器运行占80端口的web服务。这个web服务同样可以在主机80端口被找到。

```none```和```host```网络不是直接可配置在Docker。但是,可以配置默认```bridge```网络,并且可以自己定义```bridge```网络。

## ```bridge```默认网络

```bridge```默认网络存在在所有的Docker主机里。如果没有指定一个不同的网络,新容器会自动连接到这个```bridge```网络。

使用```docker network inspect```命令可以查看到网络信息:

<pre>
[root@docker-node1 ~]# docker network inspect bridge
[
    {
        "Name": "bridge",
        "Id": "a772ffc671fec2fbc0f319478a40765abcd6ecc62575513b9860d535ea8683a3",
        "Created": "2017-08-23T10:10:47.467829354+08:00",
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

运行两个```busybox```容器,这些容器使用默认的```bridge```网络。
<pre>
[root@docker-node1 ~]# docker run -itd --name=container1 busybox
3286bfe6724e866b068a475638cf680e147fcec37370e106c69f1358c7d2af61
[root@docker-node1 ~]# docker run -itd --name=container2 busybox
664afdd0b191fa070b50e61e3c36911da792ccba6faed473a328bd593808b439
</pre>

查看两个容器的```bridge```网络。两个容器都连接了网络。
<pre>
[root@docker-node1 ~]# docker network inspect bridge
[
    {
        "Name": "bridge",
        "Id": "a772ffc671fec2fbc0f319478a40765abcd6ecc62575513b9860d535ea8683a3",
        "Created": "2017-08-23T10:10:47.467829354+08:00",
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
        "Containers": {
            "3286bfe6724e866b068a475638cf680e147fcec37370e106c69f1358c7d2af61": {
                "Name": "container1",
                "EndpointID": "0074bd4a6fde7f6b35c54efb5cfe38856b0d95c188ace3482118db2c06ac7ff9",
                "MacAddress": "02:42:ac:11:00:02",
                "IPv4Address": "172.17.0.2/16",
                "IPv6Address": ""
            },
            "664afdd0b191fa070b50e61e3c36911da792ccba6faed473a328bd593808b439": {
                "Name": "container2",
                "EndpointID": "a89b1020d8114183035769b91bf996c023b286936eedb187f442db9f39cd9236",
                "MacAddress": "02:42:ac:11:00:03",
                "IPv4Address": "172.17.0.3/16",
                "IPv6Address": ""
            }
        },
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

容器连接了默认的```bridge```网络可以通过IP地址彼此通讯。Docker不支持使用默认```bridge```网络来实现服务自动注册。如果希望容器通过容器名称来解析IP地址,则需要使用自定义的网络来代替。可以通过使用额外的```docker run --link```命令来连接两个容器,但是一般不推荐这么使用。

使用```attach```查看容器网络。
<pre>
[root@docker-node1 ~]# docker attach container1
/ # ifconfig
eth0      Link encap:Ethernet  HWaddr 02:42:AC:11:00:02  
          inet addr:172.17.0.2  Bcast:0.0.0.0  Mask:255.255.0.0
          UP BROADCAST RUNNING MULTICAST  MTU:1500  Metric:1
          RX packets:16 errors:0 dropped:0 overruns:0 frame:0
          TX packets:0 errors:0 dropped:0 overruns:0 carrier:0
          collisions:0 txqueuelen:0 
          RX bytes:1296 (1.2 KiB)  TX bytes:0 (0.0 B)

lo        Link encap:Local Loopback  
          inet addr:127.0.0.1  Mask:255.0.0.0
          UP LOOPBACK RUNNING  MTU:65536  Metric:1
          RX packets:0 errors:0 dropped:0 overruns:0 frame:0
          TX packets:0 errors:0 dropped:0 overruns:0 carrier:0
          collisions:0 txqueuelen:1 
          RX bytes:0 (0.0 B)  TX bytes:0 (0.0 B)
</pre>

进入另外一个容器ping第一个容器:
<pre>
[root@docker-node1 ~]# docker attach container2
/ # ifconfig
eth0      Link encap:Ethernet  HWaddr 02:42:AC:11:00:03  
          inet addr:172.17.0.3  Bcast:0.0.0.0  Mask:255.255.0.0
          UP BROADCAST RUNNING MULTICAST  MTU:1500  Metric:1
          RX packets:9 errors:0 dropped:0 overruns:0 frame:0
          TX packets:0 errors:0 dropped:0 overruns:0 carrier:0
          collisions:0 txqueuelen:0 
          RX bytes:718 (718.0 B)  TX bytes:0 (0.0 B)

lo        Link encap:Local Loopback  
          inet addr:127.0.0.1  Mask:255.0.0.0
          UP LOOPBACK RUNNING  MTU:65536  Metric:1
          RX packets:0 errors:0 dropped:0 overruns:0 frame:0
          TX packets:0 errors:0 dropped:0 overruns:0 carrier:0
          collisions:0 txqueuelen:1 
          RX bytes:0 (0.0 B)  TX bytes:0 (0.0 B)

/ # ping 172.17.0.1
PING 172.17.0.1 (172.17.0.1): 56 data bytes
64 bytes from 172.17.0.1: seq=0 ttl=64 time=0.143 ms
64 bytes from 172.17.0.1: seq=1 ttl=64 time=0.384 ms
64 bytes from 172.17.0.1: seq=2 ttl=64 time=0.113 ms
^C
--- 172.17.0.1 ping statistics ---
3 packets transmitted, 3 packets received, 0% packet loss
round-trip min/avg/max = 0.113/0.213/0.384 ms
</pre>

可以ping通。

## 自定义网络

推荐使用自定义```bridge```网络来控制容器之间的通讯和开启自动DNS解决容器名称到IP地址装换。Docker提供默认```network driver```来创建这些网络。可以自己创建一个新的```bridge```网络,```overlay```网络或```MACVLAN```网络。也可以定制和控制创建一个网络插件或远程网络。

可以根据需要来创建任何数量的网络并且可以同时在容器里连接多个网络。另外,可以不需要重启容器来连接或断开运行中的容器。当一个容器连接多个网络时,和外网连接按照词法顺序的第一个非内部网络提供。

### bridge network 桥接网络

```bridge```网络是Docker中使用最广泛的网络类型。桥接网络和默认的```bridge```网络很相似,但是添加一些新功能并且移除了一些旧功能。通过容器来试验创建一些```bridge```网络:
<pre>
[root@docker-node1 ~]# docker network create --driver bridge isolated_nw
30100bfa60cc48b5274e9b83f59d2f03d95ed0c7f9cd8205305efa191722d61a
[root@docker-node1 ~]# docker network inspect isolated_nw
[
    {
        "Name": "isolated_nw",
        "Id": "30100bfa60cc48b5274e9b83f59d2f03d95ed0c7f9cd8205305efa191722d61a",
        "Created": "2017-08-23T17:24:47.661494602+08:00",
        "Scope": "local",
        "Driver": "bridge",
        "EnableIPv6": false,
        "IPAM": {
            "Driver": "default",
            "Options": {},
            "Config": [
                {
                    "Subnet": "172.20.0.0/16",
                    "Gateway": "172.20.0.1"
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
        "Options": {},
        "Labels": {}
    }
]
[root@docker-node1 ~]# docker network ls
NETWORK ID          NAME                DRIVER              SCOPE
a772ffc671fe        bridge              bridge              local
a1b5f7ef31b2        docker_gwbridge     bridge              local
24f78225c44f        host                host                local
1124bo7w1p5k        ingress             overlay             swarm
30100bfa60cc        isolated_nw         bridge              local
5f9060f5b824        my_bridge           bridge              local
aa2eca268db6        none                null                local
</pre>

创建之后,可以使用```docker run --network=<NETWORK>```命令来加入到容器中:
<pre>
[root@docker-node1 ~]# docker run --network=isolated_nw -itd --name=container3 busybox
1779598032d7e206451b0806c02b63141e902cb72e01c64773451295c0f04408
[root@docker-node1 ~]# docker network inspect isolated_nw
[
    {
        "Name": "isolated_nw",
        "Id": "30100bfa60cc48b5274e9b83f59d2f03d95ed0c7f9cd8205305efa191722d61a",
        "Created": "2017-08-23T17:24:47.661494602+08:00",
        "Scope": "local",
        "Driver": "bridge",
        "EnableIPv6": false,
        "IPAM": {
            "Driver": "default",
            "Options": {},
            "Config": [
                {
                    "Subnet": "172.20.0.0/16",
                    "Gateway": "172.20.0.1"
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
        "Containers": {
            "1779598032d7e206451b0806c02b63141e902cb72e01c64773451295c0f04408": {
                "Name": "container3",
                "EndpointID": "c77a7ac78869769b8827d80e151fc9a9e5f238b92d862fac54a9d12532b9ebdf",
                "MacAddress": "02:42:ac:14:00:02",
                "IPv4Address": "172.20.0.2/16",
                "IPv6Address": ""
            }
        },
        "Options": {},
        "Labels": {}
    }
]
</pre>

给容器加入自定义的网络必须都存在在同一个Docker主机上。每个容器可以立即和其他容器在网络上进行通讯。尽管网络本身将容器和网络互相隔离出来。
![](http://i.imgur.com/EoaLj18.png)

使用自定义网络的话,连接```link```不支持。可以暴露容器的端口在这个网络里。这个是非常有用的当你想将一部分的```bridge```网络暴露在外网中。

![](http://i.imgur.com/uzipNCf.png)

当想要在一个简单的host中运行相对较小的网络,```bridge```网络是很有用的。但是,创建一个复杂大型的网络还是通过创建一个```overlay```网络。

## ```docker_gwbridge```网络

```docker_gwbridge```是Docker在两种不同情况下自动创建的一个本地的```bridge```网络:

- 当```init```或```join```一个```swarm```,Docker会创建```docker_gwbridge```网络并且用于不同主机的swarm 节点间的交流。
- 当没有容器网络能够提供外网连接,Docker连接容器的```docker_gwbridge```网络来加入其它容器的网络,这样容器就可以连接到外网或者其它的```swarm```节点。

在你需要一个定制的配置可以先创建```docker_gwbridge```网络。如果不创建,Docker会根据需要来创建它:
<pre>
[root@docker-swarm ~]# docker network create --subnet 127.30.0.0/16 \
> --opt com.docker.network.bridge.name=docker_gwbridge \
> --opt com.docker.network.bridge.enable_icc=false \
> docker_gwbridge
25e572a322e351989cb10d5b40139f1c58c96bd507dd1f364813009cb0deba0c
</pre>
当你使用```overlay```网络时,```docker_gwbridge```网络总是存在。

## swarm中的```overlay```网络

你可以通过创建一个```overlay```网络来管理swarm节点而不用额外的```key-value```存储。swarm让```overlay```网络只存在于需要服务的节点上。当创建一个使用```overlay```网络的服务时,管理节点会自动的扩展```overlay```网络到运行服务任务的节点。

如何创建一个网络并应用到swarm中管理节点的服务:
<pre>
[root@docker-swarm ~]# docker network create \
> --driver overlay \
> --subnet 10.0.9.0/24 \
> my-multi-host-network
q7f6u7ap2mlmsu138ipzpg4l5
[root@docker-swarm ~]# docker service create --replicas 2 --network my-multi-host-network --name my-web nginx
iejmftd98kf1bm9x2cmlsvxy1
Since --detach=false was not specified, tasks will be created in the background.
In a future release, --detach=false will become the default.
[root@docker-swarm ~]# docker service ls
ID                  NAME                MODE                REPLICAS            IMAGE               PORTS
iejmftd98kf1        my-web              replicated          2/2                 nginx:latest        
[root@docker-swarm ~]# 
</pre>

只有swarm服务才能连接到```overlay```网络。

### 没有Swarm的```overlay```网络

如果不使用Docker中的swarm,```overlay```网络需要一个有效的```key-value```存储服务。包括```Consul、Etcd、Zookeeper(分布式存储)```。在创建网络之间必须要先安装配置选择的```key-value```存储服务。你想要连接的网络和服务的Docker主机必须能够通讯。

> **注意**:Docker引擎如果运行swarm了,那么和其他的使用外部```key-value```存储服务不兼容。

这种方式在很多Docker用户中使用```overlay```网络不推荐。它可以与标准的swarm独立使用并能帮助系统管理员在Docker上构建解决方案。但它可能在未来被弃用。

## 定制网络插件

如果上面提出的网络没法解决需求,可以使用Docker基础插件来编写自己的网络驱动插件(network driver plugin)。插件将作为一个独立的进程运行在Docker守护进程的主机中。使用网络插件是一个高级话题。

网络插件和其他的插件一样有它主机的规则和安装方式。所有的定制的plugin使用plugin API,并且有一个围绕着安装的生命周期。开启、停止、活动。

创建和安装一个自定义的网路驱动之后,就可以创建一个网络通过使用```--driver```来使用这个网络驱动:

```
docker network create --driver <DRIVER PLUGIN> <NETWORK>
```

## 嵌入式DNS服务

Docker守护进程运行一个嵌入式DNS服务,这个DNS服务在连接相同的自定义网络中提供DNS服务来让这些容器可以将容器名称解析成IP地址。如果这个嵌入式DNS服务解决不了这个需求,Docker就会寻找其他配置在容器的DNS服务。为了便于创建容器,只有在127.0.0.11可访问的嵌入式DNS服务器将被列在容器的resolv.conf文件中。

## Exposing and publishing ports

在Docker网络里,有两种不同的机制涉及到网络端口:exposing 和 publishing端口:

- 可以在```Dockerfile```里使用```EXPOSE```关键字或者```docker run --expose```来暴露端口。暴露端口是一种记录使用哪个端口的方法,但实际上并没有映射或打开任何端口。将端口是可选的。

- 可以在```Dockerile```里使用```PUBLISH```关键字或者```docker run --publish```来公开端口。这将告诉Docker在容器接口上打开端口。当一个端口被公开,除非你在容器运行的时候指定映射的端口,否则它将映射到主机一个超过30000的端口。不能在```Dockerfile```中指定端口到主机的映射。因为无法保证端口在运行映像的主机上可用。<br>
公开容器的80端口映射在主机随机的一个大端口:
<pre>
[root@docker-node1 ~]# docker run -it -d -p 80 nginx
6de078391ebb467f06e550b2b33eed149eb373cf8183055c9a020940e69fb662
[root@docker-node1 ~]# docker ps
CONTAINER ID        IMAGE               COMMAND                  CREATED             STATUS              PORTS                   NAMES
6de078391ebb        nginx               "nginx -g 'daemon ..."   33 seconds ago      Up 30 seconds       0.0.0.0:32768->80/tcp   epic_snyder
</pre>
映射到8080
<pre>
[root@docker-node1 ~]# docker run -it -d -p 8080:80 nginx
8661f09ca9e4b15e03f47b02695bdd535c8a16d14b3aaa44bdeffd2535312727
[root@docker-node1 ~]# docker ps
CONTAINER ID        IMAGE               COMMAND                  CREATED             STATUS              PORTS                   NAMES
8661f09ca9e4        nginx               "nginx -g 'daemon ..."   4 seconds ago       Up 2 seconds        0.0.0.0:8080->80/tcp    hardcore_mcnulty
</pre>

## Links

在Docker包含一个自定义网络之前,可以使用```--link```功能来允许容器将另外一个容器的名称解析成一个IP地址也可以让它访问链接的容器的环境变量。但是,尽量避免使用```--link```标签。

## Docker和Iptables

Linux主机使用一个核心模块```iptables```来管理网络设备的使用,包括路由,端口转发,NAT等。当启动或停止发布端口的容器、创建或修改网络或将容器附加到它们或其他与网络相关的操作时,Docker修改了iptables规则。