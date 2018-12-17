package www.yema.cn.shiro.filter;

import java.io.Serializable;
import java.util.Deque;
import java.util.LinkedList;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.apache.shiro.cache.Cache;
import org.apache.shiro.cache.CacheManager;
import org.apache.shiro.session.Session;
import org.apache.shiro.session.mgt.DefaultSessionKey;
import org.apache.shiro.session.mgt.SessionManager;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.filter.AccessControlFilter;
import org.apache.shiro.web.util.WebUtils;

import www.yema.cn.vo.TbUser;


public class KickoutSessionFilter extends AccessControlFilter{
    
    private int maxSession = 1; //同一个帐号最大会话数 默认1
    
    private String kickoutUrl; //踢出后到的地址
    
    private boolean kickoutAfter = false; //踢出之前登录的/之后登录的用户 默认踢出之前登录的用户    
    
    private SessionManager sessionManager;
    
    private Cache<String, Deque<Serializable>> cache;
    
    public void setKickoutUrl(String kickoutUrl) {
        this.kickoutUrl = kickoutUrl;
    }

    public void setKickoutAfter(boolean kickoutAfter) {
        this.kickoutAfter = kickoutAfter;
    }

    public void setMaxSession(int maxSession) {
        this.maxSession = maxSession;
    }

    public void setSessionManager(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    // 设置Cache的key的前缀
    public void setCacheManager(CacheManager cacheManager) {
        //必须和ehcache缓存配置中的缓存name一致
        this.cache = cacheManager.getCache("shiro-activeSessionCache");
    }
    
   
    /**
    *
    * 表示是否允许访问；mappedValue就是[urls]配置中拦截器参数部分，如果允许访问返回true，否则false；
    * (感觉这里应该是对白名单（不需要登录的接口）放行的)
    * 如果isAccessAllowed返回true则onAccessDenied方法不会继续执行
    * 这里可以用来判断一些不被通过的链接（个人备注）
    * * 表示是否允许访问 ，如果允许访问返回true，否则false；
    * @param servletRequest
    * @param servletResponse
    * @param object 表示写在拦截器中括号里面的字符串 mappedValue 就是 [urls] 配置中拦截器参数部分
    * @return
    * @throws Exception
    * */
    @Override
    protected boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue) throws Exception {      
      //      Subject subject = getSubject(servletRequest,servletResponse); 
     //        String url = getPathWithinApplication(servletRequest); 
    //        log.info("当前用户正在访问的 url => " + url); 
    //        log.info("subject.isPermitted(url);"+subject.isPermitted(url));
        return false;

    }
    /**
     * 表示当访问拒绝时是否已经处理了；如果返回true表示需要继续处理；如果返回false表示该拦截器实例已经处理了，将直接返回即可。
     * onAccessDenied是否执行取决于isAccessAllowed的值，如果返回true则onAccessDenied不会执行；如果返回false，执行onAccessDenied
     * 如果onAccessDenied也返回false，则直接返回，不会进入请求的方法（只有isAccessAllowed和onAccessDenied的情况下）
     * */
    @Override
    protected boolean onAccessDenied(ServletRequest request, ServletResponse response) throws Exception {
       System.out.println("进入过滤器onAccessDenied");
       Subject subject = getSubject(request, response);  
       if(!subject.isAuthenticated() && !subject.isRemembered()) {  
           //如果没有登录，直接进行之后的流程  
           return true;  
       }  
     
       Session session = subject.getSession();  
       TbUser tbUser = (TbUser) subject.getPrincipal();  
       Serializable sessionId = session.getId();
       String username=tbUser.getUserName();
     //TODO 同步控制
       Deque<Serializable> deque = cache.get(username);
       if(deque == null) {
           deque = new LinkedList<Serializable>();
           cache.put(username, deque);
        }
     //如果队列里没有此sessionId，且用户没有被踢出；放入队列
       if(!deque.contains(sessionId) && session.getAttribute("kickout") == null) {
           deque.push(sessionId);
      }
       
     //如果队列里的sessionId数超出最大会话数，开始踢人
       while(deque.size() > maxSession) {
           Serializable kickoutSessionId = null;
           if(kickoutAfter) { //如果踢出后者
               kickoutSessionId = deque.removeFirst();
           } else { //否则踢出前者
               kickoutSessionId = deque.removeLast();
           }
           try {
               Session kickoutSession = sessionManager.getSession(new DefaultSessionKey(kickoutSessionId));
               if(kickoutSession != null) {
                   //设置会话的kickout属性表示踢出了
                   kickoutSession.setAttribute("kickout", true);
               }
           } catch (Exception e) {//ignore exception
           }
     }
       
     //如果被踢出了，直接退出，重定向到踢出后的地址
       if (session.getAttribute("kickout") != null) {
           //会话被踢出了
           try {
               subject.logout();
           } catch (Exception e) { //ignore
           }
           saveRequest(request);
           WebUtils.issueRedirect(request, response, kickoutUrl);
           return false;
        }
       
        return true;
    }
 
    

}
