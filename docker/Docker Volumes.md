# Docker Volumes

## 使用 Volumes

### Volumes 的优势

``` volumes(卷) ``` 是Docker容器存储数据的优先选择。```bind mount``` 完全依赖于主机的目录结构,而 ```volumes```则是由Docker容器完全管理。它相比于```bind mount```的方式有几个优点:

- 卷比绑定挂载更容易备份或迁移。


- 可以使用Docker CLI命令或Docker API来管理卷。

- 在Windows和linux上```volumes```并没有本质区别。

- 多容器间共享,```volumes```更加安全。

- ```volumes drivers```允许存储在云上或者远程主机上,添加加密或者扩展其他功能。

- 一个新卷的内容可以由一个容器预填充。

此外,```volumes```通常比持久化数据更好,因为卷的使用在容器中不会多余增加容器的大小。而且```volumes```的存在不受容器的生命周期影响。

![](http://i.imgur.com/5f1YjIP.png)

>> 如果容器想要生成的不是持久化数据,那么使用 ```tmpfs mount```更加合理(存储在内存里的)。并通过避免写入容器(数据)的方式来提高容器性能。

<b>```volumes```使用```rprivate```来绑定传播,这种方式不会配置```volumes```</b>

## 使用 -v 或 -mount

最初的版本,```-v```或者```--volumes```只是用于独立的container而```--mount```用于```swarm```,在17.06版本,```--mount```也可以用于 独立的 container。通常,```--mount```更加直率。最大的区别在于```-v```语法将所有选项组合在一个字段中而```--mount```语法将它们分开。下面是对每个标志的语法的比较。

>新用户应该使用--mount语法。有经验的用户可能更熟悉-v或-volume语法,但鼓励使用--mount因为研究表明它更容易使用。

反正他喵的用```--mount```就好了!

如果需要指定的```volume driver ```选项,那么必须使用```--mount```.

- ```-v```或```-volume```:由三个字段组成,用```:```进行分割,三个字段的先后顺序不能变动。
	- 第一个字段是卷的名称,在主机上是惟一的。省略第一个字段则为匿名卷。
	- 第二个字段是被挂载到容器的路径(目录或者文件)。
	- 第三个字段是可选的,是一个逗号分隔的选项列表,如ro。后面将讨论这些选项。

- ```--mount```由多个 ```<key>=<value>```组成,```--mount```比```-v```更加冗余,但是更直观更容易理解。
	- ```type```字段是mount的方式,可以是```volume```、```bind```、```tmpfs```。
	- ```source``` volumes的名称。这个字段可以省略。可以写成```source```或者```src```
	- ```destination``` 指定目录或者文件将会被挂载到容器里。可以写成```dst```或者```target```
	- ```readonly``` 指定挂载为只读。
	- ```volume-opt``` 指定挂载的一些配置属性(要看具体的挂载内容而定)。

### ```-v```和```--mount```的不同之处

与```bind mounts```不同,所有```volumes```都可以使用 ```--mount```和 ```-v```。

## 创建和管理```volumes```

与绑定挂载```bind mounts```不同,可以在任何容器的范围之外创建和管理```volumes```。

### 创建一个 volume

<pre>
[root@docker-node1 ~]# docker volume create my-vol
my-vol
</pre>

### 列出 volume

<pre>
[root@docker-node1 ~]# docker volume ls
DRIVER              VOLUME NAME
local               5a1f0d6b8ae04f6502ae8846b5447228e0bc8bff16ae913a89172ca7d1fe3863
local               my-vol
</pre>

### 查看 volume
<pre>
[root@docker-node1 ~]# docker volume inspect my-vol
[
    {
        "Driver": "local",
        "Labels": {},
        "Mountpoint": "/var/lib/docker/volumes/my-vol/_data",
        "Name": "my-vol",
        "Options": {},
        "Scope": "local"
    }
]
</pre>

### 删除 volume
<pre>
[root@docker-node1 ~]# docker volume rm my-vol
my-vol
</pre>

### 给容器添加 volume
<pre>
[root@docker-node1 ~]# docker run -d -it --name devtest --mount source=myvol,target=/app centos
</pre>

在 ```docker inspect devtest```中可以查看到以下信息:
<pre>
        "Mounts": [
            {
                "Type": "volume",
                "Name": "myvol",
                "Source": "/var/lib/docker/volumes/myvol/_data",
                "Destination": "/app",
                "Driver": "local",
                "Mode": "",
                "RW": true,
                "Propagation": ""
            }
        ],
</pre>

### 在服务中的差异

在使用 ```docker service create```命令时不支持```-v```或```--volume```选项。如果想要挂载一个 ```volume```在serivce容器中,那么必须使用 ```--mount```。

### 使用容器填充```volume```

意思是```run```一个容器的时候创建一个新的```volume```时,如果有指定的文件或者目录被复制在```volume```中,那么当其它容器使用这个```volume```时,能够访问这些资源(预填充的资源)。

<pre>
$ docker run -d \
  -it \
  --name=nginxtest \
  --mount source=nginx-vol,destination=/usr/share/nginx/html \
  nginx:latest
</pre>

### 使用只读 volume

有时候,我们只需要在创建挂载容器的时候对volume进行读写操作,而其它时候。我们只需要读取```volume```即可,那么使用 ```readonly```选项:

<pre>
$ docker run -d \
  -it \
  --name=nginxtest \
  --mount source=nginx-vol,destination=/usr/share/nginx/html \
  nginx:latest
</pre>

在```docker inspect nginxtest```中能够看到
<pre>
        "Mounts": [
            {
                "Type": "volume",
                "Name": "nginx-vol",
                "Source": "/var/lib/docker/volumes/nginx-vol/_data",
                "Destination": "/usr/share/nginx/html",
                "Driver": "local",
                "Mode": "",
                "RW": false, //只读
                "Propagation": ""
            }
        ],
</pre>

## 使用 volume driver

当使用 ```docker volume create``` 或者 运行一个容器 创建```volume```的时候,可以为 ```volume```指定不同的```volume driver```。

如使用```viuex/sshfs```

### 安装初始化

<pre>
[root@docker-node1 ~]# docker plugin install --grant-all-permissions vieux/sshfs
latest: Pulling from vieux/sshfs
487099c7c8b4: Download complete 
Digest: sha256:c76ced50a5973d601ace498091eac80da6f66e78d9393866a00ab1b710a618ca
Status: Downloaded newer image for vieux/sshfs:latest
Installed plugin vieux/sshfs
</pre>

### 使用```volume driver```创建一个 ```volume```

每个```volume driver ```可以有0或者多个配置项,使用```-o```指定配置属性。

如配置一个 SSH 密码:
<pre>
$ docker volume create --driver vieux/sshfs \
  -o sshcmd=test@node2:/home/test \
  -o password=testpassword \
  sshvolume
</pre>

### 启动一个容器时创建```volume```并指定```volume driver```
<pre>
$ docker run -d \
  --it \
  --name sshfs-container \
  --volume-driver vieux/sshfs \
  --mount src=sshvolume,target=/app,volume-opt=sshcmd=test@node2:/home/test,volume-opt=password=testpassword \
  nginx:latest
</pre>