package www.yema.cn.service;

import java.util.Set;

import www.yema.cn.vo.TbUser;

public interface IUserService {
    TbUser getUserByUsername(String username);
    Set<String> getRoles(String userName);
    Set<String> getPermissions(String userName);
}
