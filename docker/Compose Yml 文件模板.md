# Compose YAML 模板文件

## YAML的配置项

### image

指定为镜像名称或镜像 ID,如果本地不存在 ```Compose```会尝试去拉

如:
<pre>
image: ubuntu
image: orchardup/postgresql
image: a4bc65fd
</pre>

### build

指定```Dockerfile```所文件夹的路径。```Compose```将会利用它自动构建这个镜像,然后使用这个镜像.

``` build: /path/to/build/dir ```

### command

覆盖容器启动后默认执行的命令

```command: bundle exec thin -p 3000 ```

### links 

链接到其它服务中的容器。使用服务名称(同时作为别名)或服务名称: 服务别名 (SERVICE:ALIAS) 格式都可以

<pre>
links:
 - db
 - db:database
 - redis
</pre>

使用别名将会自动在服务容器中的 ```/etc/hosts```里创建。如:
<pre>
172.17.2.186 db
172.17.2.186 database
172.17.2.187 redis
</pre>

相应的环境变量也会被创建

### external_links

链接到 docker-compose.yml 外部的容器,甚至并非 ```Compose```管理的容器。参数格式跟```links```类似。

### ports

暴露端口信息。

使用宿主: 容器 ```(HOST:CONTAINER)``` 格式或者仅仅指定容器的端口(宿主将会随机选择端口)都可以。

<pre>
ports:
 - "3000"
 - "8000:8000"
 - "49100:22"
 - "127.0.0.1:8001:8001"
</pre>

### expose

暴露端口,但不映射到宿主机,只被连接的服务访问。

仅可以指定内部端口为参数

<pre>
expose:
 - "3000"
 - "8000"
</pre>

### volumes

卷挂载路径设置。可以设置宿主机路径(HOST:CONTAINER)或加上访问模式(HOST:CONTAINER:ro)。

<pre>
volumes:
 - /var/lib/mysql
 - cache/:/tmp/cache
 - ~/configs:/etc/configs/:ro
</pre>

### volumes_from

从另一个服务或容器挂载它的所有卷

<pre>
volumes_from:
 - service_name
 - container_name
</pre>

### environment

设置环境变量。你可以使用数组或字典两种格式

只给定名称的变量会自动获取它在Compose主机上的值,可以用来防止泄露不必要的数据。

<pre>
environment:
 RACK_ENV: devolopment
 SESSION_SECRET:

environment:
 - RACK_ENV=development
 - SESSION_SECRET
</pre>

### env_file

从文件中获取环境变量,可以为单独的文件路径或列表

如果通过```docker-compose -f FILE```指定了模板文件,则 ```env_file``` 中路径会基于模板文件路劲。

如果有变量名称与 ```environment```指令冲突,则以后者为准。

<pre>
env_file: .env

env_file:
 - ./common.env
 - ./apps/web.env
 - /opt/secrets.env
</pre>

环境变量文件中每一行必须符合格式,支持 ```#```开头的注释行。

<pre>
# common.env: Set Rails/Rack environment
RACK_ENV=development
</pre>

### extends

基于已有的服务进行扩展。例如我们已经有一个webapp的服务,模板文件为 ```common.yml```。

<pre>
# common.yml
webapp:
 build: ./webapp
 environment:
  - DEBUG=false
  - SEND_EMAILS=false
</pre>

编写一个新的 ```development.yml ```文件,使用 ```common.yml```中的 webapp 服务进行扩展.

<pre>
#development.yml
web:
 extends:
  file: common.yml
  service: webapp
 ports:
  - "8000:8000"
 links:
  - db
 environment:
  - DEBUG=true
db:
 image: postgres
</pre>

后者会自动继承 common.yml 中的 webapp 服务及相关环境变量

### net

设置网络模式。使用和 ```docker client ```的 ```--net```参数一样的值。

<pre>
net: "bridge"
net: "none"
net: "container:[name or id]"
net: "host"
</pre>

### pid

跟主机系统共享进程命名空间。打开该选项的容器可以相互通过进程ID来访问和操作。

``` pid: "host"```

### dns

配置DNS 服务器。可以是一个值,也可以是一个列表。

<pre>
dns: 8.8.8.8
dns:
 - 8.8.8.8
 - 9.9.9.9
</pre>

### cap_add, cap_drop

添加或放弃容器的 Linux 能力(Capabiliity)。

<pre>
cap_add:
 -ALL

cap_drop:
 - NET_ADMIN
 - SYS_ADMIN
</pre>

### dns_seach

配置DNS搜索域。可以是一个值,也可以是一个列表。

<pre>
dns_search: example.com
dns_search:
 - domain1.example.com
 - domain2.example.com
</pre>

<pre> working_dir, entrypoint, user, hostname, domainname, mem_limit, privileged, restart, stdin_open, tty, cpu_shares </pre>

这些都是和 docker run 支持的选项类似。

<pre>
cpu_shares: 73

working_dir: /code
entrypoint: /code/entrypoint.sh
user: postgresql

hostname: foo
domainname: foo.com

mem_limit: 100000000
privileged: true

restart: always

stdin_open: true
tty: true
</pre>
