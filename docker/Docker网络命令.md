# Docker 网络命令

提供Docker网络的子命令来操作容器。这些命令包括:

- ```docker network create```
- ```docker network connect```
- ```docker network ls```
- ```docker network rm```
- ```docker network disconnect```
- ```docker network inspect```

## 创建网络

Docker引擎在安装的时候自动的创建一个```bridge```网络。这个网络相当于Docker依赖的```docker0```bridge网络。除了这个网络,可以创建自己的```bridge```或者```overlay```网络。

```bridge```网络存在在运行Docker引擎实例的单个主机上。```overlay```网络可以跨越多个运行自己引擎的主机。如果运行```docker network create```仅仅只有一个网络名称。那么会创建一个```bridge```网络。
<pre>
[root@docker-node1 ~]# docker network create simple-network
92be0536dafa8966fca9ddb2a3280982cddf77c67a3b4e43f39b7b765a344b18
[root@docker-node1 ~]# docker network inspect simple-network
[
    {
        "Name": "simple-network",
        "Id": "92be0536dafa8966fca9ddb2a3280982cddf77c67a3b4e43f39b7b765a344b18",
        "Created": "2017-08-24T17:51:28.677375563+08:00",
        "Scope": "local",
        "Driver": "bridge",
        "EnableIPv6": false,
        "IPAM": {
            "Driver": "default",
            "Options": {},
            "Config": [
                {
                    "Subnet": "172.21.0.0/16",
                    "Gateway": "172.21.0.1"
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
</pre>

不像```bridge```,```overlay```网络需要创建前存在一些条件:

- 访问键值对存储。引擎支持```Consul```、```Etcd```和```ZooKeeper(分布式存储)```的键值对存储。
- 一组与键值存储连接的主机。
- swarm中的每个主机都有一个特别配置的引擎```daemon```。

```dockerd```选项支持```overlay```网络:

- ```--cluster-store```
- ```--cluster-store-opt```
- ```--cluster-advertise```

当创建一个网络,Docker引擎会默认创建一个不重叠的子网。可以通过使用```--subnet```重定义默认并指定一个子网。在```bridge```只能指定一个子网,但在```overlay```中支持多个子网。

> **注意**:非常建议在创建一个网络的时候使用```--subnet```项目。如果```--subnet```没有指定,docker会自动的选择和分配一个网络的子网,它可能会和其他的子网重叠因为不受docker控制。这会在容器连接网络时导致连接出问题或者失败。

除了```--subnet```,还有```--gateway```、```--ip-range```和```--aux-address```:
<pre>
[root@docker-swarm ~]# docker network create -d overlay \
> --subnet=192.168.0.0/16 \
> --subnet=192.170.0.0/16 \
> --gateway=192.168.0.100 \
> --gateway=192.170.0.100 \
> --ip-range=192.168.1.0/24 \
> --aux-address="my-router=192.168.1.5" --aux-address="my-switch=192.168.1.6" \
> --aux-address="my-printer=192.170.1.5" --aux-address="my-nas=192.170.1.6" \
> my-multihost-network
lyuq7c9b5r5n42c32wdipcz0a
</pre>

确定子网并没有重叠,如果重叠,会创建失败并且返回错误。

创建一个自定义网络时,可以给驱动传递额外的选择项。```bridge```接收一下选择项:

配置项 | 等价于 | 作用
---|---|---
```com.docker.network.bridge.name``` | ```-``` | 创建```bridge```的别名
```com.docker.network.bridge.enable_ip_masquerade``` | ```--ip-masq``` | 开启IP伪装
```com.docker.network.bridge.enable_icc``` | ```--icc``` | 启用或禁用容器内部连接
```com.docker.network.bridge.host_binding_ipv4``` | ```--ip``` | 绑定容器端口时默认的IP
```com.docker.network.driver.mtu``` | ```--mtu``` | 设置容器网络MTU

```com.docker.network.driver.mtu```同样支持```overlay```网络。

使用```docker network create```这些属性可以被无视:

属性 | 等价于 | 作用
---|---|---
```--internal``` | ```-``` | 限制对外访问网络
```--ipv6``` | ```--ipv6``` | 开启Ipv6网络

使用```-o```绑定一个指定IP,并使用```docker network inspect```来查看网络:
<pre>
[root@docker-node1 ~]# docker network create -o "com.docker.network.bridge.host_binding_ipv4"="172.17.0.1" rain_network
49b806f05eb273efe636533d5bec9b932a86d56fbb35fa3b3412492bcea1caad
[root@docker-node1 ~]# docker network inspect rain_network
[
    {
        "Name": "rain_network",
        "Id": "49b806f05eb273efe636533d5bec9b932a86d56fbb35fa3b3412492bcea1caad",
        "Created": "2017-08-25T10:04:19.868935193+08:00",
        "Scope": "local",
        "Driver": "bridge",
        "EnableIPv6": false,
        "IPAM": {
            "Driver": "default",
            "Options": {},
            "Config": [
                {
                    "Subnet": "172.22.0.0/16",
                    "Gateway": "172.22.0.1"
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
            "com.docker.network.bridge.host_binding_ipv4": "172.17.0.1"
        },
        "Labels": {}
    }
]
[root@docker-node1 ~]# docker run -d -P --name redis --network rain_network redis
c134dd61401c80586b554fe31cbda07363471ed6a658190f812b65d9ac180007
[root@docker-node1 ~]# docker ps
CONTAINER ID        IMAGE               COMMAND                  CREATED             STATUS              PORTS                        NAMES
c134dd61401c        redis               "docker-entrypoint..."   15 seconds ago      Up 14 seconds       172.17.0.1:32768->6379/tcp   redis
</pre>

## 连接容器

可以连接存在的容器到一个或多个网络。一个容器可以连接到不同的网络驱动的网络里。一旦连接,容器就会互相通讯通过使用其他容器的IP地址或者容器名。对于```overlay```或者自定义的多端口连接的网络,容器连接到同样的多主机网络但从不同的主机同样可以通过这种方式进行通讯。

下面举出例子:

### 两个基础的容器网络

1.首先,创建和运行两个容器,```container1```和```container2```:
<pre>
[root@docker-node1 ~]# docker run -itd --name=container1 busybox
4ce753ef01b29e66029f6bca00fa6b050ab19c57db105d026be02e54ce97dc0a
[root@docker-node1 ~]# docker run -itd --name=container2 busybox
e769ebd52eaae27a12259fbc81c6fb6d471900d5f6f69f63b53929a4ce602741
</pre>

2.创建一个独立的```bridge```网络来测试:
<pre>
[root@docker-node1 ~]# docker network create -d bridge --subnet 172.25.0.0/16 isolated_nw
d0462c4d1e824e18586b211707fc4abe8f3332cba166d89a8b178c2240cc1c96
</pre>

3.连接```container2```到网路然后用```inspect```验证网络的连接情况:
<pre>
[root@docker-node1 ~]# docker network connect isolated_nw container2
[root@docker-node1 ~]# docker network inspect isolated_nw
[
    {
        "Name": "isolated_nw",
        "Id": "d0462c4d1e824e18586b211707fc4abe8f3332cba166d89a8b178c2240cc1c96",
        "Created": "2017-08-25T10:14:14.789166324+08:00",
        "Scope": "local",
        "Driver": "bridge",
        "EnableIPv6": false,
        "IPAM": {
            "Driver": "default",
            "Options": {},
            "Config": [
                {
                    "Subnet": "172.25.0.0/16"
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
            "e769ebd52eaae27a12259fbc81c6fb6d471900d5f6f69f63b53929a4ce602741": {
                "Name": "container2",
                "EndpointID": "79c11f8a2d3803954802e450964f10475c7d4f069f238140cf889211fb9ea329",
                "MacAddress": "02:42:ac:19:00:02",
                "IPv4Address": "172.25.0.2/16",
                "IPv6Address": ""
            }
        },
        "Options": {},
        "Labels": {}
    }
]
</pre>

可以看到```container2```的IP地址是自动获取的。因为创建的时候指定了一个```--subnet```。这个IP地址会在子网中选择。

>**提示**:不做任何连接,```container1```连接到默认的```bridge```网络。

4.开启第三个容器,这次通过在使用```docker run```时的```--network```配置中使用```--ip```指定IP地址连接到```isolated_nw```网络:
<pre>
[root@docker-node1 ~]# docker run --network=isolated_nw --ip=172.25.3.3 -itd --name=container3 busybox
7fd34c0d78c47e72379497e099d05b4128e2106b64b94630cf27e3126c7f1d41
</pre>

只要对容器指定的IP地址是子网的一份。就可以通过```--ip```或者```--ip6```给在连接的容器指定IPv4或IPv6地址。当使用一个自定义网络通过这种方式来指定一个IP地址,该配置就会被保存为容器配置的一部分并且将在容器重新加载时启用。当使用非用户定义的网络时,分配的IP地址被保留。因为不能保证容器守护进程重启子网在将来不会有变化除非使用自定义网络。

5.查看```container3```的网络资源使用:
<pre>
[root@docker-node1 ~]# docker inspect --format='' container3
[
    {
        "Id": "7fd34c0d78c47e72379497e099d05b4128e2106b64b94630cf27e3126c7f1d41",
        "Created": "2017-08-25T02:27:26.937065812Z",
        "Path": "sh",
        "Args": [],
        "State": {
            "Status": "running",
            "Running": true,
            "Paused": false,
            "Restarting": false,
            "OOMKilled": false,
            "Dead": false,
            "Pid": 5493,
            "ExitCode": 0,
            "Error": "",
            "StartedAt": "2017-08-25T02:27:28.419782363Z",
            "FinishedAt": "0001-01-01T00:00:00Z"
        },
        "Image": "sha256:efe10ee6727fe52d2db2eb5045518fe98d8e31fdad1cbdd5e1f737018c349ebb",
        "ResolvConfPath": "/var/lib/docker/containers/7fd34c0d78c47e72379497e099d05b4128e2106b64b94630cf27e3126c7f1d41/resolv.conf",
        "HostnamePath": "/var/lib/docker/containers/7fd34c0d78c47e72379497e099d05b4128e2106b64b94630cf27e3126c7f1d41/hostname",
        "HostsPath": "/var/lib/docker/containers/7fd34c0d78c47e72379497e099d05b4128e2106b64b94630cf27e3126c7f1d41/hosts",
        "LogPath": "/var/lib/docker/containers/7fd34c0d78c47e72379497e099d05b4128e2106b64b94630cf27e3126c7f1d41/7fd34c0d78c47e72379497e099d05b4128e2106b64b94630cf27e3126c7f1d41-json.log",
        "Name": "/container3",
        "RestartCount": 0,
        "Driver": "devicemapper",
        "MountLabel": "",
        "ProcessLabel": "",
        "AppArmorProfile": "",
        "ExecIDs": null,
        "HostConfig": {
            "Binds": null,
            "ContainerIDFile": "",
            "LogConfig": {
                "Type": "json-file",
                "Config": {}
            },
            "NetworkMode": "isolated_nw",
            "PortBindings": {},
            "RestartPolicy": {
                "Name": "no",
                "MaximumRetryCount": 0
            },
            "AutoRemove": false,
            "VolumeDriver": "",
            "VolumesFrom": null,
            "CapAdd": null,
            "CapDrop": null,
            "Dns": [],
            "DnsOptions": [],
            "DnsSearch": [],
            "ExtraHosts": null,
            "GroupAdd": null,
            "IpcMode": "",
            "Cgroup": "",
            "Links": null,
            "OomScoreAdj": 0,
            "PidMode": "",
            "Privileged": false,
            "PublishAllPorts": false,
            "ReadonlyRootfs": false,
            "SecurityOpt": null,
            "UTSMode": "",
            "UsernsMode": "",
            "ShmSize": 67108864,
            "Runtime": "runc",
            "ConsoleSize": [
                0,
                0
            ],
            "Isolation": "",
            "CpuShares": 0,
            "Memory": 0,
            "NanoCpus": 0,
            "CgroupParent": "",
            "BlkioWeight": 0,
            "BlkioWeightDevice": null,
            "BlkioDeviceReadBps": null,
            "BlkioDeviceWriteBps": null,
            "BlkioDeviceReadIOps": null,
            "BlkioDeviceWriteIOps": null,
            "CpuPeriod": 0,
            "CpuQuota": 0,
            "CpuRealtimePeriod": 0,
            "CpuRealtimeRuntime": 0,
            "CpusetCpus": "",
            "CpusetMems": "",
            "Devices": [],
            "DeviceCgroupRules": null,
            "DiskQuota": 0,
            "KernelMemory": 0,
            "MemoryReservation": 0,
            "MemorySwap": 0,
            "MemorySwappiness": -1,
            "OomKillDisable": false,
            "PidsLimit": 0,
            "Ulimits": null,
            "CpuCount": 0,
            "CpuPercent": 0,
            "IOMaximumIOps": 0,
            "IOMaximumBandwidth": 0
        },
        "GraphDriver": {
            "Data": {
                "DeviceId": "286",
                "DeviceName": "docker-253:0-67183793-079fe28f5109fad562526543bd93b63ae39214c66afdfaf4172d1a6f41bdd0db",
                "DeviceSize": "10737418240"
            },
            "Name": "devicemapper"
        },
        "Mounts": [],
        "Config": {
            "Hostname": "7fd34c0d78c4",
            "Domainname": "",
            "User": "",
            "AttachStdin": false,
            "AttachStdout": false,
            "AttachStderr": false,
            "Tty": true,
            "OpenStdin": true,
            "StdinOnce": false,
            "Env": [
                "PATH=/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin"
            ],
            "Cmd": [
                "sh"
            ],
            "ArgsEscaped": true,
            "Image": "busybox",
            "Volumes": null,
            "WorkingDir": "",
            "Entrypoint": null,
            "OnBuild": null,
            "Labels": {}
        },
        "NetworkSettings": {
            "Bridge": "",
            "SandboxID": "b55461573a60a572a9acc9e809c9159bf1883ffeedd66c335970ed5280297c00",
            "HairpinMode": false,
            "LinkLocalIPv6Address": "",
            "LinkLocalIPv6PrefixLen": 0,
            "Ports": {},
            "SandboxKey": "/var/run/docker/netns/b55461573a60",
            "SecondaryIPAddresses": null,
            "SecondaryIPv6Addresses": null,
            "EndpointID": "",
            "Gateway": "",
            "GlobalIPv6Address": "",
            "GlobalIPv6PrefixLen": 0,
            "IPAddress": "",
            "IPPrefixLen": 0,
            "IPv6Gateway": "",
            "MacAddress": "",
            "Networks": {
                "isolated_nw": {
                    "IPAMConfig": {
                        "IPv4Address": "172.25.3.3"
                    },
                    "Links": null,
                    "Aliases": [
                        "7fd34c0d78c4"
                    ],
                    "NetworkID": "d0462c4d1e824e18586b211707fc4abe8f3332cba166d89a8b178c2240cc1c96",
                    "EndpointID": "c3d37590859c0278d3655f6f652aa475c9d1d78ab01371fd213e65fdb1a8fba8",
                    "Gateway": "172.25.0.1",
                    "IPAddress": "172.25.3.3",
                    "IPPrefixLen": 16,
                    "IPv6Gateway": "",
                    "GlobalIPv6Address": "",
                    "GlobalIPv6PrefixLen": 0,
                    "MacAddress": "02:42:ac:19:03:03",
                    "DriverOpts": null
                }
            }
        }
    }
]
</pre>

因为使用```isolated_nw```连接```container3```,所以它不会默认连接```bridge```网络。

6.查看```container2```的网络资源使用。如果已经安装了Python,可以这样:
<pre>
[root@docker-node1 ~]# docker inspect --format='' container2 | python -m json.tool
[
    {
        "AppArmorProfile": "",
        "Args": [],
        "Config": {
            "ArgsEscaped": true,
            "AttachStderr": false,
            "AttachStdin": false,
            "AttachStdout": false,
            "Cmd": [
                "sh"
            ],
            "Domainname": "",
            "Entrypoint": null,
            "Env": [
                "PATH=/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin"
            ],
            "Hostname": "e769ebd52eaa",
            "Image": "busybox",
            "Labels": {},
            "OnBuild": null,
            "OpenStdin": true,
            "StdinOnce": false,
            "Tty": true,
            "User": "",
            "Volumes": null,
            "WorkingDir": ""
        },
        "Created": "2017-08-25T02:12:46.770031803Z",
        "Driver": "devicemapper",
        "ExecIDs": null,
        "GraphDriver": {
            "Data": {
                "DeviceId": "284",
                "DeviceName": "docker-253:0-67183793-b21a30cf0acaeb119170ab85afa1764f9c8a5c4b0f67de9058a8a1b27cbb8aef",
                "DeviceSize": "10737418240"
            },
            "Name": "devicemapper"
        },
        "HostConfig": {
            "AutoRemove": false,
            "Binds": null,
            "BlkioDeviceReadBps": null,
            "BlkioDeviceReadIOps": null,
            "BlkioDeviceWriteBps": null,
            "BlkioDeviceWriteIOps": null,
            "BlkioWeight": 0,
            "BlkioWeightDevice": null,
            "CapAdd": null,
            "CapDrop": null,
            "Cgroup": "",
            "CgroupParent": "",
            "ConsoleSize": [
                0,
                0
            ],
            "ContainerIDFile": "",
            "CpuCount": 0,
            "CpuPercent": 0,
            "CpuPeriod": 0,
            "CpuQuota": 0,
            "CpuRealtimePeriod": 0,
            "CpuRealtimeRuntime": 0,
            "CpuShares": 0,
            "CpusetCpus": "",
            "CpusetMems": "",
            "DeviceCgroupRules": null,
            "Devices": [],
            "DiskQuota": 0,
            "Dns": [],
            "DnsOptions": [],
            "DnsSearch": [],
            "ExtraHosts": null,
            "GroupAdd": null,
            "IOMaximumBandwidth": 0,
            "IOMaximumIOps": 0,
            "IpcMode": "",
            "Isolation": "",
            "KernelMemory": 0,
            "Links": null,
            "LogConfig": {
                "Config": {},
                "Type": "json-file"
            },
            "Memory": 0,
            "MemoryReservation": 0,
            "MemorySwap": 0,
            "MemorySwappiness": -1,
            "NanoCpus": 0,
            "NetworkMode": "default",
            "OomKillDisable": false,
            "OomScoreAdj": 0,
            "PidMode": "",
            "PidsLimit": 0,
            "PortBindings": {},
            "Privileged": false,
            "PublishAllPorts": false,
            "ReadonlyRootfs": false,
            "RestartPolicy": {
                "MaximumRetryCount": 0,
                "Name": "no"
            },
            "Runtime": "runc",
            "SecurityOpt": null,
            "ShmSize": 67108864,
            "UTSMode": "",
            "Ulimits": null,
            "UsernsMode": "",
            "VolumeDriver": "",
            "VolumesFrom": null
        },
        "HostnamePath": "/var/lib/docker/containers/e769ebd52eaae27a12259fbc81c6fb6d471900d5f6f69f63b53929a4ce602741/hostname",
        "HostsPath": "/var/lib/docker/containers/e769ebd52eaae27a12259fbc81c6fb6d471900d5f6f69f63b53929a4ce602741/hosts",
        "Id": "e769ebd52eaae27a12259fbc81c6fb6d471900d5f6f69f63b53929a4ce602741",
        "Image": "sha256:efe10ee6727fe52d2db2eb5045518fe98d8e31fdad1cbdd5e1f737018c349ebb",
        "LogPath": "/var/lib/docker/containers/e769ebd52eaae27a12259fbc81c6fb6d471900d5f6f69f63b53929a4ce602741/e769ebd52eaae27a12259fbc81c6fb6d471900d5f6f69f63b53929a4ce602741-json.log",
        "MountLabel": "",
        "Mounts": [],
        "Name": "/container2",
        "NetworkSettings": {
            "Bridge": "",
            "EndpointID": "a5ebbe534f4cb2b8b0c34d89310501863c38384c3e67f3da2bcf59aefe4987d9",
            "Gateway": "172.17.0.1",
            "GlobalIPv6Address": "",
            "GlobalIPv6PrefixLen": 0,
            "HairpinMode": false,
            "IPAddress": "172.17.0.3",
            "IPPrefixLen": 16,
            "IPv6Gateway": "",
            "LinkLocalIPv6Address": "",
            "LinkLocalIPv6PrefixLen": 0,
            "MacAddress": "02:42:ac:11:00:03",
            "Networks": {
                "bridge": {
                    "Aliases": null,
                    "DriverOpts": null,
                    "EndpointID": "a5ebbe534f4cb2b8b0c34d89310501863c38384c3e67f3da2bcf59aefe4987d9",
                    "Gateway": "172.17.0.1",
                    "GlobalIPv6Address": "",
                    "GlobalIPv6PrefixLen": 0,
                    "IPAMConfig": null,
                    "IPAddress": "172.17.0.3",
                    "IPPrefixLen": 16,
                    "IPv6Gateway": "",
                    "Links": null,
                    "MacAddress": "02:42:ac:11:00:03",
                    "NetworkID": "b497b9048f0e7ff6dbc314d1deac116b301f9731b98e9656ce86f8f7cc4ee3b1"
                },
                "isolated_nw": {
                    "Aliases": [
                        "e769ebd52eaa"
                    ],
                    "DriverOpts": null,
                    "EndpointID": "79c11f8a2d3803954802e450964f10475c7d4f069f238140cf889211fb9ea329",
                    "Gateway": "172.25.0.1",
                    "GlobalIPv6Address": "",
                    "GlobalIPv6PrefixLen": 0,
                    "IPAMConfig": {},
                    "IPAddress": "172.25.0.2",
                    "IPPrefixLen": 16,
                    "IPv6Gateway": "",
                    "Links": null,
                    "MacAddress": "02:42:ac:19:00:02",
                    "NetworkID": "d0462c4d1e824e18586b211707fc4abe8f3332cba166d89a8b178c2240cc1c96"
                }
            },
            "Ports": {},
            "SandboxID": "f2fc74fd596aaf580462117f7d0059cc70fdb3655c650baa254ec1ce062dd13c",
            "SandboxKey": "/var/run/docker/netns/f2fc74fd596a",
            "SecondaryIPAddresses": null,
            "SecondaryIPv6Addresses": null
        },
        "Path": "sh",
        "ProcessLabel": "",
        "ResolvConfPath": "/var/lib/docker/containers/e769ebd52eaae27a12259fbc81c6fb6d471900d5f6f69f63b53929a4ce602741/resolv.conf",
        "RestartCount": 0,
        "State": {
            "Dead": false,
            "Error": "",
            "ExitCode": 0,
            "FinishedAt": "0001-01-01T00:00:00Z",
            "OOMKilled": false,
            "Paused": false,
            "Pid": 4890,
            "Restarting": false,
            "Running": true,
            "StartedAt": "2017-08-25T02:12:47.409078427Z",
            "Status": "running"
        }
    }
]
</pre>

注意到```container2```有两个网络。当你启动它连接到```isolated_nw```时候加入了默认的```bridge```网络。

> 一个是```--network```一个是```connect```

![](http://i.imgur.com/FpHulWk.png)

eth0 Ethernet  HWaddr "02:42:ac:11:00:03"

eth1 Ethernet  HWaddr "02:42:ac:19:00:02"

7.使用```docker attach```进入容器查看:
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

eth1      Link encap:Ethernet  HWaddr 02:42:AC:19:00:02  
          inet addr:172.25.0.2  Bcast:0.0.0.0  Mask:255.255.0.0
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

8.Docker嵌入式DNS服务器可以为连接到给定网络的容器提供名称解析。这个意味着任何在同一个网络的可连接的容器可以通过容器名称ping通彼此。比如```container2```可以通过名称ping```container3```。

<pre>
/ # ping -w 4 container3
PING container3 (172.25.3.3): 56 data bytes
64 bytes from 172.25.3.3: seq=0 ttl=64 time=0.070 ms
64 bytes from 172.25.3.3: seq=1 ttl=64 time=0.080 ms
64 bytes from 172.25.3.3: seq=2 ttl=64 time=0.080 ms
64 bytes from 172.25.3.3: seq=3 ttl=64 time=0.097 ms

--- container3 ping statistics ---
4 packets transmitted, 4 packets received, 0% packet loss
round-trip min/avg/max = 0.070/0.081/0.097 ms
</pre>

9.目前,```container2```连入了```bridge```和```isolated_nw```网络。所以它可以和```container1```和```container3```通讯。然而```container3```和```container1```没有公共的网络,所以他们不能通讯。

>即使容器没有运行,也可以将容器连接到网络。然而docker网络检查只显示运行容器的信息。

### ```link```容器不使用自定义网络

容器如果使用默认的```bridge```网络是无法通过容器名称来连接的。如果想要连接,那么需要使用```link```功能。这个只能通过使用```--link```来解决。但是最好还是使用自定义网络。

使用```link```标志的功能是对使用默认的```bridge```网络进行通讯功能:

- 容器名称解析成IP地址。
- 定义一个网络的别名来连接容器,使用```--link=CONTAINER-NAME:ALIAS```。
- 保证容器连接,通过```--icc=false```
- 环境变量注入

使用自定义的网络就已经提供了所有的功能而不用任何额外的配置。除此之外,还可以在多个网络动态的加入和离开:

- 使用DNS自动解决名称。
- 支持```--link```提供别名来连接容器。
- 在网络里自动安全隔离容器环境
- 环境变量注入

例子:
1.创建一个新的```container4```连接```isolated_nw```并且连接一个不存在的容器```container5```:
<pre>
[root@docker-node1 ~]# docker run --network=isolated_nw -itd --name=container4 --link container5:c5 busybox
38c7a4b0a500f914aee1c39a3153bf796b6766c44a70b32df59bb6b585e1b63a
</pre>

```container5```并不存在。当```container5```创建,```container4```能够通过```c5```来连接```container5```的IP地址。

> 融合通过```link```来链接的容器都是静态的。它不支持容器重启。而自定义网路支持动态的链接并且可以重启和改变IP地址。