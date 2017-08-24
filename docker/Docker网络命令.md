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
