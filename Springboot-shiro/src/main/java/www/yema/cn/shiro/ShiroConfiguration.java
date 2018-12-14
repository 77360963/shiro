package www.yema.cn.shiro;


import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.Filter;

import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.session.mgt.SessionManager;
import org.apache.shiro.spring.security.interceptor.AuthorizationAttributeSourceAdvisor;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.filter.authc.AnonymousFilter;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.apache.shiro.web.session.mgt.DefaultWebSessionManager;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
    
    @Bean("sessionManager")
    public SessionManager sessionManager() {
        DefaultWebSessionManager sessionManager = new DefaultWebSessionManager(); 
        return sessionManager;
    }
    
    //配置核心安全事务管理器
    @Bean(name="securityManager")
    public SecurityManager  securityManager(@Qualifier("authRealm") AuthRealm authRealm) {
        System.err.println("--------------shiro已经加载----------------");
        DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager();
        securityManager.setRealm(authRealm);
        securityManager.setSessionManager(sessionManager());
        return securityManager;
    }
    
  
    
    @Bean(name="kickout")
    public KickoutSessionFilter kickoutSessionControlFilter(){
        KickoutSessionFilter kickoutSessionFilter=new KickoutSessionFilter();
        kickoutSessionFilter.setSessionManager(sessionManager());
        return kickoutSessionFilter;
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
        filters.put("abc",new KickoutSessionFilter());        
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
        
        
        
       /* //配置访问权限
        LinkedHashMap<String, String> filterChainDefinitionMap=new LinkedHashMap<String, String>();
        filterChainDefinitionMap.put("/login*", "anon"); //表示可以匿名访问
        filterChainDefinitionMap.put("/loginUser", "anon");
        filterChainDefinitionMap.put("/client/test", "anon");
        filterChainDefinitionMap.put("/assert/test", "anon");//添加白名单
        filterChainDefinitionMap.put("/assert/get", "anon");//添加白名单
        filterChainDefinitionMap.put("/assert/assertQuery", "anon");//添加白名单
        filterChainDefinitionMap.put("/a", "anon");
        filterChainDefinitionMap.put("/book/list", "anon");
        filterChainDefinitionMap.put("/logout*","anon");
        filterChainDefinitionMap.put("/jsp/error.jsp*","anon");
        filterChainDefinitionMap.put("/jsp/login.jsp*","authc");
        filterChainDefinitionMap.put("/*", "authc");//表示需要认证才可以访问
        filterChainDefinitionMap.put("/**", "authc");//表示需要认证才可以访问
        filterChainDefinitionMap.put("/*.*", "authc");
        
        //限制同一帐号同时在线的个数。
        Map<String, Filter> filtersMap = new LinkedHashMap<String, Filter>();        
        filtersMap.put("authc", kickoutSessionControlFilter());        
        shiroFilterFactoryBean.setFilters(filtersMap);    
        
        shiroFilterFactoryBean.setFilterChainDefinitionMap(filterChainDefinitionMap);
        
        
      
        
        return shiroFilterFactoryBean;*/
    }

    //加入注解的使用，不加入这个注解不生效
    @Bean
    public AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor(@Qualifier("securityManager") SecurityManager securityManager) {
        AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor = new AuthorizationAttributeSourceAdvisor();
        authorizationAttributeSourceAdvisor.setSecurityManager(securityManager);
        return authorizationAttributeSourceAdvisor;
    }
    
}
