# 概念

概念这章节主要是学习Kubernetes系统的组成和抽象在集群中的代表，并且帮助你对Kubernetes的工作方式有了更深的了解。

## 概述

为了使用kubernetes工作,我们使用了Kubernetes API对象来描述集群的期望状态:运行了什么应用或者其它工作;使用了什么容器镜像;集群的数量;评估网络和硬盘资源使用情况等等。你可以使用kubernetes API来创建对象达到你期望的状态，常用是使用命令行接口——```kubectl```。当然可以直接使用kubernetes API来设置和定义集群的期望状态。

一旦我们设置了自己的一个期望状态,kubernetes 控制面板会让集群状态匹配这个期望状态。这时候，kubernetes会自动执行一些任务——比如启动或重启容器，自动扩展或减少集群应用的主备数量。Kubernetes控制面板是运行在你的集群中由一组进程的集合组成:

- ```Kubernetes Master```是由三个进程的集合组成并运行在集群中的一个节点。它被叫做主节点(master node)。这三个进程分别是```kube-apiserver```,```kube-controller-manager```和```kube-scheduler```。
- 其他运行在集群中的非主节点由两个进程组成：
	- ```kubelet``` 与Kubernetes Master(主节点)交流。
	- ```kube-proxy ``` 一个反映运行在每个节点中的kubernetes网络服务的网络代理

## Kubernetes 对象

Kubernetes包含一定数量的代表系统状态的抽象：发布的容器应用和工作，他们的关联网络和硬盘资源，其他的集群运行信息，这些被抽象成对象在Kubernetes API中；(后面会有理解这些对象的文章)

基础的kubernetes 对象包括：
- ```Pod```
- ```Service```
- ```Volume```
- ```namespace```

此外，kubernetes包含一些被称为```Controllers```的高等级的抽象，```Controllers```建立在上面的基础对象之上并且提供了额外的广泛性(functionality)和便利性(convenience)功能，包括：
- ```ReplicaSet```
- ```Deployment```
- ```StatefulSet```
- ```DaemonSet```
- ```Job```

## Kubernetes控制面板

组成kubernetes控制面板的各个部分如Kubernetes 主节点和Kubelet 进程，控制着kubernetes如果和整个集群交流。控制面板主要保持系统中所有Kubernetes对象的记录，运行持续控制循环来管理这些对象的状态(很关键！**循环**)。在任何时刻，控制面板循环控制响应集群中的变化并且使系统中集群保持正常的状态(你设定的期望)。

如，当使用KubernetesAPI创建一个发布对象(Deployment Object)，然后在系统中提供了一个新的期望状态(desired state)。Kubernetes控制面板记录对象的创建并且通过启动所需的应用程序并调度它们到集群节点，从而实现您的指令，从而使集群的实际状态与所期望的状态相匹配。

### Kubernetes Master

Kubernetes Master 主要负责维持集群的期望状态(desired state)。当我们要影响到Kubernetes，如使用```kubectl```命令行接口，我们可以通过集群中的Kubernetes master。

"master"是指管理集群状态的进程集合。通常这些进程都在集群中的单个节点上运行，这个节点通常被称为主节点。还可以为可用性和冗余进行复制。

### Kubernetes Nodes

集群中的节点是运行你的应用或者云工作流的机器。Kubernetes master会控制这些节点；很少会直接与节点交互。

### 对象元数据 Object Metadata

- ```Annotations```