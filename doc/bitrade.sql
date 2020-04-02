SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for admin
-- ----------------------------
DROP TABLE IF EXISTS `admin`;
CREATE TABLE `admin`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `area_code` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `avatar` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `email` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `enable` int(11) NULL DEFAULT NULL,
  `google_date` datetime(0) NULL DEFAULT NULL,
  `google_key` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `google_state` int(11) NULL DEFAULT NULL,
  `last_login_ip` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `last_login_time` datetime(0) NULL DEFAULT NULL,
  `mobile_phone` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `password` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `qq` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `real_name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `role_id` bigint(20) NOT NULL,
  `status` int(11) NULL DEFAULT NULL,
  `username` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `department_id` bigint(20) NULL DEFAULT NULL,
  `create_time` timestamp(0) NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `UK_gfn44sntic2k93auag97juyij`(`username`) USING BTREE,
  INDEX `FKnmmt6f2kg0oaxr11uhy7qqf3w`(`department_id`) USING BTREE,
  CONSTRAINT `FKnmmt6f2kg0oaxr11uhy7qqf3w` FOREIGN KEY (`department_id`) REFERENCES `department` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 38 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for admin_access_log
-- ----------------------------
DROP TABLE IF EXISTS `admin_access_log`;
CREATE TABLE `admin_access_log`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `access_ip` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '访问IP',
  `access_method` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '请求方式',
  `access_time` datetime(0) NULL DEFAULT NULL COMMENT '请求时间',
  `admin_id` bigint(20) NULL DEFAULT NULL COMMENT '操作人ID',
  `module` int(11) NULL DEFAULT NULL COMMENT '模块',
  `operation` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '操作说明',
  `uri` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '请求路径',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 51110 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for admin_permission
-- ----------------------------
DROP TABLE IF EXISTS `admin_permission`;
CREATE TABLE `admin_permission`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `description` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '描述',
  `name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '菜单编码',
  `parent_id` bigint(20) NULL DEFAULT NULL COMMENT '上级菜单ID',
  `sort` int(11) NULL DEFAULT NULL COMMENT '排序',
  `title` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '菜单名称',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 306 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for admin_role
-- ----------------------------
DROP TABLE IF EXISTS `admin_role`;
CREATE TABLE `admin_role`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `description` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '描述',
  `role` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '角色名称',
  `create_time` timestamp(0) NULL DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 58 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for admin_role_permission
