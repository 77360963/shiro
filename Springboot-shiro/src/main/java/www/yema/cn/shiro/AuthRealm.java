package www.yema.cn.shiro;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.springframework.beans.factory.annotation.Autowired;

import www.yema.cn.service.IUserService;
import www.yema.cn.vo.TbUser;

public class AuthRealm extends AuthorizingRealm{
    
    @Autowired
    private IUserService userService;
    
    /**
     * 授权
     * @param principal
     * @return
     */
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principal) {
        System.out.println("-----------------------------------------");
        //获取session中的用户
        TbUser user=(TbUser) principal.fromRealm(this.getClass().getName()).iterator().next();
      //获取用户名
        String username=principal.getPrimaryPrincipal().toString();
        SimpleAuthorizationInfo authorizationInfo=new SimpleAuthorizationInfo();
        //获得授权角色
        authorizationInfo.setRoles(userService.getRoles(username));
        //获得授权权限
        authorizationInfo.setStringPermissions(userService.getPermissions(username));
        
        return authorizationInfo;
    }
    
    /**
     * 认证.登录
     * @param token
     * @return
     * @throws AuthenticationException
     */
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo( AuthenticationToken token) throws AuthenticationException {
      //获取用户名密码
        String username = token.getPrincipal().toString();
        TbUser tbUser = userService.getUserByUsername(username);
        if (tbUser != null){
            //得到用户账号和密码存放到authenticationInfo中用于Controller层的权限判断 第三个参数随意不能为null
            AuthenticationInfo authenticationInfo = new SimpleAuthenticationInfo(tbUser, tbUser.getPassword(),this.getClass().getName()) ;
            return authenticationInfo ;
        }else{
            return null ;
        }
    }

}
