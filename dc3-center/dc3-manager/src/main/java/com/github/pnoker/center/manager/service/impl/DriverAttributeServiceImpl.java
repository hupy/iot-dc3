/*
 * Copyright 2019 Pnoker. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.pnoker.center.manager.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.pnoker.center.manager.mapper.DriverAttributeMapper;
import com.github.pnoker.center.manager.service.DriverAttributeService;
import com.github.pnoker.common.bean.Pages;
import com.github.pnoker.common.constant.Common;
import com.github.pnoker.common.dto.DriverAttributeDto;
import com.github.pnoker.common.exception.ServiceException;
import com.github.pnoker.common.model.DriverAttribute;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Optional;

/**
 * <p>DriverAttributeService Impl
 *
 * @author pnoker
 */
@Slf4j
@Service
public class DriverAttributeServiceImpl implements DriverAttributeService {
    @Resource
    private DriverAttributeMapper driverAttributeMapper;

    @Override
    @Caching(
            put = {
                    @CachePut(value = Common.Cache.DRIVER_ATTRIBUTE + Common.Cache.ID, key = "#driverAttribute.id", condition = "#result!=null"),
                    @CachePut(value = Common.Cache.DRIVER_ATTRIBUTE + Common.Cache.NAME, key = "#driverAttribute.name", condition = "#result!=null")
            },
            evict = {
                    @CacheEvict(value = Common.Cache.DRIVER_ATTRIBUTE + Common.Cache.DIC, allEntries = true, condition = "#result!=null"),
                    @CacheEvict(value = Common.Cache.DRIVER_ATTRIBUTE + Common.Cache.LIST, allEntries = true, condition = "#result!=null")
            }
    )
    public DriverAttribute add(DriverAttribute driverAttribute) {
        DriverAttribute select = selectByNameAndDriverId(driverAttribute.getName(), driverAttribute.getDriverId());
        if (null != select) {
            throw new ServiceException("driver attribute already exists");
        }
        if (driverAttributeMapper.insert(driverAttribute) > 0) {
            return driverAttributeMapper.selectById(driverAttribute.getId());
        }
        return null;
    }

    @Override
    @Caching(
            evict = {
                    @CacheEvict(value = Common.Cache.DRIVER_ATTRIBUTE + Common.Cache.ID, key = "#id", condition = "#result==true"),
                    @CacheEvict(value = Common.Cache.DRIVER_ATTRIBUTE + Common.Cache.NAME, allEntries = true, condition = "#result==true"),
                    @CacheEvict(value = Common.Cache.DRIVER_ATTRIBUTE + Common.Cache.DIC, allEntries = true, condition = "#result==true"),
                    @CacheEvict(value = Common.Cache.DRIVER_ATTRIBUTE + Common.Cache.LIST, allEntries = true, condition = "#result==true")
            }
    )
    public boolean delete(Long id) {
        return driverAttributeMapper.deleteById(id) > 0;
    }

    @Override
    @Caching(
            put = {
                    @CachePut(value = Common.Cache.DRIVER_ATTRIBUTE + Common.Cache.ID, key = "#driverAttribute.id", condition = "#result!=null"),
                    @CachePut(value = Common.Cache.DRIVER_ATTRIBUTE + Common.Cache.NAME, key = "#driverAttribute.name", condition = "#result!=null")
            },
            evict = {
                    @CacheEvict(value = Common.Cache.DRIVER_ATTRIBUTE + Common.Cache.DIC, allEntries = true, condition = "#result!=null"),
                    @CacheEvict(value = Common.Cache.DRIVER_ATTRIBUTE + Common.Cache.LIST, allEntries = true, condition = "#result!=null")
            }
    )
    public DriverAttribute update(DriverAttribute driverAttribute) {
        driverAttribute.setUpdateTime(null);
        if (driverAttributeMapper.updateById(driverAttribute) > 0) {
            DriverAttribute select = selectById(driverAttribute.getId());
            driverAttribute.setName(select.getName());
            return select;
        }
        return null;
    }

    @Override
    @Cacheable(value = Common.Cache.DRIVER_ATTRIBUTE + Common.Cache.ID, key = "#id", unless = "#result==null")
    public DriverAttribute selectById(Long id) {
        return driverAttributeMapper.selectById(id);
    }

    @Override
    @Cacheable(value = Common.Cache.DRIVER_ATTRIBUTE + Common.Cache.NAME, key = "#name", unless = "#result==null")
    public DriverAttribute selectByNameAndDriverId(String name, Long driverId) {
        LambdaQueryWrapper<DriverAttribute> queryWrapper = Wrappers.<DriverAttribute>query().lambda();
        queryWrapper.eq(DriverAttribute::getName, name);
        queryWrapper.eq(DriverAttribute::getDriverId, driverId);
        return driverAttributeMapper.selectOne(queryWrapper);
    }

    @Override
    @Cacheable(value = Common.Cache.DRIVER_ATTRIBUTE + Common.Cache.LIST, keyGenerator = "commonKeyGenerator", unless = "#result==null")
    public Page<DriverAttribute> list(DriverAttributeDto driverAttributeDto) {
        if (!Optional.ofNullable(driverAttributeDto.getPage()).isPresent()) {
            driverAttributeDto.setPage(new Pages());
        }
        return driverAttributeMapper.selectPage(driverAttributeDto.getPage().convert(), fuzzyQuery(driverAttributeDto));
    }

    @Override
    public LambdaQueryWrapper<DriverAttribute> fuzzyQuery(DriverAttributeDto driverAttributeDto) {
        LambdaQueryWrapper<DriverAttribute> queryWrapper = Wrappers.<DriverAttribute>query().lambda();
        Optional.ofNullable(driverAttributeDto).ifPresent(dto -> {
            if (StringUtils.isNotBlank(dto.getDisplayName())) {
                queryWrapper.like(DriverAttribute::getDisplayName, dto.getDisplayName());
            }
            if (StringUtils.isNotBlank(dto.getName())) {
                queryWrapper.eq(DriverAttribute::getName, dto.getName());
            }
            if (StringUtils.isNotBlank(dto.getType())) {
                queryWrapper.eq(DriverAttribute::getType, dto.getType());
            }
            if (null != dto.getDriverId()) {
                queryWrapper.eq(DriverAttribute::getDriverId, dto.getDriverId());
            }
        });
        return queryWrapper;
    }

}
