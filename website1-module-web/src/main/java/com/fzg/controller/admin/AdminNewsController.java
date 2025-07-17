package com.fzg.controller.admin;

import com.fzg.bo.NewsCreateBO;
import com.fzg.enums.EnumReturn;
import com.fzg.model.Result;
import com.fzg.service.NewsService;
import com.fzg.vo.NewsVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/news")
@Schema(description = "管理员新闻管理接口")
@RequiredArgsConstructor
@Slf4j
public class AdminNewsController {

    private final NewsService newsService;

    @PostMapping("/add")
    public Result createNews(@RequestBody NewsCreateBO newsCreateBO) {
        try {
            // 调用服务层方法创建新闻
            return Result.success( newsService.createNewsWithContent(newsCreateBO));
        } catch (Exception e) {
            // 处理异常
            return Result.fail(EnumReturn.NEWS_CREATE_ERROR);
        }
    }


    /**
     * 删除新闻
     * @param id 新闻ID
     * @return 操作结果
     */
    @DeleteMapping("/delete/{id}")
    public Result deleteNews(@PathVariable Integer id) {
        try {
            // 调用服务层方法删除新闻
            this.newsService.deleteNews(id);
            return Result.success(EnumReturn.OPERATION_SUCCESS);
        } catch (Exception e) {
            // 处理异常
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
    public Result updateNews(@PathVariable Integer id, @RequestBody NewsCreateBO newsCreateBO) {
        try {
            // 调用服务层方法更新新闻
            newsService.updateNews(id, newsCreateBO);
            return Result.success(EnumReturn.OPERATION_SUCCESS);
        } catch (Exception e) {
            // 处理异常
            return Result.fail(EnumReturn.NEWS_UPDATE_ERROR);
        }
    }

    @GetMapping("/list")
    public Result<List<NewsVO>> listNews() {
        Result<List<NewsVO>> listResult = newsService.newsList();
        return Result.success(listResult.getData());
    }
}
