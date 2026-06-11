package cn.edu.app.douyu.server.common.persistence;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * 数据库健康检查 Mapper：执行轻量 SQL 验证数据库连接。
 */
@Mapper
public interface DatabaseHealthMapper {
    @Select("select 1")
    int ping();
}
