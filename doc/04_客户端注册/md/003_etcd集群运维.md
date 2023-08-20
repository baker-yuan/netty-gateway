### ETCD 集群运维，故障处理转移

------

Etcd故障处理，ETCD文档：https://doczhcn.gitbook.io/etcd/

- ###### 自动磁盘清理：配置文件添加2个参数即可

  ```json
  ## 保持一个小时的历史
  ETCD_AUTO_COMPACTION_RETENTION="1"
  ## 使用版本号进行压缩
  ETCD_AUTO_COMPACTION_MODE="revision"
  ```

- ###### 手工磁盘清理：

  ```json
  /usr/local/etcd/etcdctl --write-out=json --endpoints=192.168.11.114:2379,192.168.11.115:2379,192.168.11.116:2379 endpoint status
  
  ## 查询版本号：2312367
  [{"Endpoint":"192.168.11.114:2379","Status":{"header":{"cluster_id":3452304176243217645,"member_id":13884491356182900640,"revision":2312367,"raft_term":273},"version":"3.3.18","dbSize":7737344,"leader":13181482930811280038,"raftIndex":2312828,"raftTerm":273}},{"Endpoint":"192.168.11.115:2379","Status":{"header":{"cluster_id":3452304176243217645,"member_id":7973352974592605457,"revision":2312367,"raft_term":273},"version":"3.3.18","dbSize":7737344,"leader":13181482930811280038,"raftIndex":2312828,"raftTerm":273}},{"Endpoint":"192.168.11.116:2379","Status":{"header":{"cluster_id":3452304176243217645,"member_id":13181482930811280038,"revision":2312367,"raft_term":273},"version":"3.3.18","dbSize":7737344,"leader":13181482930811280038,"raftIndex":2312828,"raftTerm":273}}]
  
  ## 执行压缩
  /usr/local/etcd/etcdctl --endpoints=192.168.11.114:2379,192.168.11.115:2379,192.168.11.116:2379 compact 2312367
  
  ## 反碎片化
  /usr/local/etcd/etcdctl --endpoints=192.168.11.114:2379,192.168.11.115:2379,192.168.11.116:2379 defrag
  
  ## 解除告警，这一步操作必须要做，不然集群仍然不可用
  /usr/local/etcd/etcdctl --endpoints=10.8.128.155:2379,10.8.129.4:2379,10.8.129.145:2379 alarm disarm
  ```

- 

