package com.fzg.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fzg.constant.RedisFollowKey;
import com.fzg.mapper.Followmapper;
import com.fzg.mapper.UserMapper;
import com.fzg.model.Follow;
import com.fzg.model.User;
import com.fzg.service.FollowService;
import com.fzg.vo.FollowVO;
import com.fzg.vo.UserVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class FollowServiceImpl extends ServiceImpl<Followmapper, Follow> implements FollowService {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean addFollow(FollowVO request) {
        Long followerId = request.getFollowerId();
        Long followingId = request.getFollowingId();
        Integer actionFollow = request.getActionFollow();
        // 不能关注自己
        if (followerId.equals(followingId)) {
            return false;
        }
        String redisKey = RedisFollowKey.getFollowLockKey(followerId, followingId);
        Boolean locked = redisTemplate.opsForValue().setIfAbsent(redisKey, "1",3, TimeUnit.SECONDS);
        if (Boolean.FALSE.equals(locked)) {
            return true;
        }
        try {
            // 1️⃣ 查询是否已关注
            Follow record = getOne(
                    new LambdaQueryWrapper<Follow>()
                            .eq(Follow::getFollowerId, followerId)
                            .eq(Follow::getFollowingId, followingId)
            );

            // 2️⃣ 未关注
            if (record == null) {
                if (actionFollow == 1) {
                    // 关注 → 插入
                    Follow follow = new Follow();
                    follow.setFollowerId(followerId);
                    follow.setFollowingId(followingId);
                    save(follow);

                    // 计数变更
                    userMapper.updateFolCount(followerId, followingId,1);
                    userMapper.updateFolingCount(followerId, followingId,1);

                    redisTemplate.opsForSet().add(
                            RedisFollowKey.followingSet(followerId),
                            String.valueOf(followingId)
                    );
                    redisTemplate.opsForSet().add(
                            RedisFollowKey.followerSet(followingId),
                            String.valueOf(followerId)
                    );

                    // 缓存关注状态
                    redisTemplate.opsForValue().set(
                            RedisFollowKey.getFollowStatusKey(followerId, followingId),
                            "1",
                            7, TimeUnit.DAYS
                    );
                }
                // 取消关注但本来就没关注 → 直接成功
                return true;
            }

            // 3️⃣ 已关注
            if (actionFollow == 0) {
                // 取消关注 → 删除
                removeById(record.getId());

                userMapper.updateFolCount(followerId, followingId,-1);
                userMapper.updateFolingCount(followerId, followingId,-1);

                redisTemplate.opsForSet().remove(
                        RedisFollowKey.followingSet(followerId),
                        String.valueOf(followingId)
                );
                redisTemplate.opsForSet().remove(
                        RedisFollowKey.followerSet(followingId),
                        String.valueOf(followerId)
                );

                redisTemplate.delete(
                        RedisFollowKey.getFollowStatusKey(followerId, followingId)
                );
            }

            // actionFollow == 1 且已关注 → 幂等成功
            return true;

        } catch (Exception e) {
            log.error("关注操作失败", e);
            return false;
        } finally {
            redisTemplate.delete(redisKey);
        }
    }


    @Override
    public List<UserVO> queryFolList(FollowVO request) {
        Integer pageNum = request.getPageNum() == null ? 1 : request.getPageNum();
        Integer pageSize = request.getPageSize() == null ? 10 : request.getPageSize();

        List<UserVO> userList = baseMapper.queryFolList(request.getFollowerId(),pageSize,(pageNum-1)*pageSize);
        return userList;
    }

    @Override
    public List<UserVO> queryFollingList(FollowVO request) {
        Integer pageNum = request.getPageNum() == null ? 1 : request.getPageNum();
        Integer pageSize = request.getPageSize() == null ? 10 : request.getPageSize();

        List<UserVO> userList = baseMapper.queryFollingList(request.getFollowingId(),pageSize,(pageNum-1)*pageSize);
        return userList;
    }
}
