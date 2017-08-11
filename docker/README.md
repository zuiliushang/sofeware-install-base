# Docker Install

## 删除遗留的旧版本

```
yum remove docker docker-common docker-selinux docker-engine
```

## 安装repository

```
 yum install -y yum-utils device-mapper-persistent-data lvm2
```

## 设置stable repository

```
yum-config-manager --add-repo https://download.docker.com/linux/centos/docker-ce.repo
```

## 列出docker
```
yum list docker-ce.x86_64  --showduplicates | sort -r
```

## 安装

```
yum -y install docker-ce
```
