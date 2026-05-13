package cn.edu.app.douyu.server.common.persistence;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface DatabaseHealthMapper {
    @Select("select 1")
    int ping();
}
