#Docker storage drivers Docker存储驱动

理想的情况下,很少数据会写入到```container```的可写层。但是,总有业务会需要把数据写入容器的可写层。而这个时候.```drivers```就出现了。

Docker使用一系列不同的存储驱动器来管理镜像和运行容器中的文件系统。这些存储驱动程序与```docker volumes```不同,后者管理##  ##可以在多个容器间共享的存储。