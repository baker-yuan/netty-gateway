### Etcd 参数配置

------

Etcd高可用分布式存储原理：https://draveness.me/etcd-introduction/

ETCD文档：https://doczhcn.gitbook.io/etcd/

- 配置参数：

  ```json
  ## name
  
      人类可读的该成员的名字
      默认值："default"
      环境变量：ETCD_NAME
      该值被该节点吃的--initial-cluster参数引用(例如 default=http://localhost:2380).如果使用静态引导程序，则需要与标志中使用的键匹配。当使用发现服务时，每一个成员需要有唯一的名字。Hostname或者machine-id是好的选择。
  
  ## data-dir
  
      数据目录的路径
      默认值："${name}.etcd"
      环境变量：ETCD_DATA_DIR
  
  ## wal-dir
  
      专用的wal目录的路径。如果这个参数被设置，etcd将会写WAL文件到walDir而不是dataDir，允许使用专用磁盘，并有助于避免日志记录和其他IO操作之间的io竞争。
      默认值：""
      环境变量：ETCD_WAL_DIR
                                    
  ## max-wals
  
      保留的wal文件最大数量（0为无限）
      默认值：5
      环境变量：ETCD_MAX_WALS
      Windows用户的默认设置是无限制的，建议手动设置到5（或出于安全性的考虑）。
  
  ## snapshot-count
  
      触发一个快照到磁盘的已提交交易的数量
      默认值："10000"
      环境变量：ETCD_SNAPSHOP_COUNT
                                    
  ## max-snapshots
  
      保留的快照文件最大数量（0为无限）
      默认值：5
      环境变量：ETCD_MAX_SNAPSHOTS
      Windows用户的默认设置是无限制的，建议手动设置到5（或出于安全性的考虑）。
                                    
  ## heartbeat-interval
  
      心跳间隔(毫秒为单位)
      默认值:"100"
      环境变量：ETCD_HEARTBEAT_INTERVAL
  
  ## election-timeout
  
      选举超时时间(毫秒为单位)，从文档/tuning.md发现更多细节
      默认值："1000"
      环境变量：ETCD_ELECTION_TIMEOUT
  
  ## listen-peer-urls
  
      监听在对等节点流量上的URL列表，该参数告诉etcd在指定的协议://IP:port组合上接受来自其对等方的传入请求。协议可以是http或者https。或者，使用unix://<file-path>或者unixs://<file-path>到unix sockets。如果将0.0.0.0作为IP，etcd将监听在所有的接口上的给定端口。如果给定了Ip和端口，etcd将监听指定的接口和端口。可以使用多个URL指定要监听的地址和端口的数量。 etcd将响应来自任何列出的地址和端口的请求。
      默认值："http://localhost:2380"
      环境变量:ETCD_LISTEN_PEER_URLS
      示例："http://10.0.0.1:2380"
      无效的示例："http://example.com:2380"(绑定的域名是无效的)
  
  ## listen-client-urls
  
      监听在客户端流量上的URL列表，该参数告诉etcd在指定的协议://IP:port组合上接受来自客户端的传入请求。协议可以是http或者https。或者，使用unix://<file-path>或者unixs://<file-path>到unix sockets。如果将0.0.0.0作为IP，etcd将监听在所有的接口上的给定端口。如果给定了Ip和端口，etcd将监听指定的接口和端口。可以使用多个URL指定要监听的地址和端口的数量。 etcd将响应来自任何列出的地址和端口的请求。
      默认值："http://localhost:2379"
      环境变量:ETCD_LISTEN_CLIENT_URLS
      示例："http://10.0.0.1:2379"
      无效的示例："http://example.com:2379"(绑定的域名是无效的)
  
  
  
  ## cors
  
      以逗号分隔的CORS来源白名单（跨来源资源共享）。
      默认值：""
      环境变量：ETCD_CORS
  
  ## quota-backent-bytes
  
      后端大小超过给定配额时引发警报（0默认为低空间配额）。
      默认值：0
      环境变量：ETCD_QUOTA_BACKEND_BYTES
  
  ## backend-batch-limit
  
      BackendBatchLimit是提交后端事务之前的最大数量的操作。
      默认值：0
      环境变量：ETCD_BACKEND_BATCH_LIMIT
  
  ## backend-bbolt-freelist-type
  
      etcd后端（bboltdb）使用的自由列表类型（支持数组和映射的类型）。
      默认值：map
      环境变量：ETCD_BACKEND_BBOLT_FREELIST_TYPE
  
  ## backend-batch-interval
  
      BackendBatchInterval是提交后端事务之前的最长时间。
      默认值：0
      环境变量：ETCD_BACKEND_BATCH_INTERVAL
  
  ## max-txn-ops
  
      交易中允许的最大操作数。
      默认值：128
      环境变量：ETCD_MAX_TXN_OPS
  
  ## max-request-bytes
  
      服务器将接受的最大客户端请求大小（以字节为单位）。
      默认值：1572864
      环境变量：ETCD_MAX_REQUEST_BYTES
  
  ## grpc-keepalive-min-time
  
      客户端在ping服务器之前应等待的最小持续时间间隔。
      默认值：5s
      环境变量：ETCD_GRPC_KEEPALIVE_MIN_TIME
  
  ## grpc-keepalive-interval
  
      服务器到客户端ping的频率持续时间，以检查连接是否有效（0禁用）。
      默认值：2h
      环境变量：ETCD_GRPC_KEEPALIVE_INTERVAL
  
  ## grpc-keepalive-timeout
  
      关闭无响应的连接之前的额外等待时间（0禁用）。
      默认值：20s
      环境变量：ETCD_GRPC_KEEPALIVE_TIMEOUT
  
  ```

