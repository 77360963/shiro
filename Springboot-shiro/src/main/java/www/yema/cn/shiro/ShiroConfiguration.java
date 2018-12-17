package www.yema.cn.shiro;


import java.io.IOException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.Filter;
import org.apache.shiro.cache.ehcache.EhCacheManager;
import org.apache.shiro.io.ResourceUtils;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.session.mgt.SessionManager;
import org.apache.shiro.session.mgt.eis.EnterpriseCacheSessionDAO;
import org.apache.shiro.spring.security.interceptor.AuthorizationAttributeSourceAdvisor;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.filter.authc.AnonymousFilter;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.apache.shiro.web.session.mgt.DefaultWebSessionManager;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import net.sf.ehcache.CacheException;
import net.sf.ehcache.CacheManager;

import www.yema.cn.shiro.filter.KickoutSessionFilter;

@Configuration
public class ShiroConfiguration {
  
  //配置自定义的密码比较器
    @Bean(name="credentialsMatcher")
    public CredentialsMatcher credentialsMatcher() {
        return new CredentialsMatcher();
    }
    
    //配置自定义的权限登录器
    @Bean(name="authRealm")
    public AuthRealm myShiroRealm(@Qualifier("credentialsMatcher") CredentialsMatcher matcher) {
        AuthRealm authRealm = new AuthRealm();
        authRealm.setCredentialsMatcher(matcher);
        return authRealm;
    }
    
    /**
     * ehcache缓存管理器；shiro整合ehcache：
     * 通过安全管理器：securityManager
     * 单例的cache防止热部署重启失败
     * @return EhCacheManager
     */
    @Bean
    public EhCacheManager  ehCacheManager() {
       // logger.debug("=====shiro整合ehcache缓存：ShiroConfiguration.getEhCacheManager()");
        EhCacheManager ehcache = new EhCacheManager();
        CacheManager cacheManager = CacheManager.getCacheManager("es");
        if (cacheManager == null) {
            try {
                cacheManager = CacheManager.create(ResourceUtils.getInputStreamForPath("classpath:ehcache-app.xml"));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        ehcache.setCacheManager(cacheManager);
        return ehcache;
}
    
    
    /**
     * EnterpriseCacheSessionDAO shiro sessionDao层的实现；
     * 提供了缓存功能的会话维护，默认情况下使用MapCache实现，内部使用ConcurrentHashMap保存缓存的会话。
     */
    @Bean
    public EnterpriseCacheSessionDAO enterCacheSessionDAO() {
        EnterpriseCacheSessionDAO enterCacheSessionDAO = new EnterpriseCacheSessionDAO();
        //添加缓存管理器
        //enterCacheSessionDAO.setCacheManager(ehCacheManager());
        //添加ehcache活跃缓存名称（必须和ehcache缓存名称一致）
        enterCacheSessionDAO.setActiveSessionsCacheName("demo");
        return enterCacheSessionDAO;
    }
    
    /**
    *
    * @描述：sessionManager添加session缓存操作DAO
    * @创建人：wyait
    * @创建时间：2018年4月24日 下午8:13:52
    * @return
    */
   @Bean
   public DefaultWebSessionManager sessionManager() {
       DefaultWebSessionManager sessionManager = new DefaultWebSessionManager();
       //sessionManager.setCacheManager(ehCacheManager());
       sessionManager.setSessionDAO(enterCacheSessionDAO());
       return sessionManager;
   }
   
   /**
   *
   * @描述：kickoutSessionFilter同一个用户多设备登录限制
   * @创建人：wyait
   * @创建时间：2018年4月24日 下午8:14:28
   * @return
   */
  public KickoutSessionFilter kickoutSessionFilter(){
      KickoutSessionFilter kickoutSessionFilter = new KickoutSessionFilter();
      //使用cacheManager获取相应的cache来缓存用户登录的会话；用于保存用户—会话之间的关系的；
      //这里我们还是用之前shiro使用的ehcache实现的cacheManager()缓存管理
      //也可以重新另写一个，重新配置缓存时间之类的自定义缓存属性
      kickoutSessionFilter.setCacheManager(ehCacheManager());
      //用于根据会话ID，获取会话进行踢出操作的；
      kickoutSessionFilter.setSessionManager(sessionManager());
      //是否踢出后来登录的，默认是false；即后者登录的用户踢出前者登录的用户；踢出顺序。
      kickoutSessionFilter.setKickoutAfter(false);
      //同一个用户最大的会话数，默认1；比如2的意思是同一个用户允许最多同时两个人登录；
      kickoutSessionFilter.setMaxSession(1);
      //被踢出后重定向到的地址；
      kickoutSessionFilter.setKickoutUrl("/login?kickout=1");
      return kickoutSessionFilter;
  }
   
   
    
    //配置核心安全事务管理器
    @Bean(name="securityManager")
    public SecurityManager  securityManager(@Qualifier("authRealm") AuthRealm authRealm) {
        System.err.println("--------------shiro已经加载----------------");
        DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager();
        securityManager.setRealm(authRealm);
        // //注入ehcache缓存管理器;
        securityManager.setCacheManager(ehCacheManager());
        // //注入session管理器;
        securityManager.setSessionManager(sessionManager());
        return securityManager;
    }    
    
  //Filter工厂，设置对应的过滤条件和跳转条件
   @Bean(name="shiroFilter")
    public ShiroFilterFactoryBean shiroFilterFactoryBean(@Qualifier("securityManager") SecurityManager securityManager) {
        ShiroFilterFactoryBean shiroFilterFactoryBean = new ShiroFilterFactoryBean();        
        shiroFilterFactoryBean.setSecurityManager(securityManager);
        //配置登录的url和登录成功的url
        shiroFilterFactoryBean.setLoginUrl("/login");
        shiroFilterFactoryBean.setSuccessUrl("/home");
        
      // 未授权界面;
        Map<String, Filter> filters = new LinkedHashMap<String, Filter> ();
        filters.put("abc",kickoutSessionFilter());        
        shiroFilterFactoryBean.setFilters(filters); 
        
     // 拦截器.
        Map<String, String> filterChainDefinitions = new LinkedHashMap<String, String> ();
        // 配置拦截的规则 顺序判断
        filterChainDefinitions.put("/login*", "anon"); //表示可以匿名访问
        filterChainDefinitions.put("/logout*", "anon"); //表示可以匿名访问    
        filterChainDefinitions.put("/*", "authc,abc");
        filterChainDefinitions.put("/**", "authc,abc");        
        filterChainDefinitions.put("/*.*", "authc,abc");    
        shiroFilterFactoryBean.setFilterChainDefinitionMap(filterChainDefinitions);
        return shiroFilterFactoryBean; 
    }

    //加入注解的使用，不加入这个注解不生效
    @Bean
    public AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor(@Qualifier("securityManager") SecurityManager securityManager) {
        AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor = new AuthorizationAttributeSourceAdvisor();
        authorizationAttributeSourceAdvisor.setSecurityManager(securityManager);
        return authorizationAttributeSourceAdvisor;
    }
    
}
