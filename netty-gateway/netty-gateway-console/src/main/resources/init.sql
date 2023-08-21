DROP DATABASE IF EXISTS `netty_gateway` ;
CREATE DATABASE `netty_gateway`;
USE netty_gateway;

CREATE TABLE `service_definition_entity`
(
    `service_id`  varchar(255) NOT NULL COMMENT '服务唯一id',
    `base_path`   varchar(255) DEFAULT '' COMMENT '服务URL前缀，全局唯一',
    `protocol`    varchar(255) DEFAULT '' COMMENT '服务的具体协议 http、dubbo、grpc',
    `enable`      tinyint(1)   DEFAULT '1' COMMENT '服务启用禁用',
    `invoker_map` text COMMENT '服务方法信息 key=invokerPath(完整路径) value=服务调用的接口模型描述',
    `draft`       text COMMENT '草稿',
    PRIMARY KEY (`service_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT '资源服务定义';

CREATE TABLE `rule_entity`
(
    `id`             varchar(255) NOT NULL COMMENT '规则ID',
    `name`           varchar(255) DEFAULT '' COMMENT '规则名称',
    `order`          int(11)      DEFAULT '0' COMMENT '规则排序',
    `filter_configs` text COMMENT '规则集合定义',
    `draft`          text COMMENT '草稿',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT '规则模型';

CREATE TABLE `service_instance_entity`
(
    `service_instance_id` varchar(255) NOT NULL COMMENT '服务实例ID(ip:port)',
    `service_id`          varchar(255) DEFAULT '' COMMENT '服务定义唯一id',
    `address`             varchar(255) DEFAULT '' COMMENT '服务实例地址(ip:port)',
    `tags`                text COMMENT '标签信息',
    `weight`              int(11)      DEFAULT '100' COMMENT '权重信息',
    `register_time`       bigint(20)   DEFAULT '0' COMMENT '服务注册的时间戳，后面我们做负载均衡，warmup预热',
    `enable`              tinyint(1)   DEFAULT '1' COMMENT '服务实例启用禁用',
    `version`             varchar(255) DEFAULT '' COMMENT '服务实例对应的版本号',
    PRIMARY KEY (`service_instance_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT '服务实例';