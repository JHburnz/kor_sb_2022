package com.kjh.exam.demo.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.kjh.exam.demo.service.ArticleService;
import com.kjh.exam.demo.utill.Ut;
import com.kjh.exam.demo.vo.Article;
import com.kjh.exam.demo.vo.ResultData;
import com.kjh.exam.demo.vo.Rq;

@Controller
public class UsrArticleController {
	@Autowired
	private ArticleService articleService;

	// 액션 메서드 시작
	@RequestMapping("/usr/article/doAdd")
	@ResponseBody
	public ResultData<Article> doAdd(HttpServletRequest req, String title, String body) {
		Rq rq = new Rq(req);
		
		if ( rq.isLogined() == false ) {
			return ResultData.from("F-A", "로그인 후 이용해주세요.");
		}
		
		if ( Ut.empty(title) ) {
			return ResultData.from("F-1", "title(을)를 입력해주세요.");
		}
		
		if ( Ut.empty(body) ) {
			return ResultData.from("F-2", "body(을)를 입력해주세요.");
		}

		ResultData<Integer> writeArticleRd = articleService.writeArticle(rq.getLoginedMemberId(), title, body);
		int id = writeArticleRd.getData1();
		
		Article article = articleService.getForPrintArticle(rq.getLoginedMemberId(), id);
		
		return ResultData.newData(writeArticleRd, "article", article);
	}
	
	@RequestMapping("/usr/article/list")
	public String showList(HttpServletRequest req, Model model) {
		Rq rq = new Rq(req);
		
		List<Article> articles = articleService.getForPrintArticles(rq.getLoginedMemberId());
		
		model.addAttribute("articles", articles);
		
		return "usr/article/list";
	}
	
	@RequestMapping("/usr/article/detail")
	public String showDetail(HttpServletRequest req, Model model, int id) {
		Rq rq = new Rq(req);
		
		Article article = articleService.getForPrintArticle(rq.getLoginedMemberId(), id);
		
		model.addAttribute("article", article);
		
		return "usr/article/detail";
	}
	
	@RequestMapping("/usr/article/getArticle")
	@ResponseBody
	public ResultData<Article> getArticle(HttpServletRequest req, int id) {
		Rq rq = new Rq(req);
		
		Article article = articleService.getForPrintArticle(rq.getLoginedMemberId(), id);
		
		if ( article == null) {
			return ResultData.from("F-1", Ut.f("%d번 게시물이 존재하지 않습니다.", id));
		}
		
		return ResultData.from("S-1", Ut.f("%d번 게시물입니다.", id), "article", article);
	}
	
	@RequestMapping("/usr/article/doDelete")
	@ResponseBody
	public String doDelete(HttpServletRequest req, int id) {
		Rq rq = new Rq(req);
		
		if ( rq.isLogined() == false ) {
			return Ut.jsHistoryBack("로그인 후 이용해주세요.");
		}
		
		Article article = articleService.getForPrintArticle(rq.getLoginedMemberId(), id);
		
		if ( article.getMemberId() != rq.getLoginedMemberId() ) {
			return Ut.jsHistoryBack("권한이 없습니다.");
		}
		
		if ( article == null ) {
			ResultData.from("F-1", Ut.f("%d번 게시물이 존재하지 않습니다.", id));
		}
		
		articleService.deleteArticle(id);
		
		return Ut.jsReplace(Ut.f("%d번 게시물을 삭제하였습니다.", id), "../article/list");
	}
	
	@RequestMapping("/usr/article/doModify")
	@ResponseBody
	public ResultData<Article> doModify(HttpServletRequest req, int id, String title, String body) {
		Rq rq = new Rq(req);
		
		if ( rq.isLogined() == false ) {
			return ResultData.from("F-A", "로그인 후 이용해주세요.");
		}
		
		Article article = articleService.getForPrintArticle(rq.getLoginedMemberId(), id);
		
		if ( article == null ) {
			ResultData.from("F-1", Ut.f("%d번 게시물이 존재하지 않습니다.", id));
		}
		
		ResultData actorCanModifyRd = articleService.actorCanModify(rq.getLoginedMemberId(), article);
		
		if ( actorCanModifyRd.isFail() ) {
			return actorCanModifyRd;
		}

		return articleService.modifyArticle(id, title, body);
	}
	// 액션 메서드 끝
}