- 集群参数：

  ```json
  ## initial-advertise-peer-urls,--initial-cluster,--initial-cluster-state,和--initial-cluster-token参数用于启动(静态启动,发现服务启动或者运行时重新配置)一个新成员，当重启已经存在的成员时将忽略。
  前缀为--discovery的参数在使用发现服务时需要被设置。
  
  ## initial-advertise-peer-urls
  
      此成员的对等URL的列表，以通告到集群的其余部分。 这些地址用于在集群周围传送etcd数据。 所有集群成员必须至少有一个路由。 这些URL可以包含域名。
      默认值："http://localhost:2380"
      环境变量：ETCD_INITIAL_ADVERTISE_PEER_URLS
      示例："http://example.com:2380, http://10.0.0.1:2380"
  
  ## initial-cluster
  
      启动集群的初始化配置
      默认值："default=http://localhost:2380"
      环境变量：ETCD_INITIAL_CLUSTER
      关键是所提供的每个节点的--name参数的值。 默认值使用default作为密钥，因为这是--name参数的默认值。
  
  ## initial-cluster-state
  
      初始群集状态（“新”或“现有”）。 对于在初始静态或DNS引导过程中存在的所有成员，将其设置为new。 如果此选项设置为existing，则etcd将尝试加入现存集群。 如果设置了错误的值，etcd将尝试启动，但会安全地失败。
      默认值："new:
      环境变量：ETCD_INITIAL_CLUSTER_STATE
  
  ## initial-cluster-token
  
      引导期间etcd群集的初始集群令牌。
      默认值："etcd-cluster"
      环境变量：ETCD_INITIAL_CLUSTER_TOKEN
  
  ## advertise-client-urls
  
      此成员的客户端URL的列表，这些URL广播给集群的其余部分。 这些URL可以包含域名。
      默认值：http://localhost:2379
      环境变量：ETCD_ADVERTISE_CLIENT_URLS
      示例："http://example.com:2379, http://10.0.0.1:2379"
      如果从集群成员中发布诸如http://localhost:2379之类的URL并使用etcd的代理功能，请小心。这将导致循环，因为代理将向其自身转发请求，直到其资源（内存，文件描述符）最终耗尽为止。
  
  ## discovery
  
      发现URL用于引导启动集群
      默认值：""
      环境变量：ETCD_DISCOVERY
  
  ## discovery-srv
  
      用于引导集群的DNS srv域。
      默认值：""
      环境变量：ETCD_DISCOVERY_SRV
  
  ## discovery-srv-name
  
      使用DNS引导时查询的DNS srv名称的后缀。
      默认值：""
      环境变量：ETCD_DISCOVERY_SRV_NAME
  
  ## discovery-fallback
  
      发现服务失败时的预期行为(“退出”或“代理”)。“代理”仅支持v2 API。
      默认值： "proxy"
      环境变量：ETCD_DISCOVERY_FALLBACK
  
  ## discovery-proxy
  
      HTTP代理，用于发现服务的流量。
      默认值：""
      环境变量：ETCD_DISCOVERY_PROXY
  
  ## ## strict-reconfig-check
  
      拒绝可能导致quorum丢失的重新配置请求。
      默认值：true
      环境变量：ETCD_STRICT_RECONFIG_CHECK
  
  ## auto-compaction-retention
  
      mvcc密钥值存储的自动压缩保留时间（小时）。 0表示禁用自动压缩。
      默认值：0
      环境变量：ETCD_AUTO_COMPACTION_RETENTION
  
  ## auto-compaction-mode
  
      解释“自动压缩保留”之一：“定期”，“修订”。 基于期限的保留的“定期”，如果未提供时间单位（例如“ 5m”），则默认为小时。 “修订”用于基于修订号的保留。
      默认值：periodic
      环境变量：ETCD_AUTO_COMPACTION_MODE
  
  ## enable-v2
  
      接受etcd V2客户端请求
      默认值：false
      环境变量：ETCD_ENABLE_V2
  
  ```

