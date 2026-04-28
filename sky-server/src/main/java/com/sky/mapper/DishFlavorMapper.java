package com.sky.mapper;

import com.sky.annotation.AutoFill;
import com.sky.entity.DishFlavor;
import com.sky.enumeration.OperationType;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface DishFlavorMapper {
    /**
     * 批量插入口味数据
     * @param dishFlavors
     */
    void insertBatch(List<DishFlavor> dishFlavors);

    /**
     * 根据菜品id删除口味数据
     * @param ids
     * @return
     */
    void deleteByDishIDs(List<Long> ids);
    /**
     * 根据菜品id查询口味数据
     * @param id
     * @return
     */
    List<DishFlavor> getByDishID(Long id);
}
