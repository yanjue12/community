package com.fzg.controller.app;

import com.fzg.constant.RedisFollowKey;
import com.fzg.enums.EnumReturn;
import com.fzg.mapper.UserMapper;
import com.fzg.model.Result;
import com.fzg.model.User;
import com.fzg.service.FollowService;
import com.fzg.vo.FollowVO;
import com.fzg.vo.UserVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/follow")
public class FollowController {

    @Autowired
    private FollowService followService;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private UserMapper userMapper;

    /**
     * 关注
     * @return
     */
    @PostMapping("add")
    public Result addFollow(@RequestBody FollowVO follow){
        if(null == follow || follow.getFollowerId() == null || follow.getFollowingId() == null){
            return Result.fail(EnumReturn.REQUSET_IS_EMPTY);
        }
        Boolean res = followService.addFollow(follow);
        return Result.handle(res);
    }


    /**
     * 获取粉丝列表
     * @return
     */
    @PostMapping("/queryFolList")
    public Result queryFolList(@RequestBody FollowVO follow) {
        if(null == follow || null == follow.getFollowerId()){
            return Result.fail(EnumReturn.REQUSET_IS_EMPTY);
        }

        List<UserVO> list = followService.queryFolList(follow);

        return Result.success(list);
    }

    @PostMapping("/queryFollingList")
    public Result queryFollingList(@RequestBody FollowVO follow) {
        if(null == follow || null == follow.getFollowingId()){
            return Result.fail(EnumReturn.REQUSET_IS_EMPTY);
        }
        List<UserVO> list = followService.queryFollingList(follow);
        return Result.success(list);
    }







    /**
     * 获取关注数和粉丝数
     * @param userId
     * @return
     */
    @PostMapping("getFolingAndFolCount")
    public Result getFollowingCount(Long userId) {

        Long followingCount = redisTemplate.opsForSet()
                .size(RedisFollowKey.followingSet(userId));
        Long followerCount = redisTemplate.opsForSet()
                .size(RedisFollowKey.followerSet(userId));
        // Redis 正常
        if (followingCount != null && followerCount != null) {
            Map<String, Long> map = new HashMap<>();
            map.put("followingCount", followingCount);
            map.put("followerCount", followerCount);
            return Result.success(map);
        }

        // Redis 异常 / 不可信 → 回源 DB
        User user = userMapper.selectById(userId);

        Map<String, Long> map = new HashMap<>();
        map.put("followingCount", Long.valueOf(user.getFollowingCount()));
        map.put("followerCount", Long.valueOf(user.getFollowerCount()));
        return Result.success(map);
    }

}
