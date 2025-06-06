package com.iteaj.iboot.plugin.code.design;

import com.baomidou.mybatisplus.generator.config.FileOutConfig;
import com.baomidou.mybatisplus.generator.config.po.TableInfo;
import com.iteaj.iboot.plugin.code.gen.DesignConfigBuilder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public abstract class LcdInjectionConfig {
    /**
     * 全局配置
     */
    private DesignConfigBuilder config;

    /**
     * 自定义返回配置 Map 对象
     */
    private Map<String, Object> map;

    /**
     * 自定义输出文件
     */
    private List<FileOutConfig> fileOutConfigList;

    /**
     * 自定义判断是否创建文件
     */
    private ILcdFileCreate fileCreate;

    /**
     * 注入自定义 Map 对象，针对所有表的全局参数
     */
    public abstract void initMap();

    /**
     * 依据表相关信息，从三方获取到需要元数据，处理方法环境里面
     *
     * @param tableInfo
     */
    public void initTableMap(TableInfo tableInfo) {
        // 子类重写注入表对应补充信息
    }

    /**
     * 模板待渲染 Object Map 预处理<br>
     * com.baomidou.mybatisplus.generator.engine.AbstractTemplateEngine
     * 方法： getObjectMap 结果处理
     */
    public Map<String, Object> prepareObjectMap(Map<String, Object> objectMap) {
        return objectMap;
    }
}
