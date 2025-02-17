package com.baiyi.opscloud.facade.sys.impl;

import com.baiyi.opscloud.common.exception.common.CommonRuntimeException;
import com.baiyi.opscloud.common.util.SessionUtil;
import com.baiyi.opscloud.domain.ErrorEnum;
import com.baiyi.opscloud.domain.generator.opscloud.AuthRoleMenu;
import com.baiyi.opscloud.domain.generator.opscloud.Menu;
import com.baiyi.opscloud.domain.generator.opscloud.MenuChild;
import com.baiyi.opscloud.domain.param.sys.MenuParam;
import com.baiyi.opscloud.domain.vo.common.TreeVO;
import com.baiyi.opscloud.domain.vo.sys.MenuVO;
import com.baiyi.opscloud.facade.sys.MenuFacade;
import com.baiyi.opscloud.packer.sys.MenuPacker;
import com.baiyi.opscloud.service.auth.AuthRoleMenuService;
import com.baiyi.opscloud.service.sys.MenuChildService;
import com.baiyi.opscloud.service.sys.MenuService;
import org.apache.logging.log4j.util.Strings;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author <a href="mailto:xiuyuan@xinc818.group">修远</a>
 * @Date 2021/6/2 10:21 上午
 * @Since 1.0
 */

@Service
public class MenuFacadeImpl implements MenuFacade {

    @Resource
    private MenuService menuService;

    @Resource
    private MenuChildService menuChildService;

    @Resource
    private MenuPacker menuPacker;

    @Resource
    private AuthRoleMenuService authRoleMenuService;

    @Override
    public void saveMenu(MenuParam.MenuSave param) {
        List<Menu> menuList = menuPacker.toDOList(param.getMenuList());
        if (!validMenuList(menuList))
            throw new CommonRuntimeException(ErrorEnum.MENU_CONTENT_EMPTY);
        menuList.forEach(menu -> {
            menu.setSeq(menuList.indexOf(menu));
            if (menu.getId() == null)
                menuService.add(menu);
            else
                menuService.update(menu);
        });
    }

    private Boolean validMenuList(List<Menu> menuList) {
        return menuList.stream().allMatch(x ->
                Strings.isNotBlank(x.getIcon())
                        && Strings.isNotBlank(x.getTitle())
        );
    }

    @Override
    public void saveMenuChild(MenuParam.MenuChildSave param) {
        List<MenuChild> menuChildList = menuPacker.toChildDOList(param.getMenuChildList());
        if (!validMenuChildList(menuChildList))
            throw new CommonRuntimeException(ErrorEnum.MENU_CHILD_CONTENT_EMPTY);
        menuChildList.forEach(menuChild -> {
            menuChild.setSeq(menuChildList.indexOf(menuChild));
            if (menuChild.getId() == null)
                menuChildService.add(menuChild);
            else
                menuChildService.update(menuChild);
        });
    }

    private Boolean validMenuChildList(List<MenuChild> menuChildList) {
        return menuChildList.stream().allMatch(x ->
                Strings.isNotBlank(x.getTitle())
                        && Strings.isNotBlank(x.getIcon())
                        && Strings.isNotBlank(x.getPath())
        );
    }

    @Override
    public List<MenuVO.Menu> queryMenu() {
        List<Menu> menuList = menuService.queryAllBySeq();
        return menuPacker.toVOList(menuList);
    }

    @Override
    public List<MenuVO.Child> queryMenuChild(Integer id) {
        List<MenuChild> menuChildList = menuChildService.listByMenuId(id);
        return menuPacker.toChildVOList(menuChildList);
    }

    @Override
    public void delMenuById(Integer id) {
        List<MenuChild> menuChildList = menuChildService.listByMenuId(id);
        if (!CollectionUtils.isEmpty(menuChildList))
            throw new CommonRuntimeException(ErrorEnum.MENU_CHILD_IS_NOT_EMPTY);
        menuService.del(id);
    }

    @Override
    public void delMenuChildById(Integer id) {
        menuChildService.del(id);
    }

    @Override
    public List<TreeVO.Tree> queryMenuTree() {
        return menuPacker.wrapTree();
    }

    @Override
    @Transactional(rollbackFor = {CommonRuntimeException.class, Exception.class})
    public void saveAuthRoleMenu(MenuParam.AuthRoleMenuSave param) {
        authRoleMenuService.deleteByRoleId(param.getRoleId());
        List<AuthRoleMenu> authRoleMenuList = param.getMenuChildIdList().stream().map(menuChildId -> {
            AuthRoleMenu authRoleMenu = new AuthRoleMenu();
            authRoleMenu.setRoleId(param.getRoleId());
            authRoleMenu.setMenuChildId(menuChildId);
            return authRoleMenu;
        }).collect(Collectors.toList());
        try {
            authRoleMenuService.addList(authRoleMenuList);
        } catch (Exception e) {
            throw new CommonRuntimeException(ErrorEnum.ROLE_MENU_SAVE_FAIL);
        }
    }

    @Override
    public List<AuthRoleMenu> queryAuthRoleMenu(Integer roleId) {
        return authRoleMenuService.queryByRoleId(roleId);
    }

    @Override
    public List<MenuVO.Menu> queryAuthRoleMenuDetail(Integer roleId) {
        return menuPacker.toVOList(roleId);
    }

    @Override
    public List<MenuVO.Menu> queryMyMenu() {
        return menuPacker.toVOList(SessionUtil.getUsername());
    }

}
