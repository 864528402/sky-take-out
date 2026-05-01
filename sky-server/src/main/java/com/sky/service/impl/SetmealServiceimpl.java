package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Category;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.exception.BaseException;
import com.sky.mapper.CategoryMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.SetmealService;
import com.sky.vo.DishItemVO;
import com.sky.vo.SetmealVO;
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
            throw new BaseException("套餐中菜品不能为空");
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

    /**
     * 套餐分页查询
     * @param setmealPageQueryDTO
     */
    @Override
    public PageResult page(SetmealPageQueryDTO setmealPageQueryDTO) {
        log.info("套餐分页查询：{}", setmealPageQueryDTO);
        PageHelper.startPage(setmealPageQueryDTO.getPage(), setmealPageQueryDTO.getPageSize());
        Page<SetmealVO> pageResult = SetmealMapper.page(setmealPageQueryDTO);
        return new PageResult(pageResult.getTotal(), pageResult.getResult());
    }

    @Override
    @Transactional
    public void delete(List<Long> ids) {
        log.info("批量删除套餐：{}", ids);
        if (ids == null || ids.size() == 0){
            throw new RuntimeException("没有要删除的");
        }
        List<Integer> setmealStatusList = SetmealMapper.getStatusByIDs(ids);
        if(setmealStatusList.contains(1)){
            throw new BaseException("起售中的套餐不能删除");
        }
        SetmealMapper.delete(ids);
        SetmealDishMapper.deleteBySetmealIDs(ids);
    }
    /**
     * 套餐修改
     * @param setmealDTO
     */
    @Override
    @Transactional
    public void update(SetmealDTO setmealDTO) {
        log.info("修改套餐：{}", setmealDTO);
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);
        SetmealMapper.update(setmeal);
        setmealDTO.getSetmealDishes().forEach(dish -> {
            dish.setSetmealId(setmeal.getId());
        });
        SetmealDishMapper.deleteBySetmealIDs(Arrays.asList(setmealDTO.getId()));
        SetmealDishMapper.insertBatch(setmealDTO.getSetmealDishes());
    }

    /**
     * 批量起售停售
     * @param status
     * @param id
     */
    public void startOrStop(Integer status, Long id) {
        log.info("批量起售停售：{}", status);
        //先查询套餐中的菜品是否停售，如果有停售的菜品则不能起售
        if(status == 1){
            List<Integer> statusList = SetmealDishMapper.getStatusById(id);
            if(statusList.contains(0)){
                throw new BaseException("套餐中有菜品未起售");
            }
        }
        log.info("批量起售停售：{}", status);
        Setmeal setmeal = Setmeal.builder()
                .id(id)
                .status(status)
                .build();
        SetmealMapper.update(setmeal);
    }
    /**
     * 条件查询
     * @param setmeal
     * @return
     */
    public List<Setmeal> list(Setmeal setmeal) {
        List<Setmeal> list = SetmealMapper.list(setmeal);
        return list;
    }
    /**
     * 根据id查询菜品选项
     * @param id
     * @return
     */
    public List<DishItemVO> getDishItemById(Long id) {
        return SetmealMapper.getDishItemBySetmealId(id);
    }
}
