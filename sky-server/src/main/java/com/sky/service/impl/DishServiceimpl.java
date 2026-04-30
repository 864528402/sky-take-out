package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.exception.BaseException;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.models.auth.In;
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
public class DishServiceimpl implements DishService {
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private DishFlavorMapper dishFlavorMapper;
    @Autowired
    private SetmealDishMapper setmealDishMapper;
    @Autowired
    private SetmealMapper setmealMapper;

    /**
     * 新增菜品，同时保存对应的口味数据
     * @param dishDTO
     */
    @Override
    @Transactional
    //涉及两个数据库操作，需要事务处理
    public void saveWithFlavor(DishDTO dishDTO){
        log.info("新增菜品 {}", dishDTO);
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        //新增菜品
        dishMapper.insert(dish);
        //新增菜品口味
        Long dishId = dish.getId();
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if(flavors != null && flavors.size() > 0){
            flavors.forEach(flavor -> {
                flavor.setDishId(dishId);
            });
            dishFlavorMapper.insertBatch(flavors);
        }
    }
    /**
     * 菜品分页查询
     * @param dishPageQueryDTO
     * @return
     */
    public PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO){
        PageHelper.startPage(dishPageQueryDTO.getPage(), dishPageQueryDTO.getPageSize());
        Page<DishVO> page = dishMapper.pageQuery(dishPageQueryDTO);
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 批量删除
     * @param ids
     */
    @Override
    @Transactional
    public void delete(List<Long> ids) {
        List<Integer> statusList = dishMapper.getStatusByIDs(ids);
        if (statusList.contains(1)){
            throw new DeletionNotAllowedException("起售中的菜品不能删除");
        }
        List<Long> setmealIds = setmealDishMapper.getSetmealIDsByDishIDs(ids);
        if (!setmealIds.isEmpty()){
            throw new DeletionNotAllowedException("有套餐正在使用，不能删除");
        }
        dishMapper.delete(ids);
        dishFlavorMapper.deleteByDishIDs(ids);
    }

    /**
     * 修改菜品
     * @param dishDTO
     */
    @Transactional
    public void update(DishDTO dishDTO) {
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        dishMapper.update(dish);
        if (dishDTO.getFlavors() != null && !dishDTO.getFlavors().isEmpty()){
            dishDTO.getFlavors().forEach(flavor -> {
                flavor.setDishId(dish.getId());
            });
            dishFlavorMapper.deleteByDishIDs(new ArrayList<Long>(){{add(dish.getId());}});
            dishFlavorMapper.insertBatch(dishDTO.getFlavors());
        }
    }
    /**
     * 根据id查询菜品和对应的口味数据
     * @param id
     * @return
     */
    @Override
    public DishVO getByIdWithFlavor(Long id) {
        Dish dish = dishMapper.getById(id);
        List<DishFlavor> dishFlavors = dishFlavorMapper.getByDishID(id);
        DishVO dishVO = new DishVO();
        BeanUtils.copyProperties(dish, dishVO);
        dishVO.setFlavors(dishFlavors);
        return dishVO;
    }

    /**
     * 起售停售
     * @param status
     * @param id
     */
    public void startOrStop(Integer status, Long id) {
        log.info("起售停售：{}", status);
        if(status == 0){
            List<Long> setmealIds = setmealDishMapper.getSetmealIDsByDishIDs(Arrays.asList(id));
            List<Integer> statusList = setmealMapper.getStatusByIDs(setmealIds);
            if(statusList.contains(1)){
                throw new BaseException("有套餐正在售卖该菜品");
            }
        }
        Dish dish = Dish.builder()
                .id(id)
                .status(status)
                .build();
        dishMapper.startOrStop(dish);
    }

    /**
     * 根据分类id查询菜品
     * @param categoryId
     * @return
     */
    @Override
    public List<DishVO> getListByCategoryId(Long categoryId) {
        List<DishVO> list = dishMapper.getListByCategoryId(categoryId);
        return list;
    }

}
