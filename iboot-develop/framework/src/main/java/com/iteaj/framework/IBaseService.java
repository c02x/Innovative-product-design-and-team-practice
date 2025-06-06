package com.iteaj.framework;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Assert;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.conditions.query.ChainQuery;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.conditions.query.QueryChainWrapper;
import com.baomidou.mybatisplus.extension.conditions.update.ChainUpdate;
import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper;
import com.baomidou.mybatisplus.extension.conditions.update.UpdateChainWrapper;
import com.baomidou.mybatisplus.extension.toolkit.ChainWrappers;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import com.iteaj.framework.result.*;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * create time: 2021/7/20
 * @author iteaj
 */
public interface IBaseService<E extends Entity> {

    /**
     * 默认批次提交数量
     */
    int DEFAULT_BATCH_SIZE = 1000;

    /**
     * 插入一条记录（选择字段，策略插入）
     *
     * @param entity 实体对象
     */
    default BooleanResult save(E entity) {
        return new BooleanResult(SqlHelper.retBool(getBaseMapper().insert(entity)));
    }

    /**
     * 插入（批量）
     *
     * @param entityList 实体对象集合
     */
    @Transactional(rollbackFor = Exception.class)
    default BooleanResult saveBatch(Collection<E> entityList) {
        return saveBatch(entityList, DEFAULT_BATCH_SIZE);
    }

    /**
     * 插入（批量）
     *
     * @param entityList 实体对象集合
     * @param batchSize  插入批次数量
     */
    BooleanResult saveBatch(Collection<E> entityList, int batchSize);

    /**
     * 批量修改插入
     *
     * @param entityList 实体对象集合
     */
    @Transactional(rollbackFor = Exception.class)
    default BooleanResult saveOrUpdateBatch(Collection<E> entityList) {
        return saveOrUpdateBatch(entityList, DEFAULT_BATCH_SIZE);
    }

    /**
     * 批量修改插入
     *
     * @param entityList 实体对象集合
     * @param batchSize  每次的数量
     */
    BooleanResult saveOrUpdateBatch(Collection<E> entityList, int batchSize);

    /**
     * 根据 ID 删除
     *
     * @param id 主键ID
     */
    default BooleanResult removeById(Serializable id) {
        return new BooleanResult(SqlHelper.retBool(getBaseMapper().deleteById(id)));
    }

    /**
     * 根据 columnMap 条件，删除记录
     *
     * @param columnMap 表字段 map 对象
     */
    default BooleanResult removeByMap(Map<String, Object> columnMap) {
        Assert.notEmpty(columnMap, "error: columnMap must not be empty");
        return new BooleanResult(SqlHelper.retBool(getBaseMapper().deleteByMap(columnMap)));
    }

    /**
     * 根据 entity 条件，删除记录
     *
     * @param queryWrapper 实体包装类 {@link com.baomidou.mybatisplus.core.conditions.query.QueryWrapper}
     */
    default BooleanResult remove(Wrapper<E> queryWrapper) {
        return new BooleanResult(SqlHelper.retBool(getBaseMapper().delete(queryWrapper)));
    }

    /**
     * 删除（根据ID 批量删除）
     *
     * @param idList 主键ID列表
     */
    default BooleanResult removeByIds(Collection<? extends Serializable> idList) {
        if (CollectionUtils.isEmpty(idList)) {
            return new BooleanResult(false);
        }
        return new BooleanResult(SqlHelper.retBool(getBaseMapper().deleteBatchIds(idList)));
    }

    /**
     * 根据 ID 选择修改
     *
     * @param entity 实体对象
     */
    default BooleanResult updateById(E entity) {
        return new BooleanResult(SqlHelper.retBool(getBaseMapper().updateById(entity)));
    }

    /**
     * 根据 UpdateWrapper 条件，更新记录 需要设置sqlset
     *
     * @param updateWrapper 实体对象封装操作类 {@link com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper}
     */
    default BooleanResult update(Wrapper<E> updateWrapper) {
        return update(null, updateWrapper);
    }

    /**
     * 根据 whereEntity 条件，更新记录
     *
     * @param entity        实体对象
     * @param updateWrapper 实体对象封装操作类 {@link com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper}
     */
    default BooleanResult update(E entity, Wrapper<E> updateWrapper) {
        return new BooleanResult(SqlHelper.retBool(getBaseMapper().update(entity, updateWrapper)));
    }

    /**
     * 根据ID 批量更新
     *
     * @param entityList 实体对象集合
     */
    @Transactional(rollbackFor = Exception.class)
    default BooleanResult updateBatchById(Collection<E> entityList) {
        return updateBatchById(entityList, DEFAULT_BATCH_SIZE);
    }

    /**
     * 根据ID 批量更新
     *
     * @param entityList 实体对象集合
     * @param batchSize  更新批次数量
     */
    BooleanResult updateBatchById(Collection<E> entityList, int batchSize);

