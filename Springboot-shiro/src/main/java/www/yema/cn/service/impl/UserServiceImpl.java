package www.yema.cn.service.impl;

import java.util.HashSet;
import java.util.Set;

import org.springframework.stereotype.Service;

import www.yema.cn.service.IUserService;
import www.yema.cn.vo.TbUser;

@Service
public class UserServiceImpl implements IUserService {

    public TbUser getUserByUsername(String username) {
        TbUser user=new TbUser();
        user.setUserName("admin");
        user.setPassword("123");
       return user;
        
    }

    public Set<String> getRoles(String userName) {
        // TODO Auto-generated method stub
        return null;
    }

    public Set<String> getPermissions(String userName) {
        Set<String> set=new HashSet<String>();
        set.add("aa");
        return set;
    }

}
