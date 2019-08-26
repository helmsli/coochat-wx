-- user_role_relation表 加索引
ALTER TABLE `user_dbcloud`.`user_role_relation` 
ADD INDEX `idx_clubId_roleId_selfStatus`(`roleId`, `clubId`, `selfStatus`) USING BTREE;