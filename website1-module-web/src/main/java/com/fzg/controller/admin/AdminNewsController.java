package com.fzg.controller.admin;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.stp.StpUtil;
import com.fzg.annotation.OperationLogAnnotation;
import com.fzg.bo.NewsCreateBO;
import com.fzg.enums.EnumReturn;
import com.fzg.model.Result;
import com.fzg.service.NewsService;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;


@RestController
@SaCheckLogin
@RequestMapping("/admin/news")
@Schema(description = "管理员新闻管理接口")
@RequiredArgsConstructor
@Slf4j
public class AdminNewsController {

    private final NewsService newsService;

    @PostMapping("/add")
    @SaCheckRole("admin")
    @OperationLogAnnotation(operationType = "POST-add",operationDesc = "创建新闻")
    public Result createNews(@RequestBody NewsCreateBO newsCreateBO) {
        try {
            return Result.success( newsService.createNewsWithContent(newsCreateBO));
        } catch (Exception e) {
            return Result.fail(EnumReturn.NEWS_CREATE_ERROR);
        }
    }


    /**
     * 删除新闻
     * @param id 新闻ID
     * @return 操作结果
     */
    @DeleteMapping("/delete/{id}")
    @SaCheckRole("admin")
    @OperationLogAnnotation(operationType = "DELETE-delete-id",operationDesc = "删除新闻")
    public Result deleteNews(@PathVariable Integer id) {
        try {
            this.newsService.deleteNews(id);
            return Result.success(EnumReturn.OPERATION_SUCCESS);
        } catch (Exception e) {
            return Result.fail(EnumReturn.NEWS_DELETE_ERROR);
        }
    }



    /**
     * 更新新闻
     * @param id 新闻ID
     * @param newsCreateBO 新闻更新信息
     * @return 操作结果
     */
    @PutMapping("/update/{id}")
    @SaCheckRole("admin")
    @OperationLogAnnotation(operationType = "PUT-update-id",operationDesc = "更新新闻")
    public Result updateNews(@PathVariable Integer id, @RequestBody NewsCreateBO newsCreateBO) {
        System.out.println(StpUtil.getRoleList());

        try {
            newsService.updateNews(id, newsCreateBO);
            return Result.success(EnumReturn.OPERATION_SUCCESS);
        } catch (Exception e) {
            return Result.fail(EnumReturn.NEWS_UPDATE_ERROR);
        }
    }

    /**
     * 获取新闻的所有信息
     * @return 新闻列表
     */
    @GetMapping("/list")
    @SaCheckRole("admin")
    public Result listNews() {

        System.out.println(StpUtil.getRoleList());

        return newsService.newsList();
    }
}
