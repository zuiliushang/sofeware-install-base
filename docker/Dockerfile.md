# Dockerfile 结构

## 创建镜像

``` docker build -t "我的镜像名称" [Dockerfile所在的路径]```

## 基本命令

### FORM

第一条指令必须为 ``` FROM <IMAGE> ```或者 ``` FORM <IMAGE>:<TAG>```

一个Dockerfile创建多个镜像,可以使用多个 FROM 来创建

### MAINTAINER

如 ``` MAINTAINER xusihan@qq.com ``` 指定维护者的信息


### RUN

格式为 ``` RUN <command> ```或者 ``` RUN ["executable","param1","param2"] ```

前者可以在 Shell 终端中运行命令 如 RUN echo "hello world"

后者用 exec 执行。如 ``` RUN ["/bin/bash","-c","echo hello"] ```

### CMD 

指定启动容器的时候执行的命令。

- ```CMD ["executable","param1","param2"]``` 使用 exec 执行 推荐方式.
- ```CMD command param1 param2 ``` 在/bin/sh 中执行,提供给需要交互的应用
- ```CMD [param1,param2] ``` 提供给 ENTRYPOINT 的默认参数

指定启动容器时执行的命令,每个 Dockerfile 只能有一条 CMD 命令。如果指定了多条命令，只有最后一条执行。

### EXPOSE

``` EXPOSE <port> [<port>...] ```

Dokcer服务端容器暴露的端口号。 相当于启动容器时的 -P

### ENV

``` ENV <key> <value> ``` 指定环境变量

### ADD

``` ADD <src> <dest> ``` 

复制指定的```<src>``` 到容器中的 ```<dest>```  其中 ```<src>``` 可以是Dockerfile所在目录的一个相对路径;也可以是一个 URL ; 还可以是一个 tar文件(自动解压)

### COPY

``` COPY <src> <dest> ``` 复制本地主机的 ```<src>``` 到容器中的 ```<dest>```

### ENTRYPOINT

- ``` ENTRYPOINT ["executable","param1","param2"] ```
- ``` ENTRYPOINT command param1 param2 (shell 中执行) ```

配置容器启动后执行的命令,并且不可被 ``` docker run ``` 提供的参数覆盖

每个Dockerfile 中只能有一个 ```ENTRYPOINT```,当指定多个时候,只有最后一个有效。

### VOLUME

```VOLUME ["/data"]``` 创建一个可以从本地主机或其他容器挂载的挂载点,一般用来存放数据库和需要保持的数据等。

### USER
```USER deamon``` 指定运行容器时的用户名或UID,后续的 ```RUN``` 也会使用指定用户。

当服务不需要管理员权限时,可以通过该命令指定运行用户。并且可以在之前创建所需要的用户:
``` RUN groupadd -r postgree $$ useradd -r -g postgres postgres ```
 
要临时获取管理员权限使用 ```gosu``` 

### WORKDIR

```WORKDIR /path/to/workdir``` 

为后续的 ```RUN``` 、```CMD```、```ENTRYPOINT```指令配置工作目录。

可以使用多个 ```WORKDIR``` 指令,后续命令如果参数是相对路径,则会基于之前命令指定的路径。如

<pre>
WORKDIR /a
WORKDIR b
WORKDIR c
RUN pwd
</pre>

最终的路径为 ```/a/b/c```

### ONBUILD

```ONBUILD [INSTRUCTION] ```

配置当所创建的镜像作为其他新创建镜像的基础镜像时,所执行的操作指令。
