package com.atguigu.gulimall.product.entity;

import com.atguigu.common.valid.AddGroup;
import com.atguigu.common.valid.UpdateGroup;
import com.atguigu.common.valid.UpdateStatusGroup;
import com.atguigu.common.valid.ValueList;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.util.Date;

import lombok.Data;
import org.apache.ibatis.annotations.Update;
import org.hibernate.validator.constraints.URL;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

/**
 * 品牌
 *
 * @author zjx
 * @email zjx@gmail.com
 * @date 2022-11-14 16:05:00
 */
@Data
@TableName("pms_brand")
public class BrandEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 品牌id
     */
    @TableId
    @NotNull(message = "id不能为空", groups = UpdateGroup.class)
    private Long brandId;
    /**
     * 品牌名
     */
    @NotEmpty(message = "品牌名不能为空", groups = {AddGroup.class})
    private String name;
    /**
     * 品牌logo地址
     */
    @NotEmpty(message = "logo不能为空", groups = AddGroup.class)
    @URL(message = "logo必须为url格式", groups = {UpdateGroup.class, AddGroup.class})
    private String logo;
    /**
     * 介绍
     */
    private String descript;
    /**
     * 显示状态[0-不显示；1-显示]
     */
    @ValueList(vals = {0, 1}, groups = {UpdateGroup.class, AddGroup.class, UpdateStatusGroup.class})
    private Integer showStatus;
    /**
     * 检索首字母
     */
    @NotEmpty(message = "检索首字母不能为空", groups = AddGroup.class)
    @Pattern(regexp = "^[a-zA-Z]$", message = "检索首字母必须是a-z之间", groups = {UpdateGroup.class, AddGroup.class})
    private String firstLetter;
    /**
     * 排序
     */
    @NotNull(message = "排序不能为空", groups = AddGroup.class)
    @Min(value = 0, message = "排序必须大于0", groups = {UpdateGroup.class, AddGroup.class})
    private Integer sort;

}
