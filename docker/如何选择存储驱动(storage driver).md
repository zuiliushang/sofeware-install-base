# 如何选择存储驱动(storage driver)

在理想的情况下。很少的数据会写进容器的```writable layer```,并且使用```Docker volumes```来写数据。然而,一些工作需要将数据写入到容器的可写层里。这就是```storage driver```出现的原因。

Docker支持数个不同的存储驱动,使用可插入式的架构。存储驱动控制了镜像和容器被本地Docker存储和管理的方式。

在工作中选择最佳的存储驱动,需要考虑三个高级因素:

- 如果内核支持多个```storage driver```,那么Docker有一个优先级列表。如果没有```storage driver```被明确的配置,假设```storage driver ```满足几个先决条件:
	- 如果```aufs```支持,那么默认配置。因为它是最老的```storage driver```。但是,并不是所有都支持。
	- 可能的话,使用配置最少的```storage driver```,如```btrfs```或```zfs```。这些都依赖于系统文件的正确配置。
	- 否则。尝试使用性能和稳定性上整体上表现比较好的```storage driver```。
		- ```overlay2```是首选,其次是```overlay```。这些不需要额外的配置。
		- ```devicemapper```是下一个选择。但是它需要在生产环境中配置```direct-lvm```。因为零配置的```loopback-lvm```性能很差。

选择的顺序可以在Docker源码定义。可以在[这里](https://github.com/moby/moby/blob/v17.03.1-ce/daemon/graphdriver/driver_linux.go#L54-L63)查看修改的地方。

- 选择的```storage driver```需要Docker版本、操作系统、分布式的支持。如:```aufs```只支持Ubuntu 和 Debian,```btrfs```支持SLES```并且要求Docker EE。
- 一些```storage driver```需要使用特点的系统文件格式支持。如果需要使用特定的文件系统支持,这个可能会限制到选择。
- 缩小了选择范围之后,工作负载特性和所需的稳定性决定选择哪种```storage driver```。

详情可以看[这里](https://docs.docker.com/engine/userguide/storagedriver/selectadriver/#supported-storage-drivers-per-linux-distribution)

## 其他的考虑因素。

### 适配工作量

在其他方面,每个```storage driver```都有自己的性能特点使它或多或少适合于不同的工作负载。考虑以下归纳:

- ```aufs```,```overlay```和```overlay2```操作是文件级别而不是块级别。这更有效地使用内存,但是容器的可写层可能在写负重的工作负载中增长相当大。
- 块级别的容器```storage driver```例如```devicemapper```,```btrfs```和```zfs```在写负重的工作中表现更好。
- 大量的碎片写入或者容器有很多```layer```或者有一个很深的文件系统,```overlay```性能比```overlay2```更好。
- ```btrf```和```zfs```需要大量内存。
- ```zfs```在高密度的工作如PaaS中是一个不错的选择。

### 共享存储系统和存储驱动

如果企业使用SAN,NAS,hardward RAID或者其它的共享存储系统,他们可能提供高可用性,提高性能,准备更小,重复压缩。在许多情况下,Docker可以在这些存储系统上工作。但是Docker并没有与它们紧密集成。

每个Docker storage driver 基于Linux文件系统或者volume管理。如:如果在共享存储系统之上使用ZFS存储驱动程序,请务必遵循在特定共享存储系统之上操作ZFS文件系统的最佳做法。

### 稳定性

对于一些用户来说,稳定性比性能更重要。```aufs```,```overlay```和```devicemapper```是稳定性更好的选择。

### 经验和技巧

选择你熟悉的方面的```storage driver```。例如:如果你使用 RHEL 或者关注其中一个分支。你可能已经有LVM和Device Mapper方面的经验。如果有```devicemapper```可能是最好的选择。

### Test with your own workloads

通过选择不同的存储驱动来测试运行的时候的写入工作量来测试Docker的性能。要确保测试环境相同。

## 检查和设置当前的```storage driver```

> <b>重点</b>:一些```storage driver ```类型,例如```devicemapper```,```btrfs```和```zfs```需要在使用它们之前在操作系统上做一些额外设置。

使用```docker info```查看```storage driver```那行:
<pre>
[root@docker-node1 ~]# docker info
Containers: 5
 Running: 5
 Paused: 0
 Stopped: 0
Images: 14
Server Version: 17.06.0-ce
Storage Driver: devicemapper
</pre>
要想设置```storage driver```,在```daemon.json```文件中设置(```/etc/docker/```linux下```C:\ProgramData\docker\config\```Windows)。在Mac或者Windows中不支持改变```storage driver```。

如果```daemon.json```不存在,创建它并且至少添加一下内容:
<pre>
{
  "storage-driver": "devicemapper"
}
</pre>

可以指定任何的storage driver 来替换```devicemapper```。

重启Docker来让配置生效。