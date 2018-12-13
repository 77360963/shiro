package www.yema.cn.service;

import java.util.Set;

import www.yema.cn.vo.TbUser;

public interface IUserService {
    public TbUser getUserByUsername(String username);
    public Set<String> getRoles(String userName);
    public Set<String> getPermissions(String userName);
}
