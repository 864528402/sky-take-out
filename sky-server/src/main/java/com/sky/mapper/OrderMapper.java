package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.entity.Orders;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface OrderMapper {
    /**
     * 插入订单数据
     * @param orders
     */
    void insert(Orders orders);

    /**
     * 修改订单信息
     * @param orders
     */
    void update(Orders orders);
    /**
     * 根据订单号查询订单
     * @param orderNumber
     */
    @Select("select * from orders where number = #{orderNumber}")
    Orders getByNumber(String orderNumber);


    /**
     * 用于替换微信支付更新数据库状态的问题
     * @param orderStatus
     * @param orderPaidStatus
     */
    @Update("update orders set status = #{orderStatus},pay_status = #{orderPaidStatus} ,checkout_time = #{check_out_time} " +
            "where number = #{orderNumber}")
    void updateStatus(Integer orderStatus, Integer orderPaidStatus, LocalDateTime check_out_time, String orderNumber);

    /**
     * 订单分页查询
     * @param ordersPageQueryDTO
     * @return
     */
    Page<Orders> pageQuery(OrdersPageQueryDTO ordersPageQueryDTO);

    /**
     * 根据id查询订单详情
     * @param id
     * @return
     */
    @Select("select * from orders where id = #{id}")
    OrderVO getOrderDetail(Long id);
    /**
     * 统计订单信息
     * @return
     */
    @Select("SELECT " +
            "SUM(CASE WHEN status = 2 THEN 1 ELSE 0 END) AS toBeConfirmed, " +
            "SUM(CASE WHEN status = 3 THEN 1 ELSE 0 END) AS confirmed, " +
            "SUM(CASE WHEN status = 4 THEN 1 ELSE 0 END) AS deliveryInProgress " +
            "FROM orders " +
            "WHERE status IN (2, 3, 4)")
    OrderStatisticsVO getOrderStatistics();

    /**
     * 查询超时未支付的订单
     * @param time
     * @return
     */
    @Select("SELECT * FROM orders WHERE status = #{status} AND order_time < #{time}")
    List<Orders> getByStatusAndOrderTimeLT(@Param("status") Integer status, @Param("time") LocalDateTime time);

    /**
     * 根据状态查询订单
     * @param status
     * @return
     */
    @Select("SELECT * FROM orders WHERE status = #{status}")
    List<Orders> getByStatus(@Param("status") Integer status);

    /**
     * 统计指定时间范围内的营业额
     * @param begin
     * @param end
     * @param status
     * @return
     */
    @Select("SELECT SUM(amount) FROM orders WHERE order_time >= #{begin} AND order_time <= #{end} AND status = #{status}")
    Double sumByMap(@Param("begin") LocalDateTime begin, @Param("end") LocalDateTime end, @Param("status") Integer status);

    /**
     * 按日期统计营业额（一次性查询整个范围）
     * 使用XML动态SQL实现
     * @param params
     * @return
     */
    List<Map<String, Object>> getTurnoverByDateRange(Map<String, Object> params);

    /**
     * 订单统计查询（按日期范围）
     * 传入begin和end：按日期分组统计
     * 只传入end：统计总数
     * @param params
     * @return
     */
    List<Map<String, Object>> getOrderStatisticsByDateRange(Map<String, Object> params);

    /**
     * 查询销量排名top10
     * @param params
     * @return
     */
    List<Map<String, Object>> getSalesTop10(Map<String, Object> params);
}
