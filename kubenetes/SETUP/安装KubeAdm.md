# 安装Kubeadm

主要分为几个部分:

## 准备工作

- Ubuntu 16.04+,Centos7 或 HypriotOS v1.0.1+
- 1GB以上的内存
- 每台机器网络连接
- 每个节点使用MAC地址或者product_uuid
- 某些端口

## 检查需要的端口

### Master node

端口范围 | 意义
---|---
6443*|Kubernetes API服务器
2379-2380|etcd客户端API
10250|Kubelet API
10251|kube-scheduler
10252|kube-controller-manager
10255|只读Kubelet API(Heapster)

### Worker node

端口范围 | 意义
--- | ---
10250|Kubelet API
10255|只读Kubelet API
30000-32767|NodePort Service默认端口。这些端口需要暴露于外部负载平衡器，或应用程序本身的其他外部使用者。

端口号带*都是可覆盖的,需要确保提供的任何自定义端口都是打开的。

虽然 etcd 端口包含在主节点中，仍然可以配置主机的etcd集群通过额外的端口。

pod网络插件可能需要额外的端口。由于这与每个pod网络插件不同，所以请参阅有关哪些端口需要的插件的文档。

## 安装kubelet 和 kubeadm

CentOS:
<pre>
cat <<EOF > /etc/yum.repos.d/kubernetes.repo
[kubernetes]
name=Kubernetes
baseurl=https://packages.cloud.google.com/yum/repos/kubernetes-el7-x86_64
enabled=1
gpgcheck=1
repo_gpgcheck=1
gpgkey=https://packages.cloud.google.com/yum/doc/yum-key.gpg
        https://packages.cloud.google.com/yum/doc/rpm-package-key.gpg
EOF
setenforce 0
yum install -y kubelet kubeadm
systemctl enable kubelet && systemctl start kubelet
</pre>

> 如果被墙了,改下HOSTS或者代理吧。
