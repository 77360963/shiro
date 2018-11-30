package www.yema.cn.controller;

import javax.servlet.http.HttpServletRequest;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.util.SavedRequest;
import org.apache.shiro.web.util.WebUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import www.yema.cn.vo.TbUser;

@RestController
public class LoginResource {
 
     //退出的时候是get请求，主要是用于退出
    @RequestMapping(value = "/login",method = RequestMethod.GET)
    public String login(){
        return "login登录界面";
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
            return "登录成功";
        } catch(Exception e) {
            return "登录失败";//返回登录页面
        }       
    }

    @RequestMapping(value = "/index")
    public String index(){
        SecurityUtils.getSubject().checkPermission("aa");
        return "进入主页";
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
        return "退出成功";
    }
}
