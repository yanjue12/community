package com.fzg.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fzg.model.Notification;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface Notificationmapper extends BaseMapper<Notification> {

    /**
     * 物理删除半个月前已读的通知记录
     * 基于创建时间判断，确保通知存在足够长时间后再删除
     * @param cutoffTime 截止时间
     * @return 删除的记录数
     */
    @Delete("DELETE FROM notification WHERE is_read = '1' AND created_at < #{cutoffTime}")
    int physicalDeleteReadNotifications(@Param("cutoffTime") String cutoffTime);

    /**
     * 基于阅读时间物理删除已读通知记录
     * @param cutoffTime 截止时间
     * @return 删除的记录数
     */
    @Delete("DELETE FROM notification WHERE is_read = '1' AND read_at < #{cutoffTime}")
    int physicalDeleteReadNotificationsByReadTime(@Param("cutoffTime") String cutoffTime);

    /**
     * 物理删除逻辑删除的通知记录
     * @param cutoffTime 截止时间
     * @return 删除的记录数
     */
    @Delete("DELETE FROM notification WHERE is_deleted = '1' AND created_at < #{cutoffTime}")
    int physicalDeleteLogicalDeletedNotifications(@Param("cutoffTime") String cutoffTime);

    /**
     * 统计总通知数
     */
    @Select("SELECT COUNT(*) FROM notification WHERE is_deleted = '0'")
    Long countTotalNotifications();

    /**
     * 统计未读通知数
     */
    @Select("SELECT COUNT(*) FROM notification WHERE is_read = '0' AND is_deleted = '0'")
    Long countUnreadNotifications();

    /**
     * 统计已读通知数
     */
    @Select("SELECT COUNT(*) FROM notification WHERE is_read = '1' AND is_deleted = '0'")
    Long countReadNotifications();

    /**
     * 统计逻辑删除通知数
     */
    @Select("SELECT COUNT(*) FROM notification WHERE is_deleted = '1'")
    Long countLogicalDeletedNotifications();

    /**
     * 查询分区状态信息
     */
    List<Map<String, Object>> getPartitionStatus();

    /**
     * 创建下个月分区
     */
    void createNextMonthPartition();

    /**
     * 删除历史分区
     */
    void dropOldPartitions(@Param("monthsToKeep") int monthsToKeep);
}