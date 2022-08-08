package com.xxxx.server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xxxx.server.pojo.Department;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author wanghao
 * @since 2022-04-19
 */
public interface DepartmentMapper extends BaseMapper<Department> {

    /**
     * 获取所有部门
     * @return
     */
    //获取所有部门
    List<Department> getAllDepartments(Integer parentId);

    //添加部门
    void addDep(Department dep);

    //删除部门
    void deleteDep(Department dep);
}