-- ----------------------------
DROP TABLE IF EXISTS `admin_role_permission`;
CREATE TABLE `admin_role_permission`  (
  `role_id` bigint(20) NOT NULL COMMENT '角色id',
  `rule_id` bigint(20) NOT NULL COMMENT '菜单id',
  UNIQUE INDEX `UKplesprlvm1sob8nl9yc5rgh3m`(`role_id`, `rule_id`) USING BTREE,
  INDEX `FKqf3fhgl5mjqqb0jeupx7yafh0`(`rule_id`) USING BTREE,
  CONSTRAINT `FK52rddd3qje4p49iubt08gplb5` FOREIGN KEY (`role_id`) REFERENCES `admin_role` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `FKqf3fhgl5mjqqb0jeupx7yafh0` FOREIGN KEY (`rule_id`) REFERENCES `admin_permission` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for advertise
-- ----------------------------
DROP TABLE IF EXISTS `advertise`;
CREATE TABLE `advertise`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `advertise_type` int(11) NOT NULL COMMENT '广告类型',
  `auto` int(11) NULL DEFAULT NULL COMMENT '是否开启自动回复',
  `autoword` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '自动回复内容',
  `coin_unit` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '币种单位',
  `create_time` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
  `deal_amount` decimal(32, 18) NULL DEFAULT NULL COMMENT '交易中数量',
  `level` int(11) NULL DEFAULT NULL COMMENT '广告级别',
  `limit_money` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '最小交易金额',
  `max_limit` decimal(18, 2) NULL DEFAULT NULL COMMENT '最高单笔交易额',
  `min_limit` decimal(18, 2) NULL DEFAULT NULL COMMENT '最低单笔交易额',
  `number` decimal(32, 18) NULL DEFAULT NULL COMMENT '计划数量',
  `pay_mode` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '付款方式',
  `premise_rate` decimal(18, 6) NULL DEFAULT NULL COMMENT '溢价百分比',
  `price` decimal(18, 2) NULL DEFAULT NULL COMMENT '交易价格',
  `price_type` int(11) NOT NULL COMMENT '价格类型（默认0固定价格）',
  `remain_amount` decimal(32, 18) NULL DEFAULT NULL COMMENT '计划剩余数量',
  `remark` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '备注',
  `status` int(11) NULL DEFAULT NULL COMMENT '状态',
  `time_limit` int(11) NULL DEFAULT NULL COMMENT '付款时间限制',
  `update_time` datetime(0) NULL DEFAULT NULL COMMENT '更新时间',
  `username` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '法币昵称',
  `version` bigint(20) NULL DEFAULT NULL,
  `coin_id` bigint(20) NOT NULL COMMENT '币种id',
  `country` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '国家',
  `member_id` bigint(20) NOT NULL COMMENT '广告持有者id',
  `top` int(11) NULL DEFAULT 0 COMMENT '置顶',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `FK75rse9iecdnimf8ugtf20c43l`(`coin_id`) USING BTREE,
  INDEX `FK9lueh92242ckyajg17xr9tcie`(`country`) USING BTREE,
  INDEX `FKigm810pf0bqcxekpr0hy1b8rt`(`member_id`) USING BTREE,
  CONSTRAINT `FK75rse9iecdnimf8ugtf20c43l` FOREIGN KEY (`coin_id`) REFERENCES `otc_coin` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `FK9lueh92242ckyajg17xr9tcie` FOREIGN KEY (`country`) REFERENCES `country` (`zh_name`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `FKigm810pf0bqcxekpr0hy1b8rt` FOREIGN KEY (`member_id`) REFERENCES `member` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 38165 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for airdrop
-- ----------------------------
DROP TABLE IF EXISTS `airdrop`;
CREATE TABLE `airdrop`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `create_time` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
  `error_index` int(11) NULL DEFAULT NULL COMMENT '报错时的下标',
  `error_msg` text CHARACTER SET utf8 COLLATE utf8_general_ci NULL COMMENT '报错信息',
  `file_name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '文件名',
  `status` int(11) NULL DEFAULT NULL COMMENT '状态',
  `success_count` int(11) NULL DEFAULT NULL COMMENT '成功数量',
  `admin_id` bigint(20) NULL DEFAULT NULL COMMENT '操作人id',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `FK3lo6cbxpuewbore50ffbwfiqr`(`admin_id`) USING BTREE,
  CONSTRAINT `FK3lo6cbxpuewbore50ffbwfiqr` FOREIGN KEY (`admin_id`) REFERENCES `admin` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '空投记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for announcement
-- ----------------------------
DROP TABLE IF EXISTS `announcement`;
CREATE TABLE `announcement`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `content` text CHARACTER SET utf8 COLLATE utf8_general_ci NULL COMMENT '内容',
  `create_time` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
  `img_url` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '图片链接',
  `is_show` bit(1) NULL DEFAULT NULL COMMENT '是否显示',
  `is_top` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '是否置顶',
  `sort` int(11) NOT NULL COMMENT '排序',
  `title` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '标题',
  `locale` varchar(6) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '语种（\"en-US\", \"zh-CN\", \"ja-JP\", \"ko-KR\", \"ar-AE\"）',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 37 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for app_revision
-- ----------------------------
DROP TABLE IF EXISTS `app_revision`;
CREATE TABLE `app_revision`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `download_url` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '下载链接',
  `platform` int(11) NULL DEFAULT NULL COMMENT '平台（安卓/ios）',
  `publish_time` datetime(0) NULL DEFAULT NULL COMMENT '发布时间',
  `remark` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '描述',
  `version` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '版本号',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 662 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for appeal
-- ----------------------------
DROP TABLE IF EXISTS `appeal`;
CREATE TABLE `appeal`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `associate_id` bigint(20) NULL DEFAULT NULL COMMENT '申诉关联者id',
  `create_time` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
  `deal_with_time` datetime(0) NULL DEFAULT NULL COMMENT '处理时间',
  `img_urls` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '图片链接',
  `initiator_id` bigint(20) NULL DEFAULT NULL COMMENT '发起者id',
  `is_success` int(11) NULL DEFAULT NULL COMMENT '是否成功',
  `remark` varchar(500) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '备注',
  `status` int(11) NULL DEFAULT NULL COMMENT '状态',
  `admin_id` bigint(20) NULL DEFAULT NULL COMMENT '操作人ID',
  `order_id` bigint(20) NOT NULL COMMENT '订单id',
  `type` int(11) NULL DEFAULT NULL COMMENT '申诉类型',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `UK_todwxorutclquf69bwow70kml`(`order_id`) USING BTREE,
  INDEX `FKanmcnj859x2tv3y0pv7u05cqa`(`admin_id`) USING BTREE,
  CONSTRAINT `FKanmcnj859x2tv3y0pv7u05cqa` FOREIGN KEY (`admin_id`) REFERENCES `admin` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `FKs3vo8h01sq39icylq1qdwekn1` FOREIGN KEY (`order_id`) REFERENCES `otc_order` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 723 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '申诉表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for asset_exchange_coin
-- ----------------------------
DROP TABLE IF EXISTS `asset_exchange_coin`;
CREATE TABLE `asset_exchange_coin`  (
  `id` bigint(20) NOT NULL,
  `exchange_rate` decimal(12, 6) NULL DEFAULT NULL COMMENT '兑换汇率',
  `from_unit` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT 'from币种单位',
  `to_unit` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT 'to币种单位',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '支持兑换的币种' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for se_fee_change_record
-- ----------------------------
DROP TABLE IF EXISTS `se_fee_change_record`;
CREATE TABLE `se_fee_change_record`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `member_id` int(11) NULL DEFAULT NULL COMMENT '用户id',
  `create_time` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
  `way` varchar(2) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '操作方式（手动切换/被动切换）',
  `status` varchar(2) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '状态',
  `type` varchar(2) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '类型（阶梯费率/SE抵扣）',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 238 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = 'SE手续费抵扣设置切换记录' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for business_auth_apply
-- ----------------------------
DROP TABLE IF EXISTS `business_auth_apply`;
CREATE TABLE `business_auth_apply`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `amount` decimal(32, 18) NULL DEFAULT NULL COMMENT '保证金数额',
  `auditing_time` datetime(0) NULL DEFAULT NULL COMMENT '审核时间',
  `auth_info` text CHARACTER SET utf8 COLLATE utf8_general_ci NULL COMMENT '提交的申请信息',
  `certified_business_status` int(11) NULL DEFAULT NULL COMMENT '商家认证状态',
  `create_time` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
  `deposit_record_id` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '保证金记录ID',
  `detail` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '描述',
  `update_time` datetime(0) NULL DEFAULT NULL COMMENT '更新时间',
  `business_auth_deposit_id` bigint(20) NULL DEFAULT NULL COMMENT '保证金id',
  `member_id` bigint(20) NULL DEFAULT NULL COMMENT '用户id',
  `verify_level` bit(1) NULL DEFAULT b'0' COMMENT '认证商家加V',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `FKds72omottejlk5isd34ha5i10`(`business_auth_deposit_id`) USING BTREE,
  INDEX `FKdfv99wau68rbi99k3m2ufc77x`(`member_id`) USING BTREE,
  CONSTRAINT `FKdfv99wau68rbi99k3m2ufc77x` FOREIGN KEY (`member_id`) REFERENCES `member` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `FKds72omottejlk5isd34ha5i10` FOREIGN KEY (`business_auth_deposit_id`) REFERENCES `business_auth_deposit` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 19 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '商家认证申请' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for business_auth_deposit
-- ----------------------------
DROP TABLE IF EXISTS `business_auth_deposit`;
CREATE TABLE `business_auth_deposit`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `amount` decimal(32, 18) NULL DEFAULT NULL COMMENT '数量',
  `create_time` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
  `status` int(11) NULL DEFAULT NULL COMMENT '状态',
  `admin_id` bigint(20) NULL DEFAULT NULL COMMENT '操作人id',
  `coin_id` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '币种id',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `FKfj3hxtr3ae1yma9bxeuqc29pj`(`admin_id`) USING BTREE,
  INDEX `FKjx7799a3pwdtnu43fkpn27kj6`(`coin_id`) USING BTREE,
  CONSTRAINT `FKfj3hxtr3ae1yma9bxeuqc29pj` FOREIGN KEY (`admin_id`) REFERENCES `admin` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `FKjx7799a3pwdtnu43fkpn27kj6` FOREIGN KEY (`coin_id`) REFERENCES `coin` (`name`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '保证金配置' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for bussiness_cancel_apply
-- ----------------------------
DROP TABLE IF EXISTS `bussiness_cancel_apply`;
CREATE TABLE `bussiness_cancel_apply`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `cancel_apply_time` datetime(0) NULL DEFAULT NULL COMMENT '取消申请时间',
  `deposit_record_id` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '保证金记录id',
  `detail` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '描述',
  `handle_time` datetime(0) NULL DEFAULT NULL COMMENT '处理时间',
  `reason` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '原因',
  `status` int(11) NULL DEFAULT NULL COMMENT '状态',
  `member_id` bigint(20) NULL DEFAULT NULL COMMENT '用户id',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `FK2wsy0m9vrcun1vvynoi5x5os8`(`member_id`) USING BTREE,
  CONSTRAINT `FK2wsy0m9vrcun1vvynoi5x5os8` FOREIGN KEY (`member_id`) REFERENCES `member` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '商家认证取消申请表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for coin
-- ----------------------------
DROP TABLE IF EXISTS `coin`;
CREATE TABLE `coin`  (
  `name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '币名称',
  `can_auto_withdraw` int(11) NULL DEFAULT NULL COMMENT ' 是否能自动提币',
  `can_recharge` int(11) NULL DEFAULT NULL COMMENT '是否能充币',
  `can_transfer` int(11) NULL DEFAULT NULL COMMENT '是否能转账',
  `can_withdraw` int(11) NULL DEFAULT NULL COMMENT '是否能提币',
  `cny_rate` decimal(32, 18) NULL DEFAULT 0.000000000000000000 COMMENT '对人民币汇率',
  `cold_wallet_address` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '冷钱包地址',
  `enable_rpc` int(11) NULL DEFAULT NULL COMMENT '是否支持rpc接口',
  `has_legal` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否是法币',
  `is_platform_coin` int(11) NULL DEFAULT 0 COMMENT '是否为平台币',
  `master_address` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '主充值地址',
  `max_daily_withdraw_rate` decimal(32, 18) NULL DEFAULT 0.000000000000000000 COMMENT '单日最大提币量',
  `max_tx_fee` double NOT NULL COMMENT '最大提币手续费',
  `max_withdraw_amount` decimal(32, 18) NULL DEFAULT NULL COMMENT '最大提币数量',
  `min_recharge_amount` decimal(32, 18) NULL DEFAULT 0.000000000000000000 COMMENT '最小充币数量',
  `min_tx_fee` double NOT NULL COMMENT '最小提币手续费',
  `min_withdraw_amount` decimal(32, 18) NULL DEFAULT NULL COMMENT '最小提币数量',
  `miner_fee` decimal(32, 18) NULL DEFAULT 0.000000000000000000 COMMENT '矿工费',
  `name_cn` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '中文名称',
  `sgd_rate` decimal(32, 18) NULL DEFAULT 0.000000000000000000 COMMENT '对新加坡币汇率',
  `sort` int(11) NOT NULL COMMENT '排序',
  `status` int(11) NULL DEFAULT NULL COMMENT '状态',
  `unit` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '单位',
  `usd_rate` decimal(32, 18) NULL DEFAULT 0.000000000000000000 COMMENT '对美元汇率',
  `withdraw_scale` int(11) NULL DEFAULT 4 COMMENT '提币精度',
  `withdraw_threshold` decimal(32, 18) NULL DEFAULT NULL COMMENT '自动提现阈值',
  `img_url` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '币种图片',
  `release_amount` decimal(32, 18) NULL DEFAULT NULL COMMENT '发型总量',
  `release_time` datetime(0) NULL DEFAULT NULL COMMENT '发行时间',
  `fund_price` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT '' COMMENT '众筹价格',
  `white_paper` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '白皮书',
  `website` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '官网',
  `block_query` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '区块查询',
  `is_settlement` bit(1) NULL DEFAULT b'0' COMMENT '是否作为结算币种',
  `burn_amount` decimal(32, 18) NULL DEFAULT 0.000000000000000000 COMMENT '销毁总量',
  `circulate_amount` decimal(32, 18) NULL DEFAULT 0.000000000000000000 COMMENT '流通总量',
  PRIMARY KEY (`name`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '币种配置' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for coin_area
-- ----------------------------
DROP TABLE IF EXISTS `coin_area`;
CREATE TABLE `coin_area`  (
  `id` int(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '名称（多语言名称在多语言表中配置）',
  `sort` int(11) UNSIGNED ZEROFILL NULL DEFAULT NULL COMMENT '排序',
  `status` varchar(2) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '状态',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 43 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '交易区配置' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for coin_chain_relation
-- ----------------------------
DROP TABLE IF EXISTS `coin_chain_relation`;
CREATE TABLE `coin_chain_relation`  (
  `coin_key` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '唯一标识',
  `coin_id` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '币种',
  `chain_id` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '所属链',
  `coin_name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '币种名称',
  PRIMARY KEY (`coin_key`) USING BTREE,
  INDEX `fk_coin_relation_1`(`coin_id`) USING BTREE,
  CONSTRAINT `fk_coin_relation_1` FOREIGN KEY (`coin_id`) REFERENCES `coin` (`name`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for country
-- ----------------------------
DROP TABLE IF EXISTS `country`;
CREATE TABLE `country`  (
  `zh_name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `area_code` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `en_name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `language` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `local_currency` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `sort` int(11) NOT NULL,
  PRIMARY KEY (`zh_name`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for data_dictionary
-- ----------------------------
DROP TABLE IF EXISTS `data_dictionary`;
CREATE TABLE `data_dictionary`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `bond` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '参数',
  `comment` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '描述',
  `creation_time` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
  `img_url` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '图片url',
  `update_time` datetime(0) NULL DEFAULT NULL COMMENT '更新时间',
  `value` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '值',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 15 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '字典表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for department
-- ----------------------------
DROP TABLE IF EXISTS `department`;
CREATE TABLE `department`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `create_time` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
  `leader_id` bigint(20) NULL DEFAULT NULL COMMENT '管理人id',
  `name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '名称',
  `remark` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '描述',
  `update_time` datetime(0) NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `UK_1t68827l97cwyxo9r1u6t4p7d`(`name`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '部门表（暂弃）' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for deposit_record
-- ----------------------------
DROP TABLE IF EXISTS `deposit_record`;
CREATE TABLE `deposit_record`  (
  `id` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `amount` decimal(32, 18) NULL DEFAULT NULL COMMENT '数量',
  `status` int(11) NULL DEFAULT NULL COMMENT '状态',
  `coin_id` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '币种id',
  `member_id` bigint(20) NULL DEFAULT NULL COMMENT '用户id',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `FK7x5q0lmqqtty5i0w5mq09o8r7`(`coin_id`) USING BTREE,
  INDEX `FK573evxr87edhss8bij5mk707d`(`member_id`) USING BTREE,
  CONSTRAINT `FK573evxr87edhss8bij5mk707d` FOREIGN KEY (`member_id`) REFERENCES `member` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `FK7x5q0lmqqtty5i0w5mq09o8r7` FOREIGN KEY (`coin_id`) REFERENCES `coin` (`name`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '充币记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for dividend_start_record
-- ----------------------------
DROP TABLE IF EXISTS `dividend_start_record`;
CREATE TABLE `dividend_start_record`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `amount` decimal(18, 6) NULL DEFAULT NULL COMMENT '数量',
  `date` datetime(0) NULL DEFAULT NULL,
  `end` bigint(20) NULL DEFAULT NULL,
  `end_date` datetime(0) NULL DEFAULT NULL,
  `rate` decimal(18, 2) NULL DEFAULT NULL COMMENT '比例',
  `start` bigint(20) NULL DEFAULT NULL,
  `start_date` datetime(0) NULL DEFAULT NULL,
  `unit` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `admin_id` bigint(20) NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `FK226c1iy2t1dt9tjjo20pum39d`(`admin_id`) USING BTREE,
  CONSTRAINT `FK226c1iy2t1dt9tjjo20pum39d` FOREIGN KEY (`admin_id`) REFERENCES `admin` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for emption_record
-- ----------------------------
DROP TABLE IF EXISTS `emption_record`;
CREATE TABLE `emption_record`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `create_time` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
  `end_time` datetime(0) NULL DEFAULT NULL COMMENT '结束时间',
  `expect_time` datetime(0) NULL DEFAULT NULL COMMENT '过期时间',
  `ieo_id` bigint(20) NULL DEFAULT NULL COMMENT 'IEOid',
  `ieo_name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT 'IEO名称',
  `pay_amount` decimal(19, 2) NULL DEFAULT NULL COMMENT '使用数量',
  `pic_view` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '缩略图',
  `raise_coin` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '募集币种',
  `ratio` decimal(19, 2) NULL DEFAULT NULL COMMENT '募集币种与发售币种的比率',
  `receive_amount` decimal(19, 2) NULL DEFAULT NULL COMMENT '认购数量',
  `sale_amount` decimal(19, 2) NULL DEFAULT NULL COMMENT '发售总量',
  `sale_coin` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '发售币种',
  `start_time` datetime(0) NULL DEFAULT NULL COMMENT '开始时间',
  `status` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '状态',
  `user_id` bigint(20) NULL DEFAULT NULL COMMENT '用户id',
  `user_mobile` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '手机号',
  `user_name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '名称',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for exchange_coin
-- ----------------------------
DROP TABLE IF EXISTS `exchange_coin`;
CREATE TABLE `exchange_coin`  (
  `symbol` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '币对',
  `base_coin_scale` int(11) NOT NULL COMMENT '结算币精度',
  `base_fee` decimal(12, 6) NULL DEFAULT 0.000000 COMMENT '结算币种手续费',
  `base_symbol` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '结算币种',
  `coin_scale` int(11) NOT NULL COMMENT '交易币精度',
  `coin_symbol` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '交易币种',
  `default_symbol` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '是否默认交易对，设为1则全平台默认，设为2则币种内默认',
  `enable` int(11) NOT NULL COMMENT '是否启用',
  `enable_market_buy` int(11) NULL DEFAULT 1 COMMENT '是否启用市价买',
  `enable_market_sell` int(11) NULL DEFAULT 1 COMMENT '是否启用市价卖',
  `fee` decimal(12, 6) NULL DEFAULT 0.000000 COMMENT '基币手续费',
  `flag` int(11) NULL DEFAULT 0,
  `max_trading_order` int(11) NULL DEFAULT 0 COMMENT '最大允许同时交易的订单数，0表示不限制',
  `max_trading_time` int(11) NULL DEFAULT 0 COMMENT '委托超时自动下架时间，单位为秒，0表示不过期',
  `max_volume` decimal(32, 18) NULL DEFAULT 0.000000000000000000 COMMENT '最大下单量',
  `min_sell_price` decimal(32, 18) NULL DEFAULT 0.000000000000000000 COMMENT '卖单最低价格',
  `min_turnover` decimal(32, 18) NULL DEFAULT 0.000000000000000000 COMMENT '最小成交额',
  `min_volume` decimal(32, 18) NULL DEFAULT 0.000000000000000000 COMMENT '最小下单量',
  `sort` int(11) NOT NULL,
  `zone` int(11) NULL DEFAULT 0,
  `new_sort` int(11) NULL DEFAULT -1 COMMENT '新币榜排序（倒序展示，小于0不展示）',
  `area_id` int(11) NULL DEFAULT NULL COMMENT '交易区id',
  PRIMARY KEY (`symbol`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '币币交易-交易对表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for exchange_favor_symbol
-- ----------------------------
DROP TABLE IF EXISTS `exchange_favor_symbol`;
CREATE TABLE `exchange_favor_symbol`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `add_time` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '添加时间',
  `member_id` bigint(20) NULL DEFAULT NULL COMMENT '用户id',
  `symbol` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '交易对',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 61 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '自选交易对表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for exchange_order
-- ----------------------------
DROP TABLE IF EXISTS `exchange_order`;
CREATE TABLE `exchange_order`  (
  `order_id` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '订单id',
  `amount` decimal(32, 18) NULL DEFAULT 0.000000000000000000 COMMENT '数量',
  `base_symbol` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '结算币种',
  `canceled_time` bigint(20) NULL DEFAULT NULL COMMENT '撤单时间',
  `coin_symbol` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '交易币种',
  `completed_time` bigint(20) NULL DEFAULT NULL COMMENT '订单完成时间',
  `direction` int(11) NULL DEFAULT NULL COMMENT '方向（0买，1卖）',
  `margin_trade` int(11) NULL DEFAULT NULL COMMENT '是否来自杠杆交易',
  `member_id` bigint(20) NULL DEFAULT NULL COMMENT '用户id',
  `order_resource` int(11) NULL DEFAULT NULL COMMENT '交易来源（0机器人，1用户，2api）',
  `price` decimal(32, 18) NULL DEFAULT 0.000000000000000000 COMMENT '价格',
  `status` int(11) NULL DEFAULT NULL COMMENT '状态',
  `symbol` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '交易对',
  `time` bigint(20) NULL DEFAULT NULL COMMENT '挂单时间',
  `traded_amount` decimal(32, 18) NULL DEFAULT 0.000000000000000000 COMMENT '成交数量',
  `trigger_price` decimal(32, 18) NULL DEFAULT 0.000000000000000000 COMMENT '触发价格',
  `turnover` decimal(32, 18) NULL DEFAULT 0.000000000000000000 COMMENT '成交额',
  `type` int(11) NULL DEFAULT NULL COMMENT '类型',
  PRIMARY KEY (`order_id`) USING BTREE,
  INDEX `index_member_id_time`(`member_id`, `time`) USING BTREE,
  INDEX `index_exchange_order_query1`(`member_id`, `symbol`, `status`, `margin_trade`, `time`) USING BTREE,
  INDEX `index_member_direction`(`member_id`, `type`, `direction`, `status`, `time`) USING BTREE,
  INDEX `index__margin_trade_time`(`margin_trade`, `time`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '币币交易-订单表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for feedback
-- ----------------------------
DROP TABLE IF EXISTS `feedback`;
CREATE TABLE `feedback`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `create_time` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
  `remark` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '描述',
  `member_id` bigint(20) NOT NULL COMMENT '用户id',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `FK4isac0b94wvo61nndr3tpl4f5`(`member_id`) USING BTREE,
  CONSTRAINT `FK4isac0b94wvo61nndr3tpl4f5` FOREIGN KEY (`member_id`) REFERENCES `member` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '反馈' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for gift_config
-- ----------------------------
DROP TABLE IF EXISTS `gift_config`;
CREATE TABLE `gift_config`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `amount` decimal(19, 2) NULL DEFAULT NULL,
  `create_time` datetime(0) NULL DEFAULT NULL,
  `gift_coin` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `gift_name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `have_amount` decimal(19, 2) NULL DEFAULT NULL,
  `have_coin` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for gift_record
-- ----------------------------
DROP TABLE IF EXISTS `gift_record`;
CREATE TABLE `gift_record`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `create_time` datetime(0) NULL DEFAULT NULL,
  `gift_amount` decimal(19, 2) NULL DEFAULT NULL,
  `gift_coin` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `gift_name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `user_id` bigint(20) NULL DEFAULT NULL,
  `user_mobile` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `user_name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for hibernate_sequence
-- ----------------------------
DROP TABLE IF EXISTS `hibernate_sequence`;
CREATE TABLE `hibernate_sequence`  (
  `next_val` bigint(20) NULL DEFAULT NULL
) ENGINE = MyISAM CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Fixed;

-- ----------------------------
-- Table structure for hot_transfer_record
-- ----------------------------
DROP TABLE IF EXISTS `hot_transfer_record`;
CREATE TABLE `hot_transfer_record`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `admin_id` bigint(20) NULL DEFAULT NULL COMMENT '操作人id',
  `amount` decimal(32, 18) NULL DEFAULT 0.000000000000000000 COMMENT '数量',
  `balance` decimal(32, 18) NULL DEFAULT 0.000000000000000000 COMMENT '热钱包余额',
  `cold_address` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '冷钱包地址',
  `miner_fee` decimal(32, 18) NULL DEFAULT 0.000000000000000000 COMMENT '矿工费',
  `transaction_number` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '实际到账数量',
  `transfer_time` datetime(0) NULL DEFAULT NULL COMMENT '转账时间',
  `unit` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '币种单位',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 970 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '转入冷钱包记录' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for ieo_emption
-- ----------------------------
DROP TABLE IF EXISTS `ieo_emption`;
CREATE TABLE `ieo_emption`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `create_time` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
  `create_user` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '创建人id',
  `end_time` datetime(0) NULL DEFAULT NULL COMMENT '结束时间',
  `expect_time` datetime(0) NULL DEFAULT NULL COMMENT '预计上线时间',
  `fee` decimal(19, 2) NULL DEFAULT NULL COMMENT '手续费',
  `have_amount` decimal(19, 2) NULL DEFAULT NULL COMMENT '持有数量',
  `have_coin` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '持有币种',
  `ieo_name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT 'IEO名称',
  `limit_amount` decimal(19, 2) NULL DEFAULT NULL COMMENT '每人抢购限额',
  `pic` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '图片',
  `pic_view` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '缩略图',
  `raise_coin` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '募集币种',
  `ratio` decimal(19, 2) NULL DEFAULT NULL COMMENT '募集币种与发售币种的比率',
  `sale_amount` decimal(19, 2) NULL DEFAULT NULL COMMENT '发售总量',
  `sale_coin` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '发售币种',
  `sell_detail` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '项目详情',
  `sell_mode` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '售卖方式',
  `start_time` datetime(0) NULL DEFAULT NULL COMMENT '募集开始时间',
  `success_ratio` decimal(19, 2) NULL DEFAULT NULL COMMENT '抢购成功几率',
  `surplus_amount` decimal(19, 2) NULL DEFAULT NULL COMMENT '剩余量',
  `update_time` datetime(0) NULL DEFAULT NULL COMMENT '更新时间',
  `update_user` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for init_plate
-- ----------------------------
DROP TABLE IF EXISTS `init_plate`;
CREATE TABLE `init_plate`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `final_price` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `init_price` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `interference_factor` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `relative_time` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `symbol` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for integration_record
-- ----------------------------
DROP TABLE IF EXISTS `integration_record`;
CREATE TABLE `integration_record`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `amount` bigint(20) NULL DEFAULT NULL,
  `create_time` datetime(0) NULL DEFAULT NULL,
  `member_id` bigint(20) NULL DEFAULT NULL,
  `type` int(11) NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 361142 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for legal_wallet_recharge
-- ----------------------------
DROP TABLE IF EXISTS `legal_wallet_recharge`;
CREATE TABLE `legal_wallet_recharge`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `amount` decimal(32, 18) NOT NULL COMMENT '充值金额',
  `creation_time` datetime(0) NULL DEFAULT NULL,
  `deal_time` datetime(0) NULL DEFAULT NULL,
  `pay_mode` int(11) NOT NULL,
  `payment_instrument` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `remark` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `state` int(11) NOT NULL,
  `update_time` datetime(0) NULL DEFAULT NULL,
  `admin_id` bigint(20) NULL DEFAULT NULL,
  `coin_name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `member_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `FKtfjvrkn1oe0yu2tfjh6qcms73`(`admin_id`) USING BTREE,
  INDEX `FKsdtoqyvbjpd0bmw4n41ijc0kk`(`coin_name`) USING BTREE,
  INDEX `FKai5n52qqrho5axk2qkysvralw`(`member_id`) USING BTREE,
  CONSTRAINT `FKai5n52qqrho5axk2qkysvralw` FOREIGN KEY (`member_id`) REFERENCES `member` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `FKsdtoqyvbjpd0bmw4n41ijc0kk` FOREIGN KEY (`coin_name`) REFERENCES `coin` (`name`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `FKtfjvrkn1oe0yu2tfjh6qcms73` FOREIGN KEY (`admin_id`) REFERENCES `admin` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for legal_wallet_withdraw
-- ----------------------------
DROP TABLE IF EXISTS `legal_wallet_withdraw`;
CREATE TABLE `legal_wallet_withdraw`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `account` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `amount` decimal(32, 18) NULL DEFAULT NULL,
  `create_time` datetime(0) NULL DEFAULT NULL,
  `deal_time` datetime(0) NULL DEFAULT NULL,
  `pay_mode` int(11) NOT NULL,
  `payment_instrument` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `remark` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `remit_time` datetime(0) NULL DEFAULT NULL,
  `status` int(11) NULL DEFAULT NULL,
  `admin_id` bigint(20) NULL DEFAULT NULL,
  `coin_name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `member_id` bigint(20) NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `FKe95o0059kwsgmsxxv3amdb0d2`(`admin_id`) USING BTREE,
  INDEX `FKbilsav1ug8vjtn4ffghrlogqx`(`coin_name`) USING BTREE,
  INDEX `FKs7imtlo1l2pitedj9uueyh4rd`(`member_id`) USING BTREE,
  CONSTRAINT `FKbilsav1ug8vjtn4ffghrlogqx` FOREIGN KEY (`coin_name`) REFERENCES `coin` (`name`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `FKe95o0059kwsgmsxxv3amdb0d2` FOREIGN KEY (`admin_id`) REFERENCES `admin` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `FKs7imtlo1l2pitedj9uueyh4rd` FOREIGN KEY (`member_id`) REFERENCES `member` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for lever_coin
-- ----------------------------
DROP TABLE IF EXISTS `lever_coin`;
CREATE TABLE `lever_coin`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `base_symbol` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `coin_symbol` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `enable` int(11) NULL DEFAULT NULL,
  `interest_rate` decimal(32, 18) NULL DEFAULT NULL,
  `min_turn_into_amount` decimal(32, 18) NULL DEFAULT NULL,
  `min_turn_out_amount` decimal(32, 18) NULL DEFAULT NULL,
  `proportion` decimal(19, 2) NOT NULL,
  `sort` int(11) NOT NULL,
  `symbol` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for lever_wallet
-- ----------------------------
DROP TABLE IF EXISTS `lever_wallet`;
CREATE TABLE `lever_wallet`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `balance` decimal(32, 18) NULL DEFAULT NULL,
  `email` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `frozen_balance` decimal(32, 18) NULL DEFAULT NULL,
  `is_lock` int(11) NULL DEFAULT 0 COMMENT '钱包是否锁定',
  `member_id` bigint(20) NULL DEFAULT NULL,
  `member_name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `mobile_phone` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `status` int(11) NULL DEFAULT 0 COMMENT '是否处于爆仓状态',
  `version` int(11) NOT NULL,
  `coin_id` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `lever_coin_id` bigint(20) NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `FKq9qs85kkbu4q5my6e8higb7m1`(`coin_id`) USING BTREE,
  INDEX `FK9o9ybbe0ak4pgh9c6b3qeqqqf`(`lever_coin_id`) USING BTREE,
  CONSTRAINT `FK9o9ybbe0ak4pgh9c6b3qeqqqf` FOREIGN KEY (`lever_coin_id`) REFERENCES `lever_coin` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `FKq9qs85kkbu4q5my6e8higb7m1` FOREIGN KEY (`coin_id`) REFERENCES `coin` (`name`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for lever_wallet_transfer_record
-- ----------------------------
DROP TABLE IF EXISTS `lever_wallet_transfer_record`;
CREATE TABLE `lever_wallet_transfer_record`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `amount` decimal(32, 18) NULL DEFAULT NULL,
  `create_time` datetime(0) NULL DEFAULT NULL,
  `member_id` bigint(20) NULL DEFAULT NULL,
  `member_name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `type` int(11) NULL DEFAULT NULL,
  `version` int(11) NOT NULL,
  `coin_id` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `lever_coin_id` bigint(20) NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `FK790qm871imload2wh6f1rgplk`(`coin_id`) USING BTREE,
  INDEX `FKgofcyu74g536u214pae6oailu`(`lever_coin_id`) USING BTREE,
  CONSTRAINT `FK790qm871imload2wh6f1rgplk` FOREIGN KEY (`coin_id`) REFERENCES `coin` (`name`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `FKgofcyu74g536u214pae6oailu` FOREIGN KEY (`lever_coin_id`) REFERENCES `lever_coin` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for loan_record
-- ----------------------------
DROP TABLE IF EXISTS `loan_record`;
CREATE TABLE `loan_record`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `accumulative` decimal(32, 18) NULL DEFAULT NULL,
  `amount` decimal(32, 18) NULL DEFAULT NULL,
  `create_time` datetime(0) NULL DEFAULT NULL,
  `interest_rate` decimal(32, 18) NULL DEFAULT NULL,
  `loan_balance` decimal(32, 18) NULL DEFAULT NULL,
  `member_id` bigint(20) NULL DEFAULT NULL,
  `member_name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `repayment` int(11) NULL DEFAULT NULL,
  `version` int(11) NOT NULL,
  `coin_id` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `lever_coin_id` bigint(20) NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `FK3kdsljtrpiglp1f0habb96yjk`(`coin_id`) USING BTREE,
  INDEX `FKo6760jivsxxr7616790x2w04p`(`lever_coin_id`) USING BTREE,
  CONSTRAINT `FK3kdsljtrpiglp1f0habb96yjk` FOREIGN KEY (`coin_id`) REFERENCES `coin` (`name`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `FKo6760jivsxxr7616790x2w04p` FOREIGN KEY (`lever_coin_id`) REFERENCES `lever_coin` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for localization_extend
-- ----------------------------
DROP TABLE IF EXISTS `localization_extend`;
CREATE TABLE `localization_extend`  (
  `table_name` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '表名',
  `busi_key` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '业务主键',
  `column_name` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '列名',
  `locale` varchar(6) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '语种编码(ar_AE/en_US/ja_JP/ko_KR/zh_CN)',
  `content` text CHARACTER SET utf8 COLLATE utf8_general_ci NULL COMMENT '内容',
  PRIMARY KEY (`table_name`, `busi_key`, `column_name`, `locale`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '国际化扩展配置表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for lock_position_record
-- ----------------------------
DROP TABLE IF EXISTS `lock_position_record`;
CREATE TABLE `lock_position_record`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `amount` decimal(32, 18) NULL DEFAULT NULL,
  `create_time` datetime(0) NULL DEFAULT NULL,
  `member_id` bigint(20) NULL DEFAULT NULL,
  `member_name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `reason` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `status` int(11) NULL DEFAULT NULL,
  `unlock_time` datetime(0) NULL DEFAULT NULL,
  `wallet_id` bigint(20) NULL DEFAULT NULL,
  `coin_id` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `FKf2t2wbo5u4htn4qfbmfepu45v`(`coin_id`) USING BTREE,
  CONSTRAINT `FKf2t2wbo5u4htn4qfbmfepu45v` FOREIGN KEY (`coin_id`) REFERENCES `coin` (`name`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for loss_threshold
-- ----------------------------
DROP TABLE IF EXISTS `loss_threshold`;
CREATE TABLE `loss_threshold`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `create_time` datetime(0) NULL DEFAULT NULL,
  `perform_actions` int(11) NULL DEFAULT NULL,
  `status` int(11) NULL DEFAULT NULL,
  `threshold` decimal(19, 2) NULL DEFAULT NULL,
  `update_time` datetime(0) NULL DEFAULT NULL,
  `coin_id` bigint(20) NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `FKoa38gp4svpx11v6a42ph2h9ld`(`coin_id`) USING BTREE,
  CONSTRAINT `FKoa38gp4svpx11v6a42ph2h9ld` FOREIGN KEY (`coin_id`) REFERENCES `lever_coin` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for member
-- ----------------------------
DROP TABLE IF EXISTS `member`;
CREATE TABLE `member`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `ali_no` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `qr_code_url` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `appeal_success_times` int(11) NOT NULL,
  `appeal_times` int(11) NOT NULL,
  `application_time` datetime(0) NULL DEFAULT NULL,
  `avatar` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `bank` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `branch` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `card_no` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `certified_business_apply_time` datetime(0) NULL DEFAULT NULL,
  `certified_business_check_time` datetime(0) NULL DEFAULT NULL,
  `certified_business_status` int(11) NULL DEFAULT NULL,
  `channel_id` int(11) NULL DEFAULT 0,
  `email` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `first_level` int(11) NOT NULL,
  `generalize_total` bigint(20) NULL DEFAULT NULL,
  `google_date` datetime(0) NULL DEFAULT NULL,
  `google_key` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `google_state` int(11) NULL DEFAULT 0,
  `id_number` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `integration` bigint(20) NULL DEFAULT NULL,
  `inviter_id` bigint(20) NULL DEFAULT NULL,
  `inviter_parent_id` bigint(20) NULL DEFAULT NULL,
  `is_channel` int(11) NULL DEFAULT 0,
  `jy_password` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `kyc_status` int(11) NULL DEFAULT NULL,
  `last_login_time` datetime(0) NULL DEFAULT NULL,
  `city` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `country` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `district` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `province` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `login_count` int(11) NOT NULL,
  `login_lock` int(11) NULL DEFAULT NULL,
  `margin` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `member_grade_id` bigint(20) NULL DEFAULT NULL,
  `member_level` int(11) NULL DEFAULT NULL,
  `mobile_phone` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `password` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `promotion_code` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `publish_advertise` int(11) NULL DEFAULT NULL,
  `real_name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `real_name_status` int(11) NULL DEFAULT NULL,
  `registration_time` datetime(0) NULL DEFAULT NULL,
  `salt` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `second_level` int(11) NOT NULL,
  `sign_in_ability` bit(1) NOT NULL DEFAULT b'1',
  `status` int(11) NULL DEFAULT NULL,
  `third_level` int(11) NOT NULL,
  `token` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `token_expire_time` datetime(0) NULL DEFAULT NULL,
  `transaction_status` int(11) NULL DEFAULT NULL,
  `transaction_time` datetime(0) NULL DEFAULT NULL,
  `transactions` int(11) NOT NULL,
  `username` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `qr_we_code_url` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `wechat` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `local` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `se_fee_switch` bit(1) NULL DEFAULT b'0' COMMENT '是否使用SE抵扣手续费',
  `phone_state` int(11) NULL DEFAULT 0 COMMENT '是否开启手机号验证',
  `email_state` int(11) NULL DEFAULT 0 COMMENT '是否开启邮箱验证',
  `is_quick` int(11) NULL DEFAULT 0 COMMENT '是否开启快速提币',
  `email_remind` varchar(2) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT '0',
  `sms_remind` varchar(2) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT '0',
  `withdrawal_status` int(2) NULL DEFAULT 1,
  `token_web` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `token_web_expire_time` datetime(0) NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `UK_mbmcqelty0fbrvxp1q58dn57t`(`email`) USING BTREE,
  UNIQUE INDEX `UK_10ixebfiyeqolglpuye0qb49u`(`mobile_phone`) USING BTREE,
  INDEX `FKj3l2piwfqhx6egxivn08ae0iq`(`local`) USING BTREE,
  CONSTRAINT `FKj3l2piwfqhx6egxivn08ae0iq` FOREIGN KEY (`local`) REFERENCES `country` (`zh_name`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 119082 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for member_address
-- ----------------------------
DROP TABLE IF EXISTS `member_address`;
CREATE TABLE `member_address`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `address` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `create_time` datetime(0) NULL DEFAULT NULL,
  `delete_time` datetime(0) NULL DEFAULT NULL,
  `member_id` bigint(20) NULL DEFAULT NULL,
  `remark` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `status` int(11) NULL DEFAULT NULL,
  `coin_id` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `FKhcqqqntcf8hqmoa6dpo95okyh`(`coin_id`) USING BTREE,
  CONSTRAINT `FKhcqqqntcf8hqmoa6dpo95okyh` FOREIGN KEY (`coin_id`) REFERENCES `coin` (`name`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 80 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for member_api_key
-- ----------------------------
DROP TABLE IF EXISTS `member_api_key`;
CREATE TABLE `member_api_key`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `api_key` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `api_name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `bind_ip` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `create_time` datetime(0) NULL DEFAULT NULL,
  `expire_time` datetime(0) NULL DEFAULT NULL,
  `member_id` bigint(20) NULL DEFAULT NULL,
  `remark` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `secret_key` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `power_limit` varchar(10) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `status` varchar(2) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 28 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for member_application
-- ----------------------------
DROP TABLE IF EXISTS `member_application`;
CREATE TABLE `member_application`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `audit_status` int(11) NOT NULL,
  `create_time` datetime(0) NULL DEFAULT NULL,
  `id_card` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `identity_card_img_front` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `identity_card_img_in_hand` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `identity_card_img_reverse` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `kyc_status` int(11) NULL DEFAULT NULL,
  `real_name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `reject_reason` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `type` int(11) NULL DEFAULT 0 COMMENT '认证类型',
  `update_time` datetime(0) NULL DEFAULT NULL,
  `video_random` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `video_url` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `member_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `FKnacrm0xske06mwr5y51jq8euc`(`member_id`) USING BTREE,
  CONSTRAINT `FKnacrm0xske06mwr5y51jq8euc` FOREIGN KEY (`member_id`) REFERENCES `member` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 33 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for member_application_config
-- ----------------------------
DROP TABLE IF EXISTS `member_application_config`;
CREATE TABLE `member_application_config`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `promotion_on` int(11) NULL DEFAULT NULL,
  `recharge_coin_on` int(11) NULL DEFAULT NULL,
  `transaction_on` int(11) NULL DEFAULT NULL,
  `withdraw_coin_on` int(11) NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for member_bonus
-- ----------------------------
DROP TABLE IF EXISTS `member_bonus`;
CREATE TABLE `member_bonus`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `arrive_time` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `coin_id` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `have_time` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `mem_bouns` decimal(32, 18) NULL DEFAULT NULL,
  `member_id` bigint(20) NULL DEFAULT NULL,
  `total` decimal(32, 18) NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for member_deposit
-- ----------------------------
DROP TABLE IF EXISTS `member_deposit`;
CREATE TABLE `member_deposit`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `address` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `amount` decimal(32, 18) NULL DEFAULT 0.000000000000000000,
  `create_time` datetime(0) NULL DEFAULT NULL,
  `member_id` bigint(20) NULL DEFAULT NULL,
  `txid` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `unit` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `UKl2ibi99fuxplt8qt3rrpb0q4w`(`txid`, `address`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 408044 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for member_grade
-- ----------------------------
DROP TABLE IF EXISTS `member_grade`;
CREATE TABLE `member_grade`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `day_withdraw_count` int(11) NULL DEFAULT NULL,
  `exchange_fee_rate` decimal(12, 6) NULL DEFAULT 0.000000 COMMENT '吃单费率',
  `grade_bound` int(11) NULL DEFAULT NULL,
  `grade_code` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `grade_name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `withdraw_coin_amount` decimal(32, 18) NULL DEFAULT NULL,
  `exchange_maker_fee_rate` decimal(12, 6) NULL DEFAULT 0.000000 COMMENT '挂单费率',
  `otc_fee_rate` decimal(20, 8) NULL DEFAULT NULL,
  `se_discount_rate` decimal(12, 6) NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for member_level
-- ----------------------------
DROP TABLE IF EXISTS `member_level`;
CREATE TABLE `member_level`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `is_default` bit(1) NOT NULL,
  `name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `remark` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for member_login_record
-- ----------------------------
DROP TABLE IF EXISTS `member_login_record`;
CREATE TABLE `member_login_record`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `member_id` int(11) NULL DEFAULT NULL,
  `login_time` datetime(0) NULL DEFAULT NULL,
  `way` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `status` varchar(2) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `ip` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 309595 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for member_promotion
-- ----------------------------
DROP TABLE IF EXISTS `member_promotion`;
CREATE TABLE `member_promotion`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `invitees_id` bigint(20) NULL DEFAULT NULL,
  `inviter_id` bigint(20) NULL DEFAULT NULL,
  `level` int(11) NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 10 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for member_sign_record
-- ----------------------------
DROP TABLE IF EXISTS `member_sign_record`;
CREATE TABLE `member_sign_record`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `amount` decimal(32, 18) NULL DEFAULT NULL,
  `creation_time` datetime(0) NULL DEFAULT NULL,
  `coin_name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `member_id` bigint(20) NULL DEFAULT NULL,
  `sign_id` bigint(20) NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `FK7qa42qkaoqxlyvwhxwdstclic`(`coin_name`) USING BTREE,
  INDEX `FKfhpreg91d66wyhvls4awqyci9`(`member_id`) USING BTREE,
  INDEX `FKq1926wgosqk7ka4kvw8rtxew`(`sign_id`) USING BTREE,
  CONSTRAINT `FK7qa42qkaoqxlyvwhxwdstclic` FOREIGN KEY (`coin_name`) REFERENCES `coin` (`name`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `FKfhpreg91d66wyhvls4awqyci9` FOREIGN KEY (`member_id`) REFERENCES `member` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `FKq1926wgosqk7ka4kvw8rtxew` FOREIGN KEY (`sign_id`) REFERENCES `sign` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for member_transaction
-- ----------------------------
DROP TABLE IF EXISTS `member_transaction`;
CREATE TABLE `member_transaction`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `address` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `airdrop_id` bigint(20) NULL DEFAULT NULL,
  `amount` decimal(32, 18) NULL DEFAULT NULL,
  `create_time` datetime(0) NULL DEFAULT NULL,
  `fee` decimal(19, 8) NULL DEFAULT NULL,
  `fee_unit` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '手续费币种',
  `flag` int(11) NOT NULL DEFAULT 0,
  `member_id` bigint(20) NULL DEFAULT NULL,
  `symbol` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `type` int(11) NULL DEFAULT NULL,
  `txid` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `is_quick` int(11) NULL DEFAULT NULL COMMENT '是否快速充提币',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `transaction_memberId`(`member_id`) USING BTREE,
  INDEX `idx_transaction_query1`(`member_id`, `create_time`) USING BTREE,
  INDEX `idx_transaction_query2`(`member_id`, `type`, `create_time`) USING BTREE,
  INDEX `idx_transaction_query4`(`type`, `create_time`) USING BTREE,
  INDEX `idx_transaction_query3`(`member_id`, `type`, `symbol`, `amount`, `create_time`) USING BTREE,
  INDEX `idx_transaction_query5`(`member_id`, `type`, `symbol`, `create_time`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 21955441 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for member_verify_record
-- ----------------------------
DROP TABLE IF EXISTS `member_verify_record`;
CREATE TABLE `member_verify_record`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `member_id` int(11) NULL DEFAULT NULL,
  `create_time` datetime(0) NULL DEFAULT NULL,
  `type` varchar(2) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `ip` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 17 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for member_wallet
-- ----------------------------
DROP TABLE IF EXISTS `member_wallet`;
CREATE TABLE `member_wallet`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `address` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `balance` decimal(32, 18) NULL DEFAULT NULL,
  `frozen_balance` decimal(32, 18) NULL DEFAULT NULL,
  `is_lock` int(11) NULL DEFAULT 0 COMMENT '钱包不是锁定',
  `member_id` bigint(20) NULL DEFAULT NULL,
  `release_balance` decimal(32, 18) NULL DEFAULT NULL,
  `version` int(11) NOT NULL,
  `coin_id` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `UKm68bscpof0bpnxocxl4qdnvbe`(`member_id`, `coin_id`) USING BTREE,
  INDEX `FKf9tgbp9y9py8t9c5xj0lllcib`(`coin_id`) USING BTREE,
  CONSTRAINT `FKf9tgbp9y9py8t9c5xj0lllcib` FOREIGN KEY (`coin_id`) REFERENCES `coin` (`name`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 420174 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for member_wallet_se_history
-- ----------------------------
DROP TABLE IF EXISTS `member_wallet_se_history`;
CREATE TABLE `member_wallet_se_history`  (
  `balance` decimal(32, 18) NULL DEFAULT NULL,
  `frozen_balance` decimal(32, 18) NULL DEFAULT NULL,
  `member_id` bigint(20) NOT NULL,
  `release_balance` decimal(32, 18) NULL DEFAULT NULL,
  `create_time` datetime(0) NOT NULL,
  `id` int(11) NOT NULL AUTO_INCREMENT,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `Primary_key`(`member_id`, `create_time`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 936593 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for member_wallet_history
-- ----------------------------
DROP TABLE IF EXISTS `member_wallet_history`;
CREATE TABLE `member_wallet_history`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `member_id` int(11) NOT NULL,
  `coin_id` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `before_balance` decimal(32, 18) NOT NULL,
  `after_balance` decimal(32, 18) NULL DEFAULT NULL,
  `before_frozen_balance` decimal(32, 18) NULL DEFAULT 0.000000000000000000,
  `after_frozen_balance` decimal(32, 18) NULL DEFAULT 0.000000000000000000,
  `op_time` datetime(0) NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 34974930 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for member_wallet_relation
-- ----------------------------
DROP TABLE IF EXISTS `member_wallet_relation`;
CREATE TABLE `member_wallet_relation`  (
  `coin_key` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '唯一键',
  `address` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '地址',
  `member_id` bigint(20) NOT NULL COMMENT '用户ID',
  `coin_id` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '币种',
  PRIMARY KEY (`coin_key`, `address`) USING BTREE,
  INDEX `idx_member_wallet_relation_1`(`member_id`, `coin_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for migrate_balance
-- ----------------------------
DROP TABLE IF EXISTS `migrate_balance`;
CREATE TABLE `migrate_balance`  (
  `foreign_id` bigint(11) NOT NULL,
  `token_id` varchar(6) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `token_name` varchar(12) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `total` decimal(32, 18) NULL DEFAULT NULL,
  `free` decimal(32, 18) NULL DEFAULT NULL,
  `locked` decimal(32, 18) NULL DEFAULT NULL,
  `position` decimal(32, 18) NULL DEFAULT NULL,
  PRIMARY KEY (`foreign_id`, `token_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for migrate_member
-- ----------------------------
DROP TABLE IF EXISTS `migrate_member`;
CREATE TABLE `migrate_member`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `ali_no` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `qr_code_url` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `appeal_success_times` int(11) NOT NULL,
  `appeal_times` int(11) NOT NULL,
  `application_time` datetime(0) NULL DEFAULT NULL,
  `avatar` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `bank` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `branch` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `card_no` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `certified_business_apply_time` datetime(0) NULL DEFAULT NULL,
  `certified_business_check_time` datetime(0) NULL DEFAULT NULL,
  `certified_business_status` int(11) NULL DEFAULT NULL,
  `channel_id` int(11) NULL DEFAULT 0,
  `email` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `first_level` int(11) NOT NULL,
  `generalize_total` bigint(20) NULL DEFAULT NULL,
  `google_date` datetime(0) NULL DEFAULT NULL,
  `google_key` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `google_state` int(11) NULL DEFAULT 0,
  `id_number` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `integration` bigint(20) NULL DEFAULT NULL,
  `inviter_id` bigint(20) NULL DEFAULT NULL,
  `inviter_parent_id` bigint(20) NULL DEFAULT NULL,
  `is_channel` int(11) NULL DEFAULT 0,
  `jy_password` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `kyc_status` int(11) NULL DEFAULT NULL,
  `last_login_time` datetime(0) NULL DEFAULT NULL,
  `city` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `country` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `district` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `province` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `login_count` int(11) NOT NULL,
  `login_lock` int(11) NULL DEFAULT NULL,
  `margin` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `member_grade_id` bigint(20) NULL DEFAULT NULL,
  `member_level` int(11) NULL DEFAULT NULL,
  `mobile_phone` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `password` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `promotion_code` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `publish_advertise` int(11) NULL DEFAULT NULL,
  `real_name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `real_name_status` int(11) NULL DEFAULT NULL,
  `registration_time` datetime(0) NULL DEFAULT NULL,
  `salt` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `second_level` int(11) NOT NULL,
  `sign_in_ability` bit(1) NOT NULL DEFAULT b'1',
  `status` int(11) NULL DEFAULT NULL,
  `third_level` int(11) NOT NULL,
  `token` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `token_expire_time` datetime(0) NULL DEFAULT NULL,
  `transaction_status` int(11) NULL DEFAULT NULL,
  `transaction_time` datetime(0) NULL DEFAULT NULL,
  `transactions` int(11) NOT NULL,
  `username` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `qr_we_code_url` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `wechat` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `local` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `se_fee_switch` bit(1) NULL DEFAULT b'0' COMMENT '是否使用SE抵扣手续费',
  `phone_state` int(11) NULL DEFAULT 0 COMMENT '是否开启手机号验证',
  `email_state` int(11) NULL DEFAULT 0 COMMENT '是否开启邮箱验证',
  `is_quick` int(11) NULL DEFAULT 0 COMMENT '是否开启快速提币',
  `foreign_id` bigint(11) NOT NULL COMMENT '外部ID',
  `foreign_inviter_id` bigint(11) NULL DEFAULT NULL COMMENT '外部上级邀请人ID',
  `foreign_inviter_parent_id` bigint(11) NULL DEFAULT NULL COMMENT '外部上上级邀请人ID',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `UK_migrate_member_foreign_id`(`foreign_id`) USING BTREE,
  UNIQUE INDEX `UK2_mbmcqelty0fbrvxp1q58dn57t`(`email`) USING BTREE,
  UNIQUE INDEX `UK2_10ixebfiyeqolglpuye0qb49u`(`mobile_phone`) USING BTREE,
  INDEX `FK2_j3l2piwfqhx6egxivn08ae0iq`(`local`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 18045 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for navigation
-- ----------------------------
DROP TABLE IF EXISTS `navigation`;
CREATE TABLE `navigation`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `create_time` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
  `url` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '图片地址',
  `sort` int(11) NOT NULL,
  `type` int(11) NULL DEFAULT NULL,
  `status` int(11) NOT NULL COMMENT '状态',
  `title` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '标题',
  `locale` varchar(6) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '语种（\"en-US\", \"zh-CN\", \"ja-JP\", \"ko-KR\", \"ar-AE\"）',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 55 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for otc_coin
-- ----------------------------
DROP TABLE IF EXISTS `otc_coin`;
CREATE TABLE `otc_coin`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `buy_min_amount` decimal(32, 18) NULL DEFAULT NULL COMMENT '买入广告最低发布数量',
  `coin_scale` int(11) NULL DEFAULT 8 COMMENT '币种精度',
  `is_platform_coin` int(11) NULL DEFAULT NULL,
  `jy_rate` decimal(18, 6) NULL DEFAULT NULL COMMENT '交易手续费率',
  `max_trading_time` int(11) NULL DEFAULT 0 COMMENT '广告上架后自动下架时间，单位为秒，0表示不过期',
  `max_volume` int(11) NULL DEFAULT 0 COMMENT '最大挂单数量，0表示不限制',
  `name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `name_cn` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `sell_min_amount` decimal(32, 18) NULL DEFAULT NULL COMMENT '卖出广告最低发布数量',
  `sort` int(11) NOT NULL,
  `status` int(11) NULL DEFAULT NULL,
  `unit` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 12 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for otc_order
-- ----------------------------
DROP TABLE IF EXISTS `otc_order`;
CREATE TABLE `otc_order`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `advertise_id` bigint(20) NOT NULL,
  `advertise_type` int(11) NOT NULL,
  `ali_no` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `qr_code_url` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `bank` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `branch` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `card_no` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `cancel_time` datetime(0) NULL DEFAULT NULL,
  `commission` decimal(32, 18) NULL DEFAULT NULL COMMENT '手续费',
  `country` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `create_time` datetime(0) NULL DEFAULT NULL,
  `customer_id` bigint(20) NOT NULL,
  `customer_name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `customer_real_name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `max_limit` decimal(18, 2) NULL DEFAULT NULL COMMENT '最高交易额',
  `member_id` bigint(20) NOT NULL,
  `member_name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `member_real_name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `min_limit` decimal(18, 2) NULL DEFAULT NULL COMMENT '最低交易额',
  `money` decimal(18, 2) NULL DEFAULT NULL COMMENT '交易金额',
  `number` decimal(32, 18) NULL DEFAULT NULL COMMENT '交易数量',
  `order_sn` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `pay_mode` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `pay_time` datetime(0) NULL DEFAULT NULL,
  `price` decimal(18, 2) NULL DEFAULT NULL COMMENT '价格',
  `reference_number` varchar(16) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT '' COMMENT '付款参考号',
  `release_time` datetime(0) NULL DEFAULT NULL,
  `remark` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `status` int(11) NULL DEFAULT NULL,
  `time_limit` int(11) NULL DEFAULT NULL,
  `version` bigint(20) NULL DEFAULT NULL,
  `qr_we_code_url` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `wechat` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `coin_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `UK_qmfpakgu6mowmslv4m5iy43t9`(`order_sn`) USING BTREE,
  INDEX `FKjh47nnmiehmu15wqjfwnh8a6u`(`coin_id`) USING BTREE,
  CONSTRAINT `FKjh47nnmiehmu15wqjfwnh8a6u` FOREIGN KEY (`coin_id`) REFERENCES `otc_coin` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 5543 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for otc_wallet
-- ----------------------------
DROP TABLE IF EXISTS `otc_wallet`;
CREATE TABLE `otc_wallet`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `balance` decimal(32, 18) NULL DEFAULT NULL,
  `frozen_balance` decimal(32, 18) NULL DEFAULT NULL,
  `is_lock` int(11) NULL DEFAULT NULL,
  `member_id` bigint(20) NULL DEFAULT NULL,
  `release_balance` decimal(32, 18) NULL DEFAULT NULL,
  `version` int(11) NULL DEFAULT NULL,
  `coin_id` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `FKcy68pru628qy2ker8klalb5j7`(`coin_id`) USING BTREE,
  CONSTRAINT `FKcy68pru628qy2ker8klalb5j7` FOREIGN KEY (`coin_id`) REFERENCES `coin` (`name`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 178 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for payment_history
-- ----------------------------
DROP TABLE IF EXISTS `payment_history`;
CREATE TABLE `payment_history`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `amount` decimal(32, 18) NULL DEFAULT NULL,
  `create_time` datetime(0) NULL DEFAULT NULL,
  `interest` decimal(32, 18) NULL DEFAULT NULL,
  `interest_rate` decimal(32, 18) NULL DEFAULT NULL,
  `loan_record_id` bigint(20) NULL DEFAULT NULL,
  `member_id` bigint(20) NULL DEFAULT NULL,
  `payment_type` int(11) NULL DEFAULT NULL,
  `principal` decimal(32, 18) NULL DEFAULT NULL,
  `status` int(11) NULL DEFAULT NULL,
  `coin_id` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `lever_coin_id` bigint(20) NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `FKqfy2j75nya39clfyban0tt2ss`(`coin_id`) USING BTREE,
  INDEX `FKo1ixtge087s4m0rjl50o5d24j`(`lever_coin_id`) USING BTREE,
  CONSTRAINT `FKo1ixtge087s4m0rjl50o5d24j` FOREIGN KEY (`lever_coin_id`) REFERENCES `lever_coin` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `FKqfy2j75nya39clfyban0tt2ss` FOREIGN KEY (`coin_id`) REFERENCES `coin` (`name`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for platform_transaction
-- ----------------------------
DROP TABLE IF EXISTS `platform_transaction`;
CREATE TABLE `platform_transaction`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `amount` decimal(32, 18) NULL DEFAULT 0.000000000000000000,
  `biz_order_id` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `day` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `direction` int(11) NOT NULL,
  `time` datetime(0) NULL DEFAULT NULL,
  `type` int(11) NOT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 54 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for poster
-- ----------------------------
DROP TABLE IF EXISTS `poster`;
CREATE TABLE `poster`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `img` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '图片url',
  `remark` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '描述',
  `status` varchar(2) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '状态',
  `locale` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '语种',
  `create_time` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `sort` int(11) NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 661 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for poundage_convert_eth
-- ----------------------------
DROP TABLE IF EXISTS `poundage_convert_eth`;
CREATE TABLE `poundage_convert_eth`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `coin_id` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `direction` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `eth_usdt_rate` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `exchange_order_id` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `member_id` bigint(20) NULL DEFAULT NULL,
  `mine_amount` decimal(32, 18) NULL DEFAULT 0.000000000000000000,
  `poundage_amount` decimal(32, 18) NULL DEFAULT 0.000000000000000000,
  `poundage_amount_eth` decimal(32, 18) NULL DEFAULT 0.000000000000000000,
  `symbol` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `transaction_time` datetime(0) NULL DEFAULT NULL,
  `type` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `usdt_rate` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for pre_coin
-- ----------------------------
DROP TABLE IF EXISTS `pre_coin`;
CREATE TABLE `pre_coin`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `amount` int(11) NOT NULL,
  `link` varchar(256) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '详情链接',
  `name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `remark` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `unit` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `version` bigint(20) NOT NULL,
  `vote_id` bigint(20) NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `FK7o7qmhrc4n2fvyl1mf5k1lhtw`(`vote_id`) USING BTREE,
  CONSTRAINT `FK7o7qmhrc4n2fvyl1mf5k1lhtw` FOREIGN KEY (`vote_id`) REFERENCES `vote` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for release_balance
-- ----------------------------
DROP TABLE IF EXISTS `release_balance`;
CREATE TABLE `release_balance`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `coin_name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `coin_unit` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `email` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `member_id` bigint(20) NULL DEFAULT NULL,
  `phone` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `register_time` datetime(0) NULL DEFAULT NULL,
  `release_balance` decimal(19, 2) NULL DEFAULT NULL,
  `release_state` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `release_time` datetime(0) NULL DEFAULT NULL,
  `user_name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 32 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for reward_activity_setting
-- ----------------------------
DROP TABLE IF EXISTS `reward_activity_setting`;
CREATE TABLE `reward_activity_setting`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `info` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `status` int(11) NULL DEFAULT NULL,
  `type` int(11) NULL DEFAULT NULL,
  `update_time` datetime(0) NULL DEFAULT NULL,
  `admin_id` bigint(20) NULL DEFAULT NULL,
  `coin_id` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `FKra9w7qwgbxti55cmkb6kycau7`(`admin_id`) USING BTREE,
  INDEX `FKmxq57nrqt4lb9lqpxwc095h1h`(`coin_id`) USING BTREE,
  CONSTRAINT `FKmxq57nrqt4lb9lqpxwc095h1h` FOREIGN KEY (`coin_id`) REFERENCES `coin` (`name`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `FKra9w7qwgbxti55cmkb6kycau7` FOREIGN KEY (`admin_id`) REFERENCES `admin` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for reward_promotion_setting
-- ----------------------------
DROP TABLE IF EXISTS `reward_promotion_setting`;
CREATE TABLE `reward_promotion_setting`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `effective_time` int(11) NOT NULL,
  `info` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `status` int(11) NULL DEFAULT NULL,
  `type` int(11) NULL DEFAULT NULL,
  `update_time` datetime(0) NULL DEFAULT NULL,
  `admin_id` bigint(20) NULL DEFAULT NULL,
  `coin_id` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `FK7fl96plmj12crmepem7t876u3`(`admin_id`) USING BTREE,
  INDEX `FKfhtlsn9g8lj5qecbo596ymhey`(`coin_id`) USING BTREE,
  CONSTRAINT `FK7fl96plmj12crmepem7t876u3` FOREIGN KEY (`admin_id`) REFERENCES `admin` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `FKfhtlsn9g8lj5qecbo596ymhey` FOREIGN KEY (`coin_id`) REFERENCES `coin` (`name`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for reward_record
-- ----------------------------
DROP TABLE IF EXISTS `reward_record`;
CREATE TABLE `reward_record`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `amount` decimal(32, 18) NULL DEFAULT NULL,
  `create_time` datetime(0) NULL DEFAULT NULL,
  `remark` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `type` int(11) NULL DEFAULT NULL,
  `coin_id` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `member_id` bigint(20) NOT NULL,
  `order_id` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `order_member_id` bigint(20) NULL DEFAULT NULL,
  `usdt_value` decimal(20, 0) NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `FKtm2ha75hh60am8n7lco838nmo`(`coin_id`) USING BTREE,
  INDEX `FKm1fpenr8jj71bk2w7q92lm80n`(`member_id`) USING BTREE,
  INDEX `idx_reward_record_query1`(`type`, `member_id`, `coin_id`, `create_time`, `amount`) USING BTREE,
  CONSTRAINT `FKm1fpenr8jj71bk2w7q92lm80n` FOREIGN KEY (`member_id`) REFERENCES `member` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `FKtm2ha75hh60am8n7lco838nmo` FOREIGN KEY (`coin_id`) REFERENCES `coin` (`name`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 3681972 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for reward_record_sum
-- ----------------------------
DROP TABLE IF EXISTS `reward_record_sum`;
CREATE TABLE `reward_record_sum`  (
  `sum_date` varchar(32) CHARACTER SET latin1 COLLATE latin1_swedish_ci NOT NULL,
  `member_id` bigint(20) NOT NULL,
  `coin_id` varchar(255) CHARACTER SET latin1 COLLATE latin1_swedish_ci NOT NULL,
  `type` int(11) NOT NULL,
  `amount` decimal(32, 18) NULL DEFAULT NULL,
  PRIMARY KEY (`sum_date`, `member_id`, `coin_id`, `type`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = latin1 COLLATE = latin1_swedish_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for reward_wallet
-- ----------------------------
DROP TABLE IF EXISTS `reward_wallet`;
CREATE TABLE `reward_wallet`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `balance` decimal(32, 18) NULL DEFAULT NULL,
  `coin_unit` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `frozen_balance` decimal(32, 18) NULL DEFAULT NULL,
  `is_lock` int(11) NULL DEFAULT 0 COMMENT '钱包不是锁定',
  `member_id` bigint(20) NULL DEFAULT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for risk_record
-- ----------------------------
DROP TABLE IF EXISTS `risk_record`;
CREATE TABLE `risk_record`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `create_time` datetime(0) NULL DEFAULT NULL,
  `current_threshold` decimal(19, 2) NULL DEFAULT NULL,
  `member_id` bigint(20) NULL DEFAULT NULL,
  `member_name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `perform_actions` int(11) NULL DEFAULT NULL,
  `lever_coin_id` bigint(20) NULL DEFAULT NULL,
  `loss_threshold_id` bigint(20) NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `FKg61lc7c35ro0snj1le341yg1c`(`lever_coin_id`) USING BTREE,
  INDEX `FKk8u66l4rub7e8hux5yn1an3vo`(`loss_threshold_id`) USING BTREE,
  CONSTRAINT `FKg61lc7c35ro0snj1le341yg1c` FOREIGN KEY (`lever_coin_id`) REFERENCES `lever_coin` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `FKk8u66l4rub7e8hux5yn1an3vo` FOREIGN KEY (`loss_threshold_id`) REFERENCES `loss_threshold` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for robot_transaction
-- ----------------------------
DROP TABLE IF EXISTS `robot_transaction`;
CREATE TABLE `robot_transaction`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `amount` decimal(32, 18) NULL DEFAULT NULL,
  `create_time` datetime(0) NULL DEFAULT NULL,
  `fee` decimal(32, 18) NULL DEFAULT NULL,
  `member_id` bigint(20) NULL DEFAULT NULL,
  `symbol` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `type` int(11) NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for settlement_coin
-- ----------------------------
DROP TABLE IF EXISTS `settlement_coin`;
CREATE TABLE `settlement_coin`  (
  `coin_name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '币种',
  `status` int(11) NULL DEFAULT NULL COMMENT '状态',
  `sort` int(11) NULL DEFAULT NULL COMMENT '排序',
  `create_time` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`coin_name`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sign
-- ----------------------------
DROP TABLE IF EXISTS `sign`;
CREATE TABLE `sign`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `amount` decimal(32, 18) NULL DEFAULT NULL,
  `creation_time` datetime(0) NULL DEFAULT NULL,
  `end_date` date NULL DEFAULT NULL,
  `status` int(11) NULL DEFAULT NULL,
  `coin_name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `FK58kn2gi7nvswa8hb76h87wtdl`(`coin_name`) USING BTREE,
  CONSTRAINT `FK58kn2gi7nvswa8hb76h87wtdl` FOREIGN KEY (`coin_name`) REFERENCES `coin` (`name`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for statistics_exchange
-- ----------------------------
DROP TABLE IF EXISTS `statistics_exchange`;
CREATE TABLE `statistics_exchange`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `date_` date NOT NULL,
  `base_symbol` varchar(20) CHARACTER SET latin1 COLLATE latin1_swedish_ci NULL DEFAULT NULL,
  `coin_symbol` varchar(20) CHARACTER SET latin1 COLLATE latin1_swedish_ci NULL DEFAULT NULL,
  `amount` decimal(32, 18) NULL DEFAULT NULL,
  `money` decimal(32, 18) NULL DEFAULT NULL,
  `money_usd` decimal(32, 18) NULL DEFAULT NULL COMMENT '交易额，折算出USDT',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0),
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `statistics_exchange_date__base_symbol_coin_symbol_uindex`(`date_`, `base_symbol`, `coin_symbol`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 876 CHARACTER SET = latin1 COLLATE = latin1_swedish_ci COMMENT = '币币交易统计信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for statistics_otc
-- ----------------------------
DROP TABLE IF EXISTS `statistics_otc`;
CREATE TABLE `statistics_otc`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `date_` date NOT NULL,
  `unit` varchar(20) CHARACTER SET latin1 COLLATE latin1_swedish_ci NULL DEFAULT NULL,
  `amount` decimal(32, 18) NULL DEFAULT NULL,
  `money` decimal(32, 18) NULL DEFAULT NULL,
  `fee` decimal(32, 18) NULL DEFAULT NULL COMMENT '手续费',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0),
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `statistics_otc_date__unit_uindex`(`date_`, `unit`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 48 CHARACTER SET = latin1 COLLATE = latin1_swedish_ci COMMENT = '法币交易统计信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for statistics_recharge
-- ----------------------------
DROP TABLE IF EXISTS `statistics_recharge`;
CREATE TABLE `statistics_recharge`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `date_` date NOT NULL COMMENT '日期',
  `currency` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '币种',
  `people_count` int(11) NULL DEFAULT NULL COMMENT '充币人数',
  `recharge_count` int(11) NULL DEFAULT NULL COMMENT '充币笔数',
  `recharge_amount` decimal(20, 8) NULL DEFAULT NULL COMMENT '充值金额',
  `create_time` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `statistics_recharge_date__currency_uindex`(`date_`, `currency`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 829 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '数据统计-充币统计' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for statistics_register
-- ----------------------------
DROP TABLE IF EXISTS `statistics_register`;
CREATE TABLE `statistics_register`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `date_` date NOT NULL COMMENT '日期',
  `total_count` int(11) NULL DEFAULT NULL COMMENT '注册总数',
  `self_count` int(11) NULL DEFAULT NULL COMMENT '自助注册总数',
  `invited_count` int(11) NULL DEFAULT NULL COMMENT '邀请注册总数',
  `indirect_count` int(11) NULL DEFAULT NULL COMMENT '间接邀请注册总数',
  `create_time` datetime(0) NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `statistics_register_date_str_uindex`(`date_`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 28 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '数据统计-注册统计表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for statistics_service_fee
-- ----------------------------
DROP TABLE IF EXISTS `statistics_service_fee`;
CREATE TABLE `statistics_service_fee`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `date_` date NOT NULL COMMENT '日期',
  `currency` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '币种',
  `coin_fee` decimal(20, 8) NULL DEFAULT NULL COMMENT '币币交易手续费',
  `legal_fee` decimal(20, 8) NULL DEFAULT NULL COMMENT '法币交易手续费',
  `create_time` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `statistics_fee_currency_date__uindex`(`date_`, `currency`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1542 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '数据统计-手续费统计' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for statistics_transaction
-- ----------------------------
DROP TABLE IF EXISTS `statistics_transaction`;
CREATE TABLE `statistics_transaction`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `date_` date NOT NULL COMMENT '日期',
  `symbol` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '交易对',
  `people_count` int(11) NULL DEFAULT NULL COMMENT '交易人数',
  `purchase_people_count` int(11) NULL DEFAULT NULL COMMENT '买入人数',
  `sell_people_count` int(11) NULL DEFAULT NULL COMMENT '卖出人数',
  `transaction_count` int(11) NULL DEFAULT NULL COMMENT '交易笔数',
  `tx_purchase_count` int(11) NULL DEFAULT NULL COMMENT '买入笔数',
  `tx_sell_count` int(11) NULL DEFAULT NULL COMMENT '卖出笔数',
  `transaction_amount` decimal(20, 8) NULL DEFAULT NULL COMMENT '交易量',
  `create_time` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `statistics_recharge_date__uindex`(`date_`, `symbol`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1317 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '数据统计-交易统计' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for statistics_withdraw
-- ----------------------------
DROP TABLE IF EXISTS `statistics_withdraw`;
CREATE TABLE `statistics_withdraw`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `date_` date NOT NULL COMMENT '日期',
  `currency` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '币种',
  `people_count` int(11) NULL DEFAULT NULL COMMENT '提币人数',
  `recharge_count` int(11) NULL DEFAULT NULL COMMENT '提币笔数',
  `recharge_amount` decimal(20, 8) NULL DEFAULT NULL COMMENT '提币金额',
  `create_time` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `statistics_withdraw_date__uindex`(`date_`, `currency`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 833 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '数据统计-提币统计' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sys_advertise
-- ----------------------------
DROP TABLE IF EXISTS `sys_advertise`;
CREATE TABLE `sys_advertise`  (
  `serial_number` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `author` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '作者',
  `content` text CHARACTER SET utf8 COLLATE utf8_general_ci NULL COMMENT '内容',
  `create_time` datetime(0) NULL DEFAULT NULL,
  `end_time` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '结束时间',
  `link_url` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '系统广告名称',
  `remark` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '备注',
  `sort` int(11) NOT NULL COMMENT '排序',
  `start_time` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '开始时间',
  `status` int(11) NOT NULL COMMENT '状态',
  `sys_advertise_location` int(11) NOT NULL COMMENT '系统广告位置 0、app首页轮播 1、pc首页轮播 2、pc分类广告',
  `url` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `locale` varchar(6) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '语种（\"en-US\", \"zh-CN\", \"ja-JP\", \"ko-KR\", \"ar-AE\"）',
  PRIMARY KEY (`serial_number`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sys_help
-- ----------------------------
DROP TABLE IF EXISTS `sys_help`;
CREATE TABLE `sys_help`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `author` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `content` text CHARACTER SET utf8 COLLATE utf8_general_ci NULL COMMENT '内容',
  `create_time` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
  `img_url` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '图片地址',
  `is_top` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `sort` int(11) NOT NULL,
  `status` int(11) NOT NULL COMMENT '状态',
  `sys_help_classification` int(11) NOT NULL COMMENT '分类',
  `title` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '标题',
  `locale` varchar(6) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '语种（\"en-US\", \"zh-CN\", \"ja-JP\", \"ko-KR\", \"ar-AE\"）',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sys_localization
-- ----------------------------
DROP TABLE IF EXISTS `sys_localization`;
CREATE TABLE `sys_localization`  (
  `id` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT 'ID',
  `locale` varchar(6) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '语种编码(ar_AE/en_US/ja_JP/ko_KR/zh_CN)',
  `content` text CHARACTER SET utf8 COLLATE utf8_general_ci NULL COMMENT '内容',
  PRIMARY KEY (`id`, `locale`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '国际化配置表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for tb_sms
-- ----------------------------
DROP TABLE IF EXISTS `tb_sms`;
CREATE TABLE `tb_sms`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `key_id` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `key_secret` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `sign_id` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `sms_name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `sms_status` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `template_id` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for transfer_address
-- ----------------------------
DROP TABLE IF EXISTS `transfer_address`;
CREATE TABLE `transfer_address`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `address` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `min_amount` decimal(32, 18) NULL DEFAULT NULL COMMENT '最低转账数目',
  `status` int(11) NULL DEFAULT NULL,
  `transfer_fee` decimal(12, 6) NULL DEFAULT NULL COMMENT '转账手续费率',
  `coin_id` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `FK7iv0cmmj36ncaw1nx92x15vtu`(`coin_id`) USING BTREE,
  CONSTRAINT `FK7iv0cmmj36ncaw1nx92x15vtu` FOREIGN KEY (`coin_id`) REFERENCES `coin` (`name`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for transfer_record
-- ----------------------------
DROP TABLE IF EXISTS `transfer_record`;
CREATE TABLE `transfer_record`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `address` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `amount` decimal(32, 18) NULL DEFAULT NULL,
  `create_time` datetime(0) NULL DEFAULT NULL,
  `fee` decimal(32, 18) NULL DEFAULT NULL COMMENT '手续费',
  `member_id` bigint(20) NULL DEFAULT NULL,
  `order_sn` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `remark` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `coin_id` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `FKkrlf3bglmf2c51sorq9fpue0g`(`coin_id`) USING BTREE,
  CONSTRAINT `FKkrlf3bglmf2c51sorq9fpue0g` FOREIGN KEY (`coin_id`) REFERENCES `coin` (`name`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for vote
-- ----------------------------
DROP TABLE IF EXISTS `vote`;
CREATE TABLE `vote`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `amount` decimal(18, 2) NULL DEFAULT NULL COMMENT '每次投票消耗的平台币数量',
  `create_time` datetime(0) NULL DEFAULT NULL,
  `status` int(11) NULL DEFAULT NULL,
  `vote_limit` int(11) NOT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for vote_detail
-- ----------------------------
DROP TABLE IF EXISTS `vote_detail`;
CREATE TABLE `vote_detail`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `amount` decimal(19, 2) NULL DEFAULT NULL,
  `create_time` datetime(0) NULL DEFAULT NULL,
  `user_id` bigint(20) NULL DEFAULT NULL,
  `vote_amount` int(11) NOT NULL,
  `pre_coin_id` bigint(20) NULL DEFAULT NULL,
  `vote_id` bigint(20) NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `FKpe45amd6s8di5y2x7tf8ir6vo`(`pre_coin_id`) USING BTREE,
  INDEX `FKrdttjtg7msjaguxmnv0ntf5yt`(`vote_id`) USING BTREE,
  CONSTRAINT `FKpe45amd6s8di5y2x7tf8ir6vo` FOREIGN KEY (`pre_coin_id`) REFERENCES `pre_coin` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `FKrdttjtg7msjaguxmnv0ntf5yt` FOREIGN KEY (`vote_id`) REFERENCES `vote` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for website_information
-- ----------------------------
DROP TABLE IF EXISTS `website_information`;
CREATE TABLE `website_information`  (
  `id` bigint(20) NOT NULL,
  `address_icon` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `contact` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `copyright` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `description` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `keywords` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `logo` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `other_information` text CHARACTER SET utf8 COLLATE utf8_general_ci NULL,
  `postcode` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `url` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for withdraw_record
-- ----------------------------
DROP TABLE IF EXISTS `withdraw_record`;
CREATE TABLE `withdraw_record`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `address` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `arrived_amount` decimal(32, 18) NULL DEFAULT NULL,
  `can_auto_withdraw` int(11) NULL DEFAULT NULL,
  `create_time` datetime(0) NULL DEFAULT NULL,
  `deal_time` datetime(0) NULL DEFAULT NULL,
  `fee` decimal(32, 18) NULL DEFAULT NULL,
  `is_auto` int(11) NULL DEFAULT NULL,
  `member_id` bigint(20) NULL DEFAULT NULL,
  `remark` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `status` int(11) NULL DEFAULT NULL,
  `total_amount` decimal(32, 18) NULL DEFAULT NULL,
  `transaction_number` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `admin_id` bigint(20) NULL DEFAULT NULL,
  `coin_id` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `is_quick` int(11) NULL DEFAULT NULL,
  `coin_key` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `FK2lmq6bcbk4nl3hmqcxbnx2kuc`(`admin_id`) USING BTREE,
  INDEX `FK6drad9oqabujy0jsre3minxi`(`coin_id`) USING BTREE,
  INDEX `fk_withdraw_record_coin_key`(`coin_key`) USING BTREE,
  CONSTRAINT `FK2lmq6bcbk4nl3hmqcxbnx2kuc` FOREIGN KEY (`admin_id`) REFERENCES `admin` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `FK6drad9oqabujy0jsre3minxi` FOREIGN KEY (`coin_id`) REFERENCES `coin` (`name`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 50127 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for work_order
-- ----------------------------
DROP TABLE IF EXISTS `work_order`;
CREATE TABLE `work_order`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `member_id` int(11) NOT NULL COMMENT '用户id',
  `description` varchar(500) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '描述',
  `img_url` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '配图',
  `type` int(11) NOT NULL COMMENT '反馈类型',
  `create_time` datetime(0) NOT NULL COMMENT '反馈时间',
  `contact` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '联系方式',
  `status` bit(1) NOT NULL COMMENT '处理状态(0,未处理 1.已处理)',
  `detail` text CHARACTER SET utf8 COLLATE utf8_general_ci NULL COMMENT '处理描述',
  `reply_time` datetime(0) NULL DEFAULT NULL COMMENT '回复时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 33 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '工单管理' ROW_FORMAT = Dynamic;

-- ----------------------------
-- View structure for reward_statistics
-- ----------------------------
DROP VIEW IF EXISTS `reward_statistics`;
CREATE ALGORITHM = UNDEFINED SQL SECURITY DEFINER VIEW `reward_statistics` AS select `reward_record`.`member_id` AS `member_id`,`reward_record`.`order_member_id` AS `order_member_id`,`reward_record`.`coin_id` AS `coin_id`,date_format(`reward_record`.`create_time`,'%Y-%m') AS `create_time`,sum(`reward_record`.`amount`) AS `amount` from `reward_record` where (`reward_record`.`type` = 0) group by `reward_record`.`member_id`,`reward_record`.`order_member_id`,`reward_record`.`coin_id`,date_format(`reward_record`.`create_time`,'%Y-%m') order by `create_time`;

-- ----------------------------
-- Procedure structure for seHistory
-- ----------------------------
DROP PROCEDURE IF EXISTS `seHistory`;
delimiter ;;
CREATE PROCEDURE `seHistory`()
BEGIN

INSERT INTO member_wallet_se_history(balance,frozen_balance,member_id,release_balance,create_time)
SELECT t.balance,t.frozen_balance,t.member_id,t.release_balance,date_sub(NOW(),interval 1 day)
FROM member_wallet t
WHERE t.coin_id = 'SE';

DELETE FROM member_wallet_se_history WHERE create_time<curdate() - interval 3 month;

END
;;
delimiter ;

-- ----------------------------
-- Procedure structure for insertStatisticsExchange
-- ----------------------------
DROP PROCEDURE IF EXISTS `insertStatisticsExchange`;
delimiter ;;
CREATE PROCEDURE `insertStatisticsExchange`()
BEGIN
    INSERT INTO statistics_exchange (date_, base_symbol, coin_symbol, amount, money, money_usd, create_time)
    select date_format(DATE_SUB(NOW(), INTERVAL 1 DAY), '%Y-%m-%d'),
           base_symbol,
           coin_symbol,
           sum(traded_amount),
           sum(turnover),
           IF(base_symbol = 'USDT', sum(turnover), null),
           now()
    from exchange_order
    where FROM_UNIXTIME(completed_time / 1000, '%Y-%m-%d') = date_format(DATE_SUB(NOW(), INTERVAL 1 DAY), '%Y-%m-%d')
      and direction = 1
      and status = 1
    group by base_symbol, coin_symbol;

END
;;
delimiter ;

-- ----------------------------
-- Procedure structure for insertStatisticsOtc
-- ----------------------------
DROP PROCEDURE IF EXISTS `insertStatisticsOtc`;
delimiter ;;
CREATE PROCEDURE `insertStatisticsOtc`()
BEGIN
    INSERT INTO statistics_otc (date_, unit, amount, money, fee, create_time)
    select date_format(b.release_time, '%Y-%m-%d'),
           a.unit            unit,
           sum(b.number)     amount,
           sum(money) money,
           sum(b.commission) fee,
           now()
    from otc_order b,
         otc_coin a
    where a.id = b.coin_id
      and b.status = 3
      and date_format(b.release_time, '%Y-%m-%d') = date_format(DATE_SUB(NOW(), INTERVAL 1 DAY), '%Y-%m-%d')
    group by a.unit, date_format(b.release_time, '%Y-%m-%d');

END
;;
delimiter ;

-- ----------------------------
-- Procedure structure for insertStatisticsRechargeData
-- ----------------------------
DROP PROCEDURE IF EXISTS `insertStatisticsRechargeData`;
delimiter ;;
CREATE PROCEDURE `insertStatisticsRechargeData`()
BEGIN
	INSERT INTO statistics_recharge ( recharge_amount, people_count, recharge_count, currency, date_, create_time ) SELECT
	IF(SUM(amount) is null, 0 ,SUM(amount)),
	COUNT( DISTINCT member_id, member_id ),
	COUNT( d.id ),
	c.unit,
	date_format( DATE_SUB( NOW( ), INTERVAL 1 DAY ), '%Y-%m-%d' ),
	NOW( ) 
FROM
	coin c
	LEFT JOIN member_deposit d ON c.unit = d.unit 
	AND date_format( create_time, '%Y-%m-%d' ) = date_format( DATE_SUB( NOW( ), INTERVAL 1 DAY ), '%Y-%m-%d' ) 
WHERE
	1 
GROUP BY
	c.UNIT;
END
;;
delimiter ;

-- ----------------------------
-- Procedure structure for insertStatisticsRegisterData
-- ----------------------------
DROP PROCEDURE IF EXISTS `insertStatisticsRegisterData`;
delimiter ;;
CREATE PROCEDURE `insertStatisticsRegisterData`()
BEGIN
	INSERT INTO statistics_register ( date_, create_time, total_count, self_count, invited_count, indirect_count ) SELECT
	date_format( DATE_SUB( NOW( ), INTERVAL 1 DAY ), '%Y-%m-%d' ),
	NOW( ),
	COUNT( 1 ),
	COUNT( IF ( inviter_id IS NULL AND inviter_parent_id IS NULL, 1, NULL ) ) self_count,
	COUNT( IF ( inviter_id IS NOT NULL AND inviter_parent_id IS NULL, 1, NULL ) ) invited_count,
	COUNT( IF ( inviter_id IS NOT NULL AND inviter_parent_id IS NOT NULL, 1, NULL ) ) indirect_count 
FROM
	member 
WHERE
	date_format( registration_time, '%Y-%m-%d' ) = date_format( DATE_SUB( NOW( ), INTERVAL 1 DAY ), '%Y-%m-%d' );

END
;;
delimiter ;

-- ----------------------------
-- Procedure structure for insertStatisticsTransaction
-- ----------------------------
DROP PROCEDURE IF EXISTS `insertStatisticsTransaction`;
delimiter ;;
CREATE PROCEDURE `insertStatisticsTransaction`()
BEGIN

    INSERT INTO statistics_transaction (date_, symbol, people_count, purchase_people_count, sell_people_count,
                                        transaction_count, tx_purchase_count, tx_sell_count, transaction_amount,
                                        create_time)
    SELECT DATE_FORMAT(DATE_SUB(NOW(), INTERVAL 1 DAY), '%Y-%m-%d'),
           c.symbol,
           COUNT(DISTINCT member_id, member_id)                  people,
           COUNT(DISTINCT member_id, IF(direction = 1, 1, NULL)) buyPeople,
           COUNT(DISTINCT member_id, IF(direction = 0, 1, NULL)) sellPeople,
           COUNT(o.order_id)                                     count,
           COUNT(IF(direction = 1, 1, NULL))                     buyCount,
           COUNT(IF(direction = 0, 1, NULL))                     sellCount,
           IF(SUM(traded_amount) is null, 0, SUM(traded_amount)) amount,
           NOW()
    FROM exchange_coin c
             LEFT JOIN
         exchange_order o ON o.symbol = c.symbol AND
                             date_format(FROM_UNIXTIME(completed_time / 1000), '%Y-%m-%d') =
                             date_format(DATE_SUB(NOW(), INTERVAL 1 DAY), '%Y-%m-%d')
    WHERE 1
    GROUP BY symbol;
END
;;
delimiter ;

-- ----------------------------
-- Procedure structure for insertStatisticsWithdrawData
-- ----------------------------
DROP PROCEDURE IF EXISTS `insertStatisticsWithdrawData`;
delimiter ;;
CREATE PROCEDURE `insertStatisticsWithdrawData`()
BEGIN
    INSERT INTO statistics_withdraw (recharge_amount, people_count, recharge_count, currency, date_, create_time)
    SELECT IF(SUM(total_amount) is NULL, 0, SUM(total_amount)),
           COUNT(DISTINCT member_id, member_id),
           COUNT(r.id),
           c.unit,
           date_format(DATE_SUB(NOW(), INTERVAL 1 DAY), '%Y-%m-%d'),
           NOW()
    FROM coin c
             LEFT JOIN withdraw_record r ON c.unit = r.coin_id
        AND date_format(create_time, '%Y-%m-%d') = date_format(DATE_SUB(NOW(), INTERVAL 1 DAY), '%Y-%m-%d')
        AND r.status = 3
    WHERE 1
    GROUP BY c.unit;

END
;;
delimiter ;

-- ----------------------------
-- Procedure structure for updateApiKeyStatus
-- ----------------------------
DROP PROCEDURE IF EXISTS `updateApiKeyStatus`;
delimiter ;;
CREATE PROCEDURE `updateApiKeyStatus`()
BEGIN
	UPDATE member_api_key set status = 1 WHERE expire_time<=NOW();

END
;;
delimiter ;

-- ----------------------------
-- Event structure for callseHistory
-- ----------------------------
DROP EVENT IF EXISTS `callseHistory`;
delimiter ;;
CREATE EVENT `callseHistory`
ON SCHEDULE
EVERY '1' DAY STARTS '2019-12-21 00:00:01'
DO CALL seHistory()
;;
delimiter ;

-- ----------------------------
-- Event structure for callRewardRecordSum
-- ----------------------------
DROP EVENT IF EXISTS `callRewardRecordSum`;
delimiter ;;
CREATE EVENT `callRewardRecordSum`
ON SCHEDULE
EVERY '6' HOUR STARTS '2020-03-18 11:59:59'
DISABLE ON SLAVE
COMMENT '每6小时整理一次邀请榜单'
DO REPLACE INTO reward_record_sum(`sum_date`, `member_id`, `type`, `coin_id`, `amount`)
SELECT
date_format( now( ), '%Y-%m' ) AS `sum_date`,
`rr`.`member_id` AS `member_id`,
`rr`.type AS `type`,
`rr`.`coin_id` AS `coin_id`,
sum( `rr`.`amount` ) AS `amount` 
FROM
	`reward_record` rr 
WHERE
	date_format( `rr`.`create_time`, '%Y-%m' ) = date_format( now( ), '%Y-%m' ) 
GROUP BY
	`rr`.`member_id`,
	`rr`.type,
	`rr`.`coin_id`
;;
delimiter ;

-- ----------------------------
-- Event structure for dealExchangeDataEveryDay
-- ----------------------------
DROP EVENT IF EXISTS `dealExchangeDataEveryDay`;
delimiter ;;
CREATE EVENT `dealExchangeDataEveryDay`
ON SCHEDULE
EVERY '1' DAY STARTS '2020-03-11 00:00:01'
DISABLE ON SLAVE
DO CALL insertStatisticsExchange()
;;
delimiter ;

-- ----------------------------
-- Event structure for dealOtcDataEveryDay
-- ----------------------------
DROP EVENT IF EXISTS `dealOtcDataEveryDay`;
delimiter ;;
CREATE EVENT `dealOtcDataEveryDay`
ON SCHEDULE
EVERY '1' DAY STARTS '2020-03-11 00:00:01'
DISABLE ON SLAVE
DO CALL insertStatisticsOtc()
;;
delimiter ;

-- ----------------------------
-- Event structure for 修改api状态
-- ----------------------------
DROP EVENT IF EXISTS `修改api状态`;
delimiter ;;
CREATE EVENT `修改api状态`
ON SCHEDULE
EVERY '1' MINUTE STARTS '2020-03-31 18:51:19'
DISABLE ON SLAVE
DO CALL updateApiKeyStatus()
;;
delimiter ;

-- ----------------------------
-- Event structure for 每天处理充币数据统计
-- ----------------------------
DROP EVENT IF EXISTS `每天处理充币数据统计`;
delimiter ;;
CREATE EVENT `每天处理充币数据统计`
ON SCHEDULE
EVERY '1' DAY STARTS '2020-02-26 00:00:01'
DISABLE ON SLAVE
DO CALL insertStatisticsRechargeData()
;;
delimiter ;

-- ----------------------------
-- Event structure for 每天处理币币交易数据统计
-- ----------------------------
DROP EVENT IF EXISTS `每天处理币币交易数据统计`;
delimiter ;;
CREATE EVENT `每天处理币币交易数据统计`
ON SCHEDULE
EVERY '1' DAY STARTS '2020-02-27 00:00:01'
DISABLE ON SLAVE
DO CALL insertStatisticsTransaction()
;;
delimiter ;

-- ----------------------------
-- Event structure for 每天处理提币数据统计
-- ----------------------------
DROP EVENT IF EXISTS `每天处理提币数据统计`;
delimiter ;;
CREATE EVENT `每天处理提币数据统计`
ON SCHEDULE
EVERY '1' DAY STARTS '2020-02-26 00:00:01'
DISABLE ON SLAVE
DO CALL insertStatisticsWithdrawData()
;;
delimiter ;

-- ----------------------------
-- Event structure for 每天处理注册数据统计
-- ----------------------------
DROP EVENT IF EXISTS `每天处理注册数据统计`;
delimiter ;;
CREATE EVENT `每天处理注册数据统计`
ON SCHEDULE
EVERY '1' DAY STARTS '2020-02-26 00:00:01'
DISABLE ON SLAVE
DO CALL insertStatisticsRegisterData()
;;
delimiter ;

-- ----------------------------
-- Triggers structure for table member_wallet
-- ----------------------------
DROP TRIGGER IF EXISTS `trigger_update_wallet`;
delimiter ;;
CREATE TRIGGER `trigger_update_wallet` AFTER UPDATE ON `member_wallet` FOR EACH ROW begin
 INSERT INTO member_wallet_history(member_id,coin_id,before_balance,after_balance,before_frozen_balance,after_frozen_balance,op_time) VALUES (new.member_id,new.coin_id,old.balance,new.balance,old.frozen_balance,new.frozen_balance,now());
end
;;
delimiter ;

SET FOREIGN_KEY_CHECKS = 1;
