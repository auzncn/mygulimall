package com.atguigu.gulimall.product.convert;

import com.atguigu.gulimall.product.entity.BrandEntity;
import com.atguigu.gulimall.product.entity.CategoryBrandRelationEntity;
import com.atguigu.gulimall.product.vo.BrandVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface BrandConvert {
    BrandConvert INSTANCT = Mappers.getMapper(BrandConvert.class);

    @Mapping(target = "brandName", source = "name")
    BrandVO convert(BrandEntity entity);

    List<BrandVO> convertList(List<BrandEntity> list);

    BrandVO convert2(CategoryBrandRelationEntity entity);

    List<BrandVO> convertList2(List<CategoryBrandRelationEntity> list);
}
