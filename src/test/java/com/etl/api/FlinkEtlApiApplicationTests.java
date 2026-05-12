package com.etl.api;

import com.etl.api.domain.base.BaseEntity;
import com.etl.api.domain.base.InsertListener;
import com.etl.api.domain.base.UpdateListener;
import com.etl.api.util.IP2RegionUtil;
import com.mybatisflex.codegen.Generator;
import com.mybatisflex.codegen.config.GlobalConfig;
import com.mybatisflex.codegen.config.TableConfig;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.sql.DataSource;

@SpringBootTest
class FlinkEtlApiApplicationTests {

    @Autowired
    DataSource dataSource;

    @Test
    void ip2regionTest(){
        val search = IP2RegionUtil.search("113.92.157.29");
        System.out.println("search = " + search);

        val search1 = IP2RegionUtil.search("240e:3b7:3272:d8d0:db09:c067:8d59:539e");
        System.out.println("search = " + search1);
    }

    @Test
    void contextLoads() {
        // 创建配置内容
        val globalConfig = new GlobalConfig();

        // 作者
        globalConfig.getJavadocConfig().setAuthor("chen");

        // 设置根包
        globalConfig.getPackageConfig()
                .setBasePackage("com.etl.api")
                .setEntityPackage("com.etl.api.domain.entity");

//        globalConfig.getTemplateConfig()
//                .setEntity("templates/entity.tpl");

        TableConfig tableConfig = new TableConfig();
        tableConfig.setInsertListenerClass(InsertListener.class);
        tableConfig.setUpdateListenerClass(UpdateListener.class);

        // 设置表前缀和只生成哪些表，setGenerateTable 未配置时，生成所有表
        globalConfig.getStrategyConfig()
                .setTablePrefix("T_")
                .setGenerateTable("T_ETL_JOB")
                .setTableConfig(tableConfig);

        // 设置生成 entity 并启用 Lombok
        globalConfig.enableEntity()
                .setSuperClass(BaseEntity.class)
                .setWithLombok(true)
                .setJdkVersion(17)
                .setLombokNoArgsConstructorEnable(false)
                .setLombokAllArgsConstructorEnable(false)
                .setOverwriteEnable(true);

        // 设置生成 entity
        globalConfig.enableEntity().setOverwriteEnable(true);

        // 设置生成 mapper
        globalConfig.enableMapper().setOverwriteEnable(false);

        // 设置生成service
        globalConfig.enableService().setOverwriteEnable(false);

        // 设置生成serviceImpl
        globalConfig.enableServiceImpl().setOverwriteEnable(false);

        // 通过 datasource 和 globalConfig 创建代码生成器
        Generator generator = new Generator(dataSource, globalConfig);

        // 生成代码
        generator.generate();
    }
}