- 安全参数：

  ```json
  安全参数有助于构建一个安全的etcd集群
  ## ca-file
  DEPRECATED
  
      客户端服务器TLS CA文件的路径。 --ca-file ca.crt可以替换为--trusted-ca-file ca.crt --client-cert-auth，而etcd将执行相同的操作。
      默认值：""
      环境变量：ETCD_CA_FILE
  
  ## cert-file
  
      客户端服务器TLS证书文件的路径
      默认值：""
      环境变量：ETCD_CERT_FILE
  
  ## key-file
  
      客户端服务器TLS秘钥文件的路径
      默认值：""
      环境变量：ETCD_KEY_FILE
  
  ## client-cert-auth
  
      开启客户端证书认证
      默认值：false
      环境变量：ETCD_CLIENT_CERT_AUTH
      CN 权限认证不支持gRPC-网关
  
  ## client-crl-file
  
      客户端被撤销的TLS证书文件的路径
      默认值：""
      环境变量：ETCD_CLIENT_CERT_ALLOWED_HOSTNAME
  
  ## client-cert-allowed-hostname
  
      允许客户端证书身份验证的TLS名称。
      默认值：""
      环境变量：ETCD_CLIENT_CERT_ALLOWED_HOSTNAME
  
  ## trusted-ca-file
  
      客户端服务器受信任的TLS CA证书文件的路径
      默认值：""
      环境变量：ETCD_TRUSTED_CA_FILE
  
  ## auto-tls
  
      客户端TLS使用自动生成的证书
      默认值：false
      环境变量：ETCD_AUTO_TLS
  
  ```

- 代理参数：

  ```json
  -proxy前缀标志将etcd配置为以代理模式运行。 “代理”仅支持v2 API。
  
  ## proxy
  
      代理模式设置(”off","readonly"或者"on")
      默认值："off"
      环境变量：ETCD_PROXY
  
  ## proxy-failure-wait
  
      在重新考虑端点请求之前，端点将保持故障状态的时间（以毫秒为单位）。
      默认值：5000
      环境变量：ETCD_PROXY_FAILURE_WAIT
  
  ## proxy-refresh-interval
  
      节点刷新间隔的时间（以毫秒为单位）。
      默认值：30000
      环境变量：ETCD_PROXY_REFRESH_INTERVAL
  
  ## proxy-dial-timeout
  
      拨号超时的时间（以毫秒为单位），或0以禁用超时
      默认值：1000
      环境变量：ETCD_PROXY_DIAL_TIMEOUT
  
  ## proxy-write-timeout
  
      写入超时的时间（以毫秒为单位）或禁用超时的时间为0。
      默认值：5000
      环境变量：ETCD_PROXY_WRITE_TIMEOUT
  
  ## proxy-read-timeout
  
      读取超时的时间（以毫秒为单位），或者为0以禁用超时。
      如果使用Watch，请勿更改此值，因为会使用较长的轮询请求。
      默认值：0
      环境变量：ETCD_PROXY_READ_TIMEOUT
  ```

