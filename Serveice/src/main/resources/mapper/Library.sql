DROP TABLE  IF EXISTS `admin`;
CREATE TABLE IF NOT EXISTS `admin` (
    `id` INT(11) PRIMARY KEY NOT NULL AUTO_INCREMENT COMMENT 'id',
    `username` VARCHAR(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '用户名',
    `password` VARCHAR(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '密码',
    `adminType` INT(11) NULL DEFAULT NULL COMMENT '管理员类型'
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT '管理员' ROW_FORMAT = Dynamic ;

INSERT INTO `admin` VALUES (1, 'admin', '12345', 1);
INSERT INTO `admin` VALUES (2, 'qqwer', '12345', 0);
INSERT INTO `admin` VALUES (4, 'xy1222', '12345', 0);

DROP TABLE IF EXISTS `book_info`;
CREATE TABLE IF NOT EXISTS `book_info`  (
                              `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'id',
                              `name` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '图书名称',
                              `author` varchar(30) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '作者',
                              `publish` varchar(30) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '出版社',
                              `isbn` varchar(30) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '书籍编号',
                              `introduction` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '简介',
                              `language` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '语言',
                              `price` double NULL DEFAULT NULL COMMENT '价格',
                              `publish_date` date NULL DEFAULT NULL COMMENT '出版时间',
                              `type_id` int(11) NULL DEFAULT NULL COMMENT '书籍类型',
                              `status` int(11) NULL DEFAULT NULL COMMENT '状态：0未借出，1已借出',
                              PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '图书信息' ROW_FORMAT = Dynamic;

INSERT INTO `book_info` VALUES (1, '西游记', '施耐庵', '机械工业出版社', '100011', '师徒四人去西天取真经', '中文', 42, '2020-03-20', 1, 0);
INSERT INTO `book_info` VALUES (2, '三国演义', '罗贯中', '清华大学出版社', '100012', '东汉末年分三国。。。', '中文', 48, '2018-03-30', 1, 1);
INSERT INTO `book_info` VALUES (3, '红楼梦', '曹雪芹', '机械工业出版社', '100013', '宝玉与众多男女之间故事', '中文', 39, '2019-04-04', 1, 1);
INSERT INTO `book_info` VALUES (4, '资本论', '马克思', '原子能出版社', '100014', '马克思的剩余价值理论', '英文', 42, '2019-04-05', 5, 0);
INSERT INTO `book_info` VALUES (5, '经济学原理', '马歇尔', '机械工业出版社', '100015', '西方经济学界公认为划时代的著作', '英文', 45, '2020-04-06', 5, 0);
INSERT INTO `book_info` VALUES (6, '大变局下的中国法治', '李卫东', '北京大学出版社', '100016', '十大经典法学著作', '中文', 42, '2015-04-05', 4, 1);

DROP TABLE IF EXISTS `reader_info`;
CREATE TABLE IF NOT EXISTS `reader_info`  (
                                `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'id',
                                `username` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '用户名',
                                `password` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '密码',
                                `realName` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '真实姓名',
                                `sex` varchar(5) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '性别',
                                `birthday` date NULL DEFAULT NULL COMMENT '出生日期',
                                `address` varchar(30) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '籍贯',
                                `tel` varchar(11) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '电话',
                                `email` varchar(15) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '邮箱',
                                `registerDate` datetime(0) NULL DEFAULT NULL COMMENT '注册日期',
                                `readerNumber` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '读者编号',
                                PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '读者信息（包括登录账号密码等）' ROW_FORMAT = Dynamic;

INSERT INTO `reader_info` VALUES (1, 'zhangsan', '12345', '彭于晏', '男', '2001-04-01', '江西南昌', '13767134834', 'yu123@163.com', '2021-04-02 13:18:59', '8120116041');
INSERT INTO `reader_info` VALUES (2, 'mary', '12345', '陈恋', '女', '2004-04-01', '湖北武汉', '15270839599', 'yx123@163.com', '2021-03-06 07:57:56', '8120116044');
INSERT INTO `reader_info` VALUES (3, 'cindy', '12345', '辛帝', '女', '2010-04-04', '北京海淀', '13834141133', 'zs1314@163.com', '2021-04-04 13:36:42', '8120116042');