    /**
     * TableId 注解存在更新记录，否插入一条记录
     *
     * @param entity 实体对象
     */
    BooleanResult saveOrUpdate(E entity);

    /**
     * 根据 ID 查询
     *
     * @param id 主键ID
     */
    default DetailResult<E> getById(Serializable id) {
        return new DetailResult<>(getBaseMapper().selectById(id));
    }


    /**
     * 查询（根据ID 批量查询）
     *
     * @param idList 主键ID列表
     */
    default ListResult<E> listByIds(Collection<? extends Serializable> idList) {
        return new ListResult<E>(getBaseMapper().selectBatchIds(idList));
    }

    /**
     * 查询（根据 columnMap 条件）
     *
     * @param columnMap 表字段 map 对象
     */
    default ListResult<E> listByMap(Map<String, Object> columnMap) {
        return new ListResult<>(getBaseMapper().selectByMap(columnMap));
    }

    /**
     * 根据 Wrapper，查询一条记录 <br/>
     * <p>结果集，如果是多个会抛出异常，随机取一条加上限制条件 wrapper.last("LIMIT 1")</p>
     *
     * @param queryWrapper 实体对象封装操作类 {@link com.baomidou.mybatisplus.core.conditions.query.QueryWrapper}
     */
    default DetailResult<E> getOne(Wrapper<E> queryWrapper) {
        return getOne(queryWrapper, true);
    }

    /**
     * 根据 Wrapper，查询一条记录
     *
     * @param queryWrapper 实体对象封装操作类 {@link com.baomidou.mybatisplus.core.conditions.query.QueryWrapper}
     * @param throwEx      有多个 result 是否抛出异常
     */
    DetailResult<E> getOne(Wrapper<E> queryWrapper, boolean throwEx);

    /**
     * 根据 Wrapper，查询一条记录
     *
     * @param queryWrapper 实体对象封装操作类 {@link com.baomidou.mybatisplus.core.conditions.query.QueryWrapper}
     */
    Map<String, Object> getMap(Wrapper<E> queryWrapper);

    /**
     * 根据 Wrapper，查询一条记录
     *
     * @param queryWrapper 实体对象封装操作类 {@link com.baomidou.mybatisplus.core.conditions.query.QueryWrapper}
     * @param mapper       转换函数
     */
    <V> V getObj(Wrapper<E> queryWrapper, Function<? super Object, V> mapper);

    /**
     * 查询总记录数
     *
     * @see Wrappers#emptyWrapper()
     */
    default NumberResult<Integer> count() {
        return count(Wrappers.emptyWrapper());
    }

    /**
     * 根据 Wrapper 条件，查询总记录数
     *
     * @param queryWrapper 实体对象封装操作类 {@link com.baomidou.mybatisplus.core.conditions.query.QueryWrapper}
     */
    default NumberResult<Integer> count(Wrapper<E> queryWrapper) {
        return new NumberResult(SqlHelper.retCount(getBaseMapper().selectCount(queryWrapper)));
    }

    /**
     * 查询一个列表
     * @param condition  查询条件
     * @return
     */
    default ListResult<E> list(E condition) {
        return list(Wrappers.query(condition));
    }

    /**
     * 查询列表
     *
     * @param queryWrapper 实体对象封装操作类 {@link com.baomidou.mybatisplus.core.conditions.query.QueryWrapper}
     */
    default ListResult<E> list(Wrapper<E> queryWrapper) {
        return new ListResult<>(getBaseMapper().selectList(queryWrapper));
    }

    /**
     * 查询所有
     *
     * @see Wrappers#emptyWrapper()
     */
    default ListResult<E> list() {
        return list(Wrappers.emptyWrapper());
    }

    default PageResult<IPage<E>> page(IPage<E> page, E condition) {
        return page(page, Wrappers.query(condition));
    }

    /**
     * 翻页查询
     *
     * @param page         翻页对象
     * @param queryWrapper 实体对象封装操作类 {@link com.baomidou.mybatisplus.core.conditions.query.QueryWrapper}
     */
    default <T extends IPage<E>> PageResult<T> page(T page, Wrapper<E> queryWrapper) {
        return new PageResult<>(getBaseMapper().selectPage(page, queryWrapper));
    }

    /**
     * 无条件翻页查询
     *
     * @param page 翻页对象
     * @see Wrappers#emptyWrapper()
     */
    default <T extends IPage<E>> PageResult<T> page(T page) {
        return page(page, Wrappers.emptyWrapper());
    }

    /**
     * 查询列表
     *
     * @param queryWrapper 实体对象封装操作类 {@link com.baomidou.mybatisplus.core.conditions.query.QueryWrapper}
     */
    default List<Map<String, Object>> listMaps(Wrapper<E> queryWrapper) {
        return getBaseMapper().selectMaps(queryWrapper);
    }

