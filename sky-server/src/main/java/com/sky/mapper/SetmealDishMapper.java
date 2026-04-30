package com.sky.mapper;


import com.sky.annotation.AutoFill;
import com.sky.entity.SetmealDish;
import com.sky.enumeration.OperationType;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Mapper
public interface SetmealDishMapper {
    /**
     * 根据id查询套餐与菜的关联
     * @param ids
     * @return
     */
    List<Long> getSetmealIDsByDishIDs(List<Long> ids);
    /**
     * 批量插入套餐与菜的关联数据
     * @param setmealDishes
     */
    void insertBatch(List<SetmealDish> setmealDishes);
    /**
     * 根据套餐id查询套餐与菜的关联数据
     * param id
     */
    List<SetmealDish> getById(Long id);
    /**
     * 根据套餐id删除套餐与菜的关联数据
     * param ids
     */
    void deleteBySetmealIDs(List<Long> ids);
    /**
     * 根据套餐id查询套餐的起售状态
     * param id
     */
    List<Integer> getStatusById(Long id);
}
