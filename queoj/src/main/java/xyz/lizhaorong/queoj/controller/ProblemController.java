package xyz.lizhaorong.queoj.controller;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import xyz.lizhaorong.queoj.controller.vo.ProblemVO;
import xyz.lizhaorong.queoj.controller.vo.SimpleProblem;
import xyz.lizhaorong.queoj.entity.Problem;
import xyz.lizhaorong.queoj.security.web.Authorization;
import xyz.lizhaorong.queoj.service.ProblemService;
import xyz.lizhaorong.queoj.support.Response;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/problem")
public class ProblemController {

    private static final String codeModel =
            "public class Solution{\n" +
            "   public static $${\n"+
            "       \n"+
            "   }\n"+
            "}";


    @Autowired
    ProblemService problemService;

    @GetMapping("/{id}")
    public Response<Problem> getProblemInfo(@PathVariable("id") Integer id){
        Problem problem = problemService.getProblemById(id);
        if (problem==null){
            return new Response<Problem>().failure("题目不存在");
        }
        return new Response<Problem>().success(problem);
    }


    @GetMapping
    public Response<Map<String,Object>> getProblemList(Integer from, Integer limit){
        Response<Map<String,Object>> response = new Response<>();
        Map<String,Object> res = new HashMap<>();
        res.put("list",problemService.getProblems(from,limit));
        res.put("total",problemService.getProblemsTotal());
        return response.success(res);
    }

    @GetMapping("/like")
    public Response<List<SimpleProblem>> getProblemList(String like){
        System.out.println(like);
        Response<List<SimpleProblem>> response = new Response<>();
        return response.success(problemService.getProblems(like));
    }

    @GetMapping("/hot")
    public Response<Set<String>> getHotTenProblems(){
        return new Response<Set<String>>().success(problemService.getHotTenProblems());
    }

    @PutMapping
    @Authorization(2)
    public Response<Boolean> addNewProblem(@Validated ProblemVO problem){
        Problem p = new Problem();
        BeanUtils.copyProperties(problem,p);
        p.setCodesig(transSig(p.getDefaultCode()));
        p.setDefaultCode(codeModel.replace("$$",problem.getDefaultCode()));
        problemService.addProblem(p);
        return Response.staticSuccess();
    }

    @DeleteMapping
    @Authorization(2)
    public Response<Boolean> removeProblem(Integer pid){
        Response<Boolean> response = new Response<>();
        if (!problemService.removeProblem(pid)){
            response.failure("题目不能被删除");
        }else {
            response.success();
        }
        return response;
    }

    private String transSig(String s){
        char[] chars = s.toCharArray();
        int f=0,g=0;
        for (int i=0;i<chars.length;i++){
            if (chars[i]==' ') {
                f=i+1;
            }
            if (chars[i]=='(') {
                g=i;
            }
        }
        return s.substring(f,g);
    }
}