    /**
     * 查询所有列表
     *
     * @see Wrappers#emptyWrapper()
     */
    default List<Map<String, Object>> listMaps() {
        return listMaps(Wrappers.emptyWrapper());
    }

    /**
     * 查询全部记录
     */
    default List<Object> listObjs() {
        return listObjs(Function.identity()).getData();
    }

    /**
     * 查询全部记录
     *
     * @param mapper 转换函数
     */
    default <V> ListResult<V> listObjs(Function<? super Object, V> mapper) {
        return listObjs(Wrappers.emptyWrapper(), mapper);
    }

    /**
     * 根据 Wrapper 条件，查询全部记录
     *
     * @param queryWrapper 实体对象封装操作类 {@link com.baomidou.mybatisplus.core.conditions.query.QueryWrapper}
     */
    default List<Object> listObjs(Wrapper<E> queryWrapper) {
        return listObjs(queryWrapper, Function.identity()).getData();
    }

    /**
     * 根据 Wrapper 条件，查询全部记录
     *
     * @param queryWrapper 实体对象封装操作类 {@link com.baomidou.mybatisplus.core.conditions.query.QueryWrapper}
     * @param mapper       转换函数
     */
    default <V> ListResult<V> listObjs(Wrapper<E> queryWrapper, Function<? super Object, V> mapper) {
        return new ListResult<>(getBaseMapper().selectObjs(queryWrapper).stream().filter(Objects::nonNull).map(mapper).collect(Collectors.toList()));
    }

    /**
     * 翻页查询
     *
     * @param page         翻页对象
     * @param queryWrapper 实体对象封装操作类 {@link com.baomidou.mybatisplus.core.conditions.query.QueryWrapper}
     */
    default <P extends IPage<Map<String, Object>>> PageResult<P> pageMaps(P page, Wrapper<E> queryWrapper) {
        return new PageResult<>(getBaseMapper().selectMapsPage(page, queryWrapper));
    }

    /**
     * 无条件翻页查询
     *
     * @param page 翻页对象
     * @see Wrappers#emptyWrapper()
     */
    default <P extends IPage<Map<String, Object>>> PageResult<P> pageMaps(P page) {
        return pageMaps(page, Wrappers.emptyWrapper());
    }

    /**
     * 获取对应 entity 的 BaseMapper
     *
     * @return BaseMapper
     */
    BaseMapper<E> getBaseMapper();

    /**
     * 获取 entity 的 class
     *
     * @return {@link Class<E>}
     */
    Class<E> getEntityClass();

    /**
     * 以下的方法使用介绍:
     *
     * 一. 名称介绍
     * 1. 方法名带有 query 的为对数据的查询操作, 方法名带有 update 的为对数据的修改操作
     * 2. 方法名带有 lambda 的为内部方法入参 column 支持函数式的
     * 二. 支持介绍
     *
     * 1. 方法名带有 query 的支持以 {@link ChainQuery} 内部的方法名结尾进行数据查询操作
     * 2. 方法名带有 update 的支持以 {@link ChainUpdate} 内部的方法名为结尾进行数据修改操作
     *
     * 三. 使用示例,只用不带 lambda 的方法各展示一个例子,其他类推
     * 1. 根据条件获取一条数据: `query().eq("column", value).one()`
     * 2. 根据条件删除一条数据: `update().eq("column", value).remove()`
     *
     */

    /**
     * 链式查询 普通
     *
     * @return QueryWrapper 的包装类
     */
    default QueryChainWrapper<E> query() {
        return ChainWrappers.queryChain(getBaseMapper());
    }

    /**
     * 链式查询 lambda 式
     * <p>注意：不支持 Kotlin </p>
     *
     * @return LambdaQueryWrapper 的包装类
     */
    default LambdaQueryChainWrapper<E> lambdaQuery() {
        return ChainWrappers.lambdaQueryChain(getBaseMapper());
    }

    /**
     * 链式更改 普通
     *
     * @return UpdateWrapper 的包装类
     */
    default UpdateChainWrapper<E> update() {
        return ChainWrappers.updateChain(getBaseMapper());
    }

    /**
     * 链式更改 lambda 式
     * <p>注意：不支持 Kotlin </p>
     *
     * @return LambdaUpdateWrapper 的包装类
     */
    default LambdaUpdateChainWrapper<E> lambdaUpdate() {
        return ChainWrappers.lambdaUpdateChain(getBaseMapper());
    }

    /**
     * <p>
     * 根据updateWrapper尝试更新，否继续执行saveOrUpdate(E)方法
     * 此次修改主要是减少了此项业务代码的代码量（存在性验证之后的saveOrUpdate操作）
     * </p>
     *
     * @param entity 实体对象
     */
    default BooleanResult saveOrUpdate(E entity, Wrapper<E> updateWrapper) {
        BooleanResult update = update(entity, updateWrapper);
        return update.getData() == true ? update : saveOrUpdate(entity);
    }
}
