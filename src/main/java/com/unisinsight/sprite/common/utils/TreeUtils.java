package com.unisinsight.sprite.common.utils;///*
// * www.unisinsight.com Inc.
// * Copyright (c) 2018 All Rights Reserved
// */
//package com.unisinsight.ic.commons.utils;
//
//
//import com.unisinsight.ic.commons.enums.BaseResultCode;
//import com.unisinsight.ic.commons.exception.CommonException;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.util.CollectionUtils;
//
//import java.lang.reflect.Field;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.HashSet;
//import java.util.List;
//import java.util.Map;
//import java.util.Set;
//
///**
// * 组织树构建
// *
// * @author long [KF.long@h3c.com]
// * @date 2018/9/6 17:12
// * @param <E> 对象类型
// * @param <I> id、parent_id属性的类型
// * @since 1.0
// */
//public class TreeUtils<E, I> {
//
//    /**
//     * 日志
//     */
//    private static final Logger LOGGER = LoggerFactory.getLogger(TreeUtils.class);
//
//    /**
//     * Id的属性名称
//     */
//    private String idName;
//
//    /**
//     * parentId的属性名称
//     */
//    private String parentIdName;
//
//    /**
//     * 子节点列表的属性名
//     */
//    private String childsName;
//
//    /**
//     * 报错信息
//     */
//    private static final String ERROR_ILLEGAL = "对象无权访问";
//
//    /**
//     * 初始化
//     *
//     * @param name       id的属性名
//     * @param parent 父id的属性名
//     * @param childs   子节点列表的属性名
//     */
//    public TreeUtils(String name, String parent, String childs) {
//        this.idName = name;
//        this.parentIdName = parent;
//        this.childsName = childs;
//    }
//
//    /**
//     * 获取root节点(传入的base是根节点的父节点时(resultFindId为null)返回根节点，其余情况返回id为base的节点)
//     *
//     * @param all 节点列表（非树形结构）
//     * @param base  节点id/父节点id
//     * @return 节点id/父节点id为base的一个节点
//     */
//    public E getRoot(List<E> all, I base) {
//
//        if (isNull(all) || isNull(base)) {
//            LOGGER.error("TreeUtils=>getRoot入参错误");
//        }
//
//        E resultFindId = null;
//        E resultFindParentId = null;
//        for (E one : all) {
//            if (base.equals(getIDProperty(one, parentIdName))) {
//                resultFindParentId = one;
//            }
//            if (base.equals(getIDProperty(one, idName))) {
//                resultFindId = one;
//            }
//        }
//
//        if (isNull(resultFindParentId) && isNull(resultFindId)) {
//            LOGGER.error("TreeUtils=>根节点获取失败：无id为" + base + "的节点；且无父id为" + base + "的节点");
//        }
//
//        if (resultFindId != null) {
//            return resultFindId;
//        }
//        return resultFindParentId;
//
//    }
//
//    /**
//     * 生成树形
//     *
//     * @param nodes 节点
//     * @param root  根节点
//     * @return E 根节点
//     */
//    public E buildTree(List<E> nodes, E root) {
//
//        if (isNull(nodes) || isNull(root)) {
//            LOGGER.error("TreeUtils=>buildTree入参错误");
//        }
//
//        if (nodes.isEmpty()) {
//            return root;
//        }
//
//        //父节点id，List<节点E>
//        Map<I, List<E>> parentIdIndex = getParentIdChildsMap(nodes);
//        //放入root节点
//        I rootId = getIDProperty(root, idName);
//        //获取root的子节点
//        List<E> childs = parentIdIndex.get(rootId);
//        if (CollectionUtils.isEmpty(childs)) {
//            return root;
//        }
//        //将childs放入root
//        setProperty(root, childs, childsName);
//
//        //树中出现的id的集合
//        List<I> circles = new ArrayList<>(nodes.size());
//        circles.add(rootId);
//        while (true) {
//            List<E> tempChildren = new ArrayList<>();
//            for (E child : childs) {
//
//                I id = getIDProperty(child, idName);
//
//                //成环判断
//                if (circles.contains(id)) {
//                    LOGGER.error("TreeUtils=>参数存在循环依赖;请数据库管理员检查" + child.getClass() + "数据" + id);
//                }
//                circles.add(id);
//
//                List<E> temp = parentIdIndex.get(id);
//                if (CollectionUtils.isEmpty(temp)) {
//                    continue;
//                }
//                setProperty(child, temp, childsName);
//                tempChildren.addAll(temp);
//            }
//
//            childs = tempChildren;
//            if (CollectionUtils.isEmpty(tempChildren)) {
//                break;
//            }
//        }
//        return root;
//    }
//
//    /**
//     * 向树形结构的根节点中添加子节点（新增的节点层级无法高于根节点）
//     *
//     * @param root 根节点
//     * @param nodes 新增的节点
//     */
//    public void appendChilds(E root, List<E> nodes) {
//        if (isNull(root) || CollectionUtils.isEmpty(nodes)) {
//        }
//
//        //用Map存放新增节点的<parentId,List<E>>
//        Map<I, List<E>> map = getParentIdChildsMap(nodes);
//
//        //树中出现的id的集合
//        List<I> circles = new ArrayList<>();
//        List<E> childs = new ArrayList<>();
//        childs.add(root);
//        while (!childs.isEmpty()) {
//            E object = childs.get(0);
//            I id = getIDProperty(object, idName);
//            //成环判断
//            if (circles.contains(id)) {
//                LOGGER.error("TreeUtils=>参数存在循环依赖;请数据库管理员检查" + object.getClass() + "数据");
//            }
//            circles.add(id);
//
//            List<E> oldChilds = getChildProperty(object);
//            if (map.containsKey(id)) {
//                if (isNull(oldChilds)) {
//                    oldChilds = map.get(id);
//                } else {
//                    oldChilds.addAll(map.get(id));
//                }
//                setProperty(object, oldChilds, childsName);
//            }
//
//            childs.remove(0);
//            if (!CollectionUtils.isEmpty(oldChilds)) {
//                childs.addAll(oldChilds);
//            }
//        }
//    }
//
//    /**
//     * 获取子节点id列表
//     *
//     * @param trees 节点（树形）
//     * @param ids 子节点id集合
//     * @return java.util.List<ID>
//     */
//    public List<I> getChildIds(List<E> trees, List<I> ids)  {
//        if (isNull(ids)) {
//            LOGGER.error("TreeUtils=>getChildIds入参错误,ids为空");
//        }
//        //若node为空则返回空List
//        if (null == trees) {
//           return new ArrayList<>();
//        }
//        for (E tmp: trees) {
//            ids.add(getIDProperty(tmp, idName));
//            Object object = getChildProperty(tmp);
//            if (null != object && !((List) object).isEmpty()) {
//                @SuppressWarnings("unchecked")
//                List<E> result = (List) object;
//                getChildIds(result, ids);
//            }
//        }
//        return ids;
//    }
//
//    /**
//     * 获取ID的值
//     * @param obj 对象
//     * @param fieldName 属性名
//     * @return ID
//     */
//    private I getIDProperty(E obj, String fieldName) {
//        try {
//            Field field = obj.getClass().getDeclaredField(fieldName);
//            field.setAccessible(true);
//            @SuppressWarnings("unchecked")
//            I id = (I) field.get(obj);
//            field.setAccessible(false);
//            return id;
//        } catch (IllegalAccessException e) {
//            LOGGER.error("TreeUtils=>" + ERROR_ILLEGAL + fieldName);
//            throw CommonException.of(BaseResultCode.DATA_FORMAT_ERROR,
//                    ERROR_ILLEGAL + fieldName);
//        } catch (NoSuchFieldException e) {
//            LOGGER.error("TreeUtils=>对象没有" + fieldName + "属性");
//            throw CommonException.of(BaseResultCode.DATA_FORMAT_ERROR,
//                    "对象没有" + fieldName + "属性");
//        }
//    }
//
//    /**
//     * 获取child的值
//     * @param obj 对象
//     * @return List<E>
//     */
//    private List<E> getChildProperty(E obj) {
//        try {
//            Field field = obj.getClass().getDeclaredField(childsName);
//            field.setAccessible(true);
//            @SuppressWarnings("unchecked")
//            List<E> result = (List) field.get(obj);
//            field.setAccessible(false);
//            return result;
//        } catch (IllegalAccessException e) {
//            LOGGER.error("TreeUtils=>" + ERROR_ILLEGAL + childsName);
//            throw CommonException.of(BaseResultCode.DATA_FORMAT_ERROR,
//                    ERROR_ILLEGAL + childsName);
//        } catch (NoSuchFieldException e) {
//            LOGGER.error("TreeUtils=>对象没有" + childsName + "属性");
//            throw CommonException.of(BaseResultCode.DATA_FORMAT_ERROR,
//                    "对象没有" + childsName + "属性");
//        }
//    }
//
//    /**
//     * 设置属性的的值
//     * @param obj 对象
//     * @param value 值
//     * @param fieldName 属性名
//     * @param <F> 值的类型
//     */
//    private <F> void setProperty(E obj, F value, String fieldName) {
//        try {
//            Field field = obj.getClass().getDeclaredField(fieldName);
//            field.setAccessible(true);
//            field.set(obj, value);
//            field.setAccessible(false);
//        } catch (IllegalAccessException e) {
//            LOGGER.error("TreeUtils=>" + ERROR_ILLEGAL + fieldName);
//            throw CommonException.of(BaseResultCode.DATA_FORMAT_ERROR,
//                    ERROR_ILLEGAL + fieldName);
//        } catch (NoSuchFieldException e) {
//            throw CommonException.of(BaseResultCode.DATA_FORMAT_ERROR,
//                    "对象没有" + fieldName + "属性");
//        }
//    }
//
//    /**
//     * 空值判断
//     * @param obj 对象
//     * @param <F> 对象类型
//     * @return true/false
//     */
//    private <F> boolean isNull(F obj) {
//        return null == obj;
//    }
//
//    /**
//     * 用Map将节点通过parentId归类的<parentId,List<E>>
//     *
//     * @param nodes 节点列表（非树形）
//     * @return Map<parentId,List<E>>
//     */
//    private Map<I, List<E>> getParentIdChildsMap(List<E> nodes) {
//        Map<I, List<E>> map = new HashMap<>();
//        for (E node: nodes) {
//            I parentId = getIDProperty(node, parentIdName);
//            List<E> newChilds;
//            if (map.containsKey(parentId)) {
//                newChilds = map.get(parentId);
//            } else {
//                newChilds = new ArrayList<>();
//            }
//            newChilds.add(node);
//            map.put(parentId, newChilds);
//        }
//        return map;
//    }
//
//    /**
//     * 树根据传入的Set<id>减支,兄弟节点不在,子组织在
//     * 减支逻辑：遍历判断当前节点或其子孙节点在findOrgId中存在,则将其下标放入index中。
//     * 再次遍历List,将不在index中的元素删除。
//     *
//     * @param layerAll  树
//     * @param findId 需保留的id
//     * @param childNot 是否保留无关的子节点
//     * @return 当前组织节点或子节点在Set<id>中
//     */
//    public boolean getPath(List<E> layerAll, Set<I> findId, boolean childNot) {
//        if (CollectionUtils.isEmpty(layerAll)) {
//            return false;
//        }
//        Set<I> index = new HashSet<>();
//        for (E layer : layerAll) {
//            I id = getIDProperty(layer, idName);
//            List<E> child = getChildProperty(layer);
//            if (childNot) {
//                if (findId.contains(id)
//                        || getPath(child, findId, childNot)) {
//                    index.add(id);
//                }
//            } else {
//                boolean existInChild = getPath(child, findId, childNot);
//                if (findId.contains(id) || existInChild) {
//                    index.add(id);
//                }
//                if (!existInChild) {
//                    setProperty(layer, null, childsName);
//                }
//            }
//        }
//        if (index.isEmpty()) {
//            return false;
//        }
//        for (int i = layerAll.size() - 1; i >= 0; i--) {
//            E layer = layerAll.get(i);
//            I id = getIDProperty(layer, idName);
//            if (!index.contains(id)) {
//                layerAll.remove(layer);
//            }
//        }
//        return true;
//    }
//}
