# 使用 ```bind mount```

```bind mount``` 在```docker```早期就已经存在了。它的作用是将你主机的目录或者文件挂载进一个容器里面。该路径和内容受到主机的控制影响。与此相反,如果使用```volume```,那么是由Docker来控制管理 ```volume```。

在使用```bind mount```不需要这个文件或者目录在Docker存在。```Docker```会按需创建。```bind mount```性能非常高。但是它依赖于主机需要有一个目录或者文件可用。如果正在开发一个新的```docker```软件,那么最好还是使用 ```named volumes```。因为你不能使用```Docker CLI```命令来管理```bind mount```。

## 使用 -v 或 -mount

- ```-v```或```-volume```:由三个字段组成,用```:```进行分割,三个字段的先后顺序不能变动。
	- 第一个字段是卷的名称,在主机上是惟一的。省略第一个字段则为匿名卷。
	- 第二个字段是被挂载到容器的路径(目录或者文件)。
	- 第三个字段是可选的,是一个逗号分隔的选项列表,如```ro```、```consistend```、```delegated```、```cached```、```z```和```Z```。后面将讨论这些选项。

- ```--mount```由多个 ```<key>=<value>```组成,```--mount```比```-v```更加冗余,但是更直观更容易理解。
	- ```type```字段是mount的方式,可以是```volume```、```bind```、```tmpfs```。
	- ```source``` volumes的名称。这个字段可以省略。可以写成```source```或者```src```
	- ```destination``` 指定目录或者文件将会被挂载到容器里。可以写成```dst```或者```target```
	- ```readonly``` 指定挂载为只读。
	- ```bind-propagation``` 如果支持。可能是```rprivate```、```private```、```rshared```、```shared```、```rslave```、```slave```。
	- ```consistency```如果支持。可能是```consistent```、```delegated```、```cached```。这个设置只适用于Mac的Docker，在所有其他平台上都被忽略。
	- ```--mount```不支持```z```和```Z```选项。

### ```-v```和```--mount```的不同之处

如果使用```-v```或```-volume```来绑定在Docker主机上尚不存在的文件或目录,那么它总是被创建为一个目录。

如果使用```--mount```到Docker主机上尚不存在的文件或目录,Docker报错。

## 	启动容器使用```bind mount```

```-v```创建,没有这个目录会创建
<pre>
docker run -d \
  -it \
  --name devtest \
  -v /tmp/target:/app \
  nginx:latest
</pre>

```--mount```创建。
<pre>
docker run -d \
  -it \
  --name devtest \
  --mount type=bind,source=/tmp/target,target=/app \
  nginx:latest

docker: Error response from daemon: invalid mount config for type "bind": bind source path does not exist.
See 'docker run --help'.
</pre>
没有目录报错。

### ```bind mount```在一个非空目录的容器里

如果绑定在一个容器的非空目录下,```bind mount```会把这个目录下的内容隐藏掉。于```docker volumes```不同。

<pre>
$ docker run -d \
  -it \
  --name broken-container \
  --mount type=bind,source=/tmp,target=/usr \
  nginx:latest

docker: Error response from daemon: oci runtime error: container_linux.go:262:
starting container process caused "exec: \"nginx\": executable file not found in $PATH".
</pre>


### 配置绑定传播```bind propagation```

```bind propagation```默认值是 ```rprivate```(```bind mount ```和 ```volume```)。只有在linux上而且只有```bind mount```才能配置,```bind propagation```是一项高级配置,一般不需要配置。

```bind propagation```是指是否将```bind mount ```或者```named volume```传播给
复制挂载。```replicas of that mount```

传播行为 | 行为作用
---|---
shared | 源挂载项的子挂载项将暴露在复制挂载中,同样复制挂载的子挂载项也会暴露给源挂载项。
slave | 单向挂载共享,如果```origin volume```暴露出```sub-mount```那么slave的sub-mount 将不会被origin的察觉到,相反也是。
private | mount 私有,相互的```sub-mount```不被对方共享。
rshared | 与共享相同,但传播也扩展到在任何原始或副本挂载点内嵌套的挂载点上。子项的子项都会被影响。
rslave | 同slave,但传播也扩展到在任何原始或副本挂载点内嵌套的挂载点上。子项的子项都会被影响。
rprivate | 默认,原始或副本挂载点内的任何挂载点都不会在任意方向上传播。

>>在设置```bind propagation```之前,需要主机的文件系统支持```bind propagation```。

