package com.sky.service.impl;

import com.sky.dto.SetmealDTO;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.mapper.CategoryMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.service.SetmealService;
import com.sky.vo.SetmealVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
@Slf4j
public class SetmealServiceimpl implements SetmealService {
    @Autowired
    private SetmealMapper SetmealMapper;
    @Autowired
    private SetmealDishMapper SetmealDishMapper;
    @Autowired
    private CategoryMapper categoryMapper;

    /**
     * 新增套餐，同时需要保存套餐和菜品的关联关系
     * @param setmealDTO
     */
    @Override
    @Transactional
    public void saveWithDish(SetmealDTO setmealDTO) {
        log.info("套餐数据：{}", setmealDTO);
        if (setmealDTO.getSetmealDishes() == null || setmealDTO.getSetmealDishes().size() == 0){
            throw new RuntimeException("套餐中菜品不能为空");
        }
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);
        SetmealMapper.insert(setmeal);
        Long setmealId = setmeal.getId();
        setmealDTO.getSetmealDishes().forEach(dish -> {
            dish.setSetmealId(setmealId);
        });
        SetmealDishMapper.insertBatch(setmealDTO.getSetmealDishes());
    }

    /***
     * 根据id查询套餐
     * @param id
     */
    public SetmealVO getByIdWithDish(Long id) {
        Setmeal setmeal = SetmealMapper.getById(id);
        List<SetmealDish> setmealDish = SetmealDishMapper.getById(id);
        String categoryName = categoryMapper.getCategoryNameByID(setmeal.getCategoryId());
        SetmealVO setmealVO = SetmealVO.builder()
                .id(setmeal.getId())
                .categoryId(setmeal.getCategoryId())
                .name(setmeal.getName())
                .price(setmeal.getPrice())
                .status(setmeal.getStatus())
                .description(setmeal.getDescription())
                .image(setmeal.getImage())
                .updateTime(setmeal.getUpdateTime())
                .categoryName(categoryName)
                .setmealDishes(setmealDish)
                .build();
                return setmealVO;
    }
}
