package com.example.springboottemplate.serviceimpl;
import com.example.springboottemplate.entity.Response;
import com.example.springboottemplate.entity.User;
import com.example.springboottemplate.mapper.UserMapper;
import com.example.springboottemplate.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.springboottemplate.utils.ValidateUtil;
import java.util.List;

@Service
@Transactional
public class UserServiceimpl implements UserService {

    @Autowired
    private UserMapper usermapper;
    @Override
    public Response addUser(User user) {
        usermapper.addUser(user);
        return new Response(200, null, "操作成功");
    }

    @Override
    public Response findUser(User user) {
        List<User> list = usermapper.findUser(user);
        return new Response(200, list, "操作成功");
    }

    @Override
    public Response updateUser(User user) {
        usermapper.updateUser(user);
        return new Response(200, null, "操作成功");
    }

    @Override
    public Response deleteUser(List<Integer> idList) {
        if (ValidateUtil.isEmpty(idList)) {  // 使用工具类
            return new Response(400, null, "操作失败，ID 列表不能为空");
        }
        int affectedRows = usermapper.deleteBatchIds(idList); // 调用mybatis-plus的逻辑删除，返回受影响行数
        if (affectedRows > 0) {
            return new Response(200, null, "操作成功");
        } else {
            return new Response(400, null, "操作失败，未找到需要删除的记录");
        }
    }
}