- 其他参数：

  ```json
  日志参数
  
  ## logger
  
  v3.4可以使用，警告：--logger=capnslog在v3.5被抛弃使用
  
      指定“ zap”用于结构化日志记录或“ capnslog”。
      默认值：capnslog
      环境变量：ETCD_LOGGER
  
  ## log-outputs
  
      指定“ stdout”或“ stderr”以跳过日志记录，即使在systemd或逗号分隔的输出目标列表下运行时也是如此。
      默认值：defalut
      环境变量：ETCD_LOG_OUTPUTS
      default在zap logger迁移期间对v3.4使用stderr配置
  
  ## log-level
  v3.4可以使用
  
      配置日志等级，仅支持debug,info,warn,error,panic,fatal
      默认值：info
      环境变量：ETCD_LOG_LEVEL
      default使用info.
  
  ## debug
  警告：在v3.5被抛弃使用
  
      将所有子程序包的默认日志级别降为DEBUG。
      默认值：false(所有的包使用INFO)
      环境变量：ETCD_DEBUG
  
  ## log-package-levels
  警告：在v3.5被抛弃使用
  
      将各个etcd子软件包设置为特定的日志级别。 一个例子是etcdserver = WARNING，security = DEBUG
      默认值：""(所有的包使用INFO)
      环境变量：ETCD_LOG_PACKAGE_LEVELS
  
  
  风险参数
  使用不安全标志时请小心，因为它将破坏共识协议提供的保证。 例如，如果群集中的其他成员仍然存在，可能会panic。 使用这些标志时，请遵循说明。
  ## force-new-cluster
  
      强制创建一个新的单成员群集。 它提交配置更改，以强制删除群集中的所有现有成员并添加自身，但是强烈建议不要这样做。 请查看灾难恢复文档以了解首选的v3恢复过程。
      默认值：false
      环境变量：ETCD_FORCE_NEW_CLUSTER
  
  
  杂项参数
  
  ## version
  
      打印版本并退出
      默认值：false
  
  ## config-file
  
      从文件加载服务器配置。 请注意，如果提供了配置文件，则其他命令行标志和环境变量将被忽略。
      默认值：""
      示例：配置文件示例
      环境变量：ETCD_CONFIG_FILE
  
  
  分析参数
  
  ## enable-pprof
  
      通过HTTP服务器启用运行时分析数据。地址位于客户端URL+“/debug/pprof/”
      默认值：false
      环境变量：ETCD_ENABLE_PPROF
  
  ## metrics
  
      设置导出指标的详细程度，specify 'extensive' to include server side grpc histogram metrics.
      默认值：basic
      环境变量：ETCD_METRICS
  
  ## listen-metrics-urls
  
      可以响应/metrics和/health端点的其他URL列表
      默认值：""
      环境变量：ETCD_LISTEN_METRICS_URLS
  
  
  权限参数
  
  ## auth-token
  
      指定令牌类型和特定于令牌的选项，特别是对于JWT,格式为type,var1=val1,var2=val2,...,可能的类型是simple或者jwt.对于具体的签名方法jwt可能的变量为sign-method（可能的值为'ES256', 'ES384', 'ES512', 'HS256', 'HS384', 'HS512', 'RS256', 'RS384', 'RS512', 'PS256', 'PS384','PS512'）
      对于非对称算法（“ RS”，“ PS”，“ ES”），公钥是可选的，因为私钥包含足够的信息来签名和验证令牌。pub-key用于指定用于验证jwt的公钥的路径,priv-key用于指定用于对jwt进行签名的私钥的路径，ttl用于指定jwt令牌的TTL。
      JWT的示例选项：-auth-token jwt，pub-key=app.rsa.pub，privkey=app.rsasign-method = RS512，ttl = 10m
      默认值："simple"
      环境变量：ETCD_AUTH_TOKEN
  
  ## bcrypt-cost
  
      指定用于哈希认证密码的bcrypt算法的成本/强度。 有效值在4到31之间。
      默认值：10
      环境变量：(不支持)
  
  
  
  实验参数：
  
  ## experimental-corrupt-check-time
  
      群集损坏检查通过之间的时间间隔
      默认值：0s
      环境变量：ETCD_EXPERIMENTAL_CORRUPT_CHECK_TIME
  
  ## experimental-compaction-batch-limit
  
      设置每个压缩批处理中删除的最大修订。
      默认值：1000
      环境变量：ETCD_EXPERIMENTAL_COMPACTION_BATCH_LIMIT
  
  ## experimental-peer-skip-client-san-verification
  
      跳过客户端证书中对等连接的SAN字段验证。 这可能是有帮助的，例如 如果群集成员在NAT后面的不同网络中运行。在这种情况下，请确保使用基于私有证书颁发机构的对等证书.--peer-cert-file, --peer-key-file, --peer-trusted-ca-file
      默认值：false
      环境变量：ETCD_EXPERIMENTAL_PEER_SKIP_CLIENT_SAN_VERIFICATION
  
  ```

- 1