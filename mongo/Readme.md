# MongoDB 学习

自己记录用

## 使用 MongoDB

### 1.docker pull 镜像
<pre>
docker pull mongo:3.6.1-jessie
</pre>

### 2使用

<pre>
[root@localhost ~]# docker run --name mongo-test -d mongo:3.6.1-jessie --auth                
ec355e1c9ccf694c9dcdc2d7f04662d5aa96fabd8ab5a311291eb810cb2fe379
[root@localhost ~]# docker exec -it mongo-test mongo admin          
MongoDB shell version v3.6.1
connecting to: mongodb://127.0.0.1:27017/admin
MongoDB server version: 3.6.1
Welcome to the MongoDB shell.
For interactive help, type "help".
For more comprehensive documentation, see
        http://docs.mongodb.org/
Questions? Try the support group
        http://groups.google.com/group/mongodb-user
2018-01-11T08:13:29.681+0000 E -        [main] Error loading history file: FileOpenFailed: Unable to fopen() file /root/.dbshell: No such file or directory
> 
</pre>


`https://www.jianshu.com/p/86fc8a53aad5`
