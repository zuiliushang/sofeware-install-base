# 使用 tmpfs mounts

```volumes```和```bind mounts```默认挂载在```container```的文件系统中。但是它们的内容是存储在主机上。

如果不想把```container```的数据存储在主机中,或者因为性能、安全、临时的数据并不想把数据写入```container```可写层里。可以使用```tmpfs```进行挂载。这样数据只会存储在主机的内存中!容器停止的时候,```tmpfs mount```就会被删除。特别是如果```container commit```,数据也不会被保存(```tmpfs mount```不会保存在容器)!

## 使用 ```--tmpfs```或者```--mount```标签

最初```--tmpfs```用于独立的容器而```--mount```用于```swarm```服务。但是从```Docker 17.06```开始也可以使用独立容器安装。一般来说```--mount```更加明确和详细。最大的区别在于```--tmpfs```标志不支持任何可配置选项。

>他喵的用--mount就行了 管他喵的

- ```--tmpfs```: 挂载一个 ```tmpfs```不允许指定配置项.而且只能用于独立的容器。
- ```--mount```:由多个 ```<key>=<value>```对组成。
	- ```type```指定挂载类型。```bind```,```volume```,```tmpfs```这里应该是```tmpfs```。
	- ```destination```,指定挂载路径。也可以写成```dst```,```target```。

## ```tmpfs```容器的局限性。

- ```tmpfs```挂载在容器间不能共享。
- ```tmpfs```只能在```Linux```系统上起作用。

## 使用 ```tmpfs```

<pre>
[root@docker-node1 ~]# docker run -d \
> -it \
> --name tmptest \
> --mount type=tmpfs,target=/app \
> nginx:latest
14d562bb38a04c192036cee310bb1e345e16473eeb036a58b3a3fc808db85f6d
</pre>

在```docker inspect tmptest``` 中可以看到
<pre>
        "Mounts": [
            {
                "Type": "tmpfs",
                "Source": "",
                "Destination": "/app",
                "Mode": "",
                "RW": true,
                "Propagation": ""
            }
        ],
</pre>

### 指定 ```tmpfs```选项

配置项 | 描述
---|---
```tmpfs-size``` | 指定tmpfs的大小,默认是无限大。
```tmpfs-mode``` | octal中tmpfs的文件模式。例如:700或0770。默认值为1777或world - writable。