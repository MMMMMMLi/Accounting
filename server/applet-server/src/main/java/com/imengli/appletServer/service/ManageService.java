/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2020 mmmmmengli@gmail.com
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.imengli.appletServer.service;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.imengli.appletServer.common.ResultStatus;
import com.imengli.appletServer.common.SysConstant;
import com.imengli.appletServer.dao.ManageRepostory;
import com.imengli.appletServer.dao.SysUserRepostory;
import com.imengli.appletServer.daomain.SysUserDO;
import com.imengli.appletServer.daomain.WechatUserDO;
import com.imengli.appletServer.dto.ResultDTO;
import com.imengli.appletServer.utils.RedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author: Weijia Jiang
 * @date: Created in 2020/10/22 15:26
 * @description: 管理相关Service
 * @version: v1
 */
@Service
public class ManageService {

    private final static Logger LOGGER = LoggerFactory.getLogger(ManageService.class);

    @Autowired
    private RedisUtil redisUtil;

    @Resource
    private ManageRepostory manageRepostory;

    @Resource
    private SysUserRepostory sysUserRepostory;

    @Autowired
    private SysConstant sysConstant;

    private static DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");


    /**
     * 获取当日的订单汇总信息
     *
     * @param token
     * @return
     */
    public ResultDTO getSummaryOrderInfo(String token) {
        // 校验token
        WechatUserDO wechatUserDO = redisUtil.getWechatAuthEntity(token);
        // 根据信息完善度返回
        if (wechatUserDO != null) {
            // TODO: 后续添加管理员校验

            return new ResultDTO(ResultStatus.SUCCESS, manageRepostory.getSummaryOrderInfo());
        }
        return new ResultDTO(ResultStatus.ERROR_AUTH_TOKEN);
    }

    /**
     * 根据不同的Type和State 获取报告
     *
     * @param token
     * @param type  category | size | person | time
     * @return
     */
    public ResultDTO getReportByDay(String token, String type) {
        // 校验token
        WechatUserDO wechatUserDO = redisUtil.getWechatAuthEntity(token);
        // 根据信息完善度返回
        if (wechatUserDO != null) {
            // TODO: 后续添加管理员校验

            // 设置起始时间
            LocalDateTime startTime = LocalDateTime.of(LocalDate.now(), LocalTime.MIN).minusDays(1);
            // 结束时间都是当前时间
            LocalDateTime endTime = LocalDateTime.now();
            // 根据不同的 Type类型来获取数据
            // 返回小程序端的数据结构:
            // legend_data: 导航名字
            // xAxis_data:  X栏数据
            // series[  // 是否存在多个   导航名字
            //      {
            //          name: 导航名字[0],
            //          data: 数据
            //      }....
            // ]
            Map<String, Object> result = new HashMap<>();
            switch (type) {
                // 各品种销量情况
                case "category":
                    // 获取数据
                    List<Map<String, Object>> reportByCategory = manageRepostory.getReportByCategory(startTime, endTime);
                    if (reportByCategory.size() > 0) {
                        // 拼装数据
                        // legend_data
                        assembleData(result, reportByCategory, "category");
                    }
                    break;
                // 各型号销量情况
                case "size":
                    // 获取数据
                    List<Map<String, Object>> reportBySize = manageRepostory.getReportBySize(startTime, endTime);
                    if (reportBySize.size() > 0) {
                        // 拼装数据
                        assembleData(result, reportBySize, "size");
                    }
                    break;
                // 成交量情况
                case "person":
                    // 成交量排行,只看当天的即可。
                    startTime = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
                    // 获取数据
                    List<Map<String, Object>> reportByPerson = manageRepostory.getReportByPerson(startTime, endTime);
                    if (reportByPerson.size() > 0) {
                        // 拼装数据
                        // xAxis_data
                        result.put("xAxis_data", reportByPerson.stream().map(info -> info.get("userName")).collect(Collectors.toList()));
                        // series
                        List<Object> seriesInfoList = new ArrayList<>();
                        Map<String, Object> seriesInfo = new HashMap<>();
                        // 这个只有一个柱状图,所有表头提示就省略掉了.
                        seriesInfo.put("name", "");
                        Map<String, Object> totalPriceInfo = new HashMap<>();
                        seriesInfo.put("data", reportByPerson.stream().map(info -> info.get("totalPrice")).collect(Collectors.toList()));

                        seriesInfoList.add(seriesInfo);

                        result.put("series", seriesInfoList);
                    }
                    break;
                // 各个时间段的交易情况
                case "time":
                    // 时间段排行,只看当天的即可。
                    startTime = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
                    // 获取数据
                    List<Map<String, Object>> reportByTime = manageRepostory.getReportByTime(startTime, endTime);
                    if (reportByTime.size() > 0) {
                        // 拼装数据
                        // legend_data
                        List<String> legendDataList = Arrays.asList("价钱/元", "重量/KG");
                        result.put("legend_data", legendDataList);
                        // List<String> timeList = Arrays.asList("00", "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23");
                        List<Object> timeList = reportByTime.stream().map(report -> report.get("dateTime")).collect(Collectors.toList());
                        // xAxis_data
                        result.put("xAxis_data", timeList);
                        // series
                        List<Object> seriesList = new ArrayList<>();
                        legendDataList.stream().forEach(k -> {
                            Map<String, Object> infos = new HashMap<>();
                            infos.put("name", k);
                            infos.put("data", timeList.stream().map(time -> {
                                List<Object> collect = reportByTime.stream()
                                        .filter(report -> time.equals(report.get("dateTime").toString()))
                                        .map(report -> {
                                            String flag = "sumPrice";
                                            if (k.equals("重量/KG")) {
                                                flag = "sumWeight";
                                            }
                                            return report.getOrDefault(flag, 0);
                                        }).collect(Collectors.toList());
                                if (collect.size() > 0) {
                                    return collect.get(0);
                                }
                                return 0;
                            }).collect(Collectors.toList()));
                            seriesList.add(infos);
                        });
                        result.put("series", seriesList);
                    }
                    break;
                default:
                    break;
            }
            if (result.size() > 0) {
                return new ResultDTO(ResultStatus.SUCCESS, result);
            }
            return new ResultDTO(ResultStatus.NULL, result);
        }
        return new ResultDTO(ResultStatus.ERROR_AUTH_TOKEN);
    }

