# 使用kubeadm 创建集群

## 准备工作

1. Ubuntu 16.04+ CentOS7 HypriotOS v1.0.1+
2. 1GB或者更高的内存
3. 完整的网络连接(共有私有都OK)

## 目的

- 安装一个安全的Kubernetes集群
- 在集群中安装一个pod网络让所有应用组件(pod)可以相互交流
- 在集群中安装一个微服务应用(一个袜子店)

## 指令

### (1/4)安装kubeadm

**注意:**如果已经安装了了kubeadm,使用```yum update```来获取最新版本的kubeadm。

kubelet现在每隔几秒钟就重新启动一次，因为它在一个crashloop中等待kubeadm告诉它该做什么。

### (2/4)初始化master

master是运行着控制面板的机器,包括etcd(集群数据库)和API server (kubectl Cli交互点)

为了初始化master,确定有一台机器已经安装了kubeadm,运行:
<pre>
kubeadm init
</pre>

**注意:**-你需要选择一个Pod网络插件在下一步。根据第三方提供的不同,需要设置```--pod-network-cidr```设置特定的提供者。下面的选项将包含有关```kubeadm init```中需要什么标志的通知。这将自动检测网络接口来通知master作为与默认网关的接口通告主机。如果使用一个不同的接口。运行```kubeadm init```时候使用```--apiserver-advertise-address=<ip-address>```属性。

```kubeadm init ```会先运行一系列的例行检查来确定机器已经准备好运行kubernetes。如果发现错误会警告并且退出。如果OK下一步会下载安装集群数据库和控制面板组件。这个需要几分钟。

没有关闭集群时不能运行两次```kubeadm init```(除非你用v1.6到v1.7)

