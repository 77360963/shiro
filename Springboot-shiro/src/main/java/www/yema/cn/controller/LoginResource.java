package www.yema.cn.controller;

import javax.servlet.http.HttpServletRequest;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.util.SavedRequest;
import org.apache.shiro.web.util.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import www.yema.cn.service.IUserService;
import www.yema.cn.vo.TbUser;

@Controller
public class LoginResource {
    
    @Autowired
    private IUserService userService;           
     
     //退出的时候是get请求，主要是用于退出
    @RequestMapping(value = "/login",method = RequestMethod.GET)
    public String login(HttpServletRequest request){
        SavedRequest savedRequest = WebUtils.getSavedRequest(request);
        String url = null;
        if (null != savedRequest) {
            url = savedRequest.getRequestUrl();
        }
        System.out.println("要转到的url="+url);
        return "/login";
    }
    //post登录
    @RequestMapping(value = "/login",method = RequestMethod.POST)
    public String login(String username, String password,HttpServletRequest request){
        //获取跳转到登录页面的上一个地址
        SavedRequest savedRequest = WebUtils.getSavedRequest(request);
        String url = null;
        if (null != savedRequest) {
            url = savedRequest.getRequestUrl();
        }
        System.out.println("要转到的url="+url);
        //添加用户认证信息
        UsernamePasswordToken usernamePasswordToken = new UsernamePasswordToken( username, password);       
        Subject subject = SecurityUtils.getSubject();
        try {
            //完成登录
            subject.login(usernamePasswordToken);
            //获得用户对象
            TbUser user=(TbUser) subject.getPrincipal();
            //存入session
            request.getSession().setAttribute("user", user);
            return "/user/index";
        } catch(Exception e) {
            return "/login";//返回登录页面
        }       
    }

    @RequestMapping(value = "/index")
    public String index(){       
        return "/user/index1";
    }
    
    @RequestMapping(value = "/index2")
    public String index2(){       
        return "/user/index2";
    }
    
    /**
     * 退出
     *
     * @return
     */
    @RequestMapping(value = "logout", method = RequestMethod.GET)    
    public String logout() {
        try {
            SecurityUtils.getSubject().logout();
        } catch (Exception e) {
            return "error";
        }
        return "/login";
    }
}