    /**
     * 根据不同的参数组装 前端 图标的 数据信息
     *
     * @param result
     * @param reportBySize
     * @param type
     */
    private void assembleData(Map<String, Object> result, List<Map<String, Object>> reportBySize, String type) {
        // legend_data
        result.put("legend_data", reportBySize.parallelStream().map(info -> info.get("dateTime")).collect(Collectors.toSet()));
        // xAxis_data
        result.put("xAxis_data", SysConstant.getValueByTypeAndKey("web", type));
        // series
        reportBySize
                .parallelStream()
                .collect(Collectors.groupingBy(e -> e.get("dateTime")))
                .forEach((k, v) -> {
                    List<Object> series = new ArrayList<>();
                    if (result.containsKey("series")) {
                        series = (ArrayList) result.get("series");
                    }
                    Map<String, Object> info = new HashMap<>();
                    info.put("name", k);
                    info.put("data",
                            ((ArrayList) SysConstant.getValueByTypeAndKey("web", type))
                                    .stream()
                                    .map(value -> {
                                        List<Object> weight = v.parallelStream()
                                                .filter(vInfo -> vInfo.get(type).equals(value))
                                                .map(vInfo -> vInfo.get("sumWeight"))
                                                .collect(Collectors.toList());
                                        if (weight.size() > 0) {
                                            return weight.get(0);
                                        }
                                        return 0;
                                    }).collect(Collectors.toList()));
                    series.add(info);
                    result.put("series", series);
                });
    }

    /**
     * 获取用户列表
     *
     * @param token
     * @param page
     * @param size
     * @param searchType  查询类型  order 排序 | search 搜索 | .... 搜索和排序都存在的情况
     * @param searchValue
     * @return
     */
    public ResultDTO getUserList(String token, Integer page, Integer size, String searchType, String searchValue) {
        // 校验token
        WechatUserDO wechatUserDO = redisUtil.getWechatAuthEntity(token);
        // 根据信息完善度返回
        if (wechatUserDO != null) {
            // TODO: 后续添加管理员校验
            // 构建分页信息
            PageHelper.startPage(page, size);
            // 构建查询Map
            Map<String, String> searchMap = new HashMap<>();
            // 如果搜索信息有一个为空,则走普通查询
            if (StringUtils.isNoneBlank(searchType, searchValue)) {
                if (searchType.equals("order") || searchType.equals("search")) {
                    // 如果是标准的查询,则直接构建查询Map即可
                    searchMap.put(searchType, searchValue);
                } else {
                    // 如果不是,则需要构建Map
                    searchMap.put("order", searchType);
                    searchMap.put("search", searchValue);
                }
            }
            // 普通查询
            List<Map<String, Object>> sysUserDOList = manageRepostory.getUserList(searchMap);
            // 完善页面展示信息
            sysUserDOList.parallelStream().forEach(userInfo -> {
                if (searchMap.containsKey("order")) {
                    userInfo.put("showInfo", userInfo.get(searchMap.get("order")));
                }
            });
            // 构建分页信息
            PageInfo<Map<String, Object>> sysUserDOPageInfo = new PageInfo<>(sysUserDOList);
            // 返回
            return new ResultDTO(ResultStatus.SUCCESS, null, sysUserDOPageInfo);
        }
        return new ResultDTO(ResultStatus.ERROR_AUTH_TOKEN);
    }

    public ResultDTO getUserDetails(String token, String userId, Integer page, Integer size) {
        // 校验token
        WechatUserDO wechatUserDO = redisUtil.getWechatAuthEntity(token);
        // 根据信息完善度返回
        if (wechatUserDO != null) {
            // TODO: 后续添加管理员校验}

            // 构建分页信息
            PageHelper.startPage(page, size);
            // 查询
            List<Map<String, Object>> userDetails = manageRepostory.getUserDetails(userId);
            // 构建分页信息
            PageInfo<Map<String, Object>> userDetailsPageInfo = new PageInfo<>(userDetails);
            // 返回
            return new ResultDTO(ResultStatus.SUCCESS, null, userDetailsPageInfo);
        }
        return new ResultDTO(ResultStatus.ERROR_AUTH_TOKEN);
    }

    /**
     * 获取系统一些变量
     *
     * @param token
     * @return
     */
    public ResultDTO getSysInfo(String token) {
        // 校验token
        WechatUserDO wechatUserDO = redisUtil.getWechatAuthEntity(token);
        // 根据信息完善度返回
        if (wechatUserDO != null) {
            // TODO: 后续添加管理员校验}
            // 返回
            return new ResultDTO(ResultStatus.SUCCESS,
                    manageRepostory.getSysContant()
                            .parallelStream()
                            .collect(Collectors.groupingBy(e -> e.get("key"))));
        }
        return new ResultDTO(ResultStatus.ERROR_AUTH_TOKEN);
    }

    /**
     * 修改系统变量
     *
     * @param sysInfo
     * @return
     */
    @Transactional
    public ResultDTO updateSystemInfo(String sysInfo) {

        // 此处就不校验Token了.
        // 格式化一下参数
        Map<String, List<Map<String, Object>>> parseArrayInfo = JSON.parseObject(sysInfo, HashMap.class);
        // 将所有的参数都归并到一个集合中
        List<Map<String, Object>> updateInfos = new ArrayList<>();
        List<Map<String, Object>> insertInfos = new ArrayList<>();
        parseArrayInfo.forEach((k, infoList) -> {
            insertInfos.addAll(
                    infoList.parallelStream()
                            .filter(info -> StringUtils.isBlank(String.valueOf(info.get("id"))))
                            .collect(Collectors.toList())
            );
            updateInfos.addAll(
                    infoList.parallelStream()
                            .filter(info -> StringUtils.isNotBlank(String.valueOf(info.get("id"))))
                            .filter(info -> info.containsKey("flag"))
                            .collect(Collectors.toList())
            );
        });
        // 更新数据库
        // 新建参数
        if (insertInfos.size() > 0) {
            manageRepostory.insertSystemInfo(insertInfos);
        }
        // 更新参数
        updateInfos.parallelStream().forEach(info -> {
            manageRepostory.updateSystemInfo(info);
        });

        // 每次更新完数据库之后,都需要更新一下当前系统保存的常用系统列表
        sysConstant.update();

        return new ResultDTO(ResultStatus.SUCCESS);
    }

    public ResultDTO getWarnList(String token, Integer page, Integer size, Integer value) {
        // 校验token
        WechatUserDO wechatUserDO = redisUtil.getWechatAuthEntity(token);
        // 根据信息完善度返回
        if (wechatUserDO != null) {
            // TODO: 后续添加管理员校验
            // 构建分页信息
            PageHelper.startPage(page, size);
            // 普通查询
            List<Map<String, Object>> searchInfo = manageRepostory.getWarnList(value);
            // 构建分页信息
            PageInfo<Map<String, Object>> searchInfoPageInfo = new PageInfo<>(searchInfo);
            // 返回
            return new ResultDTO(ResultStatus.SUCCESS, null, searchInfoPageInfo);
        }
        return new ResultDTO(ResultStatus.ERROR_AUTH_TOKEN);
    }

    public ResultDTO getMergeUserInfo(String token, String userId, String userName, String phoneNumber) {
        // 校验token
        WechatUserDO wechatUserDO = redisUtil.getWechatAuthEntity(token);
        // 根据信息完善度返回
        if (wechatUserDO != null) {
            // TODO: 后续添加管理员校验
            // 返回
            return new ResultDTO(ResultStatus.SUCCESS, manageRepostory.getMergeUserInfo(userId, userName, phoneNumber));
        }
        return new ResultDTO(ResultStatus.ERROR_AUTH_TOKEN);
    }

    /**
     * 归并用户
     *
     * @param token
     * @param userId
     * @param mergeUserId
     * @return
     */
    public ResultDTO mergeByUserId(String token, String userId, String mergeUserId) {
        // 校验token
        WechatUserDO wechatUserDO = redisUtil.getWechatAuthEntity(token);
        // 根据信息完善度返回
        if (wechatUserDO != null) {
            // TODO: 后续添加管理员校验
            // Step 1 校验一下用户是否存在
            if (Optional.ofNullable(sysUserRepostory.getUserInfoByUserId(userId)).isPresent()
                    && Optional.ofNullable(sysUserRepostory.getUserInfoByUserId(mergeUserId)).isPresent()) {
                // Step2 都存在,则进行合并,
                // 主要是将 临时用户 订单/库存操作记录 指向当前用户,然后用户状态置为 冻结
                manageRepostory.mergeInfoByUserId(userId,mergeUserId);
                return new ResultDTO(ResultStatus.SUCCESS);
            }
            return new ResultDTO(ResultStatus.ERROR_USER_NOT_EXIST);
        }
        return new ResultDTO(ResultStatus.ERROR_AUTH_TOKEN);

    }

    /**
     * 修改用户状态
     *
     * @param token
     * @param userId
     * @param state
     * @return
     */
    public ResultDTO frozenByUserId(String token, String userId, Integer state) {
        // 校验token
        WechatUserDO wechatUserDO = redisUtil.getWechatAuthEntity(token);
        // 根据信息完善度返回
        if (wechatUserDO != null) {
            // TODO: 后续添加管理员校验
            // Step 1 校验一下用户是否存在
            if (Optional.ofNullable(sysUserRepostory.getUserInfoByUserId(userId)).isPresent()) {
                // Step2 修改状态
                sysUserRepostory.update(SysUserDO.builder().id(userId).state(state).build());
                return new ResultDTO(ResultStatus.SUCCESS);
            }
            return new ResultDTO(ResultStatus.ERROR_USER_NOT_EXIST);
        }
        return new ResultDTO(ResultStatus.ERROR_AUTH_TOKEN);
    }

    /**
     * 获取订单统计信息
     * @param token
     * @param page
     * @param size
     * @return
     */
    public ResultDTO getStatisticsList(String token, Integer page, Integer size) {
        // 校验token
        WechatUserDO wechatUserDO = redisUtil.getWechatAuthEntity(token);
        // 根据信息完善度返回
        if (wechatUserDO != null) {
           PageHelper.startPage(page,size);
           List<Map<String,String>> statisticsList = manageRepostory.getStatisticsList();
           PageInfo<Map<String,String>> pageInfo = PageInfo.of(statisticsList);
           return new ResultDTO(ResultStatus.SUCCESS,pageInfo);
        }
        return new ResultDTO(ResultStatus.ERROR_AUTH_TOKEN);
    }
